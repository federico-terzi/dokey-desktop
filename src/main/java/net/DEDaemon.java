package net;

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
                DEPacket packet = manager.receivePacket();
            }
        }
    }
}
