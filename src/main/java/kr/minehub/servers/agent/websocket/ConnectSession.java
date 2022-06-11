package kr.minehub.servers.agent.websocket;

import com.neovisionaries.ws.client.*;
import com.stella_it.meiling.InvalidRefreshTokenException;
import kr.minehub.servers.agent.Main;
import kr.minehub.servers.agent.api.MinehubAPI;
import kr.minehub.servers.agent.api.MinehubServer;
import kr.minehub.servers.agent.core.AgentCore;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.URI;

public class ConnectSession {
    WebSocket ws;
    WebSocketAdapter adapter = null;

    boolean preventReconnect = false;
    private boolean isConnecting = false;

    private MinehubServer server;
    public ConnectSession(MinehubServer server) {
        this.server = server;
    }

    public void setPreventReconnect(boolean preventReconnect) {
        this.preventReconnect = preventReconnect;
    }

    public WebSocket connect() throws IOException, InvalidRefreshTokenException, WebSocketException {
        this.isConnecting = true;
        try {
            if (this.isConnecting()) return null;
            if (this.isConnected()) return null;
            
            // if prevent reconnect is activated, do not reconnect.
            if (this.ws != null && !this.isConnected()) {
                if (this.preventReconnect) {
                    return null;
                }
            }
    
            WebSocket ws;
    
            WebSocketFactory factory = new WebSocketFactory();
    
            URI wsURI = URI.create("wss://"+ MinehubAPI.getHostname() +"/v1/servers/"+this.server.getServerId()+"/ws/server");
            factory.setServerName(wsURI.getHost());
    
            ws = factory.createSocket(wsURI);
    
            if (this.adapter == null) {
                this.adapter = new SessionListener(this);
            }
    
            ws.addHeader("Authorization", "Bearer "+ Main.core.authorization.getAccessToken());
            ws.addListener(this.adapter);
            ws.setPingInterval(20 * 1000);
    
            ws.connect();
            this.ws = ws;
    
            return this.ws;
        } catch(IOException | InvalidRefreshTokenException | WebSocketException e) {
            this.isConnecting = false;
            this.ws = null;
            throw e;
        }
    }

    public void retryConnect() {

    }

    public void disconnect() {
        this.preventReconnect = true;
        ws.disconnect();
    }

    public boolean isConnected() {
        return this.ws != null && this.ws.isOpen();
    }

    public boolean isConnecting() {
        return !this.isConnected() && this.isConnecting;
    }

    public void sendMessage(String content) {
        ws.sendText(content);
    }

    public void sendMessage(JSONObject json) {
        this.sendMessage(json.toJSONString());
    }
}
