package kr.mcsv.client.utils;

import org.json.simple.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

public class MCSVJSONUtils {

    public static JSONObject createMetadataJSON() {
        return MCSVJSONUtils.createMetadataJSON(false);
    }

    public static JSONObject createExceptionJSON(Exception e) {
        JSONObject exception = new JSONObject();
        exception.put("message", e.getMessage());
        exception.put("localizedMessage", e.getLocalizedMessage());
        exception.put("cause", e.getCause());

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        exception.put("stacktrace", sw.toString());
        return exception;
    }

    public static JSONObject createMetadataJSON(boolean includeRaw) {
        JSONObject json = new JSONObject();

        json.put("version",  1);
        json.put("serverInfo", MCSVJSONUtils.createServerInfoJSON());

        return json;
    }

    public static JSONObject createServerStartupJSON() {
        JSONObject json = new JSONObject();

        json.put("version",  1);
        json.put("serverInfo", createServerInfoJSON());

        return json;
    }

    public static JSONObject createServerInfoJSON() {
        return createServerInfoJSON(false);
    }

    public static JSONObject createServerInfoJSON(boolean includeRaw) {
        JSONObject json = createPerformanceJSON(includeRaw);

        json.put("os", createOSMetadataJSON());
        json.put("process", createProcessMetadataJSON());
        json.put("hardware", createHardwareMetadataJSON());
        json.put("network", createNetworkMetadataJSON());

        if (includeRaw) {
            json.put("raw", createRAWMetadataJSON());
        }

        return json;
    }

    public static JSONObject createPerformanceJSON() {
        return createPerformanceJSON(false);
    }

    public static JSONObject createPerformanceJSON(boolean includeRaw) {
        JSONObject json = new JSONObject();

        json.put("java", createJavaMetadataJSON());
        json.put("cpu", createCPUMetadataJSON());
        json.put("memory", createMemoryMetadataJSON());

        return json;
    }

    public static JSONObject createNetworkMetadataJSON() {
        JSONObject json = new JSONObject();

        json.put("ip", MCSVNetworkUtils.getLocalIP().getHostAddress());
        json.put("publicIp", MCSVNetworkUtils.getPublicIP().getHostAddress());

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
