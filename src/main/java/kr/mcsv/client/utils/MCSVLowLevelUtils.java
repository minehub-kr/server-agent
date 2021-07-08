package kr.mcsv.client.utils;

public class MCSVLowLevelUtils {
    public static String getOSName() {
        return System.getProperty("os.name");
    }

    public static String getOSArchitecture() {
        return System.getProperty("os.arch");
    }

    public static int getCoreCount() {
        return Runtime.getRuntime().availableProcessors();
    }

    public static long getFreeJVMMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    public static long getMaxJVMMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    public static String getJavaVersion() {
        return System.getProperty("java.version");
    }
}
