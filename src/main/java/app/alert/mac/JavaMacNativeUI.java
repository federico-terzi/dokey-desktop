package app.alert.mac;

import com.sun.jna.Callback;
import com.sun.jna.Library;

public interface JavaMacNativeUI extends Library
{
    void displayDialog(String imageUrl, String title, String description, String[] buttons, int buttonsCount,
                       int isCritical, JavaMacNativeUI.DialogCallback callback);

    interface DialogCallback extends Callback {
        void invoke(int buttonNumber);
    }
}