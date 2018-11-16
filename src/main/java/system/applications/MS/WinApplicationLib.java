package system.applications.MS;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.WinDef;

/**
 * Binding to the native library used to manage active applications.
 */
public interface WinApplicationLib extends Library {
    WinApplicationLib INSTANCE = (WinApplicationLib) Native.loadLibrary("WinApplicationLib", WinApplicationLib.class);

    /*
        Enumerate all the active applications, by invoking the callback for each one found.
        Note: the callback is invoked only once per application.
    */
    void listActiveApplications(ActiveAppCallback callback, WinDef.HWND[] lastActiveHandles, int lastActiveHandlesSize);

    /*
        Find the application currently in focus.
    */
    void getActiveApplication(ActiveAppCallback callback);

    /*
        Attempt to focus the requested application.
        Return:
        * 1 if succeeded.
        * 2 the application was already focused.
        * -1 if the application was not open.
        * -2 if the application was open, but could not be focused.
    */
    int focusApplication(WString appId, WinDef.HWND appHwnd, WinDef.HWND[] lastActiveHandles, int lastActiveHandlesSize);

    /*
        Extract the application directory for the given dokeyAppId ( store:familyName!AppId )
        The path will be exported to the pathBuffer. The bufferSize parameter specify the number
        of characters that the buffer accepts. If the path exceeds that size, it will be truncated.
        Return 1 if the path was correctly found, -1 if an error occurred.
    */
    int extractUWPApplicationDirectory(WString appId, byte[] pathBuffer, int bufferSize);

    interface ActiveAppCallback extends Callback {
        boolean invoke(WinDef.HWND hwnd, WString executablePath, int isUWPApp, WString appId);
    }
}