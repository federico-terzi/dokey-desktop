package system;

import org.apache.commons.codec.digest.DigestUtils;
import json.JSONObject;
import json.JSONTokener;
import section.model.Section;
import section.model.SectionType;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * The SectionManager is used to manage sections.
 */
public class SectionManager {
    public static final String SECTION_FOLDER_NAME = "sections";
    public static final String TEMPLATE_DB_FILENAME = "templates.txt";

    // This map will hold the association between app name and template file
    public Map<String, String> templateMap = new HashMap<>();

    public SectionManager() {
        loadTemplateMap();  // load the template map from the TEMPLATE_DB_FILE
    }

    public Section getShortcutSection(String appPath) {
        // Get the section file
        File sectionFile = getSectionFile(appPath);

        // Read the content
        return getSectionFromFile(sectionFile);
    }

    /**
     * Read a section from the given file
     * @param sectionFile the section File
     * @return the Section read from the file
     */
    private Section getSectionFromFile(File sectionFile) {
        // Read the content
        try {
            JSONTokener tokener = new JSONTokener(new FileInputStream(sectionFile));
            JSONObject jsonContent = new JSONObject(tokener);

            // Create the section by de-serialization
            Section section = Section.fromJson(jsonContent);

            return section;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Return the File of the Section associated with the given appPath.
     * If a section is not available, it generates an empty one and then
     * writes it to the file.
     *
     * @param appPath the application path.
     * @return the File of the Section associated with the application.
     */
    private File getSectionFile(String appPath) {
        // Get the hash of the app path
        String appPathHash = DigestUtils.md5Hex(appPath);

        // Get the section directory
        File userSectionDir = CacheManager.getInstance().getSectionDir();

        // Get the section file
        File sectionFile = new File(userSectionDir, appPathHash + ".json");

        // If the file doesn't exist, check if a template is available
        // if not, fill it with an empty section.
        if (!sectionFile.isFile()) {
            // Check if a template is available
            Section section = getTemplateSectionForApp(appPath);

            // If no template has been found, generate an empty section
            if (section == null) {
                section = generateEmptyShortcutSection(appPath);
            }

            // Write the section to the file
            writeSectionToFile(section, sectionFile);
        }

        return sectionFile;
    }

    /**
     * Generate an empty Shortcut section for the given app.
     *
     * @param appPath the application path.
     * @return a Shortcut Section associated to the given appPath.
     */
    private Section generateEmptyShortcutSection(String appPath) {
        // Create an empty section
        Section section = new Section();
        section.setSectionType(SectionType.SHORTCUTS);
        section.setLastEdit(System.currentTimeMillis());
        section.setRelatedAppId(appPath);

        return section;
    }

    /**
     * Search for a template for the given app path.
     * @param appPath the path of the application. C:\...\prog.exe or /Applications/App.app
     * @return the requested Section if a template was found, null otherwise.
     */
    private Section getTemplateSectionForApp(String appPath) {
        // Extract the filename from the appPath
        File appFile = new File(appPath);
        String appName = appFile.getName();

        // Search in the templateMap
        String templateFilename = templateMap.get(appName);

        // If a template was not found, return null
        if (templateFilename == null) {
            return null;
        }

        // Template was found, read the Section
        File templateFile = new File(getClass().getResource("/sections/"+templateFilename).getPath());

        // Read the section
        Section section = getSectionFromFile(templateFile);

        return section;
    }

    /**
     * Write the given section to the given file
     *
     * @param section the Section to save.
     * @param dest    the destination File.
     * @return true if succeeded, false otherwise.
     */
    private boolean writeSectionToFile(Section section, File dest) {
        try {
            // Write the json section to the file
            FileOutputStream fos = new FileOutputStream(dest);
            PrintWriter pw = new PrintWriter(fos);
            pw.write(section.json().toString());
            pw.close();

            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Load the template association into the map
     */
    private void loadTemplateMap() {
        // Get the template file
        File templateDb = new File(getClass().getResource("/sections/"+TEMPLATE_DB_FILENAME).getPath());

        // Reset the template map
        templateMap = new HashMap<>();

        // Read all the file and populate the map
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(templateDb)));

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                StringTokenizer st = new StringTokenizer(line, "=");
                String appName = st.nextToken();
                String templateFilename = st.nextToken();

                // Add it to the map
                templateMap.put(appName, templateFilename);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
