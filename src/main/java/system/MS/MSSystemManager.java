package system.MS;

import system.SystemManager;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

/**
 * Windows system manager.
 */
public class MSSystemManager implements SystemManager {

    @Override
    public boolean shutdown() {
        return false;
    }

    @Override
    public boolean restart() {
        return false;
    }

    @Override
    public boolean logout() {
        return false;
    }

    @Override
    public boolean suspend() {
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
