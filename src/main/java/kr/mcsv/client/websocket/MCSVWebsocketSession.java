package kr.mcsv.client.websocket;

import com.neovisionaries.ws.client.*;
import com.stella_it.meiling.InvalidRefreshTokenException;
import kr.mcsv.client.authorization.MCSVAuthorization;
import kr.mcsv.client.core.MCSVCore;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.util.List;

public class MCSVWebsocketSession {
    private boolean isConnected = false;

    WebSocket ws;
    WebSocketAdapter adapter = null;

    private MCSVCore core;
    public MCSVWebsocketSession(MCSVCore core) {
        this.core = core;
    }

    public WebSocket connect() throws IOException, InvalidRefreshTokenException, WebSocketException {
        if (this.isConnected()) return null;
        WebSocket ws;

        if (this.ws == null) {
            WebSocketFactory factory = new WebSocketFactory();

            URI wsURI = URI.create("wss://api.mcsv.kr/servers/"+this.core.server.getServerId()+"/ws");
            factory.setServerName(wsURI.getHost());

            ws = factory.createSocket("wss://localhost/endpoint");
        } else {
            ws = this.ws.recreate();
        }

        if (this.adapter == null) {
            this.adapter = new MCSVWebsocketListener(this);
        }

        ws.addHeader("Authorization", "Bearer "+this.core.authorization.getAccessToken());
        ws.addListener(this.adapter);

        ws.connect();
        this.ws = ws;
        return this.ws;
    }

    public boolean isConnected() {
        return this.ws != null && this.ws.isOpen();
    }
}
