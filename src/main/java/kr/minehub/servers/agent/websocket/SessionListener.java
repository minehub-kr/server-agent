package kr.minehub.servers.agent.websocket;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFrame;
import kr.minehub.servers.agent.log.AgentLogger;
import kr.minehub.servers.agent.utils.JSONUtils;

import org.bukkit.Bukkit;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class SessionListener extends WebSocketAdapter {
    ConnectSession session;
    CommandHandler handler;

    public SessionListener(ConnectSession session) {
        super();
        this.session = session;
        this.handler = new CommandHandler(session);
    }

    @Override
    public void onTextMessage(WebSocket websocket, String text) throws Exception {
        super.onTextMessage(websocket, text);
        String to = null;
        String action = null;

        try {
            JSONParser parser = new JSONParser();
            JSONObject reqJson = (JSONObject) parser.parse(text);
            if (reqJson.get("payload") == null) return;
            if (reqJson.get("from") == null) return;

            to = (String) reqJson.get("from");
            JSONObject requestPayload = (JSONObject) reqJson.get("payload");
            action = (String) requestPayload.get("action");

            JSONObject payload = this.handler.processWebsocket(requestPayload);
            JSONObject json = new JSONObject();
            json.put("from", "server");
            json.put("to", to);
            json.put("payload", payload);

            String response = json.toJSONString();
            websocket.sendText(response);
            return;
        } catch (ParseException e) {
            return;
        } catch (Exception e) {
            if (to != null) {
                JSONObject json = new JSONObject();
                json.put("to", to);
                json.put("from", "server");

                JSONObject payload = new JSONObject();
                payload.put("error", "java_exception");

                if (action == null) payload.put("action", action);

                JSONObject exception = JSONUtils.createExceptionJSON(e);
                payload.put("exception", exception);

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
            Bukkit.getLogger().warning(AgentLogger.warn("Minehub?????? ????????? ????????? ???????????????. ?????? ????????? ???????????????."));
            
            if (session != null) {
                session.connect();
            }
        } catch(Exception e) {
            Bukkit.getLogger().severe(AgentLogger.error("Minehub?????? ????????? ????????? ?????? ???????????? ?????? ????????? ??????????????????. ?????? ????????????????????? ???????????????."));
            e.printStackTrace();
        }

        super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
    }
}
