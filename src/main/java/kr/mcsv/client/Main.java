package kr.mcsv.client;

import com.stella_it.meiling.InvalidRefreshTokenException;
import com.stella_it.meiling.MeilingAuthorization;
import com.stella_it.meiling.MeilingClient;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public final class Main extends JavaPlugin {
    public static String mcsvClientId = "33ead755-dd70-4d3f-b29a-3a11d5956e41";
    public static MeilingClient client = new MeilingClient(mcsvClientId);
    public static MeilingAuthorization authorization = null;

    public static FileConfiguration config;

    Logger logger;

    @Override
    public void onEnable() {
        // Plugin startup logic
        logger = Bukkit.getLogger();
        logger.info("mcsv.kr client is starting up...");

        this.saveDefaultConfig();
        config = this.getConfig();

        try {
            migrateConfig(config);
        } catch(IOException e) {
            logger.severe("mcsv.kr have failed to migrate your config.");
        }

        authorization = loadAuthorization();
    }

    @Override
    public void onDisable() {
        logger.info("mcsv.kr client is shutting down...");
        saveAuthorization();
    }

    private void migrateConfig(FileConfiguration config) throws IOException {
        // TODO: Implement migration for later.
        int currentLatestVersion = 1;
        int version = config.getInt("version");

        if (version < currentLatestVersion) {
            switch (currentLatestVersion) {
                case 0:
                default:
                    break;
            }
        }

        this.saveConfig();
    }

    public boolean isAuthorized() {
        return authorization != null;
    }

    private MeilingAuthorization loadAuthorization() {
        String accessToken = config.getString("credentials.accessToken", null);
        String refreshToken = config.getString("credentials.refreshToken", null);

        if (accessToken == null || refreshToken == null) {
            return null;
        }

        try {
            MeilingAuthorization authorization = new MeilingAuthorization(client, accessToken, refreshToken);
            authorization.getAccessToken();

            return authorization;
        } catch (InvalidRefreshTokenException e) {
            return null;
        }
    }


    private void saveAuthorization() {
        try {
            config.set("credentials.accessToken", authorization.getAccessToken());
            config.set("credentials.refreshToken", authorization.getRefreshToken());
        } catch (InvalidRefreshTokenException e) {
            authorization = null;
        }
    }
}
