package net.packets;

import net.model.RemoteApplication;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AppSwitchPacketTest {
    @Test
    public void testParse() {
        RemoteApplication app1 = new RemoteApplication();
        app1.setName("app1");
        app1.setPath("path1");

        AppSwitchPacket packet = AppSwitchPacket.create(app1);
        AppSwitchPacket result = new AppSwitchPacket(packet.getPayload());
        result.parse();

        assertEquals(app1, result.getApplication());
    }
}
