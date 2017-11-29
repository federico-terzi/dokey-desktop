package system.adb;

import json.JSONObject;
import net.discovery.DiscoveryManager;
import net.model.DeviceInfo;
import net.model.ServerInfo;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Used as a discovery server with a USB connection through ADB.
 * It listen to requests to the specified port and when connected to a phone
 * it returns the server info.
 */
public class ADBDiscoveryServer extends Thread{
    private volatile boolean shouldStop = false;

    private ServerInfo serverInfo;

    public ADBDiscoveryServer(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(ADBManager.DISCOVERY_PORT);

            // Get the payload
            byte[] deviceInfoPayload = serverInfo.getDeviceInfo().json().toString().getBytes(Charset.forName("UTF-8"));

            // Wait for new requests
            while(!shouldStop) {
                Socket socket = serverSocket.accept();

                // Send the server info

                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                // Write the server port
                dos.writeInt(serverInfo.getPort());

                // Write the payload length
                dos.writeInt(deviceInfoPayload.length);
                // Write the payload
                dos.write(deviceInfoPayload, 0, deviceInfoPayload.length);

                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stop the discovery server.
     */
    public void stopServer() {
        shouldStop = true;
    }
}
