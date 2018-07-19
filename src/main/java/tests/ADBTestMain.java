package tests;

import system.adb.ADBConnection;

import java.io.IOException;

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