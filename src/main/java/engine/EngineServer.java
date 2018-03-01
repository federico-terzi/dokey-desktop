package engine;

import app.MainApp;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import section.model.Section;
import system.ActiveApplicationsDaemon;
import system.ApplicationSwitchDaemon;
import system.SystemManager;
import system.WebLinkResolver;
import system.model.ApplicationManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class EngineServer extends Thread implements ApplicationContextAware {
    private ServerSocket serverSocket = null;

    private EngineWorker.OnDeviceConnectionListener deviceConnectionListener;

    private volatile boolean shouldStop = false;

    // Create the logger
    private final static Logger LOG = Logger.getGlobal();

    private ApplicationContext context;

    public EngineServer(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        setName("Engine Server");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @Override
    public void run() {
        LOG.fine("Server started!");

        // Endless request loop
        while (!shouldStop) {
            try {
                Socket socket = serverSocket.accept();

                EngineWorker worker = context.getBean(EngineWorker.class, socket);
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
