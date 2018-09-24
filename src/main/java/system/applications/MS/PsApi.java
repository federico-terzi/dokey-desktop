package system.applications.MS;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.win32.StdCallLibrary;

/**
 * Used to get the executable path for the given pid
 */
public interface PsApi extends StdCallLibrary {
    PsApi INSTANCE = (PsApi) Native.loadLibrary("psapi", PsApi.class);

    int GetModuleFileNameExA(WinNT.HANDLE process, WinNT.HANDLE module ,
                             byte[] name, int i);

}