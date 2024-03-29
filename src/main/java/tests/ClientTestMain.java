package tests;

import json.JSONObject;
import model.command.AnalogCommand;
import model.command.Command;
import model.command.SimpleCommand;
import net.DEManager;
import net.LinkManager;
import net.discovery.ClientDiscoveryDaemon;
import net.model.DeviceInfo;
import net.model.ServerInfo;
import utils.SystemInfoManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

public class ClientTestMain {
    public static void main(String[] args) {
        boolean useServiceDiscovery = true;

        if (args.length == 3) {
            int port = Integer.parseInt(args[1]);
            InetAddress address = null;
            try {
                //address = InetAddress.getByName("192.168.56.101");
                address = InetAddress.getByName(args[0]);
            } catch (UnknownHostException e) {
                System.err.println("Errore nell'indirizzo.");
                System.exit(0);
            }

            String keyString = args[2];
            int[] keyInt = Arrays.stream(keyString.split(",")).mapToInt(digit -> Byte.parseByte(digit)).toArray();
            byte[] key = new byte[keyInt.length];
            for (int i = 0; i < keyInt.length; i++) {
                key[i] = (byte) keyInt[i];
            }

            createConnection(address, port, key);
        }else{
            System.out.println("Listening for servers using server discovery...");
            ClientDiscoveryDaemon daemon = new ClientDiscoveryDaemon(new ClientDiscoveryDaemon.OnDiscoveryUpdatedListener() {
                @Override
                public void onDiscoveryUpdated(List<ServerInfo> list) {
                    // Server found, create connection
                    ServerInfo serverInfo = list.get(0);
                    System.out.println("Service found: "+serverInfo);
                    createConnection(serverInfo.getAddress(), serverInfo.getPort(), null);
                }
            });
            daemon.start();
        }
    }

    private static void createConnection(InetAddress address, int port, byte[] key) {
        System.out.print("Connecting to: "+address.getHostAddress()+":"+port+" ...");
        // Apro la socket
        Socket socket = null;

        try {
            socket = new Socket(address, port);
        } catch (IOException e1) {
            e1.printStackTrace();
            System.err.println("Errore nell'apertura della socket.");
            System.exit(4);
        }

        System.out.println("Connected!");

        try {
            for (int i = 0; i<4; i++) {
                // Read the initial dokey numbers
                socket.getInputStream().read();
            }

            LinkManager manager = new LinkManager(socket, SystemInfoManager.getDeviceInfo(), 3,
                    8, true, key, false, null,
                    new DEManager.OnConnectionListener() {
                        @Override
                        public void onConnectionStarted(DeviceInfo deviceInfo, int i) {
                            System.out.println("Connection started with: "+deviceInfo);
                        }

                        @Override
                        public void onReceiverVersionTooLow(DeviceInfo deviceInfo, int i) {
                            System.out.println("Receiver version too low: "+deviceInfo);
                        }

                        @Override
                        public void onConnectionNotAccepted(DeviceInfo deviceInfo, int i) {
                            System.out.println("Connection NOT ACCEPTED: "+deviceInfo);
                        }

                        @Override
                        public void onInvalidKey() {
                            System.out.println("Invalid key");
                        }
                    });


            manager.startDaemon();

            System.out.println("Client started!");

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String line = null;
            while((line = br.readLine()) != null) {
                if (line.startsWith("service")) {  // SERVICE
                    StringTokenizer st = new StringTokenizer(line, ":");
                    st.nextToken();
                    String serviceName = st.nextToken();
                    manager.requestService(serviceName, null, new LinkManager.OnServiceResponseListener() {
                        @Override
                        public void onServiceResponse(JSONObject jsonObject) {
                            System.out.println(jsonObject);
                        }

                        @Override
                        public void onServiceError() {
                            System.out.println("Error");
                        }
                    });
                }else if (line.startsWith("command")) {
                    StringTokenizer st = new StringTokenizer(line, ":");
                    st.nextToken();
                    String commandId = st.nextToken();
                    Command command = new SimpleCommand();
                    command.setId(Integer.valueOf(commandId));
                    manager.sendCommand(command, new LinkManager.OnCommandAcknowledgedListener() {
                        @Override
                        public void onCommandAcknowledged() {
                            System.out.println("ACK");
                        }
                    });
                }else if (line.startsWith("analog")) {  // SERVICE
                    StringTokenizer st = new StringTokenizer(line, ":");
                    st.nextToken();
                    String commandId = st.nextToken();
                    String value = st.nextToken();
                    AnalogCommand command = new AnalogCommand();
                    command.setId(Integer.valueOf(commandId));
                    command.setValue(Float.valueOf(value));
                    manager.sendCommand(command, new LinkManager.OnCommandAcknowledgedListener() {
                        @Override
                        public void onCommandAcknowledged() {
                            System.out.println("ACK");
                        }
                    });
                }
//                }else if (line.startsWith("activeapps")) {  // ACTIVE APP LIST REQUEST
//                    manager.requestAppList(AppListPacket.ACTIVE_APPS, new LinkManager.OnAppListResponseListener() {
//                        @Override
//                        public void onAppListResponseReceived(@NotNull List<? extends RemoteApplication> apps) {
//                            for (RemoteApplication app : apps) {
//                                System.out.println(app);
//                            }
//                        }
//                    });
//                }else if (line.startsWith("cmd")) {  // COMMAND REQUEST
//                    StringTokenizer st = new StringTokenizer(line, ";");
//                    st.nextToken();
//                    String cmd = st.nextToken();
//                    manager.requestCommand(cmd, new LinkManager.OnCommandResponseListener() {
//                        @Override
//                        public void onCommandResponseReceived(String s) {
//                            System.out.println("RES: "+s);
//                        }
//                    });
//                }else if (line.startsWith("ainfo")) {  // APP INFO REQUEST
//                    StringTokenizer st = new StringTokenizer(line, ";");
//                    st.nextToken();
//                    String app = st.nextToken();
//                    manager.requestAppInfo(app, new LinkManager.OnAppInfoResponseListener() {
//                        @Override
//                        public void onAppInfoNotFound(String s) {
//                            System.out.println("Not found for: "+s);
//                        }
//
//                        @Override
//                        public void onAppInfoFound(String s, RemoteApplication remoteApplication) {
//                            System.out.println(remoteApplication);
//                        }
//                    });
//                }else if (line.startsWith("icon")) {  // APP ICON REQUEST
//                    StringTokenizer st = new StringTokenizer(line, ";");
//                    st.nextToken();
//                    String appPath = st.nextToken();
//                    RemoteApplication remoteApplication = new RemoteApplication();
//                    remoteApplication.setPath(appPath);
//
//                    manager.requestAppIcon(remoteApplication, new LinkManager.OnAppIconResponseListener() {
//                        @Override
//                        public void onAppIconReceived(String s, File file) {
//                            System.out.println("Icon found: "+file.getPath());
//                        }
//
//                        @Override
//                        public void onAppIconNotFound(String s) {
//                            System.out.println("Icon not found for: "+s);
//                        }
//                    });
//                }else if (line.startsWith("sicon")) {  // SHORTCUT ICON REQUEST
//                    StringTokenizer st = new StringTokenizer(line, ";");
//                    st.nextToken();
//                    String id = st.nextToken();
//                    IconTheme theme = IconTheme.valueOf(st.nextToken());
//
//                    manager.requestShortcutIcon(id, theme, new LinkManager.OnShortcutIconResponseListener() {
//                        @Override
//                        public void onShortcutIconReceived(String s, File file) {
//                            System.out.println("Icon found: "+file.getPath());
//                        }
//
//                        @Override
//                        public void onShortcutIconNotFound(String s) {
//                            System.out.println("Icon not found for: "+s);
//                        }
//                    });
//                }else if (line.startsWith("wicon")) {  // WEB ICON REQUEST
//                    StringTokenizer st = new StringTokenizer(line, ";");
//                    st.nextToken();
//                    String url = st.nextToken();
//
//                    manager.requestWebLinkIcon(url, new LinkManager.OnWebLinkIconResponseListener() {
//                        @Override
//                        public void onWebLinkIconReceived(String s, File file) {
//                            System.out.println("Icon found: "+file.getPath());
//                        }
//
//                        @Override
//                        public void onWebLinkIconNotFound(String s) {
//                            System.out.println("Icon not found for: "+s);
//                        }
//                    });
//                }else if (line.startsWith("open")) {  // APP OPEN REQUEST
//                    StringTokenizer st = new StringTokenizer(line, ";");
//                    st.nextToken();
//                    String app = st.nextToken();
//                    manager.requestAppOpen(app, new LinkManager.OnAppOpenAckListener() {
//                        @Override
//                        public void onAppOpenAckReceived(String response) {
//                            System.out.println("Response: "+response);
//                        }
//                    });
//                }else if (line.startsWith("folder")) {  // FOLDER OPEN REQUEST
//                    StringTokenizer st = new StringTokenizer(line, ";");
//                    st.nextToken();
//                    String folder = st.nextToken();
//                    manager.requestFolderOpen(folder, new LinkManager.OnFolderOpenAckListener() {
//                        @Override
//                        public void onFolderOpenAckReceived(String response) {
//                            System.out.println("Response: "+response);
//                        }
//                    });
//                }else if (line.startsWith("url")) {  // WEB LINK OPEN REQUEST
//                    StringTokenizer st = new StringTokenizer(line, ";");
//                    st.nextToken();
//                    String url = st.nextToken();
//                    manager.requestWebLink(url, new LinkManager.OnWebLinkAckListener() {
//                        @Override
//                        public void onWebLinkAckReceived(String response) {
//                            System.out.println("Response: "+response);
//                        }
//                    });
//                }else if (line.startsWith("section")) {  // SECTION REQUEST
//                    StringTokenizer st = new StringTokenizer(line, ";");
//                    st.nextToken();
//                    String app = st.nextToken();
//                    long lastEdit = Long.parseLong(st.nextToken());
//                    manager.requestSection(app, lastEdit, new LinkManager.OnSectionResponseListener() {
//                        @Override
//                        public void onSectionAlreadyUpToDate(String s) {
//                            System.out.println("Already up to date");
//                        }
//
//                        @Override
//                        public void onSectionUpdateReceived(String s, long l, Section section) {
//                            System.out.println("Section: "+section);
//                        }
//
//                        @Override
//                        public void onSectionNotFound(String s) {
//                            System.out.println("Not found");
//                        }
//                    });
//                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
