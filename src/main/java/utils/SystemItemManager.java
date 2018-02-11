package utils;

import section.model.SystemCommands;
import system.ResourceUtils;

import java.io.File;

public class SystemItemManager {
    private static File iconDir = ResourceUtils.getResource("/sysicons/");

    public static File getIconForType(SystemCommands type) {
        File iconFile = new File(iconDir, type.toString()+".png");
        if (iconFile.isFile()) {
            return iconFile;
        }else{
            return null;
        }
    }

    public static SystemCommands[] getCommands() {
        return SystemCommands.values();
    }
}
