package net.packets;

import net.packets.DEPacket;
import org.junit.*;
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

public class KeyboardShortcutPacketTest {

    @Test
    public void testCleanKeyCombination() {
        assertEquals(KeyboardShortcutPacket.cleanKeyCombination("ctrl + c"), "CTRL+C");
        assertEquals(KeyboardShortcutPacket.cleanKeyCombination("  ctrl +   +shift+ c"), "CTRL+SHIFT+C");
        assertEquals(KeyboardShortcutPacket.cleanKeyCombination("ctrl +alt+ c"), "CTRL+ALT+C");
    }
}
