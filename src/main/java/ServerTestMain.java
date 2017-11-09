import net.DEDaemon;
import net.DEManager;
import net.DEPacket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerTestMain {
    public static void main(String[] args) {
        // Apro la server socket
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(1234);
        } catch (IOException e1) {
            e1.printStackTrace();
            System.err.println("Errore nell'apertura della socket.");
            System.exit(4);
        }

        // Ciclo che avvia i gestori di richieste
        while(true) {
            try {
                Socket socket = serverSocket.accept();

                DEManager deManager = new DEManager(socket);
                deManager.startDaemon();

                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                String line = null;
                while((line = br.readLine()) != null) {
                    byte[] b = line.getBytes();
                    DEPacket packet = new DEPacket(1, 1234, DEPacket.RESPONSE_FLAG_REQUEST,
                            b.length, b);
                    deManager.sendPacket(packet);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Errore nell'apertura della socket.");
            }
        }
    }
}
