package app.bindings;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;

public interface JavaMacNativeUI extends Library
{
    JavaMacNativeUI INSTANCE = Native.loadLibrary("JavaMacNativeUI", JavaMacNativeUI.class);

    /*
    DIALOG RELATED METHODS
     */

    void displayDialog(String imageUrl, String title, String description, String[] buttons, int buttonsCount,
                       int isCritical, JavaMacNativeUI.DialogCallback callback);

    interface DialogCallback extends Callback {
        void invoke(int buttonNumber);
    }

    /*
    STATUS BAR ICON METHODS
     */

    void initializeStatusItem();
    void setStatusItemImage(String imagePath);
    void setStatusItemTooltip(String tooltip);
    void setStatusItemHighlighted(int highlighted);
    void setStatusItemAction(StatusItemClickCallback callback);

    interface StatusItemClickCallback extends Callback {
        void invoke(int x, int y);
    }
}