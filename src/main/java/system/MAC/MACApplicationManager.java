package system.MAC;

import system.CacheManager;
import system.model.Application;
import system.model.ApplicationManager;
import system.model.Window;

import java.io.*;
import java.util.*;

public class MACApplicationManager implements ApplicationManager {

    // This map will hold the applications, associated with their executable path
    private Map<String, Application> applicationMap = new HashMap<>();

    /**
     * Focus an application if already open or start it if not.
     *
     * @param executablePath path to the application.
     * @return true if succeeded, false otherwise.
     */
    @Override
    public boolean openApplication(String executablePath) {
        // Get windows to find out if application is already open
        List<Window> openWindows = getWindowList();

        // Cycle through windows to find if the app is already open
        boolean isApplicationOpen = false;
        Window firstOpenWindow = null;

        for (Window window : openWindows) {
            if (window.getApplication().getExecutablePath().equals(executablePath)) {
                isApplicationOpen = true;
                firstOpenWindow = window;
                break;
            }
        }

        if (isApplicationOpen) {  // App is open, focus the first window
            firstOpenWindow.focusWindow();
        }else{     // App is not open, start it.
            Application application = applicationMap.get(executablePath);
            if (application == null) {
                application = addApplicationFromAppPath(executablePath);
            }

            // Make sure the app is valid before opening it
            if (application != null) {
                return application.open();
            }else{
                return false;
            }
        }

        return true;
    }

    /**
     * @return the Window object of the active system.window.
     */
    @Override
    public Window getActiveWindow() {
        String scriptPath = getClass().getResource("/applescripts/getActiveWindow.scpt").getPath();
        Runtime runtime = Runtime.getRuntime();

        try {
            // Execute the process
            Process proc = runtime.exec(new String[]{"osascript", scriptPath});

            // Get the output
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

            // Read the fields
            String appName = br.readLine();
            int pid = Integer.parseInt(br.readLine());
            String windowTitle = br.readLine();

            // Get the executable path
            String executablePath = getExecutablePathFromPID(pid);

            // Get the app folder path
            String appPath = getAppPathFromExecutablePath(executablePath);

            // Get the application
            Application application = null;
            if (appPath != null) {
                application = addApplicationFromAppPath(appPath);
            }

            Window window = new MACWindow(windowTitle, application, appName);
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
                } else { // Part of the executable name
                    sb.append(token);
                    sb.append(" ");
                }
            }

            // Remove the final space and get the path
            String executablePath = sb.toString().substring(0, sb.toString().length() - 1);
            return executablePath;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Parse the app application folder from an executable path
     *
     * @param executablePath the executable path
     * @return
     */
    private String getAppPathFromExecutablePath(String executablePath) {
        String[] tokens = executablePath.split("/");

        List<String> pathTokens = new ArrayList<>();

        boolean forceFinish = false;

        for (int i = (tokens.length - 1); i >= 0; i--) {
            if (tokens[i].endsWith(".app") || forceFinish) {
                pathTokens.add(0, tokens[i]);
                forceFinish = true;
            }
        }

        if (pathTokens.size() == 0) {
            return null;
        } else {
            return String.join("/", pathTokens);
        }
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
            } catch (Exception e) {
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
        String scriptPath = getClass().getResource("/applescripts/getWindowList.scpt").getPath();
        Runtime runtime = Runtime.getRuntime();

        List<Window> windowList = new ArrayList<>();

        try {
            // Execute the process
            Process proc = runtime.exec(new String[]{"osascript", scriptPath});

            // Get the output
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

            // Read the fields
            String appName;
            while ((appName = br.readLine()) != null) {
                int pid = Integer.parseInt(br.readLine());

                // Get the executable path
                String executablePath = getExecutablePathFromPID(pid);

                // Get the app folder path
                String appPath = getAppPathFromExecutablePath(executablePath);

                Application application = null;

                if (appPath != null) {
                    // Get the application
                    application = applicationMap.get(appPath);

                    // If not already present, load it
                    if (application == null) {
                        application = addApplicationFromAppPath(appPath);
                    }
                }

                String windowTitle;
                while ((windowTitle = br.readLine()) != null && !windowTitle.trim().isEmpty()) {
                    Window window = new MACWindow(windowTitle, application, appName);
                    windowList.add(window);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return windowList;
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
            while ((line = br.readLine()) != null) {
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

            // Add the application
            Application application = addApplicationFromAppPath(appPath);

            // Update the listener and increase the counter
            if (listener != null) {
                listener.onProgressUpdate(application.getName(), application.getIconPath(), current, fileList.size());
            }
            current++;
        }

        // Signal the end of the process
        if (listener != null) {
            listener.onApplicationsLoaded();
        }
    }

    /**
     * Parse and analyze the application from the app folder. Then add it to the applicationMap.
     *
     * @param appPath path to the app folder
     * @return the Application object.
     */
    private Application addApplicationFromAppPath(String appPath) {
        // Make sure the target is an app
        if (appPath.toLowerCase().endsWith(".app")) {
            // Get the app folder
            File app = new File(appPath);

            // Get the application name by removing the ".app" suffix
            String applicationName = app.getName().substring(0, app.getName().length() - 4);

            // Get the app icon
            String iconPath = getIconPath(appPath);

            // Create the application
            Application application = new MACApplication(applicationName, appPath, iconPath);

            // Add it to the map
            applicationMap.put(appPath, application);

            return application;
        }
        return null;
    }

    /**
     * Generate the icon file for the given app
     *
     * @param appPath the app folder
     * @return the icon File
     */
    private File generateIconFile(String appPath) {
        // Obtain the application ID
        String appID = Application.Companion.getHashIDForExecutablePath(appPath);

        // Get the icon file
        return new File(CacheManager.getInstance().getIconCacheDir(), appID + ".png");
    }

    /**
     * Obtain the icon associated with the given application.
     *
     * @param appPath path to the app folder.
     * @return the icon associated with the given app.
     */
    private String getIconPath(String appPath) {
        // Get the icon file
        File iconFile = generateIconFile(appPath);

        // If the file doesn't exist, it must be generated
        if (!iconFile.isFile()) {
            iconFile = extractIcon(appPath);

            // App doesn't have an image, return null
            if (iconFile == null) {
                return null;
            }
        }

        // Return the icon file path
        return iconFile.getAbsolutePath();
    }

    /**
     * Extract the icon from the given app.
     *
     * @param appPath the app folder.
     * @return the icon image file. Return null if an error occurred.
     */
    private File extractIcon(String appPath) {
        // Get the icon file
        File iconFile = generateIconFile(appPath);

        // Open the app directory
        File appDir = new File(appPath);

        // Open the app resources directory
        File resourcesDir = new File(appDir, "/Contents/Resources");
        // Make sure the resource dir is valid
        if (!resourcesDir.isDirectory()) {
            return null;
        }

        // Get the icon file
        File internalIconFile = getInternalIconFileFromPlistFile(appDir);

        // If the internal icon is valid, convert it to png
        if (internalIconFile != null && internalIconFile.isFile()) {
            // CONVERSION PROCESS

            Runtime runtime = Runtime.getRuntime();

            try {
                // Convert the icon to png and move to the image cache folder
                Process proc = runtime.exec(new String[]{"sips", "-s", "format", "png", "-Z", "256",
                        internalIconFile.getAbsolutePath(), "--out", iconFile.getAbsolutePath()});

                // If an error occurred, return null
                if (proc.getErrorStream().available() > 0) {
                    return null;
                }

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }

        return iconFile;
    }

    /**
     * Return the icon filename from the app info.plist file
     *
     * @param appDir the application folder
     * @return the icon file
     */
    public File getInternalIconFileFromPlistFile(File appDir) {
        File infoPlistFile = new File(appDir, "/Contents/info.plist");

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(infoPlistFile)));

            String line = null;

            while ((line = br.readLine()) != null) {
                String trimmedLine = line.trim();
                // Check if line contains one of the icon attributes
                if (line.contains("CFBundleIconFile") || line.contains("CFBundleIconName") ||
                        line.contains("CFBundleIconFiles") || line.contains("CFBundleIcons")) {
                    String keyLine = null;
                    while ((keyLine = br.readLine()) != null) {
                        keyLine = keyLine.trim();
                        if (!keyLine.isEmpty()) {
                            // Get the appicon name
                            String appIconName = keyLine.split(">")[1].split("</")[0];

                            // Append the .icns if not present
                            if (!appIconName.endsWith(".icns")) {
                                appIconName += ".icns";
                            }

                            // Get the icon file
                            File iconFile = new File(appDir, "/Contents/Resources/" + appIconName);

                            // Make sure it is a valid path
                            if (iconFile.isFile()) {
                                return iconFile;
                            } else {
                                return null;
                            }
                        }
                    }
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Return a list of Application(s) installed in the system.
     * Must be called after "loadApplications()".
     *
     * @return the list of Application installed in the system.
     */
    @Override
    public List<Application> getApplicationList() {
        return new ArrayList<>(this.applicationMap.values());
    }
}
