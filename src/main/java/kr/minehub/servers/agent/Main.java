package kr.minehub.servers.agent;

import kr.minehub.servers.agent.command.MinehubCommand;
import kr.minehub.servers.agent.core.AgentCore;

import kr.minehub.servers.agent.core.BukkitEvent;
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
    public static JavaPlugin plugin;

    public static String version = "";
    public static FileConfiguration config;

    public static AgentCore core;
    public static Logger logger;
    
    public static long startedAt = System.currentTimeMillis();

    @Override
    public void onEnable() {
        plugin = this;
        version = this.getDescription().getVersion();

        System.setProperty("http.agent", "minehub-svagent/"+version);
        
        startedAt = System.currentTimeMillis();

        // Plugin startup logic
        logger = Bukkit.getLogger();
        logger.info("Minehub ServerAgent is starting up...");

        this.saveDefaultConfig();
        config = this.getConfig();

        try {
            migrateConfig(config);
        } catch(IOException e) {
            logger.severe("Minehub ServerAgent have failed to migrate your config.");
        }

        File credentialsFile = new File(this.getDataFolder(), "credentials.yml");
        if (!credentialsFile.exists()) {
            try {
                credentialsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Event Register
        getServer().getPluginManager().registerEvents(new BukkitEvent(), this);

        core = new AgentCore(null);
        core.setCredentialsFile(credentialsFile);
        core.start();
    }

    @Override
    public void onDisable() {
        core.stop();
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
        return MinehubCommand.onTabComplete(sender, command, label, args);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return MinehubCommand.onCommand(sender, command, label, args);
    }
}
