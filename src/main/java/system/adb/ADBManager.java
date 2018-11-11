package system.adb;

import net.model.DeviceInfo;
import net.model.ServerInfo;
import system.ResourceUtils;
import utils.OSValidator;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class ADBManager implements ADBDaemon.OnDiscoveryUpdatedListener {
    public String adbPath = "adb";  // path to the adb executable

    public static final int REMOTE_PORT = 34729;  // Port used for the forwarding
    public static final int DISCOVERY_PORT = 34730;  // Port used to detect a usb connection from the phone

    private ADBDaemon daemon;
    private ADBDiscoveryServer adbDiscoveryServer;
    private OnUSBDeviceConnectedListener listener;
    private ServerInfo serverInfo;

    // Create the logger
    private final static Logger LOG = Logger.getGlobal();

    public ADBManager(OnUSBDeviceConnectedListener listener, ServerInfo serverInfo, byte[] key) {
        this.listener = listener;
        this.serverInfo = serverInfo;

        boolean adbFound = false;

        // Check if ADB is included in system path
        if (checkIfADBIsEnabled("adb")) {
            LOG.info("ADB was found in system PATH: " + adbPath);
            adbFound = true;
        }else if (checkIfADBIsEnabled(getAndroidSDKADB())) {  // Check if ADB is included in the default Android SDK directory
            adbPath = getAndroidSDKADB();
            LOG.info("ADB was found in the default Android SDK directory: " + adbPath);
            adbFound = true;
        }else{
            // Use the built in ADB version
            // Construct the path based on the OS.
            String OSSuffix = null;
            if (OSValidator.isWindows()) {
                OSSuffix=".exe";
            }else if (OSValidator.isMac()) {
                OSSuffix="";
            }
            File adbExecutable = ResourceUtils.getResource("/"+OSValidator.TAG+"/adb/adb"+OSSuffix);
            if (adbExecutable != null) {
                if (checkIfADBIsEnabled(adbExecutable.getAbsolutePath())) {  // Check if it works
                    adbPath = adbExecutable.getAbsolutePath();
                    LOG.info("ADB: using built in version: "+adbPath);
                    adbFound = true;
                }
            }
        }

        // Make sure ADB is enabled
        if (adbFound) {
            daemon = new ADBDaemon(adbPath, this);
            adbDiscoveryServer = new ADBDiscoveryServer(serverInfo, key);
        }else{
            LOG.warning("ADB can't be executed!");
        }
    }

    /**
     * Called when a device is connected to usb
     * @param device
     */
    @Override
    public void onDeviceConnected(DeviceInfo device) {
        createPortConnection(device);
        createDiscoveryConnection(device);
        LOG.info("USB CON "+device);
        listener.onUSBDeviceConnected(device);
    }

    /**
     * Called when a device is disconnected from usb
     * @param device
     */
    @Override
    public void onDeviceDisconnected(DeviceInfo device) {
        LOG.info("USB DIS "+device);
        listener.onUSBDeviceDisconnected(device);
    }

    /**
     * Start the discovery daemon.
     */
    public void startDaemon() {
        if (daemon != null) {
            daemon.start();
            LOG.fine("ADB Daemon started!");

            adbDiscoveryServer.start();
            LOG.fine("ADB Discovery Server started!");
        }
    }

    /**
     * Stop the discovery daemon
     */
    public void stopDaemon() {
        if (daemon != null) {
            daemon.stopDiscovery();
            adbDiscoveryServer.stopServer();
        }
    }

    /**
     * Open a reverse port forwarding session for the given device
     * using ADB.
     * @param device the device to connect.
     */
    private void createPortConnection(DeviceInfo device) {
        Runtime runtime = Runtime.getRuntime();
        try {
            // Execute the adb process
            Process proc = runtime.exec(new String[]{adbPath, "-s", device.getID(),
                    "reverse", "tcp:"+REMOTE_PORT, "tcp:"+serverInfo.getPort()});

            proc.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Open a reverse port forwarding session for the given device
     * using ADB for the discovery service.
     * @param device the device to connect.
     */
    private void createDiscoveryConnection(DeviceInfo device) {
        Runtime runtime = Runtime.getRuntime();
        try {
            // Execute the adb process
            Process proc = runtime.exec(new String[]{adbPath, "-s", device.getID(),
                    "reverse", "tcp:"+DISCOVERY_PORT, "tcp:"+DISCOVERY_PORT});

            proc.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if ADB is reachable by the system at the specified position.
     * @return true if succeeded, false otherwise.
     */
    private boolean checkIfADBIsEnabled(String adbPath) {
        if (adbPath == null)
            return false;

        // Make the executable runnable if the platform is mac
        if (OSValidator.isMac()) {
            enableADBExecutablePermissions(adbPath);
        }

        Runtime runtime = Runtime.getRuntime();
        try {
            // Execute the adb process
            runtime.exec(new String[]{adbPath});

            return true;
        } catch (IOException e) {
        }
        return false;
    }

    /**
     * Make the executable file runnable.
     * @return true if succeeded, false otherwise.
     */
    private boolean enableADBExecutablePermissions(String adbPath) {
        Runtime runtime = Runtime.getRuntime();
        try {
            // Execute the adb process
            Process proc = runtime.exec(new String[]{"chmod", "u+x", adbPath});

            proc.waitFor();
            return true;
        } catch (IOException e) {
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @return the expected path of ADB in the android sdk installation directory
     */
    private static String getAndroidSDKADB() {
        File homeDir = new File(System.getProperty("user.home")); // Get the user home directory

        // Find the ADB location based on the OS.
        if (OSValidator.isWindows()) {
            File adbFile = new File(homeDir, "AppData/Local/Android/sdk/platform-tools/adb.exe");
            if (adbFile.isFile()) {
                return adbFile.getAbsolutePath();
            }
        }else if(OSValidator.isMac()) {
            File adbFile = new File(homeDir, "Library/Android/sdk/platform-tools/adb");
            if (adbFile.isFile()) {
                return adbFile.getAbsolutePath();
            }
        }

        return null;
    }

    public interface OnUSBDeviceConnectedListener {
        void onUSBDeviceConnected(DeviceInfo deviceInfo);
        void onUSBDeviceDisconnected(DeviceInfo deviceInfo);
    }
}
