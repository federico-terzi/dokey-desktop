package system.external.photoshop.MAC;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * Binding to the native library used to manage photoshop.
 */
public interface MACPhotoshopBindings extends Library {
    MACPhotoshopBindings INSTANCE = (MACPhotoshopBindings) Native.loadLibrary("MACPhotoshopBindings", MACPhotoshopBindings.class);

    void executeJavascript(String code, double[] args, int argCount);
}