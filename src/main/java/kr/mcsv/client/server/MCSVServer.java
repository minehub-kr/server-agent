package kr.mcsv.client.server;

import com.neovisionaries.ws.client.WebSocketException;
import com.stella_it.meiling.InvalidRefreshTokenException;
import kr.mcsv.client.Main;
import kr.mcsv.client.api.MCSVAPI;
import kr.mcsv.client.scheduler.MCSVReportScheduler;
import kr.mcsv.client.scheduler.MCSVWebsocketWatchdog;
import kr.mcsv.client.utils.MCSVJSONUtils;
import kr.mcsv.client.websocket.MCSVWebsocketSession;
import org.jetbrains.annotations.Nullable;
import kr.mcsv.client.core.MCSVCore;
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

public class MCSVServer {
    private String serverId = null;
    private MCSVWebsocketSession session;
    private MCSVReportScheduler reportScheduler;
    private MCSVWebsocketWatchdog wsWatchdog;

    private boolean isStartedUp = false;

    public MCSVServer(@Nullable String serverId) {
        this.serverId = serverId;
    }

    /* Server creation */
    public static MCSVServer createServer() {
        return MCSVServer.createServer(null);
    }

    public static MCSVServer createServer(@Nullable String name) {

        Main.logger.info("MCSV Platform에 서버를 등록하는 중...");
        JSONObject json = new JSONObject();

        if (name == null) {
            try {
                name = InetAddress.getLocalHost().getHostName();
            } catch(UnknownHostException e) {
                Random random = new Random();

                name = "MCSV-"+String.format("%05d", random.nextInt(100000));
            }
        }

        json.put("name", name);

        try {
            HttpRequest request = new HttpRequest(HttpRequestMethod.POST, new URL(MCSVCore.mcsvAPI + "/v1/servers"), json);

            Main.core.authorization.setToken(request);
            HttpResponse response = request.getResponse();

            if (!response.code.isOK()) {
                return null;
            }

            JSONObject responseJson = response.toJson();

            String serverId = (String) responseJson.get("uid");
            MCSVServer server = new MCSVServer(serverId);

            Main.logger.info("MCSV Platform에 이 서버를 등록 완료하였습니다! ID: "+serverId);

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
        if (session == null) session = new MCSVWebsocketSession(this);
        if (reportScheduler == null) reportScheduler = new MCSVReportScheduler(this);
        if (wsWatchdog == null) wsWatchdog = new MCSVWebsocketWatchdog(this);

        session.connect();
        reportScheduler.start();
        wsWatchdog.start();

        this.isStartedUp = true;
        this.reportServerStartup();
    }

    public void stop() {
        if (!this.isStartedUp) return;

        this.reportServerShutdown();

        // disable websocket watchdog and disconnect.
        if (wsWatchdog != null) wsWatchdog.stop();
        if (session != null) session.disconnect();
        if (reportScheduler != null) reportScheduler.stop();
        
        this.isStartedUp = false;
    }

    /* = Setter/Getter = */
    public boolean isRegistered() {
        if (this.serverId == null) return false;
        List<String> servers = MCSVAPI.getServers(Main.core.authorization, this.serverId);

        if (servers == null) return true;
        for (String server : servers) {
            if (server.equalsIgnoreCase(this.serverId)) return true;
        }

        return false;
    }

    public String getServerId() {
        return this.serverId;
    }

    public MCSVWebsocketSession getWebsocketSession() {
        return this.session;
    }

    /* = API Calls = */
    public boolean reportServerStartup() {
        return MCSVAPI.reportServerStartup(Main.core.authorization, this.serverId, MCSVJSONUtils.createServerStartupJSON());
    }

    public boolean reportServerShutdown() {
        return MCSVAPI.reportServerShutdown(Main.core.authorization, this.serverId);
    }

    public boolean updateMetadata() {
        return MCSVAPI.reportMetadata(Main.core.authorization, this.serverId, MCSVJSONUtils.createMetadataJSON());
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
