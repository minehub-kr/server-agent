package kr.mcsv.client.websocket;

import kr.mcsv.client.server.MCSVServer;
import org.json.simple.JSONObject;

public class MCSVWebsocketHandler {
    MCSVWebsocketSession session;

    public MCSVWebsocketHandler(MCSVWebsocketSession session) {
        this.session = session;
    }

    public JSONObject processWebsocket(JSONObject json) {
        JSONObject response = new JSONObject();
        String actionStr;

        if (!json.containsKey("action")) return response;
        actionStr = (String) json.get("action");

        MCSVWebsocketActions action = MCSVWebsocketActions.getActionByName(actionStr);
        if (action == MCSVWebsocketActions.PING) {
            response.put("action", action);
            response.put("data", "pong");
        } else {
            response.put("action", actionStr);
            response.put("error", "invalid_action");
        }

        return response;
    }
}
