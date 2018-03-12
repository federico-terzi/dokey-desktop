package system.MS;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import system.CacheManager;
import system.KeyboardManager;
import system.ResourceUtils;
import system.StartupManager;
import system.model.Application;
import system.model.ApplicationManager;
import system.model.Window;
import utils.IconManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

import static com.sun.jna.platform.WindowUtils.getIconSize;

public class MSApplicationManager extends ApplicationManager {
    public static final int OPEN_APPLICATION_TIMEOUT = 2000;  // Timeout of the application open requests
    public static final int OPEN_APPLICATION_CHECK_INTERVAL = 300;  // How often to check that the app is effectively open.

    private static final int MAX_TITLE_LENGTH = 1024;

    public static String START_MENU_CACHE_FILENAME = "startmenucache.txt";
    public static String APP_CACHE_FILENAME = "appcache.txt";

    // This map will hold the applications, associated with their executable path
    private Map<String, Application> applicationMap = new HashMap<>();

    // Used to focus the explorer window after a folder request.
    private Application explorerApp = null;

    private boolean isPowerShellEnabled;

    // Create the logger
    private final static Logger LOG = Logger.getGlobal();

    private StartupManager startupManager;

    public MSApplicationManager(StartupManager startupManager) {
        this.startupManager = startupManager;

        // Check if powershell is enabled in this machine
        isPowerShellEnabled = checkPowerShellEnabled();

        disableForegroundLock();
    }

    interface WUser32 extends User32 {
        WUser32 INSTANCE = (WUser32) Native.loadLibrary("User32", WUser32.class, W32APIOptions.DEFAULT_OPTIONS);
        int SystemParametersInfo(int uiAction, int uiParam, int pvParam, int fWinIni);
    }

    /**
     * Disable foreground lock that can cause problems when opening an application
     */
    private void disableForegroundLock() {
        // Change the foreground timeout
        //win32gui.SystemParametersInfo(win32con.SPI_SETFOREGROUNDLOCKTIMEOUT, 0, win32con.SPIF_SENDWININICHANGE | win32con.SPIF_UPDATEINIFILE)
        int res = WUser32.INSTANCE.SystemParametersInfo(0x2001, 0, 0, 0x01 | 0x02);
    }

    /**
     * This function is used to attach the current thread to the foreground one
     * to enable getting the focus.
     */
    public void enableFocusWorkaround() {
        // Attach the thread to the foreground one
        int foregroundThreadID = User32.INSTANCE.GetWindowThreadProcessId(User32.INSTANCE.GetForegroundWindow(), null);
        int currentThreadID = Kernel32.INSTANCE.GetCurrentThreadId();
        User32.INSTANCE.AttachThreadInput(new WinDef.DWORD(foregroundThreadID), new WinDef.DWORD(currentThreadID), true);
    }

    /**
     * Focus an application if already open or start it if not.
     *
     * @param executablePath path to the application.
     * @param forceRun if the application is not running, start it.
     * @return true if succeeded, false otherwise.
     */
    public synchronized boolean openApplication(String executablePath, boolean forceRun) {
        if (executablePath == null)
            return false;

        // Get the currently active application PID
        int activePID = getActivePID();

        // Get the time at the beginning of the open application try
        long initialTime = System.currentTimeMillis();

        // Get windows to find out if application is already open
        List<Window> openWindows = getWindowList();

        // Cycle through windows to find if the app is already open
        boolean isApplicationOpen = false;
        Window firstOpenWindow = null;

        for (Window window : openWindows) {
            if (window.getApplication() != null && window.getApplication().getExecutablePath().equals(executablePath)) {
                isApplicationOpen = true;
                firstOpenWindow = window;
                break;
            }
        }

        boolean hasBeenOpened = false;

        // Try to open the application until a timeout occurs
        while ((System.currentTimeMillis()-initialTime) < OPEN_APPLICATION_TIMEOUT && !hasBeenOpened) {
            if (isApplicationOpen) {
                // Attach to the foreground thread to gain focus rights
                enableFocusWorkaround();

                firstOpenWindow.focusWindow();
            }else{
                // Get the requested application
                Application application = applicationMap.get(executablePath);
                if (application == null) {
                    application = addApplicationFromExecutablePath(executablePath, null, null);
                }
                // Make sure the app is valid before opening it
                if (application == null) {
                    return false;
                }

                // Try to open the application
                if (forceRun) {
                    return application.open();
                }
            }

            // Get the current active application PID
            int currentlyActivePID = getActivePID();

            // If the PIDs are equal, it means that the opening didn't work
            // or the app was already open
            if (currentlyActivePID == activePID) {
                // Get the path of the currently opened application
                String focusedExecutablePath = getExecutablePathFromPID(activePID);

                // If the executable path is the one requested, it means the app is already open
                if (focusedExecutablePath != null && focusedExecutablePath.equals(executablePath)) {
                    return true;
                }

                // Try send the ALT-TAB shortcut to unlock the situation
                triggerAppSwitch();
                LOG.info("WIN LOCK DETECTED: trying with ALT-TAB...");
            }else{
                hasBeenOpened = true;
                break;
            }

            // Sleep for a bit
            try {
                Thread.sleep(OPEN_APPLICATION_CHECK_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return hasBeenOpened;
    }

    /**
     * Focus an application if already open or start it if not.
     *
     * @param executablePath path to the application.
     * @return true if succeeded, false otherwise.
     */
    @Override
    public synchronized boolean openApplication(String executablePath) {
        return openApplication(executablePath, true);
    }

    /**
     * Return the icon file associated with the specified application.
     * @param executablePath path to the application
     * @return the icon image File object.
     */
    @Override
    public File getApplicationIcon(String executablePath) {
        // Get the application
        Application application = applicationMap.get(executablePath);
        if (application == null) {
            application = addApplicationFromExecutablePath(executablePath, null, null);
        }

        // Make sure the application exists
        if (application != null) {
            File iconFile = new File(application.getIconPath());
            return iconFile;
        }else{
            return null;
        }
    }

    @Override
    public boolean openFolder(String folderPath) {
        // Make sure the folder exists
        File folder = new File(folderPath);
        if (!folder.isDirectory()) {
            return false;
        }

        Runtime runtime = Runtime.getRuntime();

        try {
            // Execute the process
            Process proc = runtime.exec(new String[]{"explorer", folderPath});
            proc.waitFor();

            // Also focus the explorer.exe application to bring it to front.
            if (explorerApp != null) {
                openApplication(explorerApp.getExecutablePath());
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean openWebLink(String url) {
        // Make sure the url is valid
        try {
            URL u = new URL(url);
            u.toURI();
        } catch (Exception e) {
            LOG.fine("URL CONVERSION ERROR: "+url + " "+e.toString());
            return false;
        }

        Runtime runtime = Runtime.getRuntime();

        try {
            // Execute the process
            // Add the ending space as a workaround because if the url has parameters, explorer interprets it wrong
            // unless there are quotes and the space force the quotes to appear.
            Process proc = runtime.exec(new String[]{"explorer", url + " "});
            proc.waitFor();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean openTerminalWithCommand(String command) {
        Runtime runtime = Runtime.getRuntime();

        try {
            // Execute the process
            Process proc = runtime.exec(new String[]{"cmd", "/c", "start", "cmd.exe", "/k", command});

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean focusDokey() {
        // Focus dokey by opening the currently active Dokey process
        return openApplication(startupManager.getCurrentExecutablePath(), false);
    }

    @Override
    public boolean isApplicationAlreadyPresent(String executablePath) {
        return applicationMap.containsKey(executablePath);
    }

    /**
     * Get the application associated with the given executable path.
     * @param executablePath the path to the application.
     * @return the Application associated with the executable path if found, null otherwise.
     */
    @Override
    public Application getApplication(String executablePath) {
        return applicationMap.get(executablePath);
    }

    /**
     * @return the Window object of the active system.window.
     */
    @Override
    public synchronized Window getActiveWindow() {
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

        // Make sure an executable path exists
        if (executablePath == null) {
            return null;
        }

        // Get the application
        Application application = applicationMap.get(executablePath);

        // If application is not present in the list, load it dynamically
        if (application == null) {
            application = addApplicationFromExecutablePath(executablePath, null, null);
        }

        Window window = new MSWindow(titleText, application, PID.getValue(), hwnd);
        return window;
    }

    /**
     * @return PID of the current active system.window. -1 is returned in case of errors.
     */
    @Override
    public synchronized int getActivePID() {
        HWND hwnd = User32.INSTANCE.GetForegroundWindow();

        // Get the PID
        IntByReference PID = new IntByReference();
        User32.INSTANCE.GetWindowThreadProcessId(hwnd, PID);

        return PID.getValue();
    }

    /**
     * @return the active Application
     */
    @Override
    public Application getActiveApplication() {
        Window activeWindow = getActiveWindow();
        if (activeWindow != null) {
            return activeWindow.getApplication();
        }else{
            return null;
        }
    }

    @Override
    public List<Application> getActiveApplications() {
        List<Application> apps = new ArrayList<>();

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
                //String executablePath = executablesMap.get(PID.getValue());
                String executablePath= getExecutablePathFromPID(PID.getValue());

                // If the executablePath is empty, skip the process
                if (executablePath == null) {
                    return true;
                }

                // Get the application
                Application application = applicationMap.get(executablePath);

                // If application is not present in the list, load it dynamically
                if (application == null) {
                    application = addApplicationFromExecutablePath(executablePath, null, null);
                }

                // If the application could not be found, return
                if (application == null) {
                    return true;
                }

                // Avoid duplicates
                if (!apps.contains(application)) {
                    apps.add(application);
                }
                return true;
            }
        }, null);

        return apps;
    }

    /**
     * Used to get the executable path for the given pid
     */
    public interface PsApi extends StdCallLibrary {

        int GetModuleFileNameExA(WinNT.HANDLE process, WinNT.HANDLE module ,
                                 byte[] name, int i);

    }

    /**
     * Return the executable path for the given PID. Return null if not found.
     * It uses a kernel call to obtain it.
     *
     * @param pid process PID.
     * @return the executable path for the given PID. null if not found.
     */
    private String getExecutablePathFromPID(int pid) {
        PsApi psapi = (PsApi) Native.loadLibrary("psapi", PsApi.class);
        byte[] pathText = new byte[1024];
        WinNT.HANDLE process = Kernel32.INSTANCE.OpenProcess(0x0400 | 0x0010, false, pid);
        psapi.GetModuleFileNameExA(process, null, pathText, 1024);
        String executablePath= Native.toString(pathText);

        // If the executablePath is empty, return null
        if (executablePath.length() == 0) {
            return null;
        }

        return executablePath;
    }

    /**
     * Return the executable path for the given PID. Return null if not found.
     * It uses the WMIC command line utility.
     *
     * @param pid process PID.
     * @return the executable path for the given PID. null if not found.
     */
    private String getExecutablePathFromPIDUsingWMIC(int pid) {
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
     * Used when windows is stucked and doesn't change window.
     */
    private void triggerAppSwitch() {
        Robot robot = null;
        try {
            robot = new Robot();
            robot.setAutoDelay(40);
            robot.setAutoWaitForIdle(true);
            robot.keyPress(KeyEvent.VK_ALT);
            robot.delay(40);
            robot.keyPress(KeyEvent.VK_TAB);
            robot.delay(40);
            robot.keyRelease(KeyEvent.VK_ALT);
            robot.delay(40);
            robot.keyRelease(KeyEvent.VK_TAB);
            robot.delay(40);
        } catch (AWTException e) {
            e.printStackTrace();
        }

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
        // Deprecated in favour of a native kernel call
        // Map<Integer, String> executablesMap = getExecutablePathsMap();

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
                //String executablePath = executablesMap.get(PID.getValue());
                String executablePath= getExecutablePathFromPID(PID.getValue());

                // If the executablePath is empty, skip the process
                if (executablePath == null) {
                    return true;
                }

                // Get the application
                Application application = applicationMap.get(executablePath);

                // If application is not present in the list, load it dynamically
                if (application == null) {
                    application = addApplicationFromExecutablePath(executablePath, null, null);
                }

                // If the application could not be found, return
                if (application == null) {
                    return true;
                }

                Window window = new MSWindow(titleText, application, PID.getValue(), hwnd);
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

        // Create a list that will hold all the link files
        List<File> linkFileList = new ArrayList<>();

        // Cycle through all start menus
        for (String startPath : pathsToScan) {
            // Get the files contained in the folder recoursively
            Collection<File> files = FileUtils.listFiles(new File(startPath), extensionsToMatch, true);
            // Add all the files to the collection
            linkFileList.addAll(files);
        }

        // Get the start menu lnk destination cache
        Map<String, String> lnkCacheMap = loadLnkDestinationCacheMap();

        // The list that will hold all the application executable paths
        List<String> executablePaths = new ArrayList<>();

        // This map will hold the association between the executable path and the app name
        Map<String, String> appNameMap = new HashMap<>();

        // Populate the list
        int current = 0; // Current application in the list

        for (File file : linkFileList) {
            try {
                // Skip uninstallers
                if (file.getName().toLowerCase().contains("uninstall")) {
                    LOG.fine("SKIP :"+file.getAbsolutePath());
                    continue;
                }

                String applicationName = file.getName().replace(".lnk", "");
                String executablePath;
                String iconPath = null;

                // Try to load the executable path from the cache
                if (lnkCacheMap.containsKey(file.getAbsolutePath())) {  // APP in cache
                    executablePath = lnkCacheMap.get(file.getAbsolutePath());

                    // Check if the requested executable path should be skipped
                    if (executablePath.equals("SKIP")) {
                        continue;
                    }
                } else {  // APP not in cache
                    // Calculate the correct values
                    executablePath = getLnkExecutablePath(file.getAbsolutePath());

                    // Make sure the executable exists
                    if (executablePath != null) {
                        // Save the destination to the cache
                        writeLnkDestinationToCache(file.getAbsolutePath(), executablePath);
                    }else{
                        // Mark the link as skippable
                        writeLnkDestinationToCache(file.getAbsolutePath(), "SKIP");
                    }
                }

                // If found, notify the listener
                if (executablePath != null) {
                    // Add the executable to the list
                    executablePaths.add(executablePath);

                    // Add the application name to the map
                    appNameMap.put(executablePath, applicationName);

                    // Update the listener
                    if (listener != null) {
                        listener.onPreloadUpdate(applicationName, current, linkFileList.size());
                    }
                }
            }catch(Exception e) {
                LOG.info("EXCEPTION WITH APP "+file.getName() + " " + e.toString());
            }

            current++;
        }

        // Load the list of the external applications
        List<String> externalApps = loadExternalAppPaths();
        executablePaths.addAll(externalApps);

        // Current application in the list
        current = 0;

        // Load the application cache map
        Map<String, MSCachedApplication> appCacheMap = loadAppCacheMap();

        // Cycle through all executable paths
        for (String executablePath : executablePaths) {
            try {
                String iconPath = null;

                // Try to load the application info from the cache
                if (appCacheMap.containsKey(executablePath)) {  // APP in cache
                    MSCachedApplication cachedApp = appCacheMap.get(executablePath);
                    executablePath = cachedApp.getExecutablePath();
                    iconPath = cachedApp.getIconPath();
                } else {  // APP not in cache
                    // Get the app icon
                    iconPath = getIconPath(executablePath);

                    // Save the info to the cache
                    writeAppToCache(executablePath, iconPath);
                }

                // Get the application name
                String applicationName = appNameMap.get(executablePath);
                // If the application name is not present, calculate it dynamically
                if (applicationName == null) {
                    applicationName = calculateAppNameFromExecutablePath(executablePath);
                }

                // Add the application
                addApplicationFromExecutablePath(executablePath, applicationName, iconPath);

                // Update the listener
                if (listener != null) {
                    listener.onProgressUpdate(applicationName, iconPath, current, executablePaths.size());
                }
            }catch(Exception e) {
                LOG.info("EXCEPTION WITH APP "+executablePath + " " + e.toString());
            }

            current++;
        }

        // Signal the end of the process
        if (listener != null) {
            listener.onApplicationsLoaded();
        }

        // Mark the apps as initialized
        setInitialized();
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
        String scriptPath = ResourceUtils.getResource("/vbscripts/readLnk.vbs").getAbsolutePath();

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

    /**
     * Obtain the application of the given executablePath and returns it.
     * Also add it to the applicationMap.
     * If applicationName is not specified, it calculates dynamically from the executablePath.
     * If executablePath is already present in the applicationMap,
     * to mitigate ambiguities, the application name is forced to the one calculated dynamically.
     *
     * @param executablePath  path of the app exe
     * @param applicationName application name
     * @param iconPath        the path to the icon. If null is dynamically generated
     * @return an Application object.
     */
    private synchronized Application addApplicationFromExecutablePath(String executablePath, String applicationName, String iconPath) {
        // Make sure the target is an exe file
        if (executablePath.toLowerCase().endsWith(".exe")) {
            // Generate the application name if null or if
            // executablePath is already present, to mitigate ambiguities of the program name,
            // the executable filename becomes the Application name ( without .exe )
            if (applicationMap.containsKey(executablePath) || applicationName == null) {
                applicationName = calculateAppNameFromExecutablePath(executablePath);
            }

            // If the application icon is null, find it
            if (iconPath == null) {
                iconPath = getIconPath(executablePath);
            }

            // Create the application
            Application application = new MSApplication(applicationName, executablePath, iconPath);

            // Populate the explorer app if not yet present.
            // A reference to the explorer app is needed later in the
            // request folder.
            if (explorerApp == null && executablePath.endsWith("explorer.exe")){
                explorerApp = application;
            }

            // Add it to the map
            applicationMap.put(executablePath, application);

            return application;
        }
        return null;
    }

    /**
     * Calculate the application name by extracting it from the executable path
     * @param executablePath path to the app exe
     * @return the extracted name.
     */
    private String calculateAppNameFromExecutablePath(String executablePath) {
        File appExe = new File(executablePath);
        // Create the new app name extracting the filename, removing the extension
        // and capitalizing the first letter
        return StringUtils.capitalize(appExe.getName().toLowerCase().replace(".exe", ""));
    }

    /**
     * Check if powershell is enabled in this machine.
     *
     * @return true if powershell is enabled, else otherwise.
     */
    private static boolean checkPowerShellEnabled() {
        Runtime runtime = Runtime.getRuntime();

        try {
            // Execute powershell
            Process proc = runtime.exec(new String[]{"powershell", "echo yes"});

            // If there was an error, return false
            if (proc.getErrorStream().available() > 0) {
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
     *
     * @param executablePath the executable with the icon
     * @return the icon File
     */
    private File generateIconFile(String executablePath) {
        // Obtain the application ID
        String appID = Application.Companion.getHashIDForExecutablePath(executablePath);

        // Get the icon file
        return new File(CacheManager.getInstance().getIconCacheDir(), appID + ".png");
    }

    /**
     * Obtain the icon associated with the given executable.
     *
     * @param executablePath path to the executable.
     * @return the icon associated with the given executable.
     */
    private String getIconPath(String executablePath) {
        // Get the icon file
        File iconFile = null;

        // Generate the icon file
        iconFile = generateIconFile(executablePath);

        // If the file doesn't exist, it must be generated
        if (!iconFile.isFile()) {
            iconFile = extractIcon(executablePath);
            LOG.fine("ICON EXTRACTED: "+executablePath);
        }

        // Return the icon file path
        return iconFile.getAbsolutePath();
    }

    /**
     * Extract the icon from the given executable.
     *
     * @param executablePath the executable with the icon.
     * @return the icon image file. Return null if an error occurred.
     */
    private File extractIcon(String executablePath) {
        // Get the icon file
        File iconFile = generateIconFile(executablePath);

        // Try to generate the icon using the native method
        try {
            File extractedIcon = extractIconUsingExe(executablePath, iconFile, true);
            if (extractedIcon != null) {
                return extractedIcon;
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        // Native method had an exception, use the fallback methods.

        // The icon can be obtained in two ways, but using powershell the
        // resulting image is better ( higher resolution ).
        if (isPowerShellEnabled) {  // Best method
            // Extract the icon
            boolean ris = extractIconUsingPowershellScript(executablePath, iconFile.getAbsolutePath());

            // An error occurred, return null
            if (!ris) {
                return null;
            } else {
                // Return the icon file ( generate again the file to avoid problems )
                return generateIconFile(executablePath);
            }
        } else {  // Not so good, but should do the trick
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
     *
     * @param executablePath  path of the executable
     * @param destinationFile path of the destination image file
     * @return true if succeeded, false otherwise.
     */
    private boolean extractIconUsingPowershellScript(String executablePath, String destinationFile) {
        Runtime runtime = Runtime.getRuntime();

        try {
            // Execute powershell
            Process proc = runtime.exec(new String[]{"powershell",
                    "[System.Reflection.Assembly]::LoadWithPartialName('System.Drawing')  | Out-Null ; [System.Drawing.Icon]::ExtractAssociatedIcon('" + executablePath + "').ToBitmap().Save('" + destinationFile + "'); echo 'ok'"});

            // If there was an error, return false
            if (proc.getErrorStream().available() > 0) {
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
     * Check if the given image is low res ( smaller than 48x48 ).
     * Used to filter big images 256x256 with small icons on the top left corner.
     * @param image the image to analyze.
     * @return true if image is smaller than 48x48, false otherwise.
     */
    public static boolean isLowResImage(BufferedImage image) {
        if (image.getHeight() < 250)
            return true;

        int index = image.getHeight()-1;
        while (image.getRGB(index, index) == 0 && index > 0) {
            index--;
        }

        return index < 48;
    }

    /**
     * Extract the icon from the executable using the extractIcon.exe method
     *
     * @param executablePath  path of the executable
     * @param destinationFile path of the destination image file
     * @param bigIcon if true, request the 256x256 icon. If false 48x48 is requested.
     * @return true if succeeded, false otherwise.
     */
    public File extractIconUsingExe(String executablePath, File destinationFile, boolean bigIcon) {
        Runtime runtime = Runtime.getRuntime();
        String exePath = ResourceUtils.getResource("/win/extractIcon.exe").getAbsolutePath();

        try {
            String quality = bigIcon ? "jumbo" : "small";

            // Execute powershell
            Process proc = runtime.exec(new String[]{exePath, executablePath, destinationFile.getAbsolutePath(), quality});
            proc.waitFor();

            // If a big icon has been requested, make sure the resulting icon is valid.
            if (bigIcon && destinationFile.isFile()) {
                // Reload the destination file
                destinationFile = new File(destinationFile.getAbsolutePath());

                // If the image is low resolution, request the 48x48 image.
                BufferedImage image = ImageIO.read(destinationFile);
                if (isLowResImage(image)) {
                    return extractIconUsingExe(executablePath, destinationFile, false);
                }
            }

            return destinationFile;
        } catch (IOException e) {
            System.out.println(executablePath);
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Extract the icon from the executable using the native method.
     * THIS METHOD USES BLACK MAGIC TO WORK, DON'T MESS WITH IT.
     *
     * @param executablePath  path of the executable
     * @param destinationFile destination image file
     * @return the extracted file if succeeded, false otherwise.
     */
    private File extractIconNative(String executablePath, File destinationFile, int size) {
        // Create an array that will hold the icon references
        WinDef.HICON[] icons = new WinDef.HICON[10];

        // Extract the icons, 128x128 size. ( HIGHER SIZES GIVE PROBLEMS ).
        WinNT.HRESULT hresult = ShellLib.INSTANCE.SHDefExtractIcon(executablePath,
                0,
                0,
                icons,
                null,
                size);
        // Cycle through the icons.
        for (int j = 0; j<icons.length; j++) {
            // Get the icon
            WinDef.HICON hIcon = icons[j];
            final Dimension iconSize = getIconSize(hIcon);

            if (iconSize.width == 0 || iconSize.height == 0)
                return null;

            final int width = iconSize.width;
            final int height = iconSize.height;
            final short depth = 24;

            final byte[] lpBitsColor = new byte[width * height * depth / 8];
            final Pointer lpBitsColorPtr = new Memory(lpBitsColor.length);
            final byte[] lpBitsMask = new byte[width * height * depth / 8];
            final Pointer lpBitsMaskPtr = new Memory(lpBitsMask.length);
            final WinGDI.BITMAPINFO bitmapInfo = new WinGDI.BITMAPINFO();
            final WinGDI.BITMAPINFOHEADER hdr = new WinGDI.BITMAPINFOHEADER();

            bitmapInfo.bmiHeader = hdr;
            hdr.biWidth = width;
            hdr.biHeight = height;
            hdr.biPlanes = 1;
            hdr.biBitCount = depth;
            hdr.biCompression = 0;
            hdr.write();
            bitmapInfo.write();

            final WinDef.HDC hDC = User32.INSTANCE.GetDC(null);
            final WinGDI.ICONINFO iconInfo = new WinGDI.ICONINFO();
            User32.INSTANCE.GetIconInfo(hIcon, iconInfo);
            iconInfo.read();

            GDI32.INSTANCE.GetDIBits(hDC, iconInfo.hbmColor, 0, height,
                    lpBitsColorPtr, bitmapInfo, 0);
            lpBitsColorPtr.read(0, lpBitsColor, 0, lpBitsColor.length);
            GDI32.INSTANCE.GetDIBits(hDC, iconInfo.hbmMask, 0, height,
                    lpBitsMaskPtr, bitmapInfo, 0);
            lpBitsMaskPtr.read(0, lpBitsMask, 0, lpBitsMask.length);
            final BufferedImage image = new BufferedImage(width, height,
                    BufferedImage.TYPE_INT_ARGB);

            int r, g, b, a, argb;
            int x = 0, y = height - 1;
            for (int i = 0; i < lpBitsColor.length; i = i + 3) {
                b = lpBitsColor[i] & 0xFF;
                g = lpBitsColor[i + 1] & 0xFF;
                r = lpBitsColor[i + 2] & 0xFF;
                a = 0xFF - lpBitsMask[i] & 0xFF;
                // Remove the black
                if (b==0&&g==0&&r==0) {
                    a=0;
                }
                argb = (a << 24) | (r << 16) | (g << 8) | b;
                image.setRGB(x, y, argb);
                x = (x + 1) % width;
                if (x == 0)
                    y--;
            }

            User32.INSTANCE.ReleaseDC(null, hDC);

            // Save the image
            try {
                ImageIO.write(image, "png", destinationFile);
                return destinationFile;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Used to extract icons using the kernel calls.
     */
    public interface ShellLib extends Shell32 {
        ShellLib INSTANCE = (ShellLib) Native.loadLibrary("Shell32", ShellLib.class, W32APIOptions.DEFAULT_OPTIONS);

        WinNT.HRESULT SHDefExtractIcon(String lpszFile, int nIconIndex, int flags, WinDef.HICON[] phiconLarge, WinDef.HICON[] phiconSmall, int iconSize);
    }

    /**
     * Converts an icon to a buffered image
     *
     * @param icon the icon to convert
     * @return the BufferedImage with the icon
     */
    private static Image iconToImage(Icon icon) {
        if (icon instanceof ImageIcon) {
            return ((ImageIcon) icon).getImage();
        } else {
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

    /**
     * Write the specified lnk file path / destination to the cache file.
     *
     * @param lnkFilePath  the lnk file path
     * @param executablePath the destination file the lnk file points to
     */
    private void writeLnkDestinationToCache(String lnkFilePath, String executablePath) {
        // Get the cache manager
        CacheManager cacheManager = CacheManager.getInstance();

        // Get the cache destination file
        File cacheFile = new File(cacheManager.getCacheDir(), START_MENU_CACHE_FILENAME);

        // Open the file
        try (FileWriter fw = new FileWriter(cacheFile, true)) {

            // Append the info
            fw.write(lnkFilePath);
            fw.write('\n');
            fw.write(executablePath);
            fw.write('\n');
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write the specified application to the cache file.
     *
     * @param executablePath  the executable path of the application
     * @param iconPath path to the app icon.
     */
    private void writeAppToCache(String executablePath, String iconPath) {
        // Get the cache manager
        CacheManager cacheManager = CacheManager.getInstance();

        // Get the cache destination file
        File cacheFile = new File(cacheManager.getCacheDir(), APP_CACHE_FILENAME);

        // Open the file
        try (FileWriter fw = new FileWriter(cacheFile, true)) {

            // Append the info
            fw.write(executablePath);
            fw.write('\n');

            // Write the appropriate value
            if (iconPath != null) {
                fw.write(iconPath);
                fw.write('\n');
            } else {
                fw.write("NULL\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read the start menu cache and return a map that associates the
     * lnk file path to the cached application info.
     *
     * @return a map containing the association < LnkPath, CachedApp >
     */
    private Map<String, String> loadLnkDestinationCacheMap() {
        CacheManager cacheManager = CacheManager.getInstance();

        // Get the cache destination file
        File cacheFile = new File(cacheManager.getCacheDir(), START_MENU_CACHE_FILENAME);

        Map<String, String> output = new HashMap<>();

        // Make sure the file exists
        if (cacheFile.isFile()) {
            // Read all the info
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(cacheFile)))) {
                String lnkPath;
                while ((lnkPath = reader.readLine()) != null) {
                    String executablePath = reader.readLine();

                    // Add the tuple to the map
                    output.put(lnkPath, executablePath);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return output;
    }

    /**
     * Read the app and return a map that associates the
     * executable path to the cached application info.
     *
     * @return a map containing the association < ExecutablePath, CachedApp >
     */
    private Map<String, MSCachedApplication> loadAppCacheMap() {
        CacheManager cacheManager = CacheManager.getInstance();

        // Get the cache destination file
        File cacheFile = new File(cacheManager.getCacheDir(), APP_CACHE_FILENAME);

        Map<String, MSCachedApplication> output = new HashMap<>();

        // Make sure the file exists
        if (cacheFile.isFile()) {
            // Read all the info
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(cacheFile)))) {
                String executablePath;
                while ((executablePath = reader.readLine()) != null) {
                    String iconPath = reader.readLine();

                    // Convert the null iconpath
                    if (iconPath.equals("NULL")) {
                        iconPath = null;
                    }

                    // Add the tuple to the map
                    output.put(executablePath, new MSCachedApplication(executablePath, iconPath));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return output;
    }
}
