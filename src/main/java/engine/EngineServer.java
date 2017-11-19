package engine;

import app.MainApp;
import system.ApplicationSwitchDaemon;
import system.model.ApplicationManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class EngineServer extends Thread {

    public static final int SERVER_PORT = 1234;

    private ServerSocket serverSocket = null;

    private ApplicationManager appManager;
    private ApplicationSwitchDaemon applicationSwitchDaemon;
    private EngineWorker.OnDeviceConnectionListener deviceConnectionListener;

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
            System.err.println("Error opening socket.");
            System.exit(4);
        }

        System.out.println("Server started!");

        // Endless request loop
        while(true) {
            try {
                Socket socket = serverSocket.accept();

                EngineWorker worker = new EngineWorker(socket, appManager, applicationSwitchDaemon);
                worker.setDeviceConnectionListener(deviceConnectionListener);
                worker.start();

                System.out.println("Connected with: "+socket.getInetAddress().toString());
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Socket error.");
            }
        }
    }

    public EngineWorker.OnDeviceConnectionListener getDeviceConnectionListener() {
        return deviceConnectionListener;
    }

    public void setDeviceConnectionListener(EngineWorker.OnDeviceConnectionListener deviceConnectionListener) {
        this.deviceConnectionListener = deviceConnectionListener;
    }
}
