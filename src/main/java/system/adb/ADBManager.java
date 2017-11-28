package system.adb;

import engine.EngineServer;
import net.model.DeviceInfo;

import java.io.IOException;

public class ADBManager implements ADBDaemon.OnDiscoveryUpdatedListener {
    public static final String ADB_PATH = "adb";  // path to the adb executable

    public static final int REMOTE_PORT = 34729;
    public static final int LOCAL_PORT = EngineServer.SERVER_PORT;


    private ADBDaemon daemon;
    private OnUSBDeviceConnectedListener listener;

    public ADBManager(OnUSBDeviceConnectedListener listener) {
        this.listener = listener;
        // Make sure ADB is enabled
        if (checkIfADBIsEnabled()) {
            daemon = new ADBDaemon(this);
        }else{
            System.out.println("ADB can't be executed!");
        }
    }

    /**
     * Called when a device is connected to usb
     * @param device
     */
    @Override
    public void onDeviceConnected(DeviceInfo device) {
        createPortConnection(device);
        System.out.println("USB CON "+device);
        listener.onUSBDeviceConnected(device);
    }

    /**
     * Called when a device is disconnected from usb
     * @param device
     */
    @Override
    public void onDeviceDisconnected(DeviceInfo device) {
        System.out.println("USB DIS "+device);
        listener.onUSBDeviceDisconnected(device);
    }

    /**
     * Start the discovery daemon.
     */
    public void startDaemon() {
        if (daemon != null) {
            daemon.start();
            System.out.println("ADB Daemon started!");
        }
    }

    /**
     * Stop the discovery daemon
     */
    public void stopDaemon() {
        if (daemon != null) {
            daemon.stopDiscovery();
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
