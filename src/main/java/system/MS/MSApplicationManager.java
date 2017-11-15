package system.MS;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.IntByReference;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import system.model.Application;
import system.model.ApplicationManager;
import system.model.Window;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.List;

public class MSApplicationManager implements ApplicationManager {
    private static final int MAX_TITLE_LENGTH = 1024;

    // This map will hold the applications, associated with their executable path
    private Map<String, Application> applicationMap = new HashMap<>();

    private boolean isPowerShellEnabled;

    public MSApplicationManager() {
        // Check if powershell is enabled in this machine
        isPowerShellEnabled = checkPowerShellEnabled();
    }

    /**
     * Focus an application if already open or start it if not.
     * @param executablePath path to the application.
     * @return true if succeeded, false otherwise.
     */
    @Override
    public boolean openApplication(String executablePath) {
        return false;
    }

    /**
     * @return the Window object of the active system.window.
     */
    @Override
    public Window getActiveWindow() {
        // Get the system.window title
        char[] buffer = new char[MAX_TITLE_LENGTH * 2];
        HWND hwnd = User32.INSTANCE.GetForegroundWindow();
        User32.INSTANCE.GetWindowText(hwnd, buffer, MAX_TITLE_LENGTH);
        String titleText = Native.toString(buffer);

        // Get the PID
        IntByReference PID = new IntByReference();
        User32.INSTANCE.GetWindowThreadProcessId(hwnd, PID);

        // Get the executable path
        String executablePath = getExecutablePathFromPID(PID.getValue());

        // Get the application
        Application application = applicationMap.get(executablePath);

        Window window = new MSWindow(titleText, application, hwnd);
        return window;
    }

    /**
     * @return PID of the current active system.window. -1 is returned in case of errors.
     */
    @Override
    public int getActivePID() {
        HWND hwnd = User32.INSTANCE.GetForegroundWindow();

        // Get the PID
        IntByReference PID = new IntByReference();
        User32.INSTANCE.GetWindowThreadProcessId(hwnd, PID);

        return PID.getValue();
    }

    /**
     * Return the executable path for the given PID. Return null if not found.
     * It uses the WMIC command line utility.
     *
     * @param pid process PID.
     * @return the executable path for the given PID. null if not found.
     */
    private String getExecutablePathFromPID(int pid) {
        String cmd = "wmic process where ProcessID=" + pid + " get ProcessID,ExecutablePath /FORMAT:csv";
        Runtime runtime = Runtime.getRuntime();

        try {
            // Execute the process
            Process proc = runtime.exec(cmd);

            // Get the output
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = null;

            // Read each line
            while ((line = br.readLine()) != null) {
                String[] subPieces = line.split(",");

                // Make sure the line is not empty and ends with a number ( process PID )
                if (!line.trim().isEmpty() && isNumeric(subPieces[subPieces.length - 1])) {
                    StringTokenizer st = new StringTokenizer(line.trim(), ",");
                    st.nextToken(); // Discard the Node name
                    String executablePath = st.nextToken();
                    return executablePath;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Return the executable path map for the processes currently running.
     * The map has this configuration:
     * Map<PID, ExecutablePath>
     * <p>
     * It uses the WMIC command line utility.
     *
     * @return the executable path map for the processes currently running.
     */
    private Map<Integer, String> getExecutablePathsMap() {
        String cmd = "wmic process get ProcessID,ExecutablePath /FORMAT:csv";
        Runtime runtime = Runtime.getRuntime();

        Map<Integer, String> outputMap = new HashMap<>();

        try {
            // Execute the process
            Process proc = runtime.exec(cmd);

            // Get the output
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = null;

            // Read each line
            while ((line = br.readLine()) != null) {
                String[] tokens = line.trim().split(",");

                // Make sure the line is not empty and ends with a number ( process PID ).
                // And also the path is not empty.
                if (!line.trim().isEmpty() && isNumeric(tokens[tokens.length - 1]) &&
                        !tokens[1].isEmpty()) {
                    String executablePath = tokens[1];
                    int processPID = Integer.parseInt(tokens[2]);

                    // Add to the map
                    outputMap.put(processPID, executablePath);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputMap;
    }

    /**
     * @return true if given string is numeric.
     */
    private boolean isNumeric(String number) {
        try {
            Integer.parseInt(number);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Return the list of Windows currently active.
     */
    @Override
    public List<Window> getWindowList() {
        final List<Window> windowList = new ArrayList<>();

        // Get all the current processes
        Map<Integer, String> executablesMap = getExecutablePathsMap();

        User32.INSTANCE.EnumWindows(new WinUser.WNDENUMPROC() {
            int count = 0;

            public boolean callback(HWND hwnd, Pointer arg1) {
                char[] windowText = new char[512];
                User32.INSTANCE.GetWindowText(hwnd, windowText, 512);
                String titleText = Native.toString(windowText);

                // Skip the ones that are empty or default.
                if (titleText.isEmpty() || titleText.equals("Default IME") || titleText.equals("MSCTFIME UI")) {
                    return true;
                }

                // Make sure the system.window is visible, skip if not
                boolean isWindowVisible = User32.INSTANCE.IsWindowVisible(hwnd);
                if (!isWindowVisible) {
                    return true;
                }

                // Get the PID
                IntByReference PID = new IntByReference();
                User32.INSTANCE.GetWindowThreadProcessId(hwnd, PID);

                // Get the executable path
                String executablePath = executablesMap.get(PID.getValue());

                // Get the application
                Application application = applicationMap.get(executablePath);

                // If application is not present in the list, load it dynamically
                if (application == null) {
                    application = addApplicationFromExecutablePath(executablePath, null);
                }

                Window window = new MSWindow(titleText, application, hwnd);
                windowList.add(window);

                return true;
            }
        }, null);

        return windowList;
    }

    /**
     * Load the Application(s) installed in the system.
     * Each time it is called, the list is refreshed.
     * A listener can be specified to monitor the status of the process.
     * This function checks in the Windows Start Menu and analyzes the entries.
     */
    @Override
    public void loadApplications(OnLoadApplicationsListener listener) {
        // Get the user start menu folder from the registry
        String userStartDir = Advapi32Util.registryGetStringValue(
                WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders", "Start Menu");

        // Get the system start menu folder from the registry
        String systemStartDir = Advapi32Util.registryGetStringValue(
                WinReg.HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders", "Common Start Menu");

        // Define the search parameters
        String[] pathsToScan = new String[]{userStartDir, systemStartDir};
        String[] extensionsToMatch = new String[]{"lnk"};

        // Initialize the application maps
        applicationMap = new HashMap<>();

        // Create a list that will hold all the files
        List<File> fileList = new ArrayList<>();

        // Cycle through all start menus
        for (String startPath : pathsToScan) {
            // Get the files contained in the folder recoursively
            Collection<File> files = FileUtils.listFiles(new File(startPath), extensionsToMatch, true);
            // Add all the files to the collection
            fileList.addAll(files);
        }

        // Current application in the list
        int current = 0;

        // Cycle through all entries
        for (File file : fileList) {
            String executablePath = getLnkExecutablePath(file.getAbsolutePath());
            String applicationName = file.getName().replace(".lnk", "");

            // Make sure the target is an exe file
            if (executablePath != null) {
                // Add the application
                addApplicationFromExecutablePath(executablePath, applicationName);
            }

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
     * Return a list of Application(s) installed in the system.
     * Must be called after "loadApplications()".
     *
     * @return the list of Application installed in the system.
     */
    @Override
    public List<Application> getApplicationList() {
        return new ArrayList<>(this.applicationMap.values());
    }

    public String getLnkExecutablePath(String lnkFilePath) {
        Runtime runtime = Runtime.getRuntime();
        String scriptPath = getClass().getResource("/vbscripts/readLnk.vbs").getPath();

        // Remove the starting trailing slash
        if (scriptPath.startsWith("/")) {
            scriptPath = scriptPath.substring(1);
        }

        try {
            // Execute the process
            Process proc = runtime.exec(new String[]{"cscript", "/nologo", scriptPath, lnkFilePath});

            // Get the output
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = null;

            // Read the first line
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && line.toLowerCase().endsWith(".exe")) {
                    return line;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Application addApplicationFromExecutablePath(String executablePath, String applicationName) {
        // Make sure the target is an exe file
        if (executablePath.toLowerCase().endsWith(".exe")) {
            // Generate the application name if null or if
            // executablePath is already present, to mitigate ambiguities of the program name,
            if (applicationMap.containsKey(executablePath) || applicationName == null) {
                // the executable filename becomes the Application name ( without .exe )
                File appExe = new File(executablePath);
                // Create the new app name extracting the filename, removing the extension
                // and capitalizing the first letter
                applicationName = StringUtils.capitalize(appExe.getName().toLowerCase().replace(".exe", ""));
            }

            // Get the app icon
            String iconPath = getIconPath(executablePath);

            // Create the application
            Application application = new MSApplication(applicationName, executablePath, iconPath);

            // Add it to the map
            applicationMap.put(executablePath, application);

            return application;
        }
        return null;
    }

    /**
     * Check if powershell is enabled in this machine.
     * @return true if powershell is enabled, else otherwise.
     */
    private static boolean checkPowerShellEnabled() {
        Runtime runtime = Runtime.getRuntime();

        try {
            // Execute powershell
            Process proc = runtime.exec(new String[]{"powershell", "echo yes"});

            // If there was an error, return false
            if (proc.getErrorStream().available()>0) {
                return false;
            }

            // Make sure the line is correct
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = br.readLine();
            if (line != null && line.equals("yes")) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Generate the icon file for the given executable
     * @param executablePath the executable with the icon
     * @return the icon File
     */
    private File generateIconFile(String executablePath) {
        // Obtain the application ID
        String appID = Application.Companion.getHashIDForExecutablePath(executablePath);

        // Get the icon file
        return new File(getIconCacheDir(), appID + ".png");
    }

    /**
     * Obtain the icon associated with the given executable.
     * @param executablePath path to the executable.
     * @return the icon associated with the given executable.
     */
    private String getIconPath(String executablePath) {
        // Get the icon file
        File iconFile = generateIconFile(executablePath);

        // If the file doesn't exist, it must be generated
        if (!iconFile.isFile()) {
            iconFile = extractIcon(executablePath);
        }

        // Return the icon file path
        return iconFile.getAbsolutePath();
    }

    /**
     * Extract the icon from the given executable.
     * @param executablePath the executable with the icon.
     * @return the icon image file. Return null if an error occurred.
     */
    private File extractIcon(String executablePath) {
        // Get the icon file
        File iconFile = generateIconFile(executablePath);

        // The icon can be obtained in two ways, but using powershell the
        // resulting image is better ( higher resolution ).
        if (isPowerShellEnabled) {  // Best method
            // Extract the icon
            boolean ris = extractIconUsingPowershellScript(executablePath, iconFile.getAbsolutePath());

            // An error occurred, return null
            if (!ris) {
                return null;
            }else{
                // Return the icon file ( generate again the file to avoid problems )
                return generateIconFile(executablePath);
            }
        }else{  // Not so good, but should do the trick
            Icon icon = null;
            icon = FileSystemView.getFileSystemView().getSystemIcon(new File(executablePath));
            BufferedImage iconImage = (BufferedImage) iconToImage(icon);
            try {
                ImageIO.write(iconImage, "png", iconFile);
            } catch (IOException e) {  // ERROR
                return null;
            }
        }

        return iconFile;
    }

    /**
     * Extract the icon from the executable using the powershell method
     * @param executablePath path of the executable
     * @param destinationFile path of the destination image file
     * @return true if succeeded, false otherwise.
     */
    private boolean extractIconUsingPowershellScript(String executablePath, String destinationFile) {
        Runtime runtime = Runtime.getRuntime();

        try {
            // Execute powershell
            Process proc = runtime.exec(new String[]{"powershell",
                    "[System.Reflection.Assembly]::LoadWithPartialName('System.Drawing')  | Out-Null ; [System.Drawing.Icon]::ExtractAssociatedIcon('"+executablePath+"').ToBitmap().Save('"+destinationFile+"'); echo 'ok'"});

            // If there was an error, return false
            if (proc.getErrorStream().available()>0) {
                return false;
            }

            // Make sure everything was ok
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = br.readLine();
            if (line != null && line.equals("ok")) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
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

    /**
     * Converts an icon to a buffered image
     * @param icon the icon to convert
     * @return the BufferedImage with the icon
     */
    private static Image iconToImage(Icon icon) {
        if (icon instanceof ImageIcon) {
            return ((ImageIcon)icon).getImage();
        }
        else {
            int w = icon.getIconWidth();
            int h = icon.getIconHeight();
            GraphicsEnvironment ge =
                    GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            BufferedImage image = gc.createCompatibleImage(w, h);
            Graphics2D g = image.createGraphics();
            icon.paintIcon(null, g, 0, 0);
            g.dispose();
            return image;
        }
    }
}
