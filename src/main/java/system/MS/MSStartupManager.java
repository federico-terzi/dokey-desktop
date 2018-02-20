package system.MS;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.ptr.ByReference;
import org.apache.commons.io.FileUtils;
import system.CacheManager;
import system.ResourceUtils;
import system.StartupManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class MSStartupManager extends StartupManager {
    public static final String STARTUP_LINK_FILENAME = "dokey.lnk";

    @Override
    public int getPID() {
        return Kernel32.INSTANCE.GetCurrentProcessId();
    }

    /**
     * Used to get the executable path for the given pid
     */
    public interface PsApi extends StdCallLibrary {

        int GetModuleFileNameExA(WinNT.HANDLE process, WinNT.HANDLE module,
                                 byte[] name, int i);

    }

    @Override
    public String getExecutablePath(int pid) {
        MSStartupManager.PsApi psapi = (MSStartupManager.PsApi) Native.loadLibrary("psapi", MSStartupManager.PsApi.class);
        byte[] pathText = new byte[1024];
        WinNT.HANDLE process = Kernel32.INSTANCE.OpenProcess(0x0400 | 0x0010, false, pid);
        psapi.GetModuleFileNameExA(process, null, pathText, 1024);
        String executablePath = Native.toString(pathText);

        // If the executablePath is empty, return null
        if (executablePath.length() == 0) {
            return null;
        }

        return executablePath;
    }

    @Override
    public boolean isBundledInstance() {
        int currentPID = getPID();
        String executablePath = getExecutablePath(currentPID);
        return !executablePath.contains("java.exe");
    }

    /**
     * @return the path of the Startup folder of the user.
     */
    private String getStartupFolderPath() {
        PointerByReference pathPointer = new PointerByReference();
        Shell32.INSTANCE.SHGetKnownFolderPath(Guid.GUID.fromString("{B97D20BB-F46A-4C97-BA10-5E3608430854}"), 0, null, pathPointer);
        return pathPointer.getValue().getWideString(0);
    }

    /**
     * Used to create a temporary link file pointing to the specified executable path.
     */
    private File createLinkFile(String executablePath) {
        // Create the file in the cache directory
        File cacheDir = CacheManager.getInstance().getCacheDir();
        File startupLinkFile = new File(cacheDir, STARTUP_LINK_FILENAME);

        // If the file already exists, delete it
        if (startupLinkFile.isFile()) {
            startupLinkFile.delete();
        }

        Runtime runtime = Runtime.getRuntime();
        String scriptPath = ResourceUtils.getResource("/vbscripts/createLnk.vbs").getAbsolutePath();

        // Remove the starting trailing slash
        if (scriptPath.startsWith("/")) {
            scriptPath = scriptPath.substring(1);
        }

        try {
            // Execute the process
            Process proc = runtime.exec(new String[]{"cscript", "/nologo", scriptPath, startupLinkFile.getAbsolutePath(), executablePath});

            proc.waitFor();

            // Make sure the file exists
            startupLinkFile = new File(cacheDir, STARTUP_LINK_FILENAME);
            if (startupLinkFile.isFile()) {
                return startupLinkFile;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @return the destination lnk file in the startup folder.
     */
    private File getStartupFolderLinkFile() {
        // Obtain the startup folder path
        String startupFolderPath = getStartupFolderPath();
        File startupFolder = new File(startupFolderPath);

        // Make sure it exists
        if (!startupFolder.isDirectory())
            return null;

        // Get the link file in the startup folder
        return new File(startupFolder, STARTUP_LINK_FILENAME);
    }

    @Override
    public boolean isAutomaticStartupEnabled() {
        // Get the link file in the startup folder
        File destinationLink = getStartupFolderLinkFile();

        if (destinationLink == null)
            return false;

        // If the link exists, the automatic startup is enabled
        return destinationLink.isFile();
    }

    @Override
    public boolean enableAutomaticStartup() {
        // Get the link file in the startup folder
        File destinationLink = getStartupFolderLinkFile();

        if (destinationLink == null)
            return false;

        // Create the link file for the executable
        File linkFile = createLinkFile(executablePath);

        if (linkFile == null)
            return false;

        // Copy the link file in the startup menu
        try {
            FileUtils.copyFile(linkFile, destinationLink);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean disableAutomaticStartup() {
        // Get the link file in the startup folder
        File destinationLink = getStartupFolderLinkFile();

        if (destinationLink == null)
            return false;

        if (!destinationLink.isFile()) {
            return true;
        }

        // Delete the file
        return destinationLink.delete();
    }
}
