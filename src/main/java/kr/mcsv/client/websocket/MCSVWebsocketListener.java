package kr.mcsv.client.websocket;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFrame;
import kr.mcsv.client.core.MCSVCore;
import kr.mcsv.client.log.MCSVLogTemplate;
import kr.mcsv.client.utils.MCSVJSONUtils;

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

        try {
            JSONParser parser = new JSONParser();
            JSONObject reqJson = (JSONObject) parser.parse(text);
            if (reqJson.get("payload") == null) return;
            if (reqJson.get("from") == null) return;

            to = (String) reqJson.get("from");
            JSONObject requestPayload = (JSONObject) reqJson.get("payload");

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
                json.put("error", "java_exception");

                JSONObject exception = MCSVJSONUtils.createExceptionJSON(e);

                json.put("exception", exception);

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
            Bukkit.getLogger().info(MCSVLogTemplate.warn("MCSV.KR 서버와의 웹소켓 통신이 끊겼습니다. 다시 연결을 시도합니다."));
            session.connect();
        } catch(Exception e) {
            Bukkit.getLogger().severe(MCSVLogTemplate.error("MCSV.KR 서버와의 웹소켓 통신을 다시 시작하는 도중 오류가 발생했습니다. 아래 스택트레이스를 확인하세요."));
            e.printStackTrace();
        }

        super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
    }
}
