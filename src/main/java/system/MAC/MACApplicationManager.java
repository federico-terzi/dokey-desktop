package system.MAC;

import system.model.Application;
import system.model.ApplicationManager;
import system.model.Window;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MACApplicationManager implements ApplicationManager {

    @Override
    public Window getActiveWindow() {
        String scriptPath = getClass().getResource("/applescripts/getActiveWindow.scpt").getPath();
        Runtime runtime = Runtime.getRuntime();

        Map<Integer, String> outputMap = new HashMap<>();

        try {
            // Execute the process
            Process proc = runtime.exec(new String[] {"osascript", scriptPath});

            // Get the output
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            // Read the fields
            String appName = br.readLine();
            int pid = Integer.parseInt(br.readLine());
            String windowTitle = br.readLine();


        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getActivePID() {
        return 0;
    }

    @Override
    public List<Window> getWindowList() {
        return null;
    }

    @Override
    public void loadApplications(OnLoadApplicationsListener listener) {

    }

    @Override
    public List<Application> getApplicationList() {
        return null;
    }

    @Override
    public File getCacheDir() {
        return null;
    }

    @Override
    public File getIconCacheDir() {
        return null;
    }
}
