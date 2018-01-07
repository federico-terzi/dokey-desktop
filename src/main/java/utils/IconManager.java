package utils;

import system.ResourceUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Used to manage the cached high res icon images
 */
public class IconManager {
    // Contains the association between the exe filename and the high res icon file ( in the resource folder )
    // EXAMPLE: Photoshop.exe -> /path_to_res_folder/Photoshop.exe.png
    public Map<String, File> highResIconMap;

    public IconManager() {
        loadHighResIconMap();
    }

    /**
     * Used to populate the map containing the high res icon files contained in the resources folder
     * @return the map
     */
    private void loadHighResIconMap() {
        File iconDir = ResourceUtils.getResource("/icons/");

        highResIconMap = new HashMap<>();

        // Cycle through all icons
        for (File icon : iconDir.listFiles()) {
            // Remove the .png suffix
            String executableName = icon.getName().substring(0, icon.getName().length()-4);
            // Add it to the map
            highResIconMap.put(executableName, icon);
        }
    }
}
