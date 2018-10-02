package app.bindings;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.WString;

public interface JavaWinNativeUI extends Library
{
    JavaWinNativeUI INSTANCE = Native.loadLibrary("JavaWinNativeUI", JavaWinNativeUI.class);

    void displayDialog(WString title, WString description, int isCritical, WString[] buttons, int buttonCount, int includeCancel, int useCommandLinks, DialogCallback callback);
    void displayInfo(WString title, WString description, int isCritical, DialogCallback callback);

    interface DialogCallback extends Callback {
        void invoke(int buttonNumber);
    }
}