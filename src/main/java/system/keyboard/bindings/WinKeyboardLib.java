package system.keyboard.bindings;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.WString;

public interface WinKeyboardLib extends Library
{
    WinKeyboardLib INSTANCE = Native.loadLibrary("WinKeyboardLib", WinKeyboardLib.class);

    /**
        Search for the virtual key code of the given key using the current keyboard layout.
        Return the virtual key code if found, -1 otherwise.
    */
    int decodeVirtualKey(WString key);

    /**
        Send the given keyboard shortcut to the system input queue.
        Return the number of keys if succeeded, -1 if a key wasn't found in the current keyboard layout.
    */
    int sendShortcut(WString[] keys, int keyCount);
}