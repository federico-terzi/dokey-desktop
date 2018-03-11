
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.W32APIOptions;
import system.MAC.MACUtils;
import system.ResourceUtils;

import java.io.IOException;

public class WinTestMain {

    interface ExtractIcon extends Library {
        ExtractIcon INSTANCE = (ExtractIcon) Native.loadLibrary("extractIcon", ExtractIcon.class);
    }

    public static void main(String[] args) throws IOException {
        System.loadLibrary("extractIcon");

        ExtractIcon e = ExtractIcon.INSTANCE;

        System.exit(0);
    }
}
