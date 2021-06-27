package kr.mcsv.client.core;

import org.jetbrains.annotations.Nullable;
import com.stella_it.meiling.InvalidRefreshTokenException;
import kr.mcsv.client.authorization.MCSVAuthorization;
import kr.mcsv.client.authorization.MCSVAuthorizationDefault;
import kr.mcsv.client.server.MCSVServer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class MCSVCore {
    public static String mcsvAPI = "https://api.mcsv.kr";

    public MCSVAuthorization authorization;
    public MCSVServer server = null;

    private File credentialsFile = null;

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

    public File getCredentialsFile() { return this.credentialsFile; }
    public void setCredentialsFile(File file) {
        this.credentialsFile = file;
    }

    public boolean load() {
        if (this.credentialsFile != null) {
            try {
                YamlConfiguration config = new YamlConfiguration();
                config.load(this.credentialsFile);

                this.authorization.importConfig(config);
                this.server.importConfig(config);

                return true;
            } catch (IOException | InvalidConfigurationException e) {
                return false;
            }
        }

        return false;
    }

    public boolean save() {
        if (this.credentialsFile != null) {
            try {
                YamlConfiguration config = new YamlConfiguration();
                config.load(this.credentialsFile);

                this.authorization.exportConfig(config);
                this.server.exportConfig(config);

                config.save(this.credentialsFile);
                return true;
            } catch (IOException | InvalidConfigurationException | InvalidRefreshTokenException e) {
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
