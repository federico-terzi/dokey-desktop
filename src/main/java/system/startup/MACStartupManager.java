package system.startup;

import com.sun.jna.Library;
import com.sun.jna.Native;
import org.apache.commons.io.FileUtils;
import system.storage.StorageManager;
import system.startup.StartupManager;

import java.io.*;
import java.util.StringTokenizer;

public class MACStartupManager extends StartupManager {
    public static final String STARTUP_PLIST_FILENAME = "com.rocketguys.dokey.plist";

    private StorageManager storageManager;

    public MACStartupManager(StorageManager storageManager) {
        this.storageManager = storageManager;
    }

    private interface CLibrary extends Library {
        CLibrary INSTANCE = (CLibrary) Native.loadLibrary("c", CLibrary.class);
        int getpid ();
    }

    @Override
    public int getPID() {
        return CLibrary.INSTANCE.getpid();
    }

    @Override
    public String getExecutablePath(int pid) {
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
            return sb.toString().substring(0, sb.toString().length() - 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean isBundledInstance() {
        int currentPID = getPID();
        String executablePath = getExecutablePath(currentPID);
        return !executablePath.contains("java");
    }

    /**
     * @return the LaunchAgents directory, or null if not found.
     */
    private File getLaunchAgentsFolder() {
        File homeFolder = new File(System.getProperty("user.home"));
        if (!homeFolder.isDirectory())
            return null;

        File libraryFolder = new File(homeFolder, "Library");
        if (!libraryFolder.isDirectory())
            return null;

        // If it doesn't exist yet, create it
        File launchAgentsFolder = new File(libraryFolder, "LaunchAgents");
        if (!launchAgentsFolder.isDirectory())
            launchAgentsFolder.mkdir();

        return launchAgentsFolder;
    }

    @Override
    public boolean isAutomaticStartupEnabled() {
        File launchAgentsFolder = getLaunchAgentsFolder();
        if (launchAgentsFolder == null)
            return false;

        // Get the launch agents plist file.
        File launchAgentsFile = new File(launchAgentsFolder, STARTUP_PLIST_FILENAME);

        // The enable-status is determined by the presence of the file.
        return launchAgentsFile.isFile();
    }

    /**
     * Generate the plist file based on the given executable path.
     * @param executablePath the executable path of the application.
     * @return the plist File, or null if an error occurred.
     */
    private File createPlistFile(String executablePath) {
        // Create the file in the cache directory
        File cacheDir = storageManager.getCacheDir();
        File startupPlistFile = new File(cacheDir, STARTUP_PLIST_FILENAME);

        // If the file already exists, delete it
        if (startupPlistFile.isFile()) {
            startupPlistFile.delete();
        }

        // Create a new plist file
        try {
            startupPlistFile = new File(cacheDir, STARTUP_PLIST_FILENAME);
            PrintWriter pw = new PrintWriter(startupPlistFile);
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            pw.println("<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">");
            pw.println("<plist version=\"1.0\">");
            pw.println("<dict>");
            pw.println("<key>Label</key>");
            pw.println("<string>com.rocketguys.dokey</string>");
            pw.println("<key>ProgramArguments</key>");
            pw.println("<array>");
            pw.println("<string>"+executablePath+"</string>");
            pw.println("<string>-startup</string>");
            pw.println("</array>");
            pw.println("<key>RunAtLoad</key>");
            pw.println("<true/>");
            pw.println("</dict>");
            pw.println("</plist>");
            pw.close();

            return startupPlistFile;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean enableAutomaticStartup() {
        File launchAgentsFolder = getLaunchAgentsFolder();
        if (launchAgentsFolder == null)
            return false;

        // Get the launch agents plist file.
        File launchAgentsFile = new File(launchAgentsFolder, STARTUP_PLIST_FILENAME);

        // Create the plist file
        File plistFile = createPlistFile(executablePath);
        if (plistFile == null)
            return false;

        // Copy the plist file in the launch agents folder
        try {
            FileUtils.copyFile(plistFile, launchAgentsFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean disableAutomaticStartup() {
        File launchAgentsFolder = getLaunchAgentsFolder();
        if (launchAgentsFolder == null)
            return false;

        // Get the launch agents plist file.
        File launchAgentsFile = new File(launchAgentsFolder, STARTUP_PLIST_FILENAME);

        if (!launchAgentsFile.isFile())
            return true;

        return launchAgentsFile.delete();
    }
}
