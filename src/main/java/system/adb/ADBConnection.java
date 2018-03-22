package system.adb;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Create, manage and destroy a connection to the ADB server.
 */
public class ADBConnection {
    private Socket socket;
    private DataOutputStream dos;
    private DataInputStream din;


    public ADBConnection() throws ADBException {
        // Initialize the connection
        try {
            socket = new Socket(InetAddress.getByName("localhost"), 5037);

            // Initialize the streams
            dos = new DataOutputStream(socket.getOutputStream());
            din = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            throw new ADBException("Can't create an ADBConnection in this system");
        }
    }

    /**
     * Start the main loop that listen to changes in tracked devices.
     */
    public void trackLoop(OnDeviceStatusUpdateListener listener) throws IOException, ADBException {
        if (listener == null)
            throw new IllegalArgumentException("The listener cannot be null");

        // Send the command to initialize the tracking
        sendCommand(dos, "host:track-devices");

        // Make sure the command is valid
        if (!isCommandValid(din)) {
            throw new ADBException("An error occurred when requesting the list of devices to ADB");
        }

        while (true) {
            // Get the content of the request
            String content = getContent(din);

            // Create the list of devices
            List<String> devices = new ArrayList<>(5);

            if (!content.isEmpty()) {  // A device was found.
                String[] lines = content.split("\n");

                // Cycle through all lines
                for (String line : lines) {
                    if (!line.isEmpty()) {
                        StringTokenizer tk = new StringTokenizer(line);
                        String serialID = tk.nextToken();
                        String status = tk.nextToken();

                        // Add the device if the status is valid
                        if (status.equals("device")) {
                            devices.add(serialID);
                        }
                    }
                }
            }

            // Notify the listener
            listener.onDeviceStatusUpdate(devices);
        }
    }

    public interface OnDeviceStatusUpdateListener {
        void onDeviceStatusUpdate(List<String> devices);
    }

    /**
     * Check if ADB server is running by trying to connect to it.
     * @return true if working, false otherwise.
     */
    public static boolean isADBRunning() {
        try {
            Socket socket = new Socket(InetAddress.getByName("localhost"), 5037);
            // Initialize the streams
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            DataInputStream din = new DataInputStream(socket.getInputStream());

            sendCommand(dos, "host:version");
            if (isCommandValid(din)) {
                socket.close();
                return true;
            }

            socket.close();
        } catch (IOException e) {
        } catch (ADBException e) {
        }
        return false;
    }

    /**
     * Send the specified command to the ADB server.
     * Also set in the beginning the hash.
     * @param dos the socket output stream.
     * @param command the command to send to ADB.
     */
    private static void sendCommand(DataOutputStream dos, String command) throws IOException {
        // Get the length of the command as a 4 digit HEX string.
        String hexLengthString = getHexLength(command);

        // Combine the two
        String finalCommand = hexLengthString + command;

        // Convert to a byte array with ASCII encoding
        byte[] bytes = new String(finalCommand).getBytes(StandardCharsets.US_ASCII);

        // Send the command
        dos.write(bytes, 0, bytes.length);
    }

    /**
     * Get the ADB response string, like OKAY or FAIL.
     * @param din the socket input stream.
     * @return the ADB response string, like OKAY or FAIL
     */
    private static String getResponse(DataInputStream din) throws IOException {
        byte[] in = new byte[4];
        din.readFully(in, 0, 4);
        return new String(in, StandardCharsets.US_ASCII);
    }

    /**
     * Check if the response was OKAY or FAIL.
     * @param din the socket input stream.
     * @return true if the passed command was valid, false otherwise.
     */
    private static boolean isCommandValid(DataInputStream din) throws IOException, ADBException {
        String res = getResponse(din);

        if (res.equals("OKAY")) {
            return true;
        }else if(res.equals("FAIL")) {
            return false;
        }else{
            throw new ADBException("Received an invalid response: '"+res+"'");
        }
    }

    /**
     * Return the response payload as string
     * @param din
     * @return
     */
    private static String getContent(DataInputStream din) throws IOException {
        // Read the length of the payload
        byte[] lenBytes = new byte[4];
        din.readFully(lenBytes, 0, 4);
        String lenHex = new String(lenBytes, StandardCharsets.US_ASCII);
        int len = Integer.parseInt(lenHex, 16);

        // Get the content
        byte[] content = new byte[len];
        din.readFully(content, 0, len);
        return new String(content, StandardCharsets.US_ASCII);
    }

    /**
     * Return the string length in hex for the ADB command bootstrap.
     * For example, a string of length 12 will return 000C
     * @param command the command
     * @return the string length in hex for the ADB command bootstrap.
     */
    private static String getHexLength(String command) {
        return String.format("%04X", command.length());
    }

    /**
     * Thrown when trying to connect to an ADB server and an error occurs.
     */
    public static class ADBException extends Exception {
        public ADBException(String message) {
            super(message);
        }
    }
}
