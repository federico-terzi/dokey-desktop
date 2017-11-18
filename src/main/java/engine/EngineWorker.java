package engine;

import net.DEDaemon;
import system.model.ApplicationManager;

import java.awt.*;
import java.net.Socket;

public class EngineWorker extends Thread {
    private Socket socket;
    private ApplicationManager appManager;

    private volatile boolean shouldTerminate = false;

    public EngineWorker(Socket socket, ApplicationManager appManager) {
        this.socket = socket;
        this.appManager = appManager;
    }

    @Override
    public void run() {
        try {
            // Create the engine service
            EngineService service = new EngineService(socket, appManager);

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
        } catch (AWTException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Closing EngineWorker: "+getName());
    }
}
