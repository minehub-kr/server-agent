package kr.mcsv.client.server;

import com.stella_it.meiling.InvalidRefreshTokenException;
import kr.mcsv.client.Main;
import kr.mcsv.client.api.MCSVAPI;
import kr.mcsv.client.utils.MCSVUtils;
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
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MCSVServer {
    private String serverId = null;

    public MCSVServer(@Nullable String serverId) {
        this.serverId = serverId;
    }

    public boolean isRegistered() {
        if (this.serverId == null) return false;
        List<String> servers = MCSVAPI.getServers(Main.core.authorization, this.serverId);

        if (servers == null) return true;
        if (servers.contains(this.serverId)) return true;

        return false;
    }

    public void importConfig(YamlConfiguration config) {
        this.serverId = config.getString("server.id", null);
    }

    public void exportConfig(YamlConfiguration config) {
        if (this.serverId != null) {
            config.set("server.id", this.serverId);
        }
    }

    public String getServerId() {
        return this.serverId;
    }

    /* Server creation */
    public static MCSVServer createServer() {
        return MCSVServer.createServer(null);
    }

    public static MCSVServer createServer(@Nullable String name) {
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
            return new MCSVServer(serverId);
        } catch(IOException | ParseException | InvalidRefreshTokenException e) {

            return null;
        }
    }

    public boolean reportServerStartup() {
        return MCSVAPI.reportServerStartup(Main.core.authorization, this.serverId, MCSVUtils.createServerStartupJSON());
    }

    public boolean updateMetadata() {
        return MCSVAPI.reportMetadata(Main.core.authorization, this.serverId, MCSVUtils.createMetadataJSON());
    }
}
