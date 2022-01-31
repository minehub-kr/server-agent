package kr.mcsv.client.api;

import kr.mcsv.client.authorization.MCSVAuthorization;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class MCSVWebsocketSession {
    private boolean isConnected = false;
    private URI wsURI = URI.create("wss://api.mcsv.kr/");

    private MCSVAuthorization auth;
    public MCSVWebsocketSession(MCSVAuthorization auth) {
        this.auth = auth;
    }

    private WebSocketClient client = new WebSocketClient() {
        @Override
        public void onOpen(ServerHandshake serverHandshake) {

        }

        @Override
        public void onMessage(String s) {

        }

        @Override
        public void onClose(int i, String s, boolean b) {

        }

        @Override
        public void onError(Exception e) {

        }
    };

    public void connect() {

    }
}
