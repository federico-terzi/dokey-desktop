package net;

import net.packets.DEPacket;
import net.packets.KeyboardShortcutPacket;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class LinkManagerTest {

    private static final int TEST_PORT = 12345;

    private ServerSocket serverSocket;
    private Socket serverCurrentSocket;
    private Socket clientSocket;

    private DataOutputStream dos;
    private DataInputStream din;
    private LinkManager clientManager;
    private LinkManager serverManager;

    @Before
    public void setUp() throws IOException {
        // Setup the sockets and the DEManager
        serverSocket = new ServerSocket(TEST_PORT);
        clientSocket = new Socket(InetAddress.getByName("localhost"), TEST_PORT);
        serverCurrentSocket = serverSocket.accept();

        dos = new DataOutputStream(clientSocket.getOutputStream());
        din = new DataInputStream(serverCurrentSocket.getInputStream());

        clientManager = new LinkManager(clientSocket);
        serverManager = new LinkManager(serverCurrentSocket);
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
    public void getSpecializedPacketTest() throws IOException {
        DEPacket receivedPacket = DEPacket.stringPacket("test");
        receivedPacket.setOpType(KeyboardShortcutPacket.OP_TYPE);

        DEPacket packet = clientManager.getSpecializedPacket(receivedPacket);
        assertTrue(packet instanceof KeyboardShortcutPacket);

        KeyboardShortcutPacket keyboardShortcutPacket = (KeyboardShortcutPacket) packet;
        assertEquals(keyboardShortcutPacket.getPayloadAsString(), "test");
    }
}
