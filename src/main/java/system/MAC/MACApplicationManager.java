package system.MAC;

import org.apache.commons.lang3.StringUtils;
import system.MS.MSApplication;
import system.model.Application;
import system.model.ApplicationManager;
import system.model.Window;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.image.BufferedImage;
import java.io.*;
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

            // Add the application
            addApplicationFromExecutablePath(appPath, applicationName);

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

    /**
     * Parse and analyze the application from the app folder. Then add it to the applicationMap.
     * @param appPath path to the app folder
     * @param applicationName name of the app
     * @return the Application object.
     */
    private Application addApplicationFromExecutablePath(String appPath, String applicationName) {
        // Make sure the target is an app
        if (appPath.toLowerCase().endsWith(".app")) {
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
     * @param appPath the app folder
     * @return the icon File
     */
    private File generateIconFile(String appPath) {
        // Obtain the application ID
        String appID = Application.Companion.getIDForExecutablePath(appPath);

        // Get the icon file
        return new File(getIconCacheDir(), appID + ".png");
    }

    /**
     * Obtain the icon associated with the given application.
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
     * @param appPath the app folder.
     * @return the icon image file. Return null if an error occurred.
     */
    private File extractIcon(String appPath) {  // TODO: improve extracting icon logic by looking at the info.plist file
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

        // Get the first .icns file in the resources directory
        File internalIconFile = null;
        File[] includedFiles = resourcesDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".icns");
            }
        });
        // No icons found
        if (includedFiles.length == 0) {
            return null;
        }
        // Set the internal icon file
        internalIconFile = includedFiles[0];

        // CONVERSION PROCESS

        Runtime runtime = Runtime.getRuntime();

        try {
            // Convert the icon to png and move to the image cache folder
            Process proc = runtime.exec(new String[]{"sips", "-s", "format", "png", "-Z", "256",
                    internalIconFile.getAbsolutePath(), "--out", iconFile.getAbsolutePath()});

            // If an error occurred, return null
            if (proc.getErrorStream().available()>0) {
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return iconFile;
    }

    @Override
    public List<Application> getApplicationList() {
        return null;
    }

    /**
     * Create and retrieve the cache directory.
     * @return the Cache directory used to save files.
     */
    @Override
    public File getCacheDir() {
        // Get the user home directory
        File homeDir = new File(System.getProperty("user.home"));

        // Get the cache directory
        File cacheDir = new File(homeDir, ApplicationManager.CACHE_DIRECTORY_NAME);

        // If it doesn't exists, create it
        if (!cacheDir.isDirectory()) {
            cacheDir.mkdir();
        }

        return cacheDir;
    }

    /**
     * Create and retrieve the image cache directory.
     * @return the Image Cache directory used to save images.
     */
    @Override
    public File getIconCacheDir() {
        File cacheDir = getCacheDir();

        // Get the icon cache directory
        File iconCacheDir = new File(cacheDir, ApplicationManager.ICON_CACHE_DIRECTORY_NAME);

        // If it doesn't exists, create it
        if (!iconCacheDir.isDirectory()) {
            iconCacheDir.mkdir();
        }

        return iconCacheDir;
    }
}
