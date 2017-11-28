import net.model.DeviceInfo;
import system.MS.MSIconExtractor;
import system.adb.ADBDaemon;

public class ADBTestMain {
    public static void main(String[] args) {
        ADBDaemon daemon = new ADBDaemon(new ADBDaemon.OnDiscoveryUpdatedListener() {
            @Override
            public void onDeviceConnected(DeviceInfo device) {
                System.out.println("CONNECTED "+ device);
            }

            @Override
            public void onDeviceDisconnected(DeviceInfo device) {
                System.out.println("DISCONNECTED "+ device);
            }
        });

        daemon.start();
        try {
            daemon.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}