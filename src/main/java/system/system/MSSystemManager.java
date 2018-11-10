package system.system;

import system.system.SystemManager;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.WinDef.HWND;

import java.io.IOException;

/**
 * Windows system manager.
 */
public class MSSystemManager extends SystemManager {
    @Override
    public boolean restart() {
        Runtime runtime = Runtime.getRuntime();
        try {
            // Execute the process
            Process proc = runtime.exec(new String[]{"Shutdown.exe", "-r", "-t", "00"});
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
    public boolean shutdownPC() {
        Runtime runtime = Runtime.getRuntime();
        try {
            // Execute the process
            Process proc = runtime.exec(new String[]{"Shutdown.exe", "-s", "-t", "00"});
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
    public boolean logout() {
        Runtime runtime = Runtime.getRuntime();
        try {
            // Execute the process
            Process proc = runtime.exec(new String[]{"Rundll32.exe", "User32.dll,LockWorkStation"});
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
    public boolean suspend() {
        Runtime runtime = Runtime.getRuntime();
        try {
            // Execute the process
            Process proc = runtime.exec(new String[]{"rundll32.exe", "powrprof.dll,SetSuspendState", "0,1,0"});
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
    public boolean volumeDown() {
        HWND hwnd = User32.INSTANCE.GetForegroundWindow();
        // Constant reference
        // https://msdn.microsoft.com/en-us/library/windows/desktop/ms646247(v=vs.85).aspx
        User32.INSTANCE.SendMessage(hwnd, 0x0319, new WinDef.WPARAM(0), new WinDef.LPARAM(0x90000));

        return true;
    }

    @Override
    public boolean volumeUp() {
        HWND hwnd = User32.INSTANCE.GetForegroundWindow();
        // Constant reference
        // https://msdn.microsoft.com/en-us/library/windows/desktop/ms646247(v=vs.85).aspx
        User32.INSTANCE.SendMessage(hwnd, 0x0319, new WinDef.WPARAM(0), new WinDef.LPARAM(0xA0000));

        return true;
    }

    @Override
    public boolean volumeMute() {
        HWND hwnd = User32.INSTANCE.GetForegroundWindow();
        // Constant reference
        // https://msdn.microsoft.com/en-us/library/windows/desktop/ms646247(v=vs.85).aspx
        User32.INSTANCE.SendMessage(hwnd, 0x0319, new WinDef.WPARAM(0), new WinDef.LPARAM(0x80000));

        return true;
    }

    @Override
    public boolean playOrPause() {
        HWND hwnd = User32.INSTANCE.GetForegroundWindow();
        // Constant reference
        // https://msdn.microsoft.com/en-us/library/windows/desktop/ms646247(v=vs.85).aspx
        User32.INSTANCE.SendMessage(hwnd, 0x0319, new WinDef.WPARAM(0), new WinDef.LPARAM(0xE0000));

        return true;
    }

    @Override
    public boolean nextTrack() {
        HWND hwnd = User32.INSTANCE.GetForegroundWindow();
        // Constant reference
        // https://msdn.microsoft.com/en-us/library/windows/desktop/ms646247(v=vs.85).aspx
        User32.INSTANCE.SendMessage(hwnd, 0x0319, new WinDef.WPARAM(0), new WinDef.LPARAM(0xB0000));

        return true;
    }

    @Override
    public boolean previousTrack() {
        HWND hwnd = User32.INSTANCE.GetForegroundWindow();
        // Constant reference
        // https://msdn.microsoft.com/en-us/library/windows/desktop/ms646247(v=vs.85).aspx
        User32.INSTANCE.SendMessage(hwnd, 0x0319, new WinDef.WPARAM(0), new WinDef.LPARAM(0xC0000));

        return true;
    }
}
