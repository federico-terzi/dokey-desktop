package net;

import org.junit.*;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DEManagerTest {

    private static final int TEST_PORT = 12345;

    private ServerSocket serverSocket;
    private Socket serverCurrentSocket;
    private Socket clientSocket;

    private DataOutputStream dos;
    private DataInputStream din;
    private DEManager clientManager;
    private DEManager serverManager;

    @Before
    public void setUp() throws IOException {
        // Setup the sockets and the DEManager
        serverSocket = new ServerSocket(TEST_PORT);
        clientSocket = new Socket(InetAddress.getByName("localhost"), TEST_PORT);
        serverCurrentSocket = serverSocket.accept();

        dos = new DataOutputStream(clientSocket.getOutputStream());
        din = new DataInputStream(serverCurrentSocket.getInputStream());

        clientManager = new DEManager(clientSocket);
        serverManager = new DEManager(serverCurrentSocket);
    }

    @After
    public void tearDown() throws IOException {
        // Disconnect the sockets
        serverCurrentSocket.close();
        clientSocket.close();
        serverSocket.close();

        dos = null;
        din = null;
        clientManager = null;
        serverManager = null;
    }

    @Test
    public void socketConnectionTest() throws IOException {
        // Write and read data from the socket
        dos.writeInt(1234);
        assertEquals(1234, din.readInt());
    }

    @Test
    public void hasNewPacketsTest() throws IOException {
        // Should not have new packets
        assertFalse(clientManager.hasNewPackets());

        // Send a packet
        DEPacket packet = DEPacketFactory.Companion.generateStringPacket(1, "TEST");
        serverManager.sendPacket(packet);

        // Should have the packet
        assertTrue(clientManager.hasNewPackets());
    }

    @Test
    public void sendAndReceiveTest() throws IOException {
        // Should not have new packets
        assertFalse(clientManager.hasNewPackets());

        // Send a packet
        DEPacket packet = DEPacketFactory.Companion.generateStringPacket(1, "TEST");
        serverManager.sendPacket(packet);

        // Should have the packet
        assertTrue(clientManager.hasNewPackets());
        DEPacket received = clientManager.receivePacket();
        // Compare the string content
        assertEquals(received.getPayloadAsString(), packet.getPayloadAsString());

        // Compare the object itself
        assertTrue(packet.equals(received));
    }

    @Test
    public void sendAckCallbackTest() throws IOException {
        // Should not have new packets
        assertFalse(clientManager.hasNewPackets());

        // Create a mock listener
        DEManager.OnPacketEventListener listener = mock(DEManager.OnPacketEventListener.class);

        // Set the listener
        serverManager.setOnPacketEventListener(listener);

        // Send a packet
        DEPacket packet = DEPacketFactory.Companion.generateStringPacket(1, "TEST");
        serverManager.sendPacket(packet);

        // Trigger artificially the DEDaemon receive packet cycles
        clientManager.forceDaemonReceivePacket();
        serverManager.forceDaemonReceivePacket();

        DEPacket expectedPacket = DEPacketFactory.Companion.generateResponsePacket(packet);

        // Make sure the ack callback has been fired and the packet is correct
        verify(listener).onPacketAcknowledged(ArgumentMatchers.eq(expectedPacket));
    }

    @Test
    public void receiveCallbackTest() throws IOException {
        // Should not have new packets
        assertFalse(clientManager.hasNewPackets());

        // Create a mock listener
        DEManager.OnPacketEventListener listener = mock(DEManager.OnPacketEventListener.class);

        // Set the listener
        clientManager.setOnPacketEventListener(listener);

        // Send a packet
        DEPacket packet = DEPacketFactory.Companion.generateStringPacket(1, "TEST");
        serverManager.sendPacket(packet);

        // Trigger artificially the DEDaemon receive packet cycles
        clientManager.forceDaemonReceivePacket();
        serverManager.forceDaemonReceivePacket();

        // Make sure the ack callback has been fired and the packet is correct
        verify(listener).onPacketReceived(ArgumentMatchers.eq(packet));
    }
}
