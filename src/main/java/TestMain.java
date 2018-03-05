import app.notifications.NotificationFactory;
import com.sun.jna.Library;
import com.sun.jna.Native;
import system.MAC.MACApplicationManager;
import system.model.Application;
import system.model.ApplicationManager;
import system.model.Window;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

public class TestMain {
    private interface CLibrary extends Library {
        CLibrary INSTANCE = (CLibrary) Native.loadLibrary("c", CLibrary.class);
        int getpid ();
    }

    public static void main(String[] args) {
        int pid = CLibrary.INSTANCE.getpid();

        NotificationFactory.showNotification("Dokey: Connected", "Connected to bo");

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }
}