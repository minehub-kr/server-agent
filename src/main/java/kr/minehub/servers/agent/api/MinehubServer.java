package kr.minehub.servers.agent.api;

import com.neovisionaries.ws.client.WebSocketException;
import com.stella_it.meiling.InvalidRefreshTokenException;
import kr.minehub.servers.agent.Main;
import kr.minehub.servers.agent.log.Log4JAttacher;
import kr.minehub.servers.agent.websocket.ConnectionWatchdog;
import kr.minehub.servers.agent.utils.JSONUtils;
import kr.minehub.servers.agent.websocket.ConnectSession;
import org.jetbrains.annotations.Nullable;
import me.alex4386.gachon.network.common.http.HttpRequest;
import me.alex4386.gachon.network.common.http.HttpRequestMethod;
import me.alex4386.gachon.network.common.http.HttpResponse;
import org.bukkit.configuration.file.YamlConfiguration;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;

public class MinehubServer {
    private String serverId = null;
    private ConnectSession session;
    private ConnectionWatchdog wsWatchdog;
    private Log4JAttacher logHandler;

    private boolean isStartedUp = false;

    public MinehubServer(@Nullable String serverId) {
        this.serverId = serverId;
    }

    /* Server creation */
    public static MinehubServer createServer() {
        return MinehubServer.createServer(null);
    }

    public static MinehubServer createServer(@Nullable String name) {

        Main.logger.info("Minehub에 서버를 등록하는 중...");
        JSONObject json = new JSONObject();

        if (name == null) {
            try {
                name = InetAddress.getLocalHost().getHostName();
            } catch(UnknownHostException e) {
                Random random = new Random();

                name = "Minehub-"+String.format("%05d", random.nextInt(100000));
            }
        }

        json.put("name", name);

        try {
            HttpRequest request = new HttpRequest(HttpRequestMethod.POST, new URL(MinehubAPI.baseURL + "/v1/servers"), json);

            Main.core.authorization.setToken(request);
            HttpResponse response = request.getResponse();

            if (!response.code.isOK()) {
                return null;
            }

            JSONObject responseJson = response.toJson();

            String serverId = (String) responseJson.get("uid");
            MinehubServer server = new MinehubServer(serverId);

            Main.logger.info("Minehub에 이 서버를 등록 완료하였습니다! ID: "+serverId);

            server.start();
            return server;
        } catch(IOException | ParseException | InvalidRefreshTokenException | WebSocketException e) {
            e.printStackTrace();
            return null;
        }
    }

    /* = startup/shutdown = */
    public void start() throws InvalidRefreshTokenException, WebSocketException, IOException {
        if (!this.isRegistered() || this.getServerId() == null) return;
        if (session == null) session = new ConnectSession(this);
        if (wsWatchdog == null) wsWatchdog = new ConnectionWatchdog(this);
        if (logHandler == null) logHandler = new Log4JAttacher();

        this.isStartedUp = true;

        session.connect();
        wsWatchdog.start();
        logHandler.start();

        logHandler.registerWebsocket(session);
    }

    public void stop() {
        if (!this.isStartedUp) return;

        // stop pipelining log4j logs to websocket
        if (logHandler != null) {
            logHandler.unregisterWebsocket();
            logHandler.stop();
        }
        
        // disable websocket watchdog and disconnect.
        if (wsWatchdog != null) wsWatchdog.stop();
        if (session != null) session.disconnect();
        
        this.isStartedUp = false;
    }

    /* = Setter/Getter = */
    public boolean isRegistered() {
        if (this.serverId == null) return false;
        List<String> servers = MinehubAPI.getServers(Main.core.authorization);

        if (servers == null) return true;
        for (String server : servers) {
            if (server.equalsIgnoreCase(this.serverId)) return true;
        }

        return false;
    }

    public String getServerId() {
        return this.serverId;
    }

    public ConnectSession getWebsocketSession() {
        return this.session;
    }

    /* = Configuration = */
    public void importConfig(YamlConfiguration config) {
        this.serverId = config.getString("server.id", null);

        try {
            this.start();
        } catch (Exception e) {}
    }

    public void exportConfig(YamlConfiguration config) {
        if (this.serverId != null) {
            config.set("server.id", this.serverId);
        }
    }
}
