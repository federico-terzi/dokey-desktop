
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.W32APIOptions;
import system.MAC.MACUtils;
import system.MS.MSApplicationManager;
import system.ResourceUtils;

import java.io.File;
import java.io.IOException;

public class WinTestMain {
    static {

    }

    interface ExtractIcon extends Library {
    }

    public static void main(String[] args) throws IOException {
        MSApplicationManager appManager = new MSApplicationManager(null, null);
        for (int i = 30; i<300; i++) {
//            appManager.extractIconNative("C:\\Program Files\\FileZilla FTP Client\\filezilla.exe", new File("C:\\Users\\fredd\\Documents\\test"+i+".png"), i);
            System.out.println(i);
        }
//        System.setProperty("jna.platform.library.path", ResourceUtils.getResource("/libs/").getAbsolutePath());
//        ExtractIcon e = (ExtractIcon) Native.loadLibrary("extractIcon", ExtractIcon.class);

        System.exit(0);
    }
}
