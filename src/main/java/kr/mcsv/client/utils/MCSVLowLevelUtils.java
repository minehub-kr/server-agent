package kr.mcsv.client.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class MCSVLowLevelUtils {
    public static SystemInfo systemInfo = new SystemInfo();
    public static HardwareAbstractionLayer hardware = systemInfo.getHardware();
    public static CentralProcessor cpu = hardware.getProcessor();
    public static GlobalMemory ram = hardware.getMemory();
    public static CentralProcessor.ProcessorIdentifier identifier = cpu.getProcessorIdentifier();
    public static OperatingSystem os = systemInfo.getOperatingSystem();

    public static JSONObject rawSystemPropertiesJSON() {
        // This bass is f--kin' raw! what the f--- is going on?
        // - Gordon Ramsay @ Kitchen Nightmares

        Properties systemProps = System.getProperties();
        Map<String, String> result = new HashMap<>();

        for (Map.Entry<Object, Object> x : systemProps.entrySet()) {
            result.put((String) x.getKey(), (String) x.getValue());
        }

        return MCSVUtils.convertHashmapWithObjectKeysToJSON(result);
    }

    public static JSONObject rawSystemEnvironmentJSON() {
        Map<String, String> envs = System.getenv();

        return MCSVUtils.convertHashmapWithObjectKeysToJSON(envs);
    }

    public static JSONObject dumpSystemInfo() {
        ObjectMapper mapper = new ObjectMapper();
        JSONObject json = new JSONObject();
        JSONParser parser = new JSONParser();

        // Object to JSON in String
        String jsonInString = "{}";

        try {
            jsonInString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(hardware);
            JSONObject result = (JSONObject) parser.parse(jsonInString);
            json.put("hardware", result);

            JSONObject osJson = MCSVUtils.createOSMetadataJSON();
            jsonInString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(os.getVersionInfo());
            result = (JSONObject) parser.parse(jsonInString);
            osJson.put("version", result);

            jsonInString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(os.getNetworkParams());
            result = (JSONObject) parser.parse(jsonInString);
            osJson.put("network", result);

            jsonInString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(os.getInternetProtocolStats());
            result = (JSONObject) parser.parse(jsonInString);
            osJson.put("ips", result);

            json.put("os", osJson);

            /*
            jsonInString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(os);
            result = (JSONObject) parser.parse(jsonInString);
            json.put("os", result);
            */

        } catch(JsonProcessingException | ParseException e) {}

        return json;
    }

    public static String getOSName() {
        return System.getProperty("os.name");
    }

    public static long getSystemUptime() {
        return os.getSystemUptime();
    }

    public static long getSystemBootTime() {
        return os.getSystemBootTime();
    }

    public static String getRunningCommand() {
        return System.getProperty("sun.java.command");
    }

    public static boolean isElevatedProcess() {
        return os.isElevated();
    }

    public static String getCPUVendor() {
        return identifier.getVendor();
    }

    public static String getCPUModel() {
        return identifier.getModel();
    }

    public static String getCPUMicroarchitecture() {
        return identifier.getMicroarchitecture();
    }

    public static String getCPUName() {
        return identifier.getName();
    }

    public static long getCPUFreq() {
        return identifier.getVendorFreq();
    }

    public static double getCPUTemp() {
        return hardware.getSensors().getCpuTemperature();
    }

    public static double getCPUVoltage() {
        return hardware.getSensors().getCpuVoltage();
    }

    public static long getAvailableMemory () {
        return ram.getAvailable();
    }

    public static long getTotalMemory() {
        return ram.getTotal();
    }

    public static String getModel () {
        return hardware.getComputerSystem().getModel();
    }

    public static String getManufacturer() {
        return hardware.getComputerSystem().getManufacturer();
    }

    public static int getCPUCores() {
        return cpu.getPhysicalProcessorCount();
    }

    public static int getCPUThreads() {
        return cpu.getLogicalProcessorCount();
    }

    public static long getCPUContextSwitches() {
        return cpu.getContextSwitches();
    }

    public static String getOSVersion() {
        return os.getVersionInfo().getVersion();
    }

    public static String getOSCodename() {
        return os.getVersionInfo().getCodeName();
    }

    public static String getOSBuildNumber() {
        return os.getVersionInfo().getBuildNumber();
    }

    public static String getOSArchitecture() {
        return System.getProperty("os.arch");
    }

    public static String getJVMName() {
        return System.getProperty("java.vm.name");
    }

    public static String getJVMVersion() {
        return System.getProperty("java.runtime.version");
    }

    public static String getJVMClassVersion() {
        return System.getProperty("java.class.version");
    }

    public static int getCoreCount() {
        return Runtime.getRuntime().availableProcessors();
    }

    public static long getAvailableJVMMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    public static long getTotalJVMMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    public static String getJavaVersion() {
        return System.getProperty("java.version");
    }
}
