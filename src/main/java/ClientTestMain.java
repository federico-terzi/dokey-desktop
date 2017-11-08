import net.DEDaemon;
import net.DEManager;
import net.DEPacket;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientTestMain {
    public static void main(String[] args) {
        // Ottengo l'indirizzo
        InetAddress address = null;
        try {
            address = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            System.err.println("Errore nell'indirizzo.");
            System.exit(0);
        }

        // Apro la socket
        Socket socket = null;

        try {
            socket = new Socket(address, 1234);
        } catch (IOException e1) {
            e1.printStackTrace();
            System.err.println("Errore nell'apertura della socket.");
            System.exit(4);
        }

        try {
            DEManager deManager = new DEManager(socket);
            deManager.startDaemon();

            deManager.setOnPacketReceivedListener(new DEManager.OnPacketReceivedListener() {
                @Override
                public void onPacketReceived(@NotNull DEPacket packet) {
                    //System.out.println(packet);
                }
            });

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String line = null;
            while((line = br.readLine()) != null) {
                byte[] b = line.getBytes();
                DEPacket packet = new DEPacket(1, 1234, DEPacket.RESPONSE_FLAG_REQUEST,
                                                b.length, b);
                deManager.sendPacket(packet, true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
