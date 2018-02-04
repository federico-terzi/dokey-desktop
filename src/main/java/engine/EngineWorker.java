package engine;

import app.MainApp;
import net.DEDaemon;
import section.model.Section;
import system.ApplicationSwitchDaemon;
import system.model.ApplicationManager;

import java.awt.*;
import java.net.Socket;
import java.util.logging.Logger;

public class EngineWorker extends Thread {
    private Socket socket;
    private ApplicationManager appManager;
    private ApplicationSwitchDaemon applicationSwitchDaemon;
    private OnDeviceConnectionListener deviceConnectionListener;
    private EngineService service = null;

    private volatile boolean shouldTerminate = false;

    // Create the logger
    private final static Logger LOG = Logger.getGlobal();

    public EngineWorker(Socket socket, ApplicationManager appManager, ApplicationSwitchDaemon applicationSwitchDaemon) {
        this.socket = socket;
        this.appManager = appManager;
        this.applicationSwitchDaemon = applicationSwitchDaemon;
    }

    @Override
    public void run() {
        try {
            // Create the engine service
            service = new EngineService(socket, appManager, applicationSwitchDaemon);

            // Set up the connection closed listener
            service.setOnConnectionClosedListener(new DEDaemon.OnConnectionClosedListener() {
                @Override
                public void onConnectionClosed() {
                    shouldTerminate = true;
                }
            });

            // Start the daemons
            service.start();

            // Send the connect notification
            if (deviceConnectionListener != null) {
                deviceConnectionListener.onDeviceConnected("test123", "TEST DEVICE");  // TODO: implement device id logic
            }

            // Loop until should terminate is true
            while(!shouldTerminate) {
                Thread.sleep(1000);
            }
        } catch (AWTException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        LOG.fine("Closing EngineWorker: "+getName());

        // Close the service
        if (service != null) {
            service.close();
        }

        // Send the disconnect notification
        if (deviceConnectionListener != null) {
            deviceConnectionListener.onDeviceDisconnected("test123");  // TODO: implement device id logic
        }
    }

    public OnDeviceConnectionListener getDeviceConnectionListener() {
        return deviceConnectionListener;
    }

    public void setDeviceConnectionListener(OnDeviceConnectionListener deviceConnectionListener) {
        this.deviceConnectionListener = deviceConnectionListener;
    }

    /**
     * Used to notify when a device connects or disconnects from the server
     */
    public interface OnDeviceConnectionListener {
        void onDeviceConnected(String deviceID, String deviceName);
        void onDeviceDisconnected(String deviceID);
    }

}
