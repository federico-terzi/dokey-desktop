package app;

import app.UIControllers.InitializationController;
import engine.EngineServer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import sun.applet.Main;
import system.ApplicationManagerFactory;
import system.model.ApplicationManager;
import system.model.Window;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainApp extends Application {
    // a timer allowing the tray icon to provide a periodic notification event.
    private Timer notificationTimer = new Timer();

    private TrayIconManager trayIconManager = new TrayIconManager();
    private ApplicationManager appManager;

    private Stage primaryStage;
    private InitializationStage initializationStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;

        // Set the MODENA theme
        setUserAgentStylesheet(STYLESHEET_MODENA);

        // instructs the javafx system not to exit implicitly when the last application window is shut.
        Platform.setImplicitExit(false);

        // sets up the tray icon
        javax.swing.SwingUtilities.invokeLater(trayIconManager::initialize);

        // Initialize the application manager
        appManager = ApplicationManagerFactory.getInstance();

        // load the applications
        loadApplications();
    }

    /**
     * Start the application loading process
     */
    private void loadApplications() throws IOException {
        // Create and show the initialization stage
        initializationStage = new InitializationStage();
        initializationStage.show();

        // Task for loading the applications
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                appManager.loadApplications(new ApplicationManager.OnLoadApplicationsListener() {
                    @Override
                    public void onProgressUpdate(String applicationName, String iconPath, int current, int total) {
                        System.out.println("Loading: "+applicationName+" "+current+"/"+total);
                        // Calculate the percentage
                        double percentage = (current / (double) total);
                        // Get the icon file
                        File iconImage = new File(iconPath);

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                // Update the initialization stage
                                initializationStage.updateAppStatus(applicationName, percentage, iconImage);
                            }
                        });
                    }

                    @Override
                    public void onApplicationsLoaded() {
                        System.out.println("loaded!");
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                MainApp.this.onApplicationsLoaded();
                            }
                        });
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
        trayIconManager.setTrayIconStatus("Starting service...");

        // Start the engine server
        startEngineServer();
    }

    /**
     * Start the engine server in another thread
     */
    private void startEngineServer() {
        EngineServer engineServer = new EngineServer(appManager);
        engineServer.start();

        // Update the tray icon status
        trayIconManager.setTrayIconStatus("Running");
        trayIconManager.setTrayIcon(TrayIconManager.TRAY_ICON_FILENAME_RUNNING);
    }
}
