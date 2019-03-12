package app;

import app.alert.AlertFactory;
import app.control_panel.ControlPanelStage;
import app.control_panel.appearance.position.PositionResolver;
import app.intro.IntroStage;
import app.notifications.NotificationFactory;
import app.search.stages.SearchStage;
import app.stages.InitializationStage;
import app.tray_icon.TrayIconManager;
import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.WString;
import com.tulskiy.keymaster.common.HotKey;
import com.tulskiy.keymaster.common.HotKeyListener;
import com.tulskiy.keymaster.common.Provider;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
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
import system.internal_ipc.IPCManager;
import system.internal_ipc.IPCServer;
import system.logging.LoggerOutputStream;
import system.section.SectionManager;
import system.server.*;
import system.startup.StartupManager;
import system.storage.StorageManager;
import system.system.SystemManager;
import utils.SystemInfoManager;

import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.*;

import static system.internal_ipc.IPCManagerKt.IPC_OPEN_COMMAND;

@Service
public class MainApp extends Application implements ADBManager.OnUSBDeviceConnectedListener, MobileWorker.OnDeviceConnectionListener {

    public static int DOKEY_MOBILE_MIN_VERSION = -1;  // Don't change here, modify it in the gradle
    public static int DOKEY_VERSION_NUMBER = -1;  // Don't change here, modify it in the gradle
    public static String DOKEY_VERSION = null;  // Don't change here, modify it in the gradle

    public static final String DOCS_URL = "https://dokey.io/docs/";
    public static final String EDITOR_DOCS_URL = "https://dokey.io/docs/editor/";
    public static final String DOWNLOAD_URL = "https://dokey.io/#download";
    public static final String PLAYSTORE_URL = "https://play.google.com/store/apps/details?id=io.rocketguys.dokey";

    private static StorageManager storageManager;

    private ApplicationContext context;
    private ApplicationManager appManager;
    private TrayIconManager trayIconManager;
    private ApplicationSwitchDaemon applicationSwitchDaemon;
    private ApplicationPathResolver applicationPathResolver;
    private ActiveApplicationsDaemon activeApplicationsDaemon;
    private BookmarkManager bookmarkManager;
    private MobileServer mobileServer;
    private ADBManager adbManager;
    private SystemManager systemManager;
    private DaemonMonitor daemonMonitor;
    private SettingsManager settingsManager;
    private StartupManager startupManager;
    private CommandManager commandManager;
    private SectionManager sectionManager;
    private PositionResolver positionResolver;
    private KeyGenerator keyGenerator;
    private HandshakeDataBuilder handshakeDataBuilder;
    private IPCServer ipcServer;

    private ServerSocket serverSocket;  // This is the server socket later used by the EngineServer

    private IntroStage introStage;
    private ControlPanelStage controlPanelStage;
    private SearchStage searchStage;

    private ResourceBundle resourceBundle;

    private Provider provider = Provider.getCurrentProvider(true); // Used for global hotkeys

    // Create the logger
    private final static Logger LOG = Logger.getGlobal();
    public final static String LOG_FILENAME = "log.txt";

    public final static String LOCK_FILENAME = "lock";  // File used as lock to make sure only one instance of dokey is running at each time.
    private static RandomAccessFile lockFile = null;

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

        // Initialize the storage manager
        storageManager = StorageManager.getDefault();

        // Check if dokey is already running. If so, show a dialog and terminate.
        if (checkIfDokeyIsAlreadyRunning()) {
            LOG.severe("Another instance of dokey was already running. Sending opening request...");
            IPCManager.INSTANCE.sendCommand(IPC_OPEN_COMMAND, null);
            System.exit(5);
        }

        // Setup error/output redirection to file
        File logFile = new File(storageManager.getStorageDir(), LOG_FILENAME);
        try {
            PrintStream fileStream = new PrintStream(logFile);
            LoggerOutputStream loggerOutputStream = new LoggerOutputStream(System.out, fileStream);
            PrintStream loggerStream = new PrintStream(loggerOutputStream, true);
            System.setOut(loggerStream);
            System.setErr(loggerStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Set the logging level
        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(level);
        LOG.setUseParentHandlers( false );
        LOG.setLevel(level);
        LOG.addHandler(consoleHandler);

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
        // Initialize the server socket
        serverSocket = SocketBuilder.buildSocket();
        LOG.info("Server socket started with port: "+serverSocket.getLocalPort());

        // Setup spring
        context = new AnnotationConfigApplicationContext(AppConfig.class);

        // Get the resource bundle
        resourceBundle = context.getBean(ResourceBundle.class);

        // Set the MODENA theme
        setUserAgentStylesheet(STYLESHEET_MODENA);

        // Initialize the Key
        keyGenerator = context.getBean(KeyGenerator.class);
        keyGenerator.initialize();

        // Initialize the handshake data builder
        handshakeDataBuilder = context.getBean(HandshakeDataBuilder.class);
        handshakeDataBuilder.setServerPort(serverSocket.getLocalPort());

        // instructs the javafx system not to exit implicitly when the last application window is shut.
        Platform.setImplicitExit(false);

        // sets up the tray icon manager
        trayIconManager = context.getBean(TrayIconManager.class);
        trayIconManager.initialize();
        trayIconManager.setOnTrayIconClicked(() -> {Platform.runLater(() -> onTrayIconClicked()); return Unit.INSTANCE;});
        trayIconManager.setOnExitRequest(() -> {onExitRequest(); return Unit.INSTANCE;});

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

        // Initialize the ADB manager
        adbManager = new ADBManager(this, serverInfo, keyGenerator.getKey());

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

        // Get the application path resolver
        applicationPathResolver = context.getBean(ApplicationPathResolver.class);

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
            introStage = context.getBean(IntroStage.class);
            introStage.setOnIntroCompleted((Function0<Unit>) () -> {
                showControlPanel();
                return Unit.INSTANCE;
            });
            introStage.show();
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
                        if (introStage != null) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    // Update the intro stage
                                    introStage.setProgress(percentage);
                                }
                            });
                        }
                    }

                    @Override
                    public void onProgressUpdate(String applicationName, int current, int total) {
                        LOG.fine("Loading: " + applicationName + " " + current + "/" + total);
                        // Calculate the percentage
                        double percentage = (current / (double) total) * 0.5 + 0.5;

                        // Update the intro if visible
                        if (introStage != null) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    // Update the intro stage
                                    introStage.setProgress(percentage);
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
                                if (introStage != null) {
                                    introStage.setProgress(100.0);
                                }
                            }
                        });

                        // Set up automatic startup
                        if (introStage != null) {
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
        // Initialize command manager
        commandManager = context.getBean(CommandManager.class);
        commandManager.initialize();

        // Initialize the section manager
        sectionManager = context.getBean(SectionManager.class);
        sectionManager.initialize();

        // Inject the section manager reference, needed for some operations
        commandManager.setSectionManager(sectionManager);

        // Initialize the control panel
        controlPanelStage = context.getBean(ControlPanelStage.class);

        // Initialize the application path resolver
        applicationPathResolver.load();

        // Initialize the IPC server
        ipcServer = context.getBean(IPCServer.class);

        // Start the engine server
        startEngineServer();
    }

    /**
     * Start the engine server in another thread
     */
    private void startEngineServer() {
        // Start the application switch daemon
        applicationSwitchDaemon.start();

        // Start the ADB daemon
        adbManager.startDaemon();

        // Start the active apps daemon
        activeApplicationsDaemon.start();

        // Start the IPC server
        ipcServer.start();

        // Start the mobile server
        mobileServer = context.getBean(MobileServer.class, serverSocket, keyGenerator.getKey());
        mobileServer.setDeviceConnectionListener(this);
        mobileServer.start();

        // Update the tray icon status
        trayIconManager.setStatusText(resourceBundle.getString("not_connected"));
        trayIconManager.setLoading(false);

        // Register the global event listeners
        BroadcastManager.getInstance().registerBroadcastListener(BroadcastManager.OPEN_CONTROL_PANEL_REQUEST_EVENT, openControlPanelRequestListener);
        BroadcastManager.getInstance().registerBroadcastListener(BroadcastManager.OPEN_EDITOR_REQUEST_EVENT, openEditorRequestListener);
        BroadcastManager.getInstance().registerBroadcastListener(BroadcastManager.ENABLE_DOKEY_SEARCH_PROPERTY_CHANGED, enableDokeySearchChangedListener);

        // Register global hot keys if enabled
        if (settingsManager.getDokeySearchEnabled())
            registerHotKeys();

        if (openControlPanel) {
            showControlPanel();
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
    private static boolean checkIfDokeyIsAlreadyRunning() {
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
     * Stop all the running services
     */
    private void stopAllServices() {
        applicationSwitchDaemon.setShouldStop(true);
        mobileServer.stopServer();
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

    private void onExitRequest() {
        AlertFactory.Companion.getInstance().confirmation("Exit Dokey", "Are you sure you want to exit Dokey?" +
                "\nIf you do that, you will need to start it again manually.", () -> {
            LOG.info("Exit request");
            System.exit(0);
            return Unit.INSTANCE;
        }, () -> {return Unit.INSTANCE;}, true).show();
    }

    private void showControlPanel() {
        controlPanelStage.hide();  // TODO: this is a workaround to fix for multiple desktops, find a better solution
        controlPanelStage.show();
        // Position the control panel in the correct position
        positionResolver.positionStageOnScreen(controlPanelStage);

        controlPanelStage.animateIn();
    }

    private void hideControlPanel() {
        controlPanelStage.animateOut(null);
    }

    public void onEditorOpenRequest() {
        showControlPanel();
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

    /**
     * Called when a device connects to the server.
     *
     * @param deviceInfo the DeviceInfo object with the information about the connected device.
     */
    @Override
    public void onDeviceConnected(DeviceInfo deviceInfo) {
        LOG.info("Connected to: "+deviceInfo.getName() +" ID: "+deviceInfo.getID());

        // Wake up all daemons
        daemonMonitor.wakeUp();
        connectedClientsCount++;

        // Create the notification
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                String title = resourceBundle.getString("device_connected");
                String message = resourceBundle.getString("connected_to")+" "+deviceInfo.getName();
                NotificationFactory.showNotification(title, message);
            }
        });

        BroadcastManager.getInstance().sendBroadcast(BroadcastManager.DEVICE_CONNECTED, null);
    }

    /**
     * Called when a device disconnects from the server.
     *
     * @param deviceInfo the DeviceInfo object with the information about the connected device.
     */
    @Override
    public void onDeviceDisconnected(DeviceInfo deviceInfo) {
        LOG.info("Disconnected from: "+deviceInfo.getName() +" ID: "+deviceInfo.getID());

        // If there are no more devices connected, pause all the daemons
        connectedClientsCount--;
        if (connectedClientsCount==0) {
            daemonMonitor.pause();
        }

        // Create the notification
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                String title = resourceBundle.getString("device_disconnected");
                String message = resourceBundle.getString("disconnected_from")+" "+deviceInfo.getName();
                NotificationFactory.showNotification(title, message);
            }
        });

        BroadcastManager.getInstance().sendBroadcast(BroadcastManager.DEVICE_DISCONNECTED, null);
    }

    /**
     * Called when a connected device needs a more recent desktop version.
     * @param deviceInfo the DeviceInfo object with the information about the connected device.
     */
    @Override
    public void onDesktopVersionTooLow(DeviceInfo deviceInfo) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ButtonType cancel = new ButtonType(resourceBundle.getString("cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
                ButtonType download = new ButtonType(resourceBundle.getString("download"), ButtonBar.ButtonData.OK_DONE);

                Alert alert = new Alert(Alert.AlertType.WARNING, "", download, cancel);
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                stage.getIcons().add(new Image(MainApp.class.getResourceAsStream("/assets/icon.png")));
                stage.setAlwaysOnTop(true);
                alert.setTitle("Your Dokey Desktop version is too old :(");
                alert.setHeaderText("The version of Dokey Desktop you are running on your PC is too old!");
                alert.setContentText("Please download the new version from the Dokey Website");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == download) {
                    // Navigate to the dokey website, download section
                    appManager.openWebLink(DOWNLOAD_URL);
                }
            }
        });
    }

    /**
     * Called when a connected device has a version not supported anymore by this Desktop.
     * @param deviceInfo the DeviceInfo object with the information about the connected device.
     */
    @Override
    public void onMobileVersionTooLow(DeviceInfo deviceInfo) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ButtonType cancel = new ButtonType(resourceBundle.getString("cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
                ButtonType download = new ButtonType(resourceBundle.getString("visit_playstore"), ButtonBar.ButtonData.OK_DONE);

                Alert alert = new Alert(Alert.AlertType.WARNING, "", download, cancel);
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                stage.getIcons().add(new Image(MainApp.class.getResourceAsStream("/assets/icon.png")));
                stage.setAlwaysOnTop(true);
                alert.setTitle("Your Dokey Android version is too old :(");
                alert.setHeaderText("The version of Dokey Android you are running on your smartphone is too old!");
                alert.setContentText("Please update it from the PlayStore to connect!");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == download) {
                    // Navigate to the playstore page
                    appManager.openWebLink(PLAYSTORE_URL);
                }
            }
        });
    }

    @Override
    public void onInvalidKeyConnectionAttempt() {
        // TODO make something
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
    private BroadcastManager.BroadcastListener openControlPanelRequestListener = new BroadcastManager.BroadcastListener() {
        @Override
        public void onBroadcastReceived(Serializable param) {
            Platform.runLater(() -> {
                showControlPanel();
            });
        }
    };

    /**
     * Called when the user request to open the editor from the app.
     */
    private BroadcastManager.BroadcastListener openEditorRequestListener = new BroadcastManager.BroadcastListener() {
        @Override
        public void onBroadcastReceived(Serializable param) {
            String targetSection = (String) param;
            Platform.runLater(() -> {
                showControlPanel();

                controlPanelStage.requestSectionFocus(targetSection);
            });
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
}
