package kr.mcsv.client.utils;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class MCSVBukkitUtils {
    public static JSONObject getPlayerJSON(Player player) {
        JSONObject json = new JSONObject();
        json.put("uuid", player.getUniqueId().toString());
        json.put("name", player.getName());
        json.put("ip", player.getAddress().toString());
        json.put("displayName", player.getDisplayName());
        json.put("gamemode", player.getGameMode().getValue());
        json.put("locale", player.getLocale().toString());
        json.put("exp", getPlayerLevelJSON(player));
        json.put("location", getLocationJSON(player.getLocation()));
        json.put("ping", player.getPing());
        json.put("health", player.getHealth());
        json.put("isOp", player.isOp());

        return json;
    }

    public static JSONObject getBukkitInfoJSON() {
        JSONObject json = new JSONObject();

        json.put("server", getBukkitServerJSON());
        json.put("plugins", getBukkitPluginsJSON());

        return json;
    }

    public static JSONArray getBukkitPluginsJSON() {
        JSONArray json = new JSONArray();
        Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
        
        for (Plugin plugin:plugins) {
            json.add(getBukkitPluginJSON(plugin));
        }

        return json;
    }

    public static JSONObject getBukkitPluginJSON(Plugin plugin) {
        JSONObject json = new JSONObject();

        PluginDescriptionFile file = plugin.getDescription();

        if (file == null) {
            json.put("name", plugin.getName());
        } else {
            json.put("name", file.getName());
            json.put("fullname", file.getFullName());
            json.put("description", file.getDescription());
            json.put("website", file.getWebsite());
            json.put("version", file.getVersion());
            json.put("main", file.getMain());
            
            JSONArray authors = new JSONArray();
            for (String author: file.getAuthors()) {
                authors.add(author);
            }
            json.put("authors", authors);
        }

        return json;
    }

    public static JSONObject getBukkitServerJSON() {
        JSONObject json = new JSONObject();
        Server server = Bukkit.getServer();

        json.put("name", server.getName());
        json.put("version", server.getVersion());
        json.put("motd", server.getMotd());

        List<World> worldsRaw = server.getWorlds();
        JSONArray worlds = new JSONArray();

        for (World worldRaw: worldsRaw) {
            worlds.add(getWorldJSON(worldRaw));   
        }
        json.put("worlds", worlds);



        JSONObject settings = new JSONObject();

        settings.put("port", server.getPort());
        settings.put("ip", server.getIp());
        settings.put("hardcore", server.isHardcore());
        settings.put("viewDistance", server.getViewDistance());
        settings.put("maxPlayers", server.getMaxPlayers());

        json.put("settings", settings);

        return json;
    }

    public static JSONObject getWorldJSON(World world) {
        JSONObject json = new JSONObject();

        json.put("name", world.getName());
        json.put("seaLevel", world.getSeaLevel());
        json.put("time", world.getTime());
        json.put("worldType", world.getWorldType().getName());
        json.put("storm", world.hasStorm());
        json.put("thundering", world.isThundering());
        json.put("autosave", world.isAutoSave());
        json.put("spawnLoc", getLocationJSON(world.getSpawnLocation()));
        json.put("maxHeight", world.getMaxHeight());
        json.put("environment", getEnvironmentString(world.getEnvironment()));
        json.put("pvp", world.getPVP());

        JSONObject spawnSettings = new JSONObject();
        spawnSettings.put("monsters", world.getAllowMonsters());
        spawnSettings.put("animals", world.getAllowAnimals());

        json.put("spawnSettings", spawnSettings);


        return json;
    }

    public static String getEnvironmentString(Environment env) {
        switch(env) {
            case NORMAL:
                return "normal";
            case NETHER:
                return "nether";
            case THE_END:
                return "the_end";
            case CUSTOM:
                return "custom";
            default:
                return null;
        }
    }

    public static JSONObject getPlayerLevelJSON(Player player) {
        JSONObject json = new JSONObject();
        json.put("exp", player.getExp());
        json.put("totalExp", player.getTotalExperience());
        json.put("level", player.getLevel());

        return json;
    }

    public static JSONObject getLocationJSON(Location loc) {
        JSONObject json = new JSONObject();
        json.put("world", loc.getWorld().getName());

        json.put("x", loc.getX());
        json.put("y", loc.getY());
        json.put("z", loc.getZ());

        json.put("pitch", loc.getPitch());
        json.put("yaw", loc.getYaw());

        return json;
    }

}
