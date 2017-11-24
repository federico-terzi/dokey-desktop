import system.ApplicationManagerFactory;
import system.MS.MSIconExtractor;
import system.model.ApplicationManager;
import system.model.Window;

import java.util.List;

public class IconTestMain {
    public static void main(String[] args) {
        MSIconExtractor.saveExecutableIcon("C:\\Program Files\\FileZilla FTP Client\\filezilla.exe");
        MSIconExtractor.saveExecutableIcon("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe");

        System.exit(0);
    }
}