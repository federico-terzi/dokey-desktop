import window.Window;
import window.WindowManager;
import window.WindowManagerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        WindowManager wm = WindowManagerFactory.getInstance();
        Window window = wm.getActiveWindow();
        System.out.println(wm.getActivePID());
        List<Window> list = wm.getWindowList();

        System.exit(0);
    }
}