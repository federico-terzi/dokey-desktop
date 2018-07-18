package functional_tests;

import net.model.DeviceInfo;
import system.adb.ADBConnection;
import system.adb.ADBManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ADBTestMain {
    public static void main(String[] args) {
//        ADBManager manager = new ADBManager(new ADBManager.OnUSBDeviceConnectedListener() {
//            @Override
//            public void onUSBDeviceConnected(DeviceInfo deviceInfo) {
//
//            }
//
//            @Override
//            public void onUSBDeviceDisconnected(DeviceInfo deviceInfo) {
//
//            }
//        }, serverInfo);
//        manager.startDaemon();

        try {
            ADBConnection adbConnection = new ADBConnection();
            adbConnection.trackLoop(devices -> System.out.println(devices));
        } catch (ADBConnection.ADBException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}