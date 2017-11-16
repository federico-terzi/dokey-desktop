import system.MAC.MACApplicationManager;
import system.model.Application;
import system.model.ApplicationManager;
import system.model.Window;
import system.ApplicationManagerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

public class TestMain {
    public static void main(String[] args) {
        ApplicationManager wm = ApplicationManagerFactory.getInstance();

        //wm.openApplication("C:\\Program Files (x86)\\Dia\\bin\\diaw.exe");

        int pid = wm.getActivePID();
        Window window = wm.getActiveWindow();
        //window.focusWindow();
        System.out.println("Loading applications...");
        // powershell "[System.Reflection.Assembly]::LoadWithPartialName('System.Drawing')  | Out-Null ; [System.Drawing.Icon]::ExtractAssociatedIcon('C:\Users\Federico\Documents\Skype.exe').ToBitmap().Save('C:\Users\Federico\s.png')"
        wm.loadApplications(new ApplicationManager.OnLoadApplicationsListener() {
            @Override
            public void onProgressUpdate(String applicationName, int current, int total) {
                System.out.println("Loading: "+applicationName+" "+current+"/"+total);
            }

            @Override
            public void onApplicationsLoaded() {
                System.out.println("loaded!");
            }
        });
//
//        List<Application> apps = wm.getApplicationList();

//
//        System.out.println(wm.getActivePID());
        List<Window> list = wm.getWindowList();
//
//        Field[] fields = java.awt.event.KeyEvent.class.getDeclaredFields();
//        for (Field f : fields) {
//            if (Modifier.isStatic(f.getModifiers())) {
//                try {
//                    Integer i = f.getInt(java.awt.event.KeyEvent.class);
//                    System.out.println(f.getName() + " (\""+f.getName()+"\", \""+f.getName()+"\", " +i+"),");
//                } catch (IllegalAccessException e) {
//                    //e.printStackTrace();
//                }
//
//            }
//        }



        System.exit(0);
    }
}