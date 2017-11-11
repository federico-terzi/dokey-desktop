package system.MS;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.IntByReference;
import system.model.Application;
import system.model.ApplicationManager;
import system.model.Window;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class MSApplicationManager implements ApplicationManager {
    private static final int MAX_TITLE_LENGTH = 1024;

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
        User32.INSTANCE.GetWindowThreadProcessId(hwnd,PID);

        // Get the executable path
        String executablePath = getExecutablePathFromPID(PID.getValue());

        // Get the icon
        Icon icon = null;
        if (executablePath != null) {
            icon = FileSystemView.getFileSystemView().getSystemIcon(new File(executablePath));
        }

        Window window = new MSWindow(PID.getValue(), titleText, icon, executablePath, hwnd);
        return window;
    }

    /**
     * @return PID of the current active system.window.
     */
    @Override
    public int getActivePID() {
        HWND hwnd = User32.INSTANCE.GetForegroundWindow();

        // Get the PID
        IntByReference PID = new IntByReference();
        User32.INSTANCE.GetWindowThreadProcessId(hwnd,PID);

        return PID.getValue();
    }

    /**
     * Return the executable path for the given PID. Return null if not found.
     * It uses the WMIC command line utility.
     * @param pid process PID.
     * @return the executable path for the given PID. null if not found.
     */
    private String getExecutablePathFromPID(int pid) {
        String cmd = "wmic process where ProcessID="+pid+" get ProcessID,ExecutablePath /FORMAT:csv";
        Runtime runtime = Runtime.getRuntime();

        try {
            // Execute the process
            Process proc = runtime.exec(cmd);

            // Get the output
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = null;

            // Read each line
            while((line = br.readLine()) != null) {
                String[] subPieces = line.split(",");

                // Make sure the line is not empty and ends with a number ( process PID )
                if (!line.trim().isEmpty() && isNumeric(subPieces[subPieces.length-1])) {
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
     *
     * It uses the WMIC command line utility.
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
            while((line = br.readLine()) != null) {
                String[] tokens = line.trim().split(",");

                // Make sure the line is not empty and ends with a number ( process PID ).
                // And also the path is not empty.
                if (!line.trim().isEmpty() && isNumeric(tokens[tokens.length-1]) &&
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
        }catch (NumberFormatException e) {
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
                User32.INSTANCE.GetWindowThreadProcessId(hwnd,PID);

                // Get the executable path
                String executablePath = executablesMap.get(PID.getValue());

                // Get the icon
                Icon icon = null;
                if (executablePath != null) {
                    icon = FileSystemView.getFileSystemView().getSystemIcon(new File(executablePath));
                }

                Window window = new MSWindow(PID.getValue(), titleText, icon, executablePath, hwnd);
                windowList.add(window);

                return true;
            }
        }, null);

        return windowList;
    }

    /**
     * Return a list of Application(s) installed in the system.
     * This function checks in the Windows Start Menu and analyzes the entries.
     * @return the list of Application installed in the system.
     */
    @Override
    public List<Application> getApplicationList() {
        // Get the user start menu folder from the registry
        String userStartDir = Advapi32Util.registryGetStringValue(
                WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders", "Start Menu");

        // Get the system start menu folder from the registry
        String systemStartDir = Advapi32Util.registryGetStringValue(
                WinReg.HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders", "Common Start Menu");

        return null;
    }
}
