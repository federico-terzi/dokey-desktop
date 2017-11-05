package window;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.ptr.IntByReference;

public class MSWindowManager implements WindowManager {
    private static final int MAX_TITLE_LENGTH = 1024;

    /**
     * @return the Window object of the active window.
     */
    @Override
    public Window getActiveWindow() {
        char[] buffer = new char[MAX_TITLE_LENGTH * 2];
        HWND hwnd = User32.INSTANCE.GetForegroundWindow();
        User32.INSTANCE.GetWindowText(hwnd, buffer, MAX_TITLE_LENGTH);

        IntByReference PID = new IntByReference();
        User32.INSTANCE.GetWindowThreadProcessId(hwnd,PID);
        String titleText = Native.toString(buffer);

        Window window = new Window(PID.getValue(), titleText);
        return window;


    }
}
