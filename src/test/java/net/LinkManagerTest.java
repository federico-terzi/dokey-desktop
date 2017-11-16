package net;

import net.model.KeyboardKeys;
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
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Test
    public void sendPacketTest() throws IOException {
        // Should not have new packets
        assertFalse(clientManager.hasNewPackets());

        // Create a mock listener
        LinkManager.OnPacketAcknowledgedListener listener = mock(LinkManager.OnPacketAcknowledgedListener.class);

        // Send a packet
        DEPacket packet = DEPacket.stringPacket("TEST");
        serverManager.sendPacket(packet, listener);

        // Trigger artificially the DEDaemon receive packet cycles
        clientManager.forceDaemonReceivePacket();
        serverManager.forceDaemonReceivePacket();

        DEPacket expectedPacket = DEPacket.responsePacket(packet);

        // Make sure the ack callback has been fired and the packet is correct
        verify(listener).onPacketAcknowledged(ArgumentMatchers.eq(expectedPacket));
    }

    @Test
    public void keyboardShortcutTest() throws IOException {
        // Should not have new packets
        assertFalse(clientManager.hasNewPackets());
        assertFalse(serverManager.hasNewPackets());

        List<KeyboardKeys> expected = Arrays.asList(KeyboardKeys.VK_CONTROL, KeyboardKeys.VK_SHIFT,  KeyboardKeys.VK_K);

        // Create mock listeners
        LinkManager.OnKeyboardShortcutAcknowledgedListener clientAckListener = mock(LinkManager.OnKeyboardShortcutAcknowledgedListener.class);
        LinkManager.OnKeyboardShortcutReceivedListener serverReceivedListener = mock(LinkManager.OnKeyboardShortcutReceivedListener.class);
        when(serverReceivedListener.onKeyboardShortcutReceived("testApp", expected)).thenReturn(true);
        when(serverReceivedListener.onKeyboardShortcutReceived("wrongApp", expected)).thenReturn(false);

        // Register the server listener
        serverManager.setKeyboardShortcutListener(serverReceivedListener);

        // Send a keyboard combination
        clientManager.sendKeyboardShortcut("testApp", "ctrl+shift + k", clientAckListener);
        clientManager.sendKeyboardShortcut("wrongApp", "ctrl+shift + k", clientAckListener);

        // Trigger artificially the DEDaemon receive packet cycles
        serverManager.forceDaemonReceivePacket();
        clientManager.forceDaemonReceivePacket();
        serverManager.forceDaemonReceivePacket();
        clientManager.forceDaemonReceivePacket();

        // Make sure the keyboard shortcut ack callback has fired correctly
        verify(clientAckListener).onKeyboardShortcutAcknowledged(ArgumentMatchers.eq(KeyboardShortcutPacket.RESPONSE_OK));
        verify(clientAckListener).onKeyboardShortcutAcknowledged(ArgumentMatchers.eq(KeyboardShortcutPacket.RESPONSE_ERROR));

        // Make sure the keyboard shortcut ack callback has fired correctly
        verify(serverReceivedListener).onKeyboardShortcutReceived("testApp", expected);
        verify(serverReceivedListener).onKeyboardShortcutReceived("wrongApp", expected);
    }
}
