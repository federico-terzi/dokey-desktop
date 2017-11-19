package net.packets;

import net.model.RemoteApplication;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Event sent from the server to the client to notify an application switch
 */
public class AppSwitchPacket extends JSONPacket {

    public static final int OP_TYPE = 1003;

    // Payload values, must be populated with the parse() method.
    private RemoteApplication application;

    public AppSwitchPacket() {
        super(OP_TYPE);
    }

    public AppSwitchPacket(String payload){
        super(OP_TYPE, payload);
    }

    public AppSwitchPacket(byte[] payload){
        super(OP_TYPE, payload);
    }

    public AppSwitchPacket(long packetID, byte responseFlag, int payloadLength, byte[] payload) {
        super(OP_TYPE, packetID, responseFlag, payloadLength, payload);
    }

    /**
     * Create an AppSwitchPacket request
     */
    public static AppSwitchPacket create(RemoteApplication application){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("app", application.json());
        String json = jsonObject.toString();
        AppSwitchPacket packet = new AppSwitchPacket(json);
        return packet;
    }

    public RemoteApplication getApplication() {
        return application;
    }

    /**
     * Parse the payload json values
     */
    @Override
    public void parse() {
        JSONObject jsonObject = new JSONObject(getPayloadAsString());
        application = RemoteApplication.fromJson(jsonObject.getJSONObject("app"));
        super.parse();
    }
}
