package system;

import net.model.DeviceInfo;
import net.model.ServerInfo;
import org.apache.commons.codec.digest.DigestUtils;
import utils.OSValidator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * Used to get the system information into a ServerInfo object.
 */
public class SystemInfoManager {
    /**
     * @return a ServerInfo object with the system info.
     */
    public static ServerInfo getServerInfo(int serverPort) {
        String serverName = getName();

        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setName(serverName);
        // Generate an id hashing the server name
        deviceInfo.setID(DigestUtils.md5Hex(serverName));  // TODO: improve ID generation
        // Get the system os
        deviceInfo.setOs(getOS());

        // Create the server info object
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setPort(serverPort);
        serverInfo.setDeviceInfo(deviceInfo);

        return serverInfo;
    }

    public static String getName() {
        if (OSValidator.isWindows()) {  // WINDOWS
            return getNameWindows();
        }else if (OSValidator.isMac()) {  // MAC
            return getNameMacOS();
        }

        return null;
    }

    private static String getNameWindows() {
        Map<String, String> env = System.getenv();
        return env.get("COMPUTERNAME");
    }

    private static String getNameMacOS() {
        Runtime runtime = Runtime.getRuntime();

        try {
            // Execute the process
            Process proc = runtime.exec(new String[]{"hostname"});

            // Get the output
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            // Read the hostname
            String hostname = br.readLine();
            return hostname;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static DeviceInfo.OS getOS() {
        if (OSValidator.isWindows()) {
            return DeviceInfo.OS.WIN;
        }else if(OSValidator.isMac()) {
            return DeviceInfo.OS.MAC;
        }
        return null;
    }
}
