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

    public static JSONObject createMetadataJSON() {
        JSONObject json = new JSONObject();

        json.put("version",  1);
        json.put("os", MCSVUtils.createOSMetadataJSON());
        json.put("java", MCSVUtils.createJavaMetadataJSON());
        json.put("process", MCSVUtils.createProcessMetadataJSON());
        json.put("raw", MCSVUtils.createRAWMetadataJSON());
        json.put("cpu", MCSVUtils.createCPUMetadataJSON());
        json.put("memory", MCSVUtils.createMemoryMetadataJSON());
        json.put("hardware", MCSVUtils.createHardwareMetadataJSON());

        return json;
    }

    public static JSONObject createProcessMetadataJSON() {
        JSONObject json = new JSONObject();

        json.put("isElevated", MCSVLowLevelUtils.isElevatedProcess());
        json.put("cmdline", MCSVLowLevelUtils.getRunningCommand());

        return json;
    }

    public static JSONObject createMemoryMetadataJSON() {
        JSONObject json = new JSONObject();

        json.put("available", MCSVLowLevelUtils.getAvailableMemory());
        json.put("total", MCSVLowLevelUtils.getTotalMemory());

        return json;
    }

    public static JSONObject createHardwareMetadataJSON() {
        JSONObject json = new JSONObject();

        json.put("model", MCSVLowLevelUtils.getModel());
        json.put("manufacturer", MCSVLowLevelUtils.getManufacturer());

        return json;
    }

    public static JSONObject createCPUMetadataJSON() {
        JSONObject json = new JSONObject();

        json.put("vendor", MCSVLowLevelUtils.getCPUVendor());
        json.put("model", MCSVLowLevelUtils.getCPUModel());

        json.put("threads", MCSVLowLevelUtils.getCPUThreads());
        json.put("cores", MCSVLowLevelUtils.getCPUCores());

        json.put("contextSwitches", MCSVLowLevelUtils.getCPUContextSwitches());

        json.put("microarchitecture", MCSVLowLevelUtils.getCPUMicroarchitecture());

        json.put("name", MCSVLowLevelUtils.getCPUName());
        json.put("frequency", MCSVLowLevelUtils.getCPUFreq());
        json.put("voltage", MCSVLowLevelUtils.getCPUVoltage());
        json.put("temperature", MCSVLowLevelUtils.getCPUTemp());

        return json;
    }

    public static JSONObject createRAWMetadataJSON() {
        JSONObject json = new JSONObject();

        json.put("systemProperties", MCSVLowLevelUtils.rawSystemPropertiesJSON());
        json.put("env", MCSVLowLevelUtils.rawSystemEnvironmentJSON());
        //json.put("oshi", MCSVLowLevelUtils.dumpSystemInfo());

        return json;
    }

    public static JSONObject createOSMetadataJSON() {
        JSONObject json = new JSONObject();

        json.put("name", MCSVLowLevelUtils.getOSName());
        json.put("version", MCSVLowLevelUtils.getOSVersion());
        json.put("codename", MCSVLowLevelUtils.getOSCodename());
        json.put("buildNumber", MCSVLowLevelUtils.getOSBuildNumber());
        json.put("arch", MCSVLowLevelUtils.getOSArchitecture());
        json.put("uptime", MCSVLowLevelUtils.getSystemUptime());
        json.put("bootAt", MCSVLowLevelUtils.getSystemBootTime());

        return json;
    }

    public static JSONObject createJavaMetadataJSON() {
        JSONObject json = new JSONObject();

        JSONObject jvmInfo = new JSONObject();
        jvmInfo.put("name", MCSVLowLevelUtils.getJVMName());
        jvmInfo.put("version", MCSVLowLevelUtils.getJVMVersion());

        JSONObject classInfo = new JSONObject();
        classInfo.put("version", MCSVLowLevelUtils.getJVMClassVersion());

        JSONObject runtimeInfo = new JSONObject();
        JSONObject memoryInfo = new JSONObject();
        memoryInfo.put("available", MCSVLowLevelUtils.getAvailableJVMMemory());
        memoryInfo.put("total", MCSVLowLevelUtils.getTotalJVMMemory());

        runtimeInfo.put("cpu", MCSVLowLevelUtils.getCoreCount());
        runtimeInfo.put("memory", memoryInfo);

        json.put("jvm", jvmInfo);
        json.put("class", classInfo);
        json.put("runtime", runtimeInfo);
        json.put("version", MCSVLowLevelUtils.getJavaVersion());

        return json;
    }

    public static JSONObject convertHashmapWithObjectKeysToJSON(Map<String, ? extends Object> map) {
        JSONObject json = new JSONObject();

        for (Map.Entry<String, ? extends Object> entry : map.entrySet()) {
            String key = entry.getKey();
            String[] name = key.split("\\.");

            int i = 0;
            JSONObject traversedJSON = json;
            while (name.length > i + 1) {
                if (traversedJSON.get(name[i]) == null) {
                    traversedJSON.put(name[i], new JSONObject());
                }

                Object result = traversedJSON.get(name[i]);
                if (result instanceof JSONObject) {
                    traversedJSON = (JSONObject) result;
                } else {
                    i = -1;
                    break;
                }

                i++;
            }

            if (i < 0) {
                continue;
            }

            traversedJSON.put(name[i], entry.getValue());
        }

        return json;
    }
}
