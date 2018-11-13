package system.keyboard.bindings;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.WString;

public interface MacKeyboardLib extends Library {
    MacKeyboardLib INSTANCE = Native.loadLibrary("MacKeyboardLib", MacKeyboardLib.class);

    /**
     * Search for the virtual key code of the given key using the current keyboard layout.
     * Return the virtual key code if found, -1 otherwise.
     */
    int decodeVirtualKey(String key);

    /**
     * Send the given keyboard shortcut to the system.
     * The function is executed on the main thread and the "callback" will be called to
     * receive the result. In particular, the first argument of the callback function will be:
     * The number of keys if succeeded, -1 if a key wasn't found int the current keyboard layout.
     */
    void sendShortcut(String[] keys, int keyCount, SendShortcutResponseCallback callback);

    interface SendShortcutResponseCallback extends Callback {
        void invoke(int response);
    }

    /*
     Transform the given key to the original key, removing the effects of the given modifier
     keys. The result is returned using the given callback.
     */
    void removeModifiersFromKey(String key, int control, int alt, int shift, int command,
                                RemoveModifiersFromKeyCallback callback);

    interface RemoveModifiersFromKeyCallback extends Callback {
        void invoke(String key);
    }

    /**
     * Disable the CAPS LOCK.
     * Return 0 if correctly disabled, -1 if an error occurred
     */
    int forceDisableCapsLock();
}