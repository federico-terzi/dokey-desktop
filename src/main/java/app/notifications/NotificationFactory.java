package app.notifications;

import app.notifications.NotificationStage;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Screen;
import system.ResourceUtils;
import utils.OSValidator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

/**
 * Used to create notifications
 */
public class NotificationFactory {
    private static final double TARGET_OPACITY = 0.85;

    private final static Logger LOG = Logger.getGlobal();

    public static void showNotification(String title, String message) {
        if (OSValidator.isWindows()) {
            showNotificationWindows(title, message);
        }else if (OSValidator.isMac()) {
            showNotificationMac(title, message);
        }
    }

    private static void showNotificationMac(String title, String message) {
        String notifierPath = ResourceUtils.getResource("/mac/DokeyNotifier.app/Contents/MacOS/applet").getAbsolutePath();
        Runtime runtime = Runtime.getRuntime();

        try {
            // Execute the process
            Process proc = runtime.exec(new String[]{notifierPath}, new String[]{"DOKEY_NOTIFY_TITLE="+title, "DOKEY_NOTIFY_MSG="+message});

        } catch (Exception e) {
            LOG.warning("NOTIFIER_ERROR: "+e.toString());
        }
    }

    private static void showNotificationWindows(String title, String message) {
        NotificationStage notificationStage = null;
        try {
            // Create the stage
            notificationStage = new NotificationStage(title, message);
            notificationStage.setOpacity(0);
            notificationStage.show();

            // Calculate the coordinates
            javafx.geometry.Rectangle2D screen = Screen.getPrimary().getVisualBounds();

            double targetX = screen.getWidth() - notificationStage.getWidth() - 10;
            double targetY = screen.getHeight() - notificationStage.getHeight() - 10;
            double initialX = screen.getWidth() - notificationStage.getWidth() - 10;
            double initialY = screen.getHeight() + notificationStage.getHeight();

            // Setup the initial position
            notificationStage.setX(initialX);
            notificationStage.setY(initialY);

            // Create the transition
            SequentialTransition transition = new SequentialTransition(new PositionTransition(notificationStage, initialX, initialY, 0, targetX, targetY, TARGET_OPACITY),
                                            new DelayTransition(2), new PositionTransition(notificationStage, targetX, targetY, TARGET_OPACITY, initialX, initialY, 0));

            NotificationStage finalNotificationStage = notificationStage;
            transition.setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    finalNotificationStage.close();
                }
            });

            transition.play();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
