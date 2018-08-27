package app;

import app.control_panel.ControlPanelStage;
import app.control_panel.appearance.position.PositionResolver;
import app.search.stages.SearchStage;
import app.stages.InitializationStage;
import app.stages.SettingsStage;
import app.tray_icon.TrayIconManager;
import com.tulskiy.keymaster.common.HotKey;
import com.tulskiy.keymaster.common.HotKeyListener;
import com.tulskiy.keymaster.common.Provider;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import kotlin.Unit;
import net.discovery.ServerDiscoveryDaemon;
import net.model.DeviceInfo;
import net.model.ServerInfo;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import system.*;
import system.adb.ADBManager;
import system.bookmarks.BookmarkManager;
import system.commands.CommandManager;
import system.applications.ApplicationManager;
import system.section.SectionManager;
import system.startup.StartupManager;
import system.storage.StorageManager;
import system.system.SystemManager;

import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.*;

@Service
public class MainApp extends Application implements ADBManager.OnUSBDeviceConnectedListener{

    public static int DOKEY_MOBILE_MIN_VERSION = -1;  // Don't change here, modify it in the gradle
    public static int DOKEY_VERSION_NUMBER = -1;  // Don't change here, modify it in the gradle
    public static String DOKEY_VERSION = null;  // Don't change here, modify it in the gradle

    public static final String DOCS_URL = "https://dokey.io/docs/";
    public static final String EDITOR_DOCS_URL = "https://dokey.io/docs/editor/";
    public static final String DOWNLOAD_URL = "https://dokey.io/#download";
    public static final String PLAYSTORE_URL = "https://dokey.io/";  // TODO: change

    private ApplicationContext context;
    private ApplicationManager appManager;
    private TrayIconManager trayIconManager;
    private ApplicationSwitchDaemon applicationSwitchDaemon;
    private ServerDiscoveryDaemon serverDiscoveryDaemon;
    private ActiveApplicationsDaemon activeApplicationsDaemon;
    private BookmarkManager bookmarkManager;
//    private QuickCommandManager quickCommandManager;
//    private EngineServer engineServer;
    private ADBManager adbManager;
    private SystemManager systemManager;
    private DaemonMonitor daemonMonitor;
    private SettingsManager settingsManager;
    private StartupManager startupManager;
    private StorageManager storageManager;
    private CommandManager commandManager;
    private SectionManager sectionManager;
    private PositionResolver positionResolver;

    private ServerSocket serverSocket;  // This is the server socket later used by the EngineServer

    private InitializationStage initializationStage;
    private ControlPanelStage controlPanelStage;
    private SettingsStage settingsStage = null;
//    private CommandEditorStage commandEditorStage = null;
    private SearchStage searchStage;

    private ResourceBundle resourceBundle;

    private Provider provider = Provider.getCurrentProvider(true); // Used for global hotkeys

    // Create the logger
    private final static Logger LOG = Logger.getGlobal();
    public final static String LOG_FILENAME = "log.txt";

    public final static String LOCK_FILENAME = "lock";  // File used as lock to make sure only one instance of dokey is running at each time.
    private RandomAccessFile lockFile = null;

    // Status variables
    private int connectedClientsCount = 0;  // How many clients are currently connected

    // Argument parameters
    private static boolean isFirstStartup = true;  // If true, it means that the app is opened for the first time.
    private static boolean isAutomaticStartup = false;  // If true, it means that the app is started automatically by the system.
    private static boolean openControlPanel = false;  // If true, at startup the control panel is open;
    private static boolean openSettings = false;  // If true, at startup the settings is open;
    private static boolean openCommandEditor = false;  // If true, at startup the command editor is opened;
    private static boolean ignoreLanguage = false;  // If true, force the language to be english.

    public static Locale locale = Locale.ENGLISH;  // Current locale

    public static void main(String[] args) {
        // Load the properties
        Properties properties = new Properties();
        try {
            properties.load(MainApp.class.getResourceAsStream("/proj.properties"));
            DOKEY_VERSION_NUMBER = Integer.parseInt(properties.getProperty("vnumber"));
            DOKEY_MOBILE_MIN_VERSION = Integer.parseInt(properties.getProperty("minimum_mobile_version"));
            DOKEY_VERSION = properties.getProperty("version");
        } catch (IOException e) {
            LOG.severe("Cannot load project properties");
            System.exit(6);
        }

        Level level = Level.INFO;  // logging level

        // Check the arguments
        for (String arg : args) {
            if (arg.equals("-startup")) {
                isAutomaticStartup = true;
            }else if (arg.equals("-cpanel")) {
                openControlPanel = true;
            }else if (arg.equals("-ceditor")) {  // TODO: remove
                openCommandEditor = true;
            }else if (arg.equals("-settings")) {  // TODO: remove
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
        File logFile = new File(StorageManager.getDefault().getStorageDir(), LOG_FILENAME);
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

        LOG.info("VERSION: "+DOKEY_VERSION + " VNUM: "+DOKEY_VERSION_NUMBER+" MIN_VER: "+DOKEY_MOBILE_MIN_VERSION);

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Initialize the storage manager
        storageManager = StorageManager.getDefault();

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
        trayIconManager.initialize();
        trayIconManager.setOnTrayIconClicked(() -> {Platform.runLater(() -> onTrayIconClicked()); return Unit.INSTANCE;});

        // Setup the position resolver
        positionResolver = context.getBean(PositionResolver.class);

        // Get the DaemonMonitor
        daemonMonitor = context.getBean(DaemonMonitor.class);

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

//        // Initialize the sections
//        SectionManager sectionManager = context.getBean(SectionManager.class);
//        sectionManager.getLaunchpadSection();
//        sectionManager.getSystemSection();

        // Initialize the system manager
        systemManager = context.getBean(SystemManager.class);

        // Initialize the active applications daemon
        activeApplicationsDaemon = context.getBean(ActiveApplicationsDaemon.class);

        // Initialize the bookmark manager and import them
        bookmarkManager = context.getBean(BookmarkManager.class);
        bookmarkManager.startImport();

        // Get the settings manager
        settingsManager = context.getBean(SettingsManager.class);

        // Initialize the startup manager
        startupManager = context.getBean(StartupManager.class);

        // load the applications
        loadApplications();

        // Initialize the search bar for the first invocation
        searchStage = context.getBean(SearchStage.class);

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
                            if (startupManager.isBundledInstance()) {
                                startupManager.enableAutomaticStartup();
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

        // Initialize command manager
        commandManager = context.getBean(CommandManager.class);
        commandManager.initialize();

        // Initialize the section manager
        sectionManager = context.getBean(SectionManager.class);
        sectionManager.initialize();

        // Initialize the control panel
        controlPanelStage = context.getBean(ControlPanelStage.class);

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

//        // Start the engine server
//        engineServer = context.getBean(EngineServer.class, serverSocket);
//        engineServer.setDeviceConnectionListener(this);
//        engineServer.start();

        // Update the tray icon status
        trayIconManager.setStatusText(resourceBundle.getString("not_connected"));
        trayIconManager.setLoading(false);

        // Register the global event listeners
        BroadcastManager.getInstance().registerBroadcastListener(BroadcastManager.OPEN_CONTROL_PANEL_REQUEST_EVENT, openControlPanelRequestListener);
        BroadcastManager.getInstance().registerBroadcastListener(BroadcastManager.OPEN_SETTINGS_REQUEST_EVENT, openSettingsRequestListener);
        BroadcastManager.getInstance().registerBroadcastListener(BroadcastManager.OPEN_COMMANDS_REQUEST_EVENT, openCommandsRequestListener);
        BroadcastManager.getInstance().registerBroadcastListener(BroadcastManager.ENABLE_DOKEY_SEARCH_PROPERTY_CHANGED, enableDokeySearchChangedListener);
        BroadcastManager.getInstance().registerBroadcastListener(BroadcastManager.ADD_URL_TO_QUICK_COMMANDS_EVENT, addURLToQuickCommandsListener);

        // Register global hot keys if enabled
        if (settingsManager.isDokeySearchEnabled())
            registerHotKeys();

        if (openControlPanel) {
            showControlPanel();
        }
        if (openSettings) {
            openSettings();
        }
        if (openCommandEditor) {
            openCommandEditor();
        }
    }

    /**
     * Register Global hot keys
     */
    private void registerHotKeys() {
        // Register search hot key
        provider.register(KeyStroke.getKeyStroke("alt SPACE"), new HotKeyListener() {
            @Override
            public void onHotKey(HotKey hotKey) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        onSearchOpenRequest();
                    }
                });
            }
        });
    }

    /**
     * Check if Dokey is already running by analyzing the lock file
     * @return true if already running, false otherwise.
     */
    private boolean checkIfDokeyIsAlreadyRunning() {
        final File inputFile = new File(storageManager.getStorageDir(), LOCK_FILENAME);
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
//        engineServer.stopServer();
        adbManager.stopDaemon();
        activeApplicationsDaemon.stopDaemon();
    }

    private void onTrayIconClicked() {
        // Toggle show control panel visibility
        if (controlPanelStage.isShowing()) {
            hideControlPanel();
        }else{
            showControlPanel();
        }
    }

    private void showControlPanel() {
        controlPanelStage.show();
        // Position the control panel in the correct position
        positionResolver.positionStageOnScreen(controlPanelStage);

        controlPanelStage.animateIn();
    }

    private void hideControlPanel() {
        controlPanelStage.animateOut(() -> {controlPanelStage.hide(); return Unit.INSTANCE;});
    }

    public void onEditorOpenRequest() {
        showControlPanel();
    }

    private void openSettings() {
        if (settingsStage != null) {
            appManager.focusDokey();

            return;
        }

        settingsStage = context.getBean(SettingsStage.class,
                (SettingsStage.OnSettingsCloseListener) () -> settingsStage = null);
        settingsStage.show();
    }

    public void onSettingsOpenRequest() {
        openSettings();
    }

    private void openCommandEditor() {
//        if (commandEditorStage != null) {
//            appManager.focusDokey();
//
//            return;
//        }
//
//        commandEditorStage = context.getBean(CommandEditorStage.class,
//                (CommandEditorStage.OnCommandEditorCloseListener) () -> commandEditorStage = null);
//        commandEditorStage.show();
    }

    public void onHelpOpenRequest() {
        // Open the docs in the browser
        appManager.openWebLink(DOCS_URL);
    }

    public void onSearchOpenRequest() {
        if (!searchStage.isShowing()) {
            // Get the currently active application
            system.applications.Application activeApplication = appManager.getActiveApplication();

            searchStage.preInitialize(activeApplication);
            searchStage.show();
            searchStage.postInitialize();
            appManager.focusSearch();
        }else{
            searchStage.hide();
        }
    }

    public void onQuickCommandsOpenRequest() {
        openCommandEditor();
    }

//    /**
//     * Called when a device connects to the server.
//     *
//     * @param deviceInfo the DeviceInfo object with the information about the connected device.
//     */
//    @Override
//    public void onDeviceConnected(DeviceInfo deviceInfo) {
//        LOG.info("Connected to: "+deviceInfo.getName() +" ID: "+deviceInfo.getID());
//
//        // Set the tray icon as running
//        trayIconManager.setTrayIcon(TrayIconManager.TRAY_ICON_FILENAME_CONNECTED);
//        trayIconManager.setTrayIconStatus(resourceBundle.getString("connected"));
//
//        // Wake up all daemons
//        daemonMonitor.wakeUp();
//        connectedClientsCount++;
//
//        // Create the notification
//        Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
//                String title = resourceBundle.getString("device_connected");
//                String message = resourceBundle.getString("connected_to")+" "+deviceInfo.getName();
//                NotificationFactory.showNotification(title, message);
//            }
//        });
//    }
//
//    /**
//     * Called when a device disconnects from the server.
//     *
//     * @param deviceInfo the DeviceInfo object with the information about the connected device.
//     */
//    @Override
//    public void onDeviceDisconnected(DeviceInfo deviceInfo) {
//        LOG.info("Disconnected from: "+deviceInfo.getName() +" ID: "+deviceInfo.getID());
//
//        // Set the tray icon as ready
//        trayIconManager.setTrayIcon(TrayIconManager.TRAY_ICON_FILENAME_READY);
//        trayIconManager.setTrayIconStatus(resourceBundle.getString("not_connected"));
//
//        // If there are no more devices connected, pause all the daemons
//        connectedClientsCount--;
//        if (connectedClientsCount==0) {
//            daemonMonitor.pause();
//        }
//
//        // Create the notification
//        Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
//                String title = resourceBundle.getString("device_disconnected");
//                String message = resourceBundle.getString("disconnected_from")+" "+deviceInfo.getName();
//                NotificationFactory.showNotification(title, message);
//            }
//        });
//    }
//
//    /**
//     * Called when a connected device needs a more recent desktop version.
//     * @param deviceInfo the DeviceInfo object with the information about the connected device.
//     */
//    @Override
//    public void onDesktopVersionTooLow(DeviceInfo deviceInfo) {
//        Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
//                ButtonType cancel = new ButtonType(resourceBundle.getString("cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
//                ButtonType download = new ButtonType(resourceBundle.getString("download"), ButtonBar.ButtonData.OK_DONE);
//
//                Alert alert = new Alert(Alert.AlertType.WARNING, "", download, cancel);
//                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
//                stage.getIcons().add(new Image(MainApp.class.getResourceAsStream("/assets/icon.png")));
//                stage.setAlwaysOnTop(true);
//                alert.setTitle("Your Dokey Desktop version is too old :(");
//                alert.setHeaderText("The version of Dokey Desktop you are running on your PC is too old!");
//                alert.setContentText("Please download the new version from the Dokey Website");
//                Optional<ButtonType> result = alert.showAndWait();
//                if (result.isPresent() && result.get() == download) {
//                    // Navigate to the dokey website, download section
//                    appManager.openWebLink(DOWNLOAD_URL);
//                }
//            }
//        });
//    }
//
//    /**
//     * Called when a connected device has a version not supported anymore by this Desktop.
//     * @param deviceInfo the DeviceInfo object with the information about the connected device.
//     */
//    @Override
//    public void onMobileVersionTooLow(DeviceInfo deviceInfo) {
//        Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
//                ButtonType cancel = new ButtonType(resourceBundle.getString("cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
//                ButtonType download = new ButtonType(resourceBundle.getString("visit_playstore"), ButtonBar.ButtonData.OK_DONE);
//
//                Alert alert = new Alert(Alert.AlertType.WARNING, "", download, cancel);
//                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
//                stage.getIcons().add(new Image(MainApp.class.getResourceAsStream("/assets/icon.png")));
//                stage.setAlwaysOnTop(true);
//                alert.setTitle("Your Dokey Android version is too old :(");
//                alert.setHeaderText("The version of Dokey Android you are running on your smartphone is too old!");
//                alert.setContentText("Please update it from the PlayStore to connect!");
//                Optional<ButtonType> result = alert.showAndWait();
//                if (result.isPresent() && result.get() == download) {
//                    // Navigate to the playstore page
//                    appManager.openWebLink(PLAYSTORE_URL);
//                }
//            }
//        });
//    }


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
    private BroadcastManager.BroadcastListener openControlPanelRequestListener = new BroadcastManager.BroadcastListener() {
        @Override
        public void onBroadcastReceived(Serializable param) {
            String targetApp = (String) param;
            Platform.runLater(() -> {
                showControlPanel();
            });
        }
    };
    /**
     * Called when the user request to open settings.
     */
    private BroadcastManager.BroadcastListener openSettingsRequestListener = new BroadcastManager.BroadcastListener() {
        @Override
        public void onBroadcastReceived(Serializable param) {
            Platform.runLater(() -> openSettings());
        }
    };
    /**
     * Called when the user request to open commands editor.
     */
    private BroadcastManager.BroadcastListener openCommandsRequestListener = new BroadcastManager.BroadcastListener() {
        @Override
        public void onBroadcastReceived(Serializable param) {
            Platform.runLater(() -> openCommandEditor());
        }
    };


    /**
     * Called when the user request to enable/disable the dokey search hotkey.
     */
    private BroadcastManager.BroadcastListener enableDokeySearchChangedListener = new BroadcastManager.BroadcastListener() {
        @Override
        public void onBroadcastReceived(Serializable param) {
            Boolean isEnabled = (Boolean) param;

            // Change the hotkey registration based on the value
            if (isEnabled) {
                registerHotKeys();
            }else{
                provider.reset();
            }
        }
    };

    /**
     * Called when the user request to add an url to the quick commands.
     */
    private BroadcastManager.BroadcastListener addURLToQuickCommandsListener = new BroadcastManager.BroadcastListener() {
        @Override
        public void onBroadcastReceived(Serializable param) {
//            String url = (String) param;
//            if (url != null)
//                Platform.runLater(() -> {
//                    openCommandEditor();
//                    if (commandEditorStage != null) {
//                        commandEditorStage.insertNewWebLinkCommand(url);
//                    }
//                });
        }
    };
}
