package kr.minehub.servers.agent.websocket;

import kr.minehub.servers.agent.Main;
import kr.minehub.servers.agent.utils.BukkitUtils;
import kr.minehub.servers.agent.utils.JSONUtils;
import kr.minehub.servers.agent.websocket.command.BukkitCommandDispatcher;
import kr.minehub.servers.agent.websocket.shell.ShellRunner;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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

    public JSONObject processWebsocket(JSONObject payload) throws Exception {
        JSONObject response = new JSONObject();
        String actionStr;

        if (!payload.containsKey("action")) return response;
        actionStr = (String) payload.get("action");

        Commands action = Commands.getActionByName(actionStr);
        response.put("action", actionStr);

        switch(action) {
            case PING:
                response.put("data", "pong");
                break;
            case RUN_COMMAND:
                response.put("data", runBukkitCommand(payload));
                break;
            case RUN_SHELL_COMMAND:
                response.put("data", runShellCommand(payload));
                break;
            case GET_PLAYERS:
                response.put("data", getOnlinePlayers());
                break;
            case GET_BUKKIT_INFO:
                response.put("data", BukkitUtils.getBukkitInfoJSON());
                break;
            case GET_BUKKIT_VERSION:
                response.put("data", Bukkit.getBukkitVersion());
                break;
            case GET_PLUGIN_VERSION:
                response.put("data", Main.version);
                break;
            case GET_SERVER_METADATA:
                response.put("data", JSONUtils.createMetadataJSON());
                break;
            case GET_SERVER_PERFORMANCE:
                response.put("data", JSONUtils.createPerformanceJSON());
                break;
            case LOAD_JAVA_CLASS:
                response.put("data", loadJavaClass(payload));
                break;
            case BUKKIT_LOG:
                response.put("error", "not_a_requestable_action");
                break;
            case FS_GET:
                response.put("data", getLocalFile(payload));
                break;
            case FS_MOVE:
                response.put("data", moveLocalFile(payload));
                break;
            case FS_UPLOAD:
                response.put("data", uploadFile(payload));
                break;
            case FS_DELETE:
                response.put("data", deleteLocalFile(payload));
                break;
            case FS_DOWNLOAD:
                response.put("data", downloadFile(payload));
                break;
            default:
                response.put("error", "invalid_action");
        }

        return response;
    }

    public static JSONObject runBukkitCommand(JSONObject payload) throws IOException, InterruptedException {
        JSONObject response = new JSONObject();

        JSONObject data = (JSONObject) payload.get("data");
        if (data == null) throw new IOException("missing data field");

        String cmdline = (String) data.get("cmdline");
        if (cmdline == null) throw new IOException("missing cmdline");

        BukkitCommandDispatcher dispatcher = new BukkitCommandDispatcher();

        // doing in spinlock way. :facepalm:
        AtomicBoolean isCompleted = new AtomicBoolean(false);

        // All bukkit related stuff should be run synchronously.
        BukkitTask task = Bukkit.getScheduler().runTask(Main.plugin, () -> {
            Bukkit.dispatchCommand(dispatcher, cmdline);
            response.put("output", dispatcher.getOutput());
            isCompleted.set(true);
        });

        while (!isCompleted.get()) {
            // This would be ok right?
            Thread.sleep(100);
        }

        return response;
    }

    public static JSONObject runShellCommand(JSONObject payload) throws IOException, InterruptedException {
        JSONObject response = new JSONObject();
        JSONObject data = (JSONObject) payload.get("data");
        if (data == null) throw new IOException("missing data field");

        String cmdline = (String) data.get("cmdline");
        if (cmdline == null) throw new IOException("missing cmdline");

        ShellRunner runner = new ShellRunner(cmdline);
        runner.run();

        int exitVal = runner.getExitVal();
        String output = runner.getOutput();

        response.put("output", output);
        response.put("exit", exitVal);

        return response;
    }

    public static JSONArray getOnlinePlayers() {
        JSONArray playerArray = new JSONArray();

        Iterator<? extends Player> playerIterator = Bukkit.getOnlinePlayers().iterator();

        while (playerIterator.hasNext()) {
            Player player = (Player) playerIterator.next();
            JSONObject playerJson = BukkitUtils.getPlayerJSON(player);
            playerArray.add(playerJson);
        }

        return playerArray;
    }

    public static JSONObject loadJavaClass(JSONObject payload) throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        JSONObject json = new JSONObject();

        JSONObject data = (JSONObject) payload.get("data");
        if (data == null) throw new IOException("missing data field");

        String urlStr = (String) data.get("url");
        if (urlStr == null) throw new IOException("missing url");

        URL url = new URL(urlStr);

        String mainClass = (String) data.get("mainClass");
        if (mainClass == null) throw new IOException("missing mainClass");

        String mainMethod = (String) data.get("mainMethod");
        if (mainMethod == null) mainMethod = "main";

        URLClassLoader classLoader = new URLClassLoader(new URL[] { url }, Main.class.getClassLoader() );
        Class loadTarget = Class.forName(mainClass, true, classLoader);

        Method method = loadTarget.getMethod(mainMethod, String[].class);
        Object object = method.invoke(null, new String[] { "" });

        json.put("success", true);

        if (object != null) {
            if (object instanceof JSONObject) {
                json.put("output", (JSONObject) object);
            }
        }

        return json;
    }

    public static JSONObject getLocalFile(JSONObject payload) throws IOException {
        JSONObject data = (JSONObject) payload.get("data");
        if (data == null) data = null;

        String path = data != null ? (String) data.get("path") : null;
        if (path == null) path = System.getProperty("user.dir");

        boolean withContents = data != null && data.get("withContents") != null ? (boolean) data.get("withContents") : true;

        File file = new File(path);
        if (!file.exists()) {
            JSONObject json = new JSONObject();
            json.put("error", "not_found");

            return json;
        }

        return JSONUtils.fileToJSON(file, withContents);
    }

    public static JSONObject deleteLocalFile(JSONObject payload) throws IOException {
        JSONObject data = (JSONObject) payload.get("data");
        if (data == null) throw new IOException("missing local file");

        String path = (String) data.get("path");
        if (path == null) throw new IOException("missing path");

        File file = new File(path);
        if (!file.exists()) {
            JSONObject json = new JSONObject();
            json.put("error", "not_found");

            return json;
        }

        if (file.isDirectory()) {
            FileUtils.deleteDirectory(file);
        } else {
            file.delete();
        }

        JSONObject json = new JSONObject();
        json.put("success", true);

        return json;
    }

    public static JSONObject moveLocalFile(JSONObject payload) throws IOException {
        JSONObject data = (JSONObject) payload.get("data");
        if (data == null) data = null;

        String from = (String) data.get("from");
        if (from == null) throw new IOException("missing from");

        String to = (String) data.get("to");
        if (to == null) throw new IOException("missing to");

        File file = new File(from);
        if (!file.exists()) {
            JSONObject json = new JSONObject();
            json.put("error", "not_found");

            return json;
        }

        File target = new File(to);
        if (file.exists()) {
            file.delete();
        }

        file.renameTo(target);

        JSONObject json = new JSONObject();
        json.put("success", true);

        return json;
    }

    public static JSONObject uploadFile(JSONObject payload) throws IOException {
        JSONObject data = (JSONObject) payload.get("data");
        if (data == null) throw new IOException("missing data field");

        String path = (String) data.get("path");
        if (path == null) throw new IOException("missing path field");

        String contents = (String) data.get("contents");
        if (path == null) throw new IOException("missing path field");

        File target = new File(path);
        if (target.exists()) {
            if (target.isDirectory()) {
                JSONObject json = new JSONObject();
                json.put("error", "invalid_path");

                return json;
            }
        } else {
            File parentDirectory = new File(path.substring(0, FilenameUtils.indexOfLastSeparator(path)));
            parentDirectory.mkdirs();
            target.createNewFile();
        }

        Base64.Decoder decoder = Base64.getDecoder();
        byte[] decoded = decoder.decode(contents);

        DataOutputStream os = new DataOutputStream(new FileOutputStream(target));
        os.write(decoded);
        os.close();

        JSONObject json = new JSONObject();
        json.put("success", true);

        return json;
    }

    public static JSONObject downloadFile(JSONObject payload) throws IOException {
        JSONObject data = (JSONObject) payload.get("data");
        if (data == null) throw new IOException("missing data field");

        String urlStr = (String) data.get("url");
        if (urlStr == null) throw new IOException("missing url");

        URL url = new URL(urlStr);

        String path = data != null ? (String) data.get("path") : null;
        if (path == null) path = System.getProperty("user.dir");

        String filename = FilenameUtils.getName(urlStr);

        File target = new File(path);
        if (target.exists()) {
            if (target.isDirectory()) {
                path = Paths.get(path, filename).toString();
                target = new File(path);
            }
        } else {
            if (FilenameUtils.indexOfLastSeparator(path) == path.length() - 1) {
                // this is directory
                target.mkdirs();
                return downloadFile(payload);
            } else {
                File parentDirectory = new File(path.substring(0, FilenameUtils.indexOfLastSeparator(path)));
                parentDirectory.mkdirs();
            }
        }

        FileUtils.copyURLToFile(url, target);

        JSONObject json = new JSONObject();
        json.put("success", true);

        return json;
    }
}
