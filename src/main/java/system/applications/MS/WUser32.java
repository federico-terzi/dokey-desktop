package system.applications.MS;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.win32.W32APIOptions;

public interface WUser32 extends User32 {
    WUser32 INSTANCE = (WUser32) Native.loadLibrary("User32", WUser32.class, W32APIOptions.DEFAULT_OPTIONS);
    int SystemParametersInfo(int uiAction, int uiParam, int pvParam, int fWinIni);
}