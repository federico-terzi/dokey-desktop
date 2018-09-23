package system.applications.MS;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.WString;

/**
 * Binding to the native library used to extract the icon of an executable
 */
public interface WinExtractIconLib extends Library
{
    WinExtractIconLib INSTANCE = (WinExtractIconLib) Native.loadLibrary("WinExtractIconLib", WinExtractIconLib.class);

    int extractIconInternal(WString executablePath, WString destinationPath, int jumbo);

    static boolean extractIcon(String executablePath, String destinationPath, boolean jumbo) {
        int jumboInt = jumbo ? 1 : 0;
        int result = INSTANCE.extractIconInternal(new WString(executablePath), new WString(destinationPath), jumboInt);
        return result == 0;
    }
}