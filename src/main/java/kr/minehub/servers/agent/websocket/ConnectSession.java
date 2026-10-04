package kr.minehub.servers.agent.websocket;

import com.neovisionaries.ws.client.*;
import com.stella_it.meiling.InvalidRefreshTokenException;
import kr.minehub.servers.agent.Main;
import kr.minehub.servers.agent.api.MinehubAPI;
import kr.minehub.servers.agent.api.MinehubServer;
import kr.minehub.servers.agent.core.AgentCore;
import kr.minehub.servers.agent.log.AgentLogger;
import org.bukkit.Bukkit;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.URI;

public class ConnectSession {
    volatile WebSocket ws;
    WebSocketAdapter adapter = null;

    volatile boolean preventReconnect = false;
    private volatile boolean isConnecting = false;

    private MinehubServer server;
    public ConnectSession(MinehubServer server) {
        this.server = server;
    }

    public void setPreventReconnect(boolean preventReconnect) {
        this.preventReconnect = preventReconnect;
    }

    public synchronized WebSocket connect() throws IOException, InvalidRefreshTokenException, WebSocketException {
        if (this.preventReconnect || this.isConnecting() || this.isConnected()) return null;
        this.isConnecting = true;
        try {
            Bukkit.getLogger().info(AgentLogger.log("Minehub 서버에 연결을 시작합니다."));
    
            WebSocket ws;
            WebSocketFactory factory = new WebSocketFactory();
    
            URI wsURI = URI.create("wss://"+ MinehubAPI.getHostname() +"/v1/servers/"+this.server.getServerId()+"/ws/server");
            factory.setServerName(wsURI.getHost());

            // set timeout to 5 seconds
            factory.setConnectionTimeout(5000);

            Bukkit.getLogger().info(AgentLogger.log("Minehub 서버에 연결 시도 중: "+wsURI.toString()));
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
            this.ws = null;
            e.printStackTrace();
            throw e;
        } finally {
            this.isConnecting = false;
        }
    }

    public void disconnect() {
        this.preventReconnect = true;
        if (ws != null) ws.disconnect();
    }

    public boolean isConnected() {
        return this.ws != null && this.ws.isOpen();
    }

    public boolean isConnecting() {
        return !this.isConnected() && this.isConnecting;
    }

    public synchronized void forceReconnect() throws IOException, InvalidRefreshTokenException, WebSocketException {
        Bukkit.getLogger().info(AgentLogger.log("Minehub 서버에 강제로 연결을 다시 시작합니다."));
        this.disconnect();
        this.ws = null;
        this.isConnecting = false;
        this.preventReconnect = false;

        this.connect();
        Bukkit.getLogger().info(AgentLogger.log("Minehub 서버에 다시 연결되었습니다."));
    }

    public void sendMessage(String content) {
        ws.sendText(content);
    }

    public void sendMessage(JSONObject json) {
        this.sendMessage(json.toJSONString());
    }

    public void broadcastPayload(JSONObject json) {
        JSONObject envelope = new JSONObject();
        envelope.put("from", "server");
        envelope.put("payload", json);

        this.sendMessage(envelope);
    }

    public void sendLog(JSONObject log) {
        JSONObject json = new JSONObject();
        json.put("action", Commands.BUKKIT_LOG.toString());
        json.put("data", log);

        this.broadcastPayload(json);
    }
}
