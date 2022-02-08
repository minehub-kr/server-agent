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

        return response;
    }
}
