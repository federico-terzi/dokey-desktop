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
import system.ApplicationManagerFactory;
import system.model.ApplicationManager;
import system.model.Window;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class MainApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        setUserAgentStylesheet(STYLESHEET_MODENA);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/layouts/initialization.fxml"));
        Parent root = fxmlLoader.load();
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 400, 275));
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();

        InitializationController controller = (InitializationController) fxmlLoader.getController();

        controller.setAppNameLabel("yolo");

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
                    }
                });
                return null;
            }
        };

        new Thread(task).start();
    }
}
