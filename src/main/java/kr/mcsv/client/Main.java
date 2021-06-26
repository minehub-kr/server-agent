package kr.mcsv.client;

import kr.mcsv.client.command.MCSVCommand;
import kr.mcsv.client.core.MCSVCore;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public final class Main extends JavaPlugin {
    protected static JavaPlugin plugin;

    public static String version = "";
    public static FileConfiguration config;

    public static MCSVCore core;
    public static Logger logger;

    @Override
    public void onEnable() {
        plugin = this;
        version = this.getDescription().getVersion();

        System.setProperty("http.agent", "mcsv/"+version);

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

        File credentialsFile = new File(this.getDataFolder(), "credentials.yml");

        core = new MCSVCore(null);
        core.setConfigFile(credentialsFile);
        core.load();
    }

    @Override
    public void onDisable() {
        logger.info("mcsv.kr client is shutting down...");
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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return MCSVCommand.onTabComplete(sender, command, label, args);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return MCSVCommand.onCommand(sender, command, label, args);
    }
}
