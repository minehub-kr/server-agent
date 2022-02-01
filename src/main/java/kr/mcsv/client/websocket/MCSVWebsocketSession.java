package kr.mcsv.client.websocket;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.stella_it.meiling.InvalidRefreshTokenException;
import kr.mcsv.client.authorization.MCSVAuthorization;
import kr.mcsv.client.core.MCSVCore;

import java.io.IOException;
import java.net.URI;

public class MCSVWebsocketSession {
    private boolean isConnected = false;

    WebSocket ws;

    private MCSVCore core;
    public MCSVWebsocketSession(MCSVCore core) {
        this.core = core;
    }

    public WebSocket connect() throws IOException, InvalidRefreshTokenException {
        WebSocketFactory factory = new WebSocketFactory();

        URI wsURI = URI.create("wss://api.mcsv.kr/servers/"+this.core.server.getServerId()+"/ws");
        factory.setServerName(wsURI.getHost());

        WebSocket ws = factory.createSocket("wss://localhost/endpoint");
        ws.addHeader("Authorization", "Bearer "+this.core.authorization.getAccessToken());
        this.ws = ws;
        return this.ws;
    }

    public boolean isConnected() {
        return this.ws != null && this.ws.isOpen();
    }
}
