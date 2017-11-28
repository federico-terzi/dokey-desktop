package system.adb;

import json.JSONObject;
import net.discovery.DiscoveryManager;
import net.model.DeviceInfo;
import net.model.ServerInfo;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Daemon used to discover new devices connected using usb.
 */
public class ADBDaemon extends Thread{
    public static final String ADB_PATH = "adb";  // path to the adb executable

    public static final long CHECK_INTERVAL = 1000;  // Check interval

    private List<DeviceInfo> devices;  // List of connected devices

    private volatile boolean shouldStop = false;

    private OnDiscoveryUpdatedListener listener = null;

    public ADBDaemon(OnDiscoveryUpdatedListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        devices = new ArrayList<>();

        while (!shouldStop) {
            try {
                // Get the currently connected devices
                List<DeviceInfo> connectedDevices = getCurrentDevices();

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


                Thread.sleep(CHECK_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
     * @return List of devices currently connected using ADB command.
     */
    private List<DeviceInfo> getCurrentDevices() {
        List<DeviceInfo> output = new ArrayList<>();

        Runtime runtime = Runtime.getRuntime();
        try {
            // Execute the adb process
            Process proc = runtime.exec(new String[]{ADB_PATH, "devices", "-l"});

            // Get the output
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = null;

            // Skip the first line
            br.readLine();

            // Read the lines
            while ((line = br.readLine()) != null) {
                line = line.trim();
                // Avoid control messages and empty lines
                if (!line.isEmpty() && !line.startsWith("*")) {
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
