package engine;

import app.MainApp;
import section.model.Section;
import system.ApplicationSwitchDaemon;
import system.model.ApplicationManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class EngineServer extends Thread {

    public static final int SERVER_PORT = 1234;

    private ServerSocket serverSocket = null;

    private ApplicationManager appManager;
    private ApplicationSwitchDaemon applicationSwitchDaemon;
    private EngineWorker.OnDeviceConnectionListener deviceConnectionListener;

    private volatile boolean shouldStop = false;

    // Create the logger
    private final static Logger LOG = Logger.getGlobal();

    public EngineServer(ApplicationManager appManager, ApplicationSwitchDaemon applicationSwitchDaemon) {
        this.appManager = appManager;
        this.applicationSwitchDaemon = applicationSwitchDaemon;
    }

    @Override
    public void run() {
        // Open server socket
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
        } catch (IOException e1) {
            e1.printStackTrace();
            LOG.severe("Error opening socket. "+e1.toString());
            System.exit(4);
        }

        LOG.fine("Server started!");

        // Endless request loop
        while (!shouldStop) {
            try {
                Socket socket = serverSocket.accept();

                EngineWorker worker = new EngineWorker(socket, appManager, applicationSwitchDaemon);
                worker.setDeviceConnectionListener(deviceConnectionListener);
                worker.start();

                LOG.info("Connected with: " + socket.getInetAddress().toString());
            } catch (IOException e) {
                e.printStackTrace();
                LOG.severe("Socket error. "+e.toString());
            }
        }

        try {
            serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopServer() {
        shouldStop = true;
    }

    public EngineWorker.OnDeviceConnectionListener getDeviceConnectionListener() {
        return deviceConnectionListener;
    }

    public void setDeviceConnectionListener(EngineWorker.OnDeviceConnectionListener deviceConnectionListener) {
        this.deviceConnectionListener = deviceConnectionListener;
    }
}
