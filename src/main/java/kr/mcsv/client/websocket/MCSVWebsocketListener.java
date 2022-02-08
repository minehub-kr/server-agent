package kr.mcsv.client.websocket;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFrame;
import kr.mcsv.client.core.MCSVCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MCSVWebsocketListener extends WebSocketAdapter {
    MCSVWebsocketSession session;
    MCSVWebsocketHandler handler;

    public MCSVWebsocketListener(MCSVWebsocketSession session) {
        super();
        this.session = session;
        this.handler = new MCSVWebsocketHandler(session);
    }

    @Override
    public void onTextMessage(WebSocket websocket, String text) throws Exception {
        super.onTextMessage(websocket, text);
        String to = null;

        Bukkit.getLogger().info(""+ChatColor.GREEN+ChatColor.BOLD+"[MCSV]"+ChatColor.RESET+" Got Message: "+text);

        try {
            JSONParser parser = new JSONParser();
            JSONObject reqJson = (JSONObject) parser.parse(text);
            if (reqJson.get("payload") == null) return;
            if (reqJson.get("from") == null) return;

            to = (String) reqJson.get("from");

            JSONObject payload = this.handler.processWebsocket(reqJson);
            JSONObject json = new JSONObject();
            json.put("from", "server");
            json.put("to", to);
            json.put("payload", payload);

            websocket.sendText(json.toJSONString());
            return;
        } catch (ParseException e) {
            return;
        } catch (Exception e) {
            if (to != null) {
                JSONObject json = new JSONObject();
                json.put("to", to);
                json.put("from", "server");

                JSONObject payload = new JSONObject();
                json.put("error", "java_exception");
                json.put("exception", e.getMessage());

                json.put("payload", payload);

                websocket.sendText(json.toJSONString());
                return;
            }
        }
    }

    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
        try {
            // Reconnect!
            session.ws.connect();
        } catch(Exception e) {}

        super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
    }
}
