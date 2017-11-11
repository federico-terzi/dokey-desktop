import system.ApplicationManager;
import system.Window;
import system.ApplicationManagerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        ApplicationManager wm = ApplicationManagerFactory.getInstance();
        Window window = wm.getActiveWindow();
        System.out.println(wm.getActivePID());
        List<Window> list = wm.getWindowList();

        Field[] fields = java.awt.event.KeyEvent.class.getDeclaredFields();
        for (Field f : fields) {
            if (Modifier.isStatic(f.getModifiers())) {
                try {
                    Integer i = f.getInt(java.awt.event.KeyEvent.class);
                    System.out.println(f.getName() + " (\""+f.getName()+"\", \""+f.getName()+"\", " +i+"),");
                } catch (IllegalAccessException e) {
                    //e.printStackTrace();
                }

            }
        }

        System.exit(0);
    }
}