import engine.EngineService;
import net.DEManager;
import net.LinkManager;
import net.model.KeyboardKeys;
import net.packets.DEPacket;
import org.jetbrains.annotations.NotNull;
import system.ApplicationManagerFactory;
import system.model.ApplicationManager;
import system.model.Window;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class ServerMain {
    public static void main(String[] args) {
        // Get the application manager
        ApplicationManager wm = ApplicationManagerFactory.getInstance();
        // Load the applications
        wm.loadApplications(new ApplicationManager.OnLoadApplicationsListener() {
            @Override
            public void onProgressUpdate(String applicationName, int current, int total) {
                System.out.println("Loading: "+applicationName+" "+current+"/"+total);
            }

            @Override
            public void onApplicationsLoaded() {
                System.out.println("loaded!");
            }
        });

        // Open server socket
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(1234);
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

                System.out.println("Connected with: "+socket.getInetAddress().toString());

                EngineService engineService = new EngineService(socket, wm);
                engineService.start();

            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Socket error.");
            } catch (AWTException e) {
                e.printStackTrace();
            }
        }
    }
}
