package system.bookmarks;

import json.JSONObject;
import json.JSONTokener;
import system.CacheManager;
import system.exceptions.UnsupportedOperatingSystemException;
import system.section.importer.SectionWrapper;
import utils.OSValidator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChromeBookmarkImportAgent implements BookmarkImportAgent{
    private File bookmarkFile;

    private List<Bookmark> bookmarks;

    public ChromeBookmarkImportAgent() throws UnsupportedOperatingSystemException {
        // Calculate the correct bookmark file path
        if (OSValidator.isWindows()) {
            bookmarkFile = new File(CacheManager.getInstance().getUserHomeDir(),
                    "AppData/Local/Google/Chrome/User Data/Default/Bookmarks");  //TODO: dynamic, also for other profiles
        }else if (OSValidator.isMac()) {  //TODO: mac

        }else{
            throw new UnsupportedOperatingSystemException("This OS is not valid. YET.");
        }
    }

    @Override
    public String getName() {
        return "CHROME";
    }

    /**
     * This function is used to import bookmarks in the list.
     * @return true if succeeded, false otherwise.
     */
    @Override
    public boolean importBookmarks() {
        // Make sure the bookmarks exist
        if (!bookmarkFile.isFile()) {
            return false;
        }

        bookmarks = new ArrayList<>(200);

        // Load the bookmarks
        try {
            FileInputStream fis = new FileInputStream(bookmarkFile);
            JSONTokener tokener = new JSONTokener(fis);
            JSONObject jsonContent = new JSONObject(tokener);

            // Create the recursive map
            Map<String, Object> jsonMap = jsonContent.toMap();

            // Load the bookmarks recursively
            recourseJSONTree(jsonMap, bookmarks);

            fis.close();

            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Iterate over the json tree to find all bookmarks
     * @param jsonMap the object to parse
     * @param bookmarks the list that will contain the bookmarks
     */
    private void recourseJSONTree(Map<String, Object> jsonMap, List<Bookmark> bookmarks){
        // Check if the current element is a bookmark, if so, creathe the object, add it to the list and return
        if (jsonMap.containsKey("name") && jsonMap.containsKey("url")) {
            Bookmark bookmark = new Bookmark();
            bookmark.title = (String) jsonMap.get("name");
            bookmark.url = (String) jsonMap.get("url");
            bookmarks.add(bookmark);
            return;
        }

        // Iterate over the keys
        for (Object key : jsonMap.values()) {
            // Iterate over the list
            if (key instanceof List) {
                for (Map<String, Object> element : (List<Map<String, Object>>) key)
                recourseJSONTree(element, bookmarks);
            }else if (key instanceof Map) {  // Analyze the current element
                recourseJSONTree((Map<String, Object>) key, bookmarks);
            }
        }
    }

    @Override
    public List<Bookmark> getBookmarks() {
        return bookmarks;
    }
}
