package system.adb;

import net.model.DeviceInfo;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Daemon used to discover new devices connected using usb.
 */
public class ADBDaemon extends Thread{

    private List<DeviceInfo> devices;  // List of connected devices

    private volatile boolean shouldStop = false;

    private String adbPath;
    private OnDiscoveryUpdatedListener listener = null;

    // Create the logger
    private final static Logger LOG = Logger.getGlobal();

    public ADBDaemon(String adbPath, OnDiscoveryUpdatedListener listener) {
        this.adbPath = adbPath;
        this.listener = listener;

        setName("ADB Daemon");
    }

    @Override
    public void run() {
        devices = new ArrayList<>();

        while (!shouldStop) {
            // Make sure the ADB server is running.
            // If not running, restart it.
            if (!ADBConnection.isADBRunning()) {
                LOG.info("ADB: restarting ADB server");
                startADBServer();

                // Sleep for a bit
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            try {
                // Open the connection with ADB
                ADBConnection adbConnection = new ADBConnection();

                adbConnection.trackLoop(deviceList -> {
                    // Get the list of connected devices
                    List<DeviceInfo> connectedDevices = deviceList.stream().map(id -> {
                        // Package the information
                        DeviceInfo deviceInfo = new DeviceInfo();
                        deviceInfo.setID(id);
                        deviceInfo.setName(id);
                        deviceInfo.setOs(DeviceInfo.OS.ANDROID);

                        return deviceInfo;
                    }).collect(Collectors.toList());

                    // Check for new devices
                    for (DeviceInfo connectedDevice : connectedDevices) {
                        boolean isContained = false;

                        for (DeviceInfo savedDevice : devices) {
                            if (savedDevice.equals(connectedDevice)) {
                                isContained = true;
                                break;
                            }
                        }

                        // New device connected
                        if (!isContained) {
                            listener.onDeviceConnected(connectedDevice);
                            devices.add(connectedDevice);
                        }
                    }

                    List<DeviceInfo> toBeRemoved = new ArrayList<>();

                    // Check for disconnected devices
                    for (DeviceInfo savedDevice : devices) {
                        boolean isContained = false;

                        for (DeviceInfo connectedDevice : connectedDevices) {
                            if (savedDevice.equals(connectedDevice)) {
                                isContained = true;
                                break;
                            }
                        }

                        // Device disconnected
                        if (!isContained) {
                            listener.onDeviceDisconnected(savedDevice);
                            toBeRemoved.add(savedDevice);
                        }
                    }

                    // Remove all the disconnected devices
                    for (DeviceInfo device : toBeRemoved) {
                        devices.remove(device);
                    }
                });
            } catch (Exception e) {
                LOG.warning("ADB ERROR: "+e.getMessage());
            }
        }
    }

    /**
     * Start the ADB server
     */
    private void startADBServer() {
        Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec(new String[]{adbPath, "start-server"});
        } catch (IOException e) {
            LOG.warning("ADB ERROR: cannot start ADB server");
        }
    }

    /**
     * Stop the device discovery.
     */
    public void stopDiscovery() {
        shouldStop = true;
    }

    public interface OnDiscoveryUpdatedListener {
        void onDeviceConnected(DeviceInfo device);
        void onDeviceDisconnected(DeviceInfo device);
    }

    /**
     * Kill the adb server.
     */
    private void killAdbServer() {

        Runtime runtime = Runtime.getRuntime();
        try {
            // Execute the adb process
            Process proc = runtime.exec(new String[]{adbPath, "kill-server"});

            proc.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return List of devices currently connected using ADB command.
     */
    @Deprecated
    private List<DeviceInfo> getCurrentDevices() {
        List<DeviceInfo> output = new ArrayList<>();

        Runtime runtime = Runtime.getRuntime();
        try {
            // Execute the adb process
            Process proc = runtime.exec(new String[]{adbPath, "devices", "-l"});

            // Get the output
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = null;

            // Skip the first line
            br.readLine();

            // Read the lines
            while ((line = br.readLine()) != null) {
                line = line.trim();
                // Avoid control messages and empty lines
                if (!line.isEmpty() && !line.startsWith("*") && !line.startsWith("adb") && !line.startsWith("could") &&
                        !line.startsWith("error")) {
                    StringTokenizer st = new StringTokenizer(line);
                    String id = st.nextToken();
                    String model = null;
                    // Get the model
                    while(st.hasMoreTokens()) {
                        String current = st.nextToken();
                        if (current.startsWith("model:")) {
                            model = current.split(":")[1];
                        }
                    }
                    // Set the ID as the model if it wasn't found
                    if (model == null) {
                        model = id;
                    }

                    // Package the information
                    DeviceInfo deviceInfo = new DeviceInfo();
                    deviceInfo.setID(id);
                    deviceInfo.setName(model);
                    deviceInfo.setOs(DeviceInfo.OS.ANDROID);

                    output.add(deviceInfo);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
    }
}
