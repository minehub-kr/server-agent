package kr.minehub.servers.agent.utils;

import org.apache.logging.log4j.core.LogEvent;
import org.json.simple.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

public class JSONUtils {

    public static JSONObject createMetadataJSON() {
        return JSONUtils.createMetadataJSON(false);
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
        json.put("serverInfo", JSONUtils.createServerInfoJSON());

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

        json.put("ip", NetworkUtils.getLocalIP().getHostAddress());

        return json;
    }

    public static JSONObject createProcessMetadataJSON() {
        JSONObject json = new JSONObject();

        json.put("isElevated", SystemUtils.isElevatedProcess());
        json.put("cmdline", SystemUtils.getRunningCommand());

        return json;
    }

    public static JSONObject createMemoryMetadataJSON() {
        JSONObject json = new JSONObject();

        json.put("available", SystemUtils.getAvailableMemory());
        json.put("total", SystemUtils.getTotalMemory());

        return json;
    }

    public static JSONObject createHardwareMetadataJSON() {
        JSONObject json = new JSONObject();

        json.put("model", SystemUtils.getModel());
        json.put("manufacturer", SystemUtils.getManufacturer());

        return json;
    }

    public static JSONObject createCPUMetadataJSON() {
        JSONObject json = new JSONObject();

        json.put("vendor", SystemUtils.getCPUVendor());
        json.put("model", SystemUtils.getCPUModel());

        json.put("threads", SystemUtils.getCPUThreads());
        json.put("cores", SystemUtils.getCPUCores());

        json.put("contextSwitches", SystemUtils.getCPUContextSwitches());

        json.put("microarchitecture", SystemUtils.getCPUMicroarchitecture());

        json.put("name", SystemUtils.getCPUName());
        json.put("frequency", SystemUtils.getCPUFreq());
        json.put("voltage", SystemUtils.getCPUVoltage());
        json.put("temperature", SystemUtils.getCPUTemp());

        return json;
    }

    public static JSONObject createRAWMetadataJSON() {
        JSONObject json = new JSONObject();

        json.put("systemProperties", SystemUtils.rawSystemPropertiesJSON());
        json.put("env", SystemUtils.rawSystemEnvironmentJSON());

        return json;
    }

    public static JSONObject createOSMetadataJSON() {
        JSONObject json = new JSONObject();

        json.put("name", SystemUtils.getOSName());
        json.put("version", SystemUtils.getOSVersion());
        json.put("codename", SystemUtils.getOSCodename());
        json.put("buildNumber", SystemUtils.getOSBuildNumber());
        json.put("arch", SystemUtils.getOSArchitecture());
        json.put("uptime", SystemUtils.getSystemUptime());
        json.put("bootAt", SystemUtils.getSystemBootTime());

        return json;
    }

    public static JSONObject createJavaMetadataJSON() {
        JSONObject json = new JSONObject();

        JSONObject jvmInfo = new JSONObject();
        jvmInfo.put("name", SystemUtils.getJVMName());
        jvmInfo.put("version", SystemUtils.getJVMVersion());

        JSONObject classInfo = new JSONObject();
        classInfo.put("version", SystemUtils.getJVMClassVersion());

        JSONObject runtimeInfo = new JSONObject();
        JSONObject memoryInfo = new JSONObject();
        memoryInfo.put("available", SystemUtils.getAvailableJVMMemory());
        memoryInfo.put("total", SystemUtils.getTotalJVMMemory());

        runtimeInfo.put("cpu", SystemUtils.getCoreCount());
        runtimeInfo.put("memory", memoryInfo);

        json.put("jvm", jvmInfo);
        json.put("class", classInfo);
        json.put("runtime", runtimeInfo);
        json.put("version", SystemUtils.getJavaVersion());

        return json;
    }

    public static JSONObject logEventToJSON(LogEvent e) {
        JSONObject json = new JSONObject();

        json.put("level", e.getLevel().name());
        json.put("message", e.getMessage().getFormattedMessage());
        json.put("time", e.getTimeMillis());

        JSONObject threadJson = new JSONObject();
        threadJson.put("id", e.getThreadId());
        threadJson.put("name", e.getThreadName());
        threadJson.put("priority", e.getThreadPriority());

        json.put("thread", threadJson);
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
