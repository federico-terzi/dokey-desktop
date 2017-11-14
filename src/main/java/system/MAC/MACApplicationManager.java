package system.MAC;

import system.model.Application;
import system.model.ApplicationManager;
import system.model.Window;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class MACApplicationManager implements ApplicationManager {

    // This map will hold the applications, associated with their executable path
    private Map<String, Application> applicationMap = new HashMap<>();

    /**
     * @return the Window object of the active system.window.
     */
    @Override
    public Window getActiveWindow() {
        String scriptPath = getClass().getResource("/applescripts/getActiveWindow.scpt").getPath();
        Runtime runtime = Runtime.getRuntime();

        try {
            // Execute the process
            Process proc = runtime.exec(new String[] {"osascript", scriptPath});

            // Get the output
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

            // Read the fields
            String appName = br.readLine();
            int pid = Integer.parseInt(br.readLine());
            String windowTitle = br.readLine();

            // Get the executable path
            String executablePath = getExecutablePathFromPID(pid);

            // TODO: Application
            Window window = new MACWindow(pid, windowTitle, executablePath, null);
            return window;

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
            String executablePath = sb.toString().substring(0, sb.toString().length()-1);
            return executablePath;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @return PID of the current active system.window. -1 is returned in case of errors.
     */
    @Override
    public int getActivePID() {
        String scriptPath = getClass().getResource("/applescripts/getActivePID.scpt").getPath();
        Runtime runtime = Runtime.getRuntime();

        try {
            // Execute the process
            Process proc = runtime.exec(new String[]{"osascript", scriptPath});

            // Get the output
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

            // Get the PID
            try {
                int pid = Integer.parseInt(br.readLine());
                return pid;
            }catch(Exception e) {
                e.printStackTrace();
                return -1;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public List<Window> getWindowList() {
        return null;
    }

    /**
     * Load the Application(s) installed in the system.
     * Each time it is called, the list is refreshed.
     * A listener can be specified to monitor the status of the process.
     * This function checks in the Applications folder.
     */
    @Override
    public void loadApplications(OnLoadApplicationsListener listener) {
        // Initialize the application maps
        applicationMap = new HashMap<>();

        // Create a list that will hold all the files
        List<File> fileList = new ArrayList<>();

        Runtime runtime = Runtime.getRuntime();

        try {
            // Get the list of apps
            Process proc = runtime.exec(new String[]{"find", "/Applications", "-name", "*.app", "-maxdepth", "4"});

            // Get the output
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            // Get the applications
            String line = null;
            while((line = br.readLine()) != null) {
                File currentAppDir = new File(line);
                fileList.add(currentAppDir);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Current application in the list
        int current = 0;

        // Cycle through all entries
        for (File app : fileList) {
            String appPath = app.getAbsolutePath();
            // Get the application name by removing the ".app" suffix
            String applicationName = app.getName().substring(0, app.getName().length()-4);

//            // Make sure the target is an exe file
//            if (appPath != null) {
//                // Add the application
//                addApplicationFromExecutablePath(appPath, applicationName);
//            }

            // Update the listener and increase the counter
            if (listener != null) {
                listener.onProgressUpdate(applicationName, current, fileList.size());
            }
            current++;
        }

        // Signal the end of the process
        if (listener != null) {
            listener.onApplicationsLoaded();
        }
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
