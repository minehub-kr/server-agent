package kr.mcsv.client.websocket;

import kr.mcsv.client.Main;
import kr.mcsv.client.server.MCSVServer;
import kr.mcsv.client.websocket.command.MCSVWebsocketCommandDispatcher;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
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
        if (action == MCSVWebsocketActions.PING) {
            response.put("action", action);
            response.put("data", "pong");
        } else if (action == MCSVWebsocketActions.RUN_COMMAND) {
            JSONObject data = (JSONObject) json.get("data");
            if (data == null) throw new Exception("missing data field");

            String cmdline = (String) data.get("cmdline");
            if (cmdline == null) throw new Exception("missing cmdline");

            MCSVWebsocketCommandDispatcher dispatcher = new MCSVWebsocketCommandDispatcher();
            
            // All bukkit related stuff should be run synchronously.
            BukkitTask task = Bukkit.getScheduler().runTask(Main.plugin, () -> {
                Bukkit.dispatchCommand(dispatcher, cmdline);

                JSONObject responseData = new JSONObject();
                responseData.put("output", dispatcher.getOutput());
    
                response.put("action", action);
                response.put("data", responseData);
            });

            // wait until synchronous bukkit task ends.
            task.wait();
        } else {
            response.put("action", actionStr);
            response.put("error", "invalid_action");
        }

        return response;
    }
}
