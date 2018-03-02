package app;

import app.editor.stages.EditorStage;
import app.stages.SettingsStage;
import app.stages.InitializationStage;
import engine.EngineServer;
import engine.EngineWorker;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import net.discovery.ServerDiscoveryDaemon;
import net.model.DeviceInfo;
import net.model.ServerInfo;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import system.*;
import system.adb.ADBManager;
import system.model.ApplicationManager;
import system.section.SectionManager;

import java.io.*;
import java.net.ServerSocket;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.logging.*;

@Service
public class MainApp extends Application implements EngineWorker.OnDeviceConnectionListener, ADBManager.OnUSBDeviceConnectedListener,
        TrayIconManager.OnTrayActionListener{
    private boolean isEditorOpen = false;
    private boolean isSettingsOpen = false;

    private ApplicationContext context;

    private ApplicationManager appManager;
    private TrayIconManager trayIconManager;
    private ApplicationSwitchDaemon applicationSwitchDaemon;
    private ServerDiscoveryDaemon serverDiscoveryDaemon;
    private ActiveApplicationsDaemon activeApplicationsDaemon;
    private EngineServer engineServer;
    private ADBManager adbManager;
    private SystemManager systemManager;

    private ServerSocket serverSocket;  // This is the server socket later used by the EngineServer

    private InitializationStage initializationStage;

    private ResourceBundle resourceBundle;

    // Create the logger
    private final static Logger LOG = Logger.getGlobal();
    public final static String LOG_FILENAME = "log.txt";

    public final static String LOCK_FILENAME = "lock";  // File used as lock to make sure only one instance of dokey is running at each time.
    private RandomAccessFile lockFile = null;

    private static boolean isFirstStartup = true;  // If true, it means that the app is opened for the first time.
    private static boolean isAutomaticStartup = false;  // If true, it means that the app is started automatically by the system.
    private static boolean openEditor = false;  // If true, at startup the editor is open;
    private static boolean openSettings = false;  // If true, at startup the settings is open;
    private static boolean ignoreLanguage = false;  // If true, force the language to be english.

    public static Locale locale = Locale.ENGLISH;  // Current locale

    public static void main(String[] args) {
        Level level = Level.INFO;  // logging level

        // Check the arguments
        for (String arg : args) {
            if (arg.equals("-startup")) {
                isAutomaticStartup = true;
            }else if (arg.equals("-editor")) {
                openEditor = true;
            }else if (arg.equals("-settings")) {
                openSettings = true;
            }else if (arg.equals("-ignorelang")) {
                ignoreLanguage = true;
            }else if (arg.startsWith("-log:")) {  // Log level, for example -log:fine
                String lLevel = arg.split(":")[1];
                if (lLevel.equalsIgnoreCase("fine")) {
                    level = Level.FINE;
                }else if (lLevel.equalsIgnoreCase("finest")) {
                    level = Level.FINEST;
                }
            }
        }

        // Set the logging level
        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(level);
        LOG.setUseParentHandlers( false );
        LOG.setLevel(level);
        LOG.addHandler(consoleHandler);

        // Configure the file handler
        File logFile = new File(CacheManager.getInstance().getCacheDir(), LOG_FILENAME);
        try {
            FileHandler fileHandler = new FileHandler(logFile.getAbsolutePath());
            LOG.addHandler(fileHandler);
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Set up the language resources
        if (!ignoreLanguage) {
            // Set up the correct Locale
            locale = Locale.getDefault();
        }

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Check if dokey is already running. If so, show a dialog and terminate.
        if (checkIfDokeyIsAlreadyRunning()) {
            LOG.severe("Another instance of dokey was already running. Terminating.");
            showAlreadyRunningDialog();
            System.exit(5);
        }

        // Initialize the server socket
        try {
            serverSocket = new ServerSocket(0);  // Let the OS choose the port.
            LOG.info("Server socket started with port: "+serverSocket.getLocalPort());
        } catch (IOException e1) {
            e1.printStackTrace();
            LOG.severe("Error opening socket. "+e1.toString());
            System.exit(4);
        }

        // Setup spring
        context = new AnnotationConfigApplicationContext(AppConfig.class);
        
        // Get the resource bundle
        resourceBundle = context.getBean(ResourceBundle.class);
        
        // Set the MODENA theme
        setUserAgentStylesheet(STYLESHEET_MODENA);

        // instructs the javafx system not to exit implicitly when the last application window is shut.
        Platform.setImplicitExit(false);

        // sets up the tray icon manager
        trayIconManager = context.getBean(TrayIconManager.class);
        javax.swing.SwingUtilities.invokeLater(() -> trayIconManager.initialize(MainApp.this));

        // Initialize the application manager
        appManager = context.getBean(ApplicationManager.class);

        // If the apps are not yet initialized, it means that this is the first startup.
        isFirstStartup = !appManager.isInitialized();

        // Initialize the application switch daemon
        applicationSwitchDaemon = context.getBean(ApplicationSwitchDaemon.class);

        // Initialize the discovery daemon
        ServerInfo serverInfo = SystemInfoManager.getServerInfo(serverSocket.getLocalPort());
        serverDiscoveryDaemon = new ServerDiscoveryDaemon(serverInfo);

        // Initialize the ADB manager
        adbManager = new ADBManager(this, serverInfo);

        // Initialize the sections
        SectionManager sectionManager = context.getBean(SectionManager.class);
        sectionManager.getLaunchpadSection();
        sectionManager.getSystemSection();

        // Initialize the system manager
        systemManager = context.getBean(SystemManager.class);

        // Initialize the active applications daemon
        activeApplicationsDaemon = context.getBean(ActiveApplicationsDaemon.class);

        // load the applications
        loadApplications();

        // Add the shutdownPC hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Stopping all services...");
            stopAllServices();
            LOG.info("Goodbye Dokey");
        }));
    }

    /**
     * Start the application loading process
     */
    private void loadApplications() throws IOException {
        // Create and show the initialization stage if first startup
        if (isFirstStartup) {
            initializationStage = new InitializationStage(resourceBundle);
            initializationStage.show();
        }

        // Task for loading the applications
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                appManager.loadApplications(new ApplicationManager.OnLoadApplicationsListener() {
                    @Override
                    public void onPreloadUpdate(String applicationName, int current, int total) {
                        LOG.fine("Preload: " + applicationName + " " + current + "/" + total);

                        // Calculate the percentage
                        double percentage = (current / (double) total) * 0.5;

                        // Update the intro if visible
                        if (initializationStage != null) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    // Update the initialization stage
                                    initializationStage.updateAppStatus(applicationName, percentage);
                                }
                            });
                        }
                    }

                    @Override
                    public void onProgressUpdate(String applicationName, String iconPath, int current, int total) {
                        LOG.fine("Loading: " + applicationName + " " + current + "/" + total);
                        // Calculate the percentage
                        double percentage = (current / (double) total) * 0.5 + 0.5;

                        // Update the intro if visible
                        if (initializationStage != null) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    // Update the initialization stage
                                    initializationStage.updateAppStatus(applicationName, percentage);
                                }
                            });
                        }
                    }

                    @Override
                    public void onApplicationsLoaded() {
                        LOG.fine("loaded!");
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                MainApp.this.onApplicationsLoaded();
                            }
                        });

                        // Set up automatic startup if checked
                        if (initializationStage != null && initializationStage.isStartupBoxChecked()) {
                            if (StartupManager.getInstance().isBundledInstance()) {
                                StartupManager.getInstance().enableAutomaticStartup();
                                LOG.warning("AUTOMATIC STARTUP ENABLED");
                            }else{
                                LOG.warning("CANNOT ENABLE STARTUP FROM JAVA INSTANCE");
                            }
                        }
                    }
                });
                return null;
            }
        };

        new Thread(task).start();
    }

    /**
     * Called when all applications are loaded.
     */
    private void onApplicationsLoaded() {
        // Hide the initialization
        if (initializationStage != null) {
            initializationStage.hide();
        }

        // Update the tray icon status
        trayIconManager.setTrayIconStatus(resourceBundle.getString("starting_service"));

        // Start the engine server
        startEngineServer();
    }

    /**
     * Start the engine server in another thread
     */
    private void startEngineServer() {
        // Start the application switch daemon
        applicationSwitchDaemon.start();

        // Start the server discovery daemon
        serverDiscoveryDaemon.start();

        // Start the ADB daemon
        adbManager.startDaemon();

        // Start the active apps daemon
        activeApplicationsDaemon.start();

        // Start the engine server
        engineServer = context.getBean(EngineServer.class, serverSocket);
        engineServer.setDeviceConnectionListener(this);
        engineServer.start();

        // Update the tray icon status
        trayIconManager.setTrayIconStatus(resourceBundle.getString("not_connected"));
        trayIconManager.setLoading(false);

        // Register the global event listeners
        BroadcastManager.getInstance().registerBroadcastListener(BroadcastManager.OPEN_EDITOR_REQUEST_EVENT, openEditorRequestListener);

        if (openEditor) {
            openEditor();
        }
        if (openSettings) {
            openSettings();
        }
    }

    /**
     * Check if Dokey is already running by analyzing the lock file
     * @return true if already running, false otherwise.
     */
    private boolean checkIfDokeyIsAlreadyRunning() {
        final File inputFile = new File(CacheManager.getInstance().getCacheDir(), LOCK_FILENAME);
        try {
            lockFile = new RandomAccessFile(inputFile, "rw");
            final FileChannel fc = lockFile.getChannel();
            FileLock fileLock = fc.tryLock();
            if (fileLock == null) {
                return true;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Show a dialog to the user warning that dokey is already running, and the application will stop.
     */
    private void showAlreadyRunningDialog() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(MainApp.class.getResourceAsStream("/assets/icon.png")));
        alert.setTitle("Dokey is already running!");
        alert.setHeaderText("Dokey is already running on this computer!");
        alert.setContentText("Only one instance of Dokey can run at a time.");
        alert.showAndWait();
    }

    /**
     * Stop all the running services
     */
    private void stopAllServices() {
        applicationSwitchDaemon.setShouldStop(true);
        serverDiscoveryDaemon.stopDiscovery();
        engineServer.stopServer();
        adbManager.stopDaemon();
        activeApplicationsDaemon.stopDaemon();
    }

    private void openEditor() {
        if (isEditorOpen) {
            return;
        }

        isEditorOpen = true;
        EditorStage editorStage = context.getBean(EditorStage.class,
                (EditorStage.OnEditorEventListener) () -> isEditorOpen = false);
        editorStage.show();
    }

    @Override
    public void onEditorOpenRequest() {
        openEditor();
    }

    private void openSettings() {
        if (isSettingsOpen) {
            return;
        }

        isSettingsOpen = true;
        SettingsStage settingsStage = context.getBean(SettingsStage.class,
                (SettingsStage.OnSettingsCloseListener) () -> isSettingsOpen = false);
        settingsStage.show();
    }

    @Override
    public void onSettingsOpenRequest() {
        openSettings();
    }

    /**
     * Called when a device connects to the server.
     *
     * @param deviceID the string ID of the device
     * @param deviceName the name of the device
     */
    @Override
    public void onDeviceConnected(String deviceID, String deviceName) {
        LOG.info("Connected to: "+deviceID);

        // Set the tray icon as running
        trayIconManager.setTrayIcon(TrayIconManager.TRAY_ICON_FILENAME_CONNECTED);
        trayIconManager.setTrayIconStatus(resourceBundle.getString("connected"));
    }

    /**
     * Called when a device disconnects from the server.
     *
     * @param deviceID the string ID of the device
     */
    @Override
    public void onDeviceDisconnected(String deviceID) {
        LOG.info("Disconnected from: "+deviceID);

        // Set the tray icon as ready
        trayIconManager.setTrayIcon(TrayIconManager.TRAY_ICON_FILENAME_READY);
        trayIconManager.setTrayIconStatus(resourceBundle.getString("not_connected"));
    }


    /**
     * Called when a new USB device has been connected
     * @param deviceInfo
     */
    @Override
    public void onUSBDeviceConnected(DeviceInfo deviceInfo) {

    }

    /**
     * Called when a USB device has been disconnected.
     * @param deviceInfo
     */
    @Override
    public void onUSBDeviceDisconnected(DeviceInfo deviceInfo) {

    }

    /**
     * Called when the user request to open the editor from the app.
     */
    private BroadcastManager.BroadcastListener openEditorRequestListener = new BroadcastManager.BroadcastListener() {
        @Override
        public void onBroadcastReceived(Serializable param) {
            Platform.runLater(() -> openEditor());
        }
    };
}
