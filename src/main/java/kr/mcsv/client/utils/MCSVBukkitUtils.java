package kr.mcsv.client.utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

public class MCSVBukkitUtils {
    public static JSONObject getPlayerJSON(Player player) {
        JSONObject json = new JSONObject();
        json.put("uuid", player.getUniqueId());
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
