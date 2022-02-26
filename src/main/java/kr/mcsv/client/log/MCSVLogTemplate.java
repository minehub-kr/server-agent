package kr.mcsv.client.log;

import org.bukkit.ChatColor;

public class MCSVLogTemplate {
    public static String log(String content) {
        return "" + ChatColor.GREEN + "[MCSV - INFO]" + ChatColor.RESET + content;
    }

    public static String warn(String content) {
        return "" + ChatColor.YELLOW + "[MCSV - WARN] " + ChatColor.RESET + content;
    }

    public static String error(String content) {
        return "" + ChatColor.RED + "[MCSV - ERROR] " + ChatColor.RESET + content;
    }
}
