package system.sicons;

import org.apache.commons.lang3.text.WordUtils;
import system.ResourceUtils;

import java.io.File;
import java.util.*;

public class ShortcutIconManager {
    public static final String SICONS_DIR_NAME = "sicons";

    private Map<String, ShortcutIcon> icons = null;

    public ShortcutIconManager() {
        icons = loadIcons();
    }

    private Map<String, ShortcutIcon> loadIcons() {
        Map<String, ShortcutIcon> output = new HashMap<>();
        // Get the icon dir in the resources directory
        File iconDir = ResourceUtils.getResource("/sicons/");

        // Cycle through all the icons
        for (File file : iconDir.listFiles()) {
            String name = WordUtils.capitalize(file.getName().replace('_', ' ').replace(".png", ""));
            String id = file.getName().replace(".png", "");
            ShortcutIcon shortcutIcon = new ShortcutIcon(id, name, file);
            output.put(id, shortcutIcon);
        }

        return output;
    }

    public ShortcutIcon getIcon(String shortcutID) {
        return icons.get(shortcutID);
    }

    public List<ShortcutIcon> getIcons() {
        return new ArrayList<>(icons.values());
    }
}
