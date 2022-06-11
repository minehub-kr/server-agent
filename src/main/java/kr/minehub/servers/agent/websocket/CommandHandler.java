package kr.minehub.servers.agent.websocket;

import kr.minehub.servers.agent.Main;
import kr.minehub.servers.agent.utils.BukkitUtils;
import kr.minehub.servers.agent.utils.JSONUtils;
import kr.minehub.servers.agent.websocket.command.BukkitCommandDispatcher;
import kr.minehub.servers.agent.websocket.shell.ShellRunner;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class CommandHandler {
    ConnectSession session;

    public CommandHandler(ConnectSession session) {
        this.session = session;
    }

    public JSONObject processWebsocket(JSONObject json) throws Exception {
        JSONObject response = new JSONObject();
        String actionStr;

        if (!json.containsKey("action")) return response;
        actionStr = (String) json.get("action");

        Commands action = Commands.getActionByName(actionStr);
        response.put("action", actionStr);

        if (action == Commands.PING) {
            response.put("data", "pong");
        } else if (action == Commands.RUN_COMMAND) {
            JSONObject data = (JSONObject) json.get("data");
            if (data == null) throw new Exception("missing data field");

            String cmdline = (String) data.get("cmdline");
            if (cmdline == null) throw new Exception("missing cmdline");

            BukkitCommandDispatcher dispatcher = new BukkitCommandDispatcher();

            // doing in spinlock way. :facepalm:
            AtomicBoolean isCompleted = new AtomicBoolean(false);
            
            // All bukkit related stuff should be run synchronously.
            BukkitTask task = Bukkit.getScheduler().runTask(Main.plugin, () -> {
                Bukkit.dispatchCommand(dispatcher, cmdline);

                JSONObject responseData = new JSONObject();
                responseData.put("output", dispatcher.getOutput());
                response.put("data", responseData);

                isCompleted.set(true);
            });

            while (!isCompleted.get()) {
                // This would be ok right?
                Thread.sleep(100);
            }
        } else if (action == Commands.RUN_SHELL_COMMAND) {
            JSONObject data = (JSONObject) json.get("data");
            if (data == null) throw new Exception("missing data field");

            String cmdline = (String) data.get("cmdline");
            if (cmdline == null) throw new Exception("missing cmdline");

            ShellRunner runner = new ShellRunner(cmdline);
            runner.run();

            int exitVal = runner.getExitVal();
            String output = runner.getOutput();

            JSONObject responseData = new JSONObject();
            responseData.put("output", output);
            responseData.put("exit", exitVal);

            response.put("data", responseData);
        } else if (action == Commands.GET_PLAYERS) {
            JSONArray playerArray = new JSONArray();

            Iterator<? extends Player> playerIterator = Bukkit.getOnlinePlayers().iterator();

            while (playerIterator.hasNext()) {
                Player player = (Player) playerIterator.next();
                JSONObject playerJson = BukkitUtils.getPlayerJSON(player);
                playerArray.add(playerJson);
            }

            response.put("data", playerArray);
        } else if (action == Commands.GET_BUKKIT_VERSION) {
            response.put("data", Bukkit.getBukkitVersion());
        } else if (action == Commands.GET_PLUGIN_VERSION) {
            response.put("data", Main.version);
        } else if (action == Commands.GET_SERVER_METADATA) {
            response.put("data", JSONUtils.createMetadataJSON());
        } else if (action == Commands.GET_SERVER_PERFORMANCE) {
            response.put("data", JSONUtils.createPerformanceJSON());
        } else if (action == Commands.GET_BUKKIT_INFO) {
            JSONObject bukkitInfo = BukkitUtils.getBukkitInfoJSON();
            response.put("data", bukkitInfo);
        } else {
            response.put("error", "invalid_action");
        }

        return response;
    }
}
