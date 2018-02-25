package utils;

import system.ResourceUtils;
import system.WebLinkResolver;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Used to manage the cached high res icon images
 */
public class IconManager {
    // Contains the association between the exe filename and the high res icon file ( in the resource folder )
    // EXAMPLE: Photoshop.exe -> /path_to_res_folder/Photoshop.exe.png
    public Map<String, File> highResIconMap;

    // Contains the association between the domain and the high res icon file
    public Map<String, File> webIconMap;

    public IconManager() {
        loadHighResIconMap();
        loadWebIconMap();
    }

    /**
     * Used to populate the map containing the high res icon files contained in the resources folder
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

    /**
     * Used to populate the web map containing the high res icon files contained in the resources folder
     */
    private void loadWebIconMap() {
        File iconDir = ResourceUtils.getResource("/webicons/");

        webIconMap = new HashMap<>();

        // Cycle through all icons
        for (File icon : iconDir.listFiles()) {
            // Remove the .png suffix
            String website = icon.getName().substring(0, icon.getName().length()-4);
            // Add it to the map
            webIconMap.put(website, icon);
        }
    }

    /**
     * Return the id used to access a web icon.
     * @param url the url to search.
     * @return the string ID.
     */
    public String resolveHighResWebIcon(String url) {
        // Get the base domain
        try {
            URI uri = new URI(url);
            String[] subNames = uri.getHost().split("\\.");
            String name = subNames[subNames.length-2];
            if (webIconMap.containsKey(name)) {
                return name;
            }
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        }

        return null;
    }
}
