package kr.mcsv.client.websocket;

import com.neovisionaries.ws.client.*;
import com.stella_it.meiling.InvalidRefreshTokenException;
import kr.mcsv.client.Main;
import kr.mcsv.client.authorization.MCSVAuthorization;
import kr.mcsv.client.core.MCSVCore;
import kr.mcsv.client.server.MCSVServer;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.util.List;

public class MCSVWebsocketSession {
    WebSocket ws;
    WebSocketAdapter adapter = null;

    private MCSVServer server;
    public MCSVWebsocketSession(MCSVServer server) {
        this.server = server;
    }

    public WebSocket connect() throws IOException, InvalidRefreshTokenException, WebSocketException {
        if (this.isConnected()) return null;
        WebSocket ws;

        if (this.ws == null) {
            WebSocketFactory factory = new WebSocketFactory();

            URI wsURI = URI.create("wss://api.mcsv.kr/v1/servers/"+this.server.getServerId()+"/ws/server");
            factory.setServerName(wsURI.getHost());

            ws = factory.createSocket(wsURI);
        } else {
            ws = this.ws.recreate();
        }

        if (this.adapter == null) {
            this.adapter = new MCSVWebsocketListener(this);
        }

        ws.addHeader("Authorization", "Bearer "+ Main.core.authorization.getAccessToken());
        ws.addListener(this.adapter);
        ws.setPingInterval(20 * 1000);

        ws.connect();
        this.ws = ws;

        return this.ws;
    }

    public void disconnect() {
        ws.disconnect();
    }

    public boolean isConnected() {
        return this.ws != null && this.ws.isOpen();
    }

    public void sendMessage(String content) {
        ws.sendText(content);
    }

    public void sendMessage(JSONObject json) {
        this.sendMessage(json.toJSONString());
    }
}
