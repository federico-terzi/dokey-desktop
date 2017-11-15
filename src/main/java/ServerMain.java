import net.DEManager;
import net.LinkManager;
import net.model.KeyboardKeys;
import net.packets.DEPacket;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class ServerMain {
    public static void main(String[] args) {
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

                LinkManager manager = new LinkManager(socket);
                manager.setKeyboardShortcutListener((application, keys) ->
                        System.out.println("Received: "+keys+" for app: "+application));

                manager.startDaemon();

            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Socket error.");
            }
        }
    }
}
