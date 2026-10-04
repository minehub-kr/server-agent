import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import kr.minehub.servers.agent.Main;
import kr.minehub.servers.agent.api.MinehubServer;
import kr.minehub.servers.agent.log.Log4JAttacher;
import kr.minehub.servers.agent.utils.BukkitUtils;
import kr.minehub.servers.agent.websocket.CommandHandler;
import kr.minehub.servers.agent.websocket.ConnectSession;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class CompatibilityProbe extends JavaPlugin {
    private final JSONArray results = new JSONArray();
    private final JSONArray playerEvents = new JSONArray();

    @Override
    public void onEnable() {
        ConnectSession capture = new ConnectSession(null) {
            @Override
            public boolean isConnected() {
                return true;
            }

            @Override
            public void broadcastPayload(JSONObject payload) {
                synchronized (playerEvents) {
                    playerEvents.add(payload);
                }
            }
        };
        Main.core.server = new MinehubServer(null) {
            @Override
            public ConnectSession getWebsocketSession() {
                return capture;
            }
        };
        Bukkit.getScheduler().runTaskLater(this, this::runChecks, 40L);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        synchronized (results) {
            results.clear();
        }
        runChecks();
        return true;
    }

    private void runChecks() {
        check("agent_enabled", () -> {
            require(Main.plugin.isEnabled(), "Agent is disabled");
            return Main.version;
        });
        CommandHandler handler = new CommandHandler(null);
        for (String action : new String[] {
                "ping", "get_plugin_version", "get_bukkit_version", "get_bukkit_info",
                "get_server_metadata", "get_server_performance", "get_players"}) {
            check(action, () -> handler.processWebsocket(payload(action, null)));
        }
        for (World world : Bukkit.getWorlds()) {
            check("world_min_height:" + world.getName(), () -> {
                JSONObject data = BukkitUtils.getWorldJSON(world);
                int actual = 0;
                try {
                    actual = (Integer) world.getClass().getMethod("getMinHeight").invoke(world);
                } catch (NoSuchMethodException e) {
                    // Older Minecraft worlds start at zero and expose no getMinHeight API.
                }
                require(((Number) data.get("minHeight")).intValue() == actual,
                        "reported=" + data.get("minHeight") + ", actual=" + actual);
                return data;
            });
        }
        check("log4j_forwarding", () -> {
            CountDownLatch received = new CountDownLatch(1);
            String marker = "MINEHUB_COMPATIBILITY_LOG_MARKER";
            ConnectSession capture = new ConnectSession(null) {
                @Override
                public boolean isConnected() {
                    return true;
                }

                @Override
                public void sendLog(JSONObject log) {
                    if (marker.equals(log.get("message"))) {
                        received.countDown();
                    }
                }
            };
            Log4JAttacher attacher = new Log4JAttacher();
            try {
                attacher.registerWebsocket(capture);
                attacher.start();
                Bukkit.getLogger().info(marker);
                require(received.await(2, TimeUnit.SECONDS), "Log was not forwarded");
                return "Log received";
            } finally {
                attacher.unregisterWebsocket();
                attacher.stop();
            }
        });
        check("main_thread_command", () -> {
            JSONObject data = new JSONObject();
            data.put("cmdline", "minehub help");
            JSONObject response = CommandHandler.runBukkitCommand(payload("run_command", data));
            require(((String) response.get("output")).contains("Minehub"), "Missing help output");
            return response;
        });
        Thread remoteTests = new Thread(() -> {
            ExecutorService executor = Executors.newSingleThreadExecutor(task -> {
                Thread thread = new Thread(task, "minehub-compatibility-command");
                thread.setDaemon(true);
                return thread;
            });
            try {
                for (String command : new String[] {
                        "minehub help", "bukkit:version", "minecraft:list",
                        "minecraft:seed", "minecraft:time query minehub:missing_timeline",
                        "minehub help"}) {
                    check("run_command:" + command, () -> {
                        JSONObject data = new JSONObject();
                        data.put("cmdline", command);
                        Future<JSONObject> future = executor.submit(() ->
                                handler.processWebsocket(payload("run_command", data)));
                        try {
                            JSONObject response = future.get(5, TimeUnit.SECONDS);
                            JSONObject commandData = (JSONObject) response.get("data");
                            require(commandData != null, "Missing response data");
                            String output = (String) commandData.get("output");
                            getLogger().info("REMOTE_RESPONSE " + command + ": " + response.toJSONString());
                            require(output != null && !output.isEmpty(),
                                    "Command returned no captured output");
                            if (command.equals("minehub help")) {
                                require(output.contains("Minehub"), "Missing help text");
                            } else if (command.equals("minecraft:seed")) {
                                require(output.contains(Long.toString(Bukkit.getWorlds().get(0).getSeed())),
                                        "Missing seed value in translated feedback");
                            } else if (command.equals("minecraft:list")) {
                                require(output.contains("players online"), "Missing translated player list");
                            } else if (command.contains("missing_timeline")) {
                                require(output.contains("Incorrect argument"), "Missing command error feedback");
                            }
                            return response;
                        } catch (ExecutionException e) {
                            if (command.contains("missing_timeline") && e.getCause() instanceof java.io.IOException) {
                                return "Invalid command failed promptly: " + e.getCause();
                            }
                            throw e;
                        } finally {
                            future.cancel(true);
                        }
                    });
                }
            } finally {
                executor.shutdownNow();
                saveResults();
            }
        }, "minehub-compatibility-probe");
        remoteTests.setDaemon(true);
        remoteTests.start();
    }

    private JSONObject payload(String action, JSONObject data) {
        JSONObject payload = new JSONObject();
        payload.put("action", action);
        if (data != null) {
            payload.put("data", data);
        }
        return payload;
    }

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private void check(String name, Callable<?> test) {
        JSONObject result = new JSONObject();
        result.put("name", name);
        try {
            result.put("result", test.call());
            result.put("passed", true);
            getLogger().info("PASS " + name);
        } catch (Throwable error) {
            result.put("passed", false);
            result.put("error", error.toString());
            getLogger().warning("FAIL " + name + ": " + error);
            error.printStackTrace();
        }
        synchronized (results) {
            results.add(result);
        }
    }

    private void saveResults() {
        JSONObject report = new JSONObject();
        report.put("server", Bukkit.getVersion());
        report.put("java", System.getProperty("java.version"));
        report.put("results", results);
        report.put("playerEvents", playerEvents);
        try {
            Path path = getDataFolder().toPath();
            Files.createDirectories(path);
            Files.write(path.resolve("results.json"),
                    report.toJSONString().getBytes(StandardCharsets.UTF_8));
            getLogger().info("COMPATIBILITY_PROBE_COMPLETE");
        } catch (Exception error) {
            error.printStackTrace();
        }
    }
}
