
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.W32APIOptions;
import javafx.scene.image.Image;
import system.MAC.MACUtils;
import system.MS.MSApplicationManager;
import system.ResourceUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class WinTestMain {
    static {

    }

    interface ExtractIcon extends Library {
    }

    public static boolean isLowResImage(BufferedImage image) {
        if (image.getHeight() < 250)
            return true;

        int index = image.getHeight()-1;
        while (image.getRGB(index, index) == 0) {
            index--;
        }

        return index < 48;
    }

    public static void main(String[] args) throws IOException {
        //MSApplicationManager appManager = new MSApplicationManager(null);

        //appManager.extractIconUsingExe("C:\\Program Files\\FileZilla FTP Client\\filezilla.exe", new File("C:\\Users\\fredd\\Documents\\test.png"));

        BufferedImage image = ImageIO.read(new File("C:\\Users\\Federico\\.dokey\\icons\\86e1a12a151a7d01f9c34e6d72d6850c.png"));

        boolean res = isLowResImage(image);

        System.exit(0);
    }
}
