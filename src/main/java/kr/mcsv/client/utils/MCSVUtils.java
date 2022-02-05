package kr.mcsv.client.utils;

import org.bukkit.ChatColor;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
