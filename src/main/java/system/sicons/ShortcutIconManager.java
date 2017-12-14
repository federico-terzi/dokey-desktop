package system.sicons;

import org.apache.commons.lang3.text.WordUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShortcutIconManager {
    public static final String SICONS_DIR_NAME = "sicons";

    private Map<String, ShortcutIcon> icons = null;

    public ShortcutIconManager() {
        icons = loadIcons();
    }

    private Map<String, ShortcutIcon> loadIcons() {
        Map<String, ShortcutIcon> output = new HashMap<>();
        // Get the icon dir in the resources directory
        File iconDir = new File(getClass().getResource("/sicons").getFile());

        // Cycle through all the icons
        for (File file : iconDir.listFiles()) {
            String name = WordUtils.capitalize(file.getName().replace('_', ' ').replace(".png", ""));
            ShortcutIcon shortcutIcon = new ShortcutIcon(name, file);
            output.put(file.getName().replace(".png", ""), shortcutIcon);
        }

        return output;
    }

    public ShortcutIcon getIcon(String shortcutID) {
        return icons.get(shortcutID);
    }
}
