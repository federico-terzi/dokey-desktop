package net;

import java.io.IOException;

/**
 * The Daemon that continuously checks for new packets.
 */
public class DEDaemon extends Thread {
    private DEManager manager;

    public DEDaemon(DEManager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        while(true) {
            // Check if there are new packets available
            if (manager.hasNewPackets()) {
                // Read the packet
                try {
                    DEPacket packet = manager.receivePacket();
                    System.out.println(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
