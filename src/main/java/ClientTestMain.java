import net.DEManager;
import net.LinkManager;
import net.packets.DEPacket;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

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
            LinkManager manager = new LinkManager(socket);
            manager.startDaemon();

            System.out.println("Client started!");

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String line = null;
            while((line = br.readLine()) != null) {
                if (line.startsWith("keys")) {  // KEYBOARD SHORTCUT
                    StringTokenizer st = new StringTokenizer(line, ";");
                    st.nextToken();
                    String app = st.nextToken();
                    String keys = st.nextToken();
                    manager.sendKeyboardShortcut(app, keys, new LinkManager.OnKeyboardShortcutAcknowledgedListener() {
                        @Override
                        public void onKeyboardShortcutAcknowledged(@NotNull String response) {
                            System.out.println("Response: "+response);
                        }
                    });
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
