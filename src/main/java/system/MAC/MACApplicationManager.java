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
import java.util.StringTokenizer;

public class MACApplicationManager implements ApplicationManager {

    /**
     * @return the Window object of the active system.window.
     */
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

            // Get the executable path
            String executablePath = getExecutablePathFromPID(pid);

            // TODO: Application
            Window window = new MACWindow(pid, windowTitle, executablePath, null);


        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Return the executable path for the given PID. Return null if not found.
     * It uses the ps command line utility.
     *
     * @param pid process PID.
     * @return the executable path for the given PID. null if not found.
     */
    private String getExecutablePathFromPID(int pid) {
        Runtime runtime = Runtime.getRuntime();

        try {
            // Execute the process
            Process proc = runtime.exec(new String[]{"ps", "-x", "-o", "command", "-p", String.valueOf(pid)});

            // Get the output
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            // Skip the first header line
            br.readLine();

            // Get the actual output
            String executablePathLine = br.readLine();

            // An error occurred
            if (executablePathLine == null) {
                return null;
            }

            // Get the actual executable name without arguments ( remove the arguments )
            StringTokenizer st = new StringTokenizer(executablePathLine);
            StringBuilder sb = new StringBuilder();
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                if (token.startsWith("-")) {  // Argument, exit the cycle
                    break;
                }else{ // Part of the executable name
                    sb.append(token);
                    sb.append(" ");
                }
            }

            // Remove the final space and get the path
            String executablePath = sb.toString().substring(0, sb.toString().length()-2);

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
