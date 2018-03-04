package engine;

import net.DEDaemon;
import net.DEManager;
import net.model.DeviceInfo;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import system.*;
import system.model.ApplicationManager;

import java.awt.*;
import java.net.Socket;
import java.util.logging.Logger;

public class EngineWorker extends Thread implements ApplicationContextAware{
    private Socket socket;
    private OnDeviceConnectionListener deviceConnectionListener;
    private EngineService service = null;
    private DeviceInfo receiverInfo = null;

    private ApplicationContext context;


    private volatile boolean shouldTerminate = false;

    // Create the logger
    private final static Logger LOG = Logger.getGlobal();

    public EngineWorker(Socket socket) {
        this.socket = socket;

        setName("Engine Worker");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @Override
    public void run() {
        try {
            // Create the engine service
            service = context.getBean(EngineService.class, socket, new DEManager.OnConnectionListener() {
                @Override
                public void onConnectionNotAccepted(DeviceInfo deviceInfo, int version) {
                    receiverInfo = deviceInfo;
                    LOG.warning("Connection not accepted by the phone: "+deviceInfo.getName()+" with version: "+version);
                    shouldTerminate = true;

                    // Send the notification
                    if (deviceConnectionListener != null) {
                        deviceConnectionListener.onDesktopVersionTooLow(deviceInfo);
                    }
                }

                @Override
                public void onReceiverVersionTooLow(DeviceInfo deviceInfo, int version) {
                    receiverInfo = deviceInfo;
                    LOG.warning("Not accepting connection, phone has version too low: "+deviceInfo.getName()+" with version: "+version);
                    shouldTerminate = true;

                    // Send the notification
                    if (deviceConnectionListener != null) {
                        deviceConnectionListener.onMobileVersionTooLow(deviceInfo);
                    }
                }

                @Override
                public void onConnectionStarted(DeviceInfo deviceInfo, int version) {
                    receiverInfo = deviceInfo;
                    LOG.info("Connection accepted by: "+deviceInfo.getName()+" with version: "+version);
                    // Send the connect notification
                    if (deviceConnectionListener != null) {
                        deviceConnectionListener.onDeviceConnected(deviceInfo);
                    }
                }
            });

            // Set up the connection closed listener
            service.setOnConnectionClosedListener(new DEDaemon.OnConnectionClosedListener() {
                @Override
                public void onConnectionClosed() {
                    shouldTerminate = true;
                }
            });

            // Start the daemons
            service.start();

            // Loop until should terminate is true
            while(!shouldTerminate) {
                Thread.sleep(1000);
            }
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
            deviceConnectionListener.onDeviceDisconnected(receiverInfo);
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
        void onDeviceConnected(DeviceInfo deviceInfo);
        void onDeviceDisconnected(DeviceInfo deviceInfo);
        void onDesktopVersionTooLow(DeviceInfo deviceInfo);
        void onMobileVersionTooLow(DeviceInfo deviceInfo);
    }

}
