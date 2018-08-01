package utils;

import net.model.DeviceInfo;

/**
 * Used to determine which operating system is in use.
 */
public class OSValidator {
    private static String OS = System.getProperty("os.name").toLowerCase();

    public static boolean isWindows() {

        return (OS.indexOf("win") >= 0);

    }

    public static boolean isMac() {

        return (OS.indexOf("mac") >= 0);

    }

    public static boolean isUnix() {

        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );

    }

    /**
     * @return the correct DeviceInfo.OS based on the current operating system.
     */
    public static DeviceInfo.OS getOS() {
        if (isWindows()) {
            return DeviceInfo.OS.WIN;
        }else if (isMac()) {
            return DeviceInfo.OS.MAC;
        }else if (isUnix()) {
            return DeviceInfo.OS.LINUX;
        }

        return null;
    }
}
