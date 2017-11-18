package app;

import app.UIControllers.InitializationController;
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

    private Stage primaryStage;

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

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/layouts/initialization.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 400, 275);
        scene.getStylesheets().add(MainApp.class.getResource("/css/initialization.css").toExternalForm());
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();

        InitializationController controller = (InitializationController) fxmlLoader.getController();

        ApplicationManager wm = ApplicationManagerFactory.getInstance();

        //Task for computing the Panels:
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                wm.loadApplications(new ApplicationManager.OnLoadApplicationsListener() {
                    @Override
                    public void onProgressUpdate(String applicationName, String iconPath, int current, int total) {
                        System.out.println("Loading: "+applicationName+" "+current+"/"+total);
                        double percentage = (current / (double) total);
                        File iconImage = new File(iconPath);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                controller.setAppNameLabel(applicationName);
                                controller.setAppProgressBar(percentage);
                                controller.setAppImageFile(iconImage);
                            }
                        });
                    }

                    @Override
                    public void onApplicationsLoaded() {
                        System.out.println("loaded!");
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                primaryStage.hide();
                            }
                        });
                    }
                });
                return null;
            }
        };

        new Thread(task).start();
    }
}
