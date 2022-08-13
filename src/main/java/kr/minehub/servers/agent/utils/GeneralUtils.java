package kr.minehub.servers.agent.utils;

import org.bukkit.ChatColor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

    public static boolean hasMethod(Class<?> clazz, String method, Class<?> ...args) {
        return getMethod(clazz, method) != null;
    }

    public static Method getMethod(Class<?> clazz, String method, Class<?> ...args) {
        try {
            return clazz.getDeclaredMethod(method, args);
        } catch(NoSuchMethodException e) {
            return null;
        }
    }

    public static<T extends Object> T callMethod(Object instance, String method, Object ...args) {
        try {
            Class<?> clazz = (Class<?>) instance.getClass();

            List<Class<?>> clazzList = new ArrayList<Class<?>>();
            for (Object obj: args) {
                clazzList.add(obj.getClass());
            }

            Class<?>[] list = clazzList.toArray(new Class<?>[0]);

            Method methodInstance = getMethod(clazz, method, list);
            if (methodInstance == null) return null;
            
            methodInstance.setAccessible(true);
            T result = (T) methodInstance.invoke(instance, args);

            return result;
        } catch(InvocationTargetException | IllegalAccessException e) {
            return null;
        }
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
