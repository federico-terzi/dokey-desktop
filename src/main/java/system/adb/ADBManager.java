package system.adb;

import engine.EngineServer;
import net.model.DeviceInfo;
import net.model.ServerInfo;

import java.io.IOException;
import java.util.logging.Logger;

public class ADBManager implements ADBDaemon.OnDiscoveryUpdatedListener {
    public static final String ADB_PATH = "adb";  // path to the adb executable

    public static final int REMOTE_PORT = 34729;  // Port used for the forwarding
    public static final int DISCOVERY_PORT = 34730;  // Port used to detect a usb connection from the phone
    public static final int LOCAL_PORT = EngineServer.SERVER_PORT;  // The local port is the one used by the server


    private ADBDaemon daemon;
    private ADBDiscoveryServer adbDiscoveryServer;
    private OnUSBDeviceConnectedListener listener;
    private ServerInfo serverInfo;

    // Create the logger
    private final static Logger LOG = Logger.getGlobal();

    public ADBManager(OnUSBDeviceConnectedListener listener, ServerInfo serverInfo) {
        this.listener = listener;
        this.serverInfo = serverInfo;

        // Make sure ADB is enabled
        if (checkIfADBIsEnabled()) {
            daemon = new ADBDaemon(this);
            adbDiscoveryServer = new ADBDiscoveryServer(serverInfo);
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
        LOG.fine("USB CON "+device);
        listener.onUSBDeviceConnected(device);
    }

    /**
     * Called when a device is disconnected from usb
     * @param device
     */
    @Override
    public void onDeviceDisconnected(DeviceInfo device) {
        LOG.fine("USB DIS "+device);
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
            Process proc = runtime.exec(new String[]{ADB_PATH, "-s", device.getID(),
                    "reverse", "tcp:"+REMOTE_PORT, "tcp:"+LOCAL_PORT});

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
            Process proc = runtime.exec(new String[]{ADB_PATH, "-s", device.getID(),
                    "reverse", "tcp:"+DISCOVERY_PORT, "tcp:"+DISCOVERY_PORT});

            proc.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if ADB is reachable by the system
     * @return true if succeeded, false otherwise.
     */
    private boolean checkIfADBIsEnabled() {
        Runtime runtime = Runtime.getRuntime();
        try {
            // Execute the adb process
            Process proc = runtime.exec(new String[]{ADB_PATH});

            return true;
        } catch (IOException e) {
        }
        return false;
    }

    public interface OnUSBDeviceConnectedListener {
        void onUSBDeviceConnected(DeviceInfo deviceInfo);
        void onUSBDeviceDisconnected(DeviceInfo deviceInfo);
    }
}
