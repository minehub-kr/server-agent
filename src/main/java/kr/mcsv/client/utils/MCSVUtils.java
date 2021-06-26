package kr.mcsv.client.utils;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class MCSVUtils {

    public static List<String> searchList(List<String> haystack, String needle) {
        List<String> result = new ArrayList<>();

        for (String res : haystack) {
            if (res.startsWith(needle)) {
                result.add(res);
            }
        }

        return result;
    }

    public static String getCopyrightString() {
        return "Copyright © "+
                ChatColor.GREEN+"mcsv.kr platform "+
                ChatColor.RESET+"and "+
                ChatColor.DARK_AQUA+"Ste"+ChatColor.BLUE+"lla"+ChatColor.DARK_PURPLE+" IT";
    }
}
