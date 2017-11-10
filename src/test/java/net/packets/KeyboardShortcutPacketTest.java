package net.packets;

import net.model.KeyboardKeys;
import net.packets.DEPacket;
import org.junit.*;
import org.mockito.ArgumentMatchers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class KeyboardShortcutPacketTest {

    @Test
    public void testCleanKeyCombination() throws KeyboardShortcutPacket.KeyboardShortcutParseException {
        assertEquals(KeyboardShortcutPacket.cleanKeyCombination("ctrl + c"), "CTRL+C");
        assertEquals(KeyboardShortcutPacket.cleanKeyCombination("  ctrl +   +shift+ c"), "CTRL+SHIFT+C");
        assertEquals(KeyboardShortcutPacket.cleanKeyCombination("ctrl +alt+ c"), "CTRL+ALT+C");
    }

    @Test(expected = KeyboardShortcutPacket.KeyboardShortcutParseException.class)
    public void testCleanKeyCombinationShouldRaiseError() throws KeyboardShortcutPacket.KeyboardShortcutParseException {
        KeyboardShortcutPacket.cleanKeyCombination(" not_valid + c");
    }

    @Test(expected = KeyboardShortcutPacket.KeyboardShortcutParseException.class)
    public void testCleanKeyCombinationShouldRaiseError2() throws KeyboardShortcutPacket.KeyboardShortcutParseException {
        KeyboardShortcutPacket.cleanKeyCombination(" ctrl + shift + wat");
    }

    @Test
    public void testGetKeys() throws KeyboardShortcutPacket.KeyboardShortcutParseException {
        KeyboardShortcutPacket packet = new KeyboardShortcutPacket("ctrl +  c");
        List<KeyboardKeys> expected = Arrays.asList(KeyboardKeys.VK_CONTROL, KeyboardKeys.VK_C);

        assertTrue(packet.getKeys().equals(expected));
    }
}
