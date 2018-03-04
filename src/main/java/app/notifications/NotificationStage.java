package app.notifications;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import system.ResourceUtils;

import java.io.IOException;

public class NotificationStage extends Stage {
    private NotificationController controller;

    public NotificationStage(String title, String message) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ResourceUtils.getResource("/layouts/notification.fxml").toURI().toURL());
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        this.setScene(scene);
        setAlwaysOnTop(true);
        this.getIcons().add(new Image(NotificationStage.class.getResourceAsStream("/assets/icon.png")));
        initStyle(StageStyle.TRANSPARENT);
        scene.setFill(Color.TRANSPARENT);

        controller = (NotificationController) fxmlLoader.getController();

        controller.titleLabel.setText(title);
        controller.messageLabel.setText(message);
    }

    public NotificationController getController() {
        return controller;
    }
}
