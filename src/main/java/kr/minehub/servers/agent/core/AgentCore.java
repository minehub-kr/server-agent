package kr.minehub.servers.agent.core;

import kr.minehub.servers.agent.Main;
import kr.minehub.servers.agent.api.auth.MinehubAuthorization;
import kr.minehub.servers.agent.api.auth.MinehubAuthorizationDefault;
import kr.minehub.servers.agent.api.MinehubServer;
import org.jetbrains.annotations.Nullable;
import com.stella_it.meiling.InvalidRefreshTokenException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class AgentCore {

    public MinehubAuthorization authorization;
    public MinehubServer server = null;

    public AgentListener listener = new AgentListener();

    private int scheduleId = -1;

    public void registerSchedule() {
        if (this.scheduleId < 0) {
            this.scheduleId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                Main.plugin,
                (Runnable) () -> {
                    this.updateCredentials();
                }, 
                0l,
                20 * 60 * 30l
            );
        }
    }

    public void unregisterSchedule() {
        if (this.scheduleId >= 0) {
            Bukkit.getScheduler().cancelTask(this.scheduleId);
            this.scheduleId = -1;
        }
    }

    private File credentialsFile = null;
    public AgentCore(@Nullable String serverId) {
        this(
            new MinehubAuthorization(
                MinehubAuthorizationDefault.clientId
            ),
            serverId
        );
    }

    public AgentCore(MinehubAuthorization authorization, @Nullable String serverId) {
        this.authorization = authorization;
        this.server = new MinehubServer(serverId);
    }

    public File getCredentialsFile() { return this.credentialsFile; }
    public void setCredentialsFile(File file) {
        this.credentialsFile = file;
    }

    public void start() {
        this.load();
        this.authorization.setScope(MinehubAuthorizationDefault.clientScope);

        if (this.authorization.isAuthorized()) {
            if (this.server != null)
                new Thread((Runnable) () -> {
                    try {
                        this.server.start();
                    } catch(Exception e) {};
                }).start();
        }

        this.registerSchedule();
        this.listener.registerEvent();
    }

    public void stop() {
        if (this.server != null) {
            Bukkit.getLogger().info("reporting Minehub about server shutdown...");
            this.server.stop();
            Bukkit.getLogger().info("Minehub ServerAgent is shutting down...");
        }

        this.unregisterSchedule();
        this.listener.unregisterEvent();
    }

    public boolean load() {
        if (this.credentialsFile != null) {
            Main.logger.info("ServerAgent 컨피그에서 Credentials 불러오는 중...");

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
            Main.logger.info("ServerAgent 컨피그에 Credentials 저장 중...");

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
        MinehubServer server = MinehubServer.createServer(name);
        if (server == null) {
            return false;
        }
        this.server = server;
        return true;
    }

    public void updateCredentials() {
        if (this.authorization != null && this.authorization.isAuthorized()) {
            this.save();
        }
    }
}
