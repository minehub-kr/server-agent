package kr.mcsv.client.websocket;

import kr.mcsv.client.Main;
import kr.mcsv.client.server.MCSVServer;
import kr.mcsv.client.utils.MCSVBukkitUtils;
import kr.mcsv.client.utils.MCSVJSONUtils;
import kr.mcsv.client.websocket.command.MCSVWebsocketCommandDispatcher;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class MCSVWebsocketHandler {
    MCSVWebsocketSession session;

    public MCSVWebsocketHandler(MCSVWebsocketSession session) {
        this.session = session;
    }

    public JSONObject processWebsocket(JSONObject json) throws Exception {
        JSONObject response = new JSONObject();
        String actionStr;

        if (!json.containsKey("action")) return response;
        actionStr = (String) json.get("action");

        MCSVWebsocketActions action = MCSVWebsocketActions.getActionByName(actionStr);
        response.put("action", actionStr);

        if (action == MCSVWebsocketActions.PING) {
            response.put("data", "pong");
        } else if (action == MCSVWebsocketActions.RUN_COMMAND) {
            JSONObject data = (JSONObject) json.get("data");
            if (data == null) throw new Exception("missing data field");

            String cmdline = (String) data.get("cmdline");
            if (cmdline == null) throw new Exception("missing cmdline");

            MCSVWebsocketCommandDispatcher dispatcher = new MCSVWebsocketCommandDispatcher();

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
        } else if (action == MCSVWebsocketActions.GET_PLAYERS) {
            JSONArray playerArray = new JSONArray();

            Iterator<? extends Player> playerIterator = Bukkit.getOnlinePlayers().iterator();

            while (playerIterator.hasNext()) {
                Player player = (Player) playerIterator.next();
                JSONObject playerJson = MCSVBukkitUtils.getPlayerJSON(player);
                playerArray.add(playerJson);
            }

            response.put("data", playerArray);
        } else if (action == MCSVWebsocketActions.GET_BUKKIT_VERSION) {
            response.put("data", Bukkit.getBukkitVersion());
        } else if (action == MCSVWebsocketActions.GET_PLUGIN_VERSION) {
            response.put("data", Main.version);
        } else if (action == MCSVWebsocketActions.GET_SERVER_METADATA) {
            response.put("data", MCSVJSONUtils.createMetadataJSON());
        } else {
            response.put("error", "invalid_action");
        }

        return response;
    }
}
