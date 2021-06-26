package kr.mcsv.client.core;

import com.stella_it.meiling.MeilingAuthorization;
import com.sun.istack.internal.Nullable;
import kr.mcsv.client.authorization.MCSVAuthorization;
import kr.mcsv.client.authorization.MCSVAuthorizationDefault;
import kr.mcsv.client.server.MCSVServer;
import me.alex4386.gachon.network.common.http.HttpRequest;
import me.alex4386.gachon.network.common.http.HttpRequestMethod;
import me.alex4386.gachon.network.common.http.HttpResponse;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.Random;

public class MCSVCore {
    public static String mcsvAPI = "https://api.mcsv.kr";

    public MCSVAuthorization authorization;
    public MCSVServer server = null;

    private File configFile = null;

    public MCSVCore(@Nullable String serverId) {
        this.authorization = new MCSVAuthorization(
                MCSVAuthorizationDefault.clientId
        );

        if (serverId != null) {
            this.server = new MCSVServer(serverId);
        }
    }

    public MCSVCore(MCSVAuthorization authorization, @Nullable String serverId) {
        this.authorization = authorization;

        if (serverId != null) {
            this.server = new MCSVServer(serverId);
        }
    }

    public File getConfigFile() { return this.configFile; }
    public void setConfigFile(File file) {
        this.configFile = file;
    }

    public boolean load() {
        if (this.configFile != null) {
            try {
                YamlConfiguration config = new YamlConfiguration();
                config.load(this.configFile);

                this.authorization.importConfig(config);

                return true;
            } catch (IOException | InvalidConfigurationException e) {
                return false;
            }
        }

        return false;
    }

    public boolean registerServer() { return this.registerServer(null); }

    public boolean registerServer(@Nullable String name) {
        MCSVServer server = MCSVServer.createServer();
        if (server == null) {
            this.server = server;
            return true;
        }
        return false;
    }
}
