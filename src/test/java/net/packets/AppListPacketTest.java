package net.packets;

import net.model.KeyboardKeys;
import net.model.RemoteApplication;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AppListPacketTest {
    @Test
    public void testParse() {
        RemoteApplication app1 = new RemoteApplication();
        app1.setName("app1");
        app1.setPath("path1");

        RemoteApplication app2 = new RemoteApplication();
        app2.setName("app2");
        app2.setPath("path2");

        List<RemoteApplication> apps = new ArrayList<RemoteApplication>();

        AppListPacket request = AppListPacket.createRequest();
        AppListPacket response = AppListPacket.createResponse(request, apps);

        AppListPacket receivedResponse = new AppListPacket(response.getPayload());
        receivedResponse.parse();

        assertEquals(receivedResponse.getApplications(), apps);
    }
}
