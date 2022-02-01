package kr.mcsv.client.websocket;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFrame;
import kr.mcsv.client.core.MCSVCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class MCSVWebsocketListener extends WebSocketAdapter {
    MCSVWebsocketSession session;

    public MCSVWebsocketListener(MCSVWebsocketSession session) {
        super();
        this.session = session;
    }

    @Override
    public void onTextMessage(WebSocket websocket, String text) throws Exception {
        super.onTextMessage(websocket, text);
        Bukkit.getLogger().info(""+ChatColor.GREEN+ChatColor.BOLD+"[MCSV]"+ChatColor.RESET+" Got Message: "+text);
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
