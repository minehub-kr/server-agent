package kr.mcsv.client.websocket;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.stella_it.meiling.InvalidRefreshTokenException;
import kr.mcsv.client.authorization.MCSVAuthorization;

import java.io.IOException;
import java.net.URI;

public class MCSVWebsocketSession {
    private boolean isConnected = false;
    private URI wsURI = URI.create("wss://api.mcsv.kr/");

    WebSocket ws;

    private MCSVAuthorization auth;
    public MCSVWebsocketSession(MCSVAuthorization auth) {
        this.auth = auth;
    }

    public WebSocket connect() throws IOException, InvalidRefreshTokenException {
        WebSocketFactory factory = new WebSocketFactory();
        factory.setServerName(wsURI.getHost());
        WebSocket ws = factory.createSocket("wss://localhost/endpoint");
        ws.addHeader("Authorization", "Bearer "+auth.getAccessToken());
        this.ws = ws;
        return this.ws;
    }

    public boolean isConnected() {
        return this.ws != null && this.ws.isOpen();
    }
}
