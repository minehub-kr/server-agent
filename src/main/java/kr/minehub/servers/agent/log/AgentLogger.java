package kr.minehub.servers.agent.log;

import org.bukkit.ChatColor;

public class AgentLogger {
    public static String log(String content) {
        return "" + ChatColor.GREEN + "[Minehub - INFO] " + ChatColor.RESET + content;
    }

    public static String warn(String content) {
        return "" + ChatColor.YELLOW + "[Minehub - WARN]  " + ChatColor.RESET + content;
    }

    public static String error(String content) {
        return "" + ChatColor.RED + "[Minehub - ERROR] " + ChatColor.RESET + content;
    }
}
