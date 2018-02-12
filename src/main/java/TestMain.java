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
        //window.focusWindow();
        System.out.println("Loading applications...");
        // powershell "[System.Reflection.Assembly]::LoadWithPartialName('System.Drawing')  | Out-Null ; [System.Drawing.Icon]::ExtractAssociatedIcon('C:\Users\Federico\Documents\Skype.exe').ToBitmap().Save('C:\Users\Federico\s.png')"
        wm.loadApplications(new ApplicationManager.OnLoadApplicationsListener() {
            @Override
            public void onPreloadUpdate(String applicationName, int current, int total) {
                System.out.println("Loading: "+applicationName+" "+current+"/"+total);
            }

            @Override
            public void onProgressUpdate(String applicationName,String iconPath, int current, int total) {
                System.out.println("Loading: "+applicationName+" "+current+"/"+total);
            }

            @Override
            public void onApplicationsLoaded() {
                System.out.println("loaded!");
            }
        });

        List<Application> apps = wm.getActiveApplications();

        System.exit(0);
    }
}