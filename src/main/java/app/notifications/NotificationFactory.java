package app.notifications;

import app.notifications.NotificationStage;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Screen;
import json.JSONObject;
import system.BroadcastManager;
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
        JSONObject body = new JSONObject();
        body.put("title", title);
        body.put("text", message);
        BroadcastManager.getInstance().sendBroadcast(BroadcastManager.WINDOWS_NOTIFICATION_REQUEST, body.toString());
    }
}
