package system.sicons;

import net.model.IconTheme;
import org.apache.commons.lang3.text.WordUtils;
import system.ResourceUtils;

import java.io.File;
import java.util.*;

public class ShortcutIconManager {
    public static final String SICONS_DIR_NAME = "sicons";

    private Map<String, ShortcutIcon> icons = null;

    public ShortcutIconManager() {
        icons = new HashMap<>();
        loadIcons(IconTheme.LIGHT);
        loadIcons(IconTheme.DARK);
    }

    private Map<String, ShortcutIcon> loadIcons(IconTheme theme) {
        // Get the icon dir in the resources directory
        File iconDir = ResourceUtils.getResource("/sicons/"+theme.name()+"/");

        // Cycle through all the icons
        for (File file : iconDir.listFiles()) {
            String name = WordUtils.capitalize(file.getName().replace('_', ' ').replace(".png", ""));
            String id = file.getName().replace(".png", "");
            ShortcutIcon shortcutIcon = new ShortcutIcon(id, name, file);
            icons.put(theme.name()+":"+id, shortcutIcon);
        }

        return icons;
    }

    public ShortcutIcon getIcon(String shortcutID, IconTheme theme) {
        return icons.get(theme.name() + ":" + shortcutID);
    }

    public List<ShortcutIcon> getIcons() {
        return new ArrayList<>(icons.values());
    }
}
