package net.packets;

import net.model.RemoteApplication;
import net.model.KeyboardKeys;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Request to obtain the list of applications in the system.
 */
public class AppListPacket extends JSONPacket {

    public static final int OP_TYPE = 1002;

    // Payload values, must be populated with the parse() method.
    private List<RemoteApplication> applications = null;

    public AppListPacket() {
        super(OP_TYPE);
    }

    public AppListPacket(String payload){
        super(OP_TYPE, payload);
    }

    public AppListPacket(byte[] payload){
        super(OP_TYPE, payload);
    }

    public AppListPacket(long packetID, byte responseFlag, int payloadLength, byte[] payload) {
        super(OP_TYPE, packetID, responseFlag, payloadLength, payload);
    }

    /**
     * Create an AppListPacket
     * @param requestPacket the request DEPacket
     * @param applications List of RemoteApplication to send
     */
    public static AppListPacket createResponse(DEPacket requestPacket, List<RemoteApplication> applications){
        JSONObject jsonObject = new JSONObject();
        List<JSONObject> apps = new ArrayList<>();
        for (RemoteApplication app : applications) {
            apps.add(app.json());
        }
        jsonObject.put("apps", apps);
        String json = jsonObject.toString();
        AppListPacket packet = new AppListPacket(json);
        packet.convertToResponsePacket(requestPacket);
        return packet;
    }

    /**
     * Create an AppListPacket request
     */
    public static AppListPacket createRequest(){
        return new AppListPacket();
    }

    public List<RemoteApplication> getApplications() {
        return applications;
    }

    /**
     * Parse the payload json values
     */
    @Override
    public void parse() {
        JSONObject jsonObject = new JSONObject(getPayloadAsString());
        JSONArray jsonArray = jsonObject.getJSONArray("apps");
        applications = new ArrayList<>();

        for (Object jsonObj : jsonArray) {
            JSONObject jsonApp = (JSONObject) jsonObj;
            RemoteApplication app = RemoteApplication.fromJson(jsonApp);
            applications.add(app);
        }

        super.parse();
    }
}
