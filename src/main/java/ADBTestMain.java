import net.model.DeviceInfo;
import system.adb.ADBManager;

public class ADBTestMain {
    public static void main(String[] args) {
        ADBManager manager = new ADBManager(new ADBManager.OnUSBDeviceConnectedListener() {
            @Override
            public void onUSBDeviceConnected(DeviceInfo deviceInfo) {

            }

            @Override
            public void onUSBDeviceDisconnected(DeviceInfo deviceInfo) {

            }
        });
        manager.startDaemon();

        while(true) {}
    }
}