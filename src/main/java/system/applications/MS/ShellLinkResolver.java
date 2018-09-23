package system.applications.MS;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.WTypes;

/**
 * Binding to the native library used to get the target of a lnk file
 */
public interface ShellLinkResolver extends Library
{
    ShellLinkResolver INSTANCE = (ShellLinkResolver) Native.loadLibrary("ShellLinkResolver", ShellLinkResolver.class);

    // int resolveLnkTarget(LPCSTR lnkFilePath, LPWSTR targetBuffer, int targetBufferSize);

    /**
     * This is the method that is directly bridged to the native library, should not be used.
     * @param lnkFilePath
     * @param targetBuffer
     * @param targetBufferSize
     * @return
     */
    int resolveLnkTargetInternal(String lnkFilePath, char[] targetBuffer, int targetBufferSize);

    /**
     * Resolve the target of the given lnk.
     * @param lnkFilePath
     * @return a String with the target path if found, null otherwise.
     */
    static String resolveLnkTarget(String lnkFilePath) {
        char[] buffer = new char[512];
        int result = ShellLinkResolver.INSTANCE.resolveLnkTargetInternal(lnkFilePath, buffer, buffer.length);
        if (result > 0) {
            return Native.toString(buffer);
        }

        return null;
    }
}