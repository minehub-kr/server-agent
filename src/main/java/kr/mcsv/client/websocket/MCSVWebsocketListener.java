package kr.mcsv.client.websocket;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFrame;

public class MCSVWebsocketListener extends WebSocketAdapter {
    MCSVWebsocketSession session;

    public MCSVWebsocketListener(MCSVWebsocketSession session) {
        super();
        this.session = session;
    }

    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
        super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);


    }
}
