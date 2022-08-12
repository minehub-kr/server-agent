package kr.minehub.servers.agent.utils;

import org.bukkit.ChatColor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeneralUtils {

    public static List<String> searchList(List<String> haystack, String needle) {
        List<String> result = new ArrayList<>();

        for (String res : haystack) {
            if (res.startsWith(needle)) {
                result.add(res);
            }
        }

        return result;
    }

    public static boolean hasMethod(Class<?> clazz, String method) {
        try {
            clazz.getMethod(method, (Class<?>[]) null);
        } catch(NoSuchMethodException e) {
            return false;
        }

        return true;
    }

    public static String getCopyrightString() {
        return "Copyright © "+
                ChatColor.GREEN+"Minehub "+
                ChatColor.RESET+"and "+
                ChatColor.AQUA+"St"+ChatColor.DARK_AQUA+"e;"+ChatColor.BLUE+"la"+ChatColor.DARK_PURPLE+" IT";
    }

    public static byte[] loadFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
        byte[] bytes = new byte[(int)length];

        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }

        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }

        is.close();
        return bytes;
    }


}
