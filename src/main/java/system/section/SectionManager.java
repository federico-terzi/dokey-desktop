package system.section;

import org.apache.commons.codec.digest.DigestUtils;
import json.JSONObject;
import json.JSONTokener;
import org.apache.commons.io.FileUtils;
import section.model.*;
import system.CacheManager;
import system.ResourceUtils;
import system.section.importer.SectionWrapper;

import java.io.*;
import java.util.*;

/**
 * The SectionManager is used to manage sections.
 */
public class SectionManager {
    public static final String SECTION_FOLDER_NAME = "sections";
    public static final String TEMPLATE_DB_FILENAME = "templates.txt";

    public static final int DEFAULT_PAGE_ROWS = 4;
    public static final int DEFAULT_PAGE_COLS = 4;

    // This map will hold the association between app name and template file
    public Map<String, String> templateMap = new HashMap<>();

    public SectionManager() {
        loadTemplateMap();  // load the template map from the TEMPLATE_DB_FILE
    }

    /**
     * Get the list of all user Section(s).
     * @return the list of all user sections.
     */
    public List<Section> getSections() {
        List<Section> output = new ArrayList<>();
        // Get the section directory
        File userSectionDir = CacheManager.getInstance().getSectionDir();

        // Go through all user section files
        for (File sectionFile : userSectionDir.listFiles()) {
            // Get the section from the file
            Section currentSection = getSectionFromFile(sectionFile);
            output.add(currentSection);
        }

        return output;
    }

    public Section getShortcutSection(String appPath) {
        // Get the section file
        File sectionFile = getAppSectionFile(appPath);

        // Read the content
        return getSectionFromFile(sectionFile);
    }

    public Section getLaunchpadSection() {
        // Get the section file
        File sectionFile = getLaunchpadSectionFile();

        // Read the content
        return getSectionFromFile(sectionFile);
    }

    public Section getSystemSection() {
        // Get the section file
        File sectionFile = getSystemSectionFile();

        // Read the content
        return getSectionFromFile(sectionFile);
    }

    public synchronized boolean saveSection(Section section) {
        File sectionFile = null;

        // Get the appropriate destination file
        if (section.getSectionType() == SectionType.SHORTCUTS) {
            sectionFile = getAppSectionFile(section.getRelatedAppId());
        }else if (section.getSectionType() == SectionType.LAUNCHPAD) {
            sectionFile = getLaunchpadSectionFile();
        }else{
            return false;
        }

        // Update the last edit
        section.setLastEdit(System.currentTimeMillis());

        // Save the section
        return writeSectionToFile(section, sectionFile);
    }

    public synchronized boolean deleteSection(Section section) {
        File sectionFile = null;

        // Get the appropriate destination file and delete it
        if (section.getSectionType() == SectionType.SHORTCUTS) {
            sectionFile = getAppSectionFile(section.getRelatedAppId());
            sectionFile.delete();
            return true;
        }

        return false;
    }

    /**
     * Read a section from the given file
     * @param sectionFile the section File
     * @return the Section read from the file
     */
    public Section getSectionFromFile(File sectionFile) {
        // Read the content
        try {
            FileInputStream fis = new FileInputStream(sectionFile);
            JSONTokener tokener = new JSONTokener(fis);
            JSONObject jsonContent = new JSONObject(tokener);

            // Create the section by de-serialization
            Section section = Section.fromJson(jsonContent);

            fis.close();

            return section;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Read a section from the given file with the optional import parameters.
     * @param sectionFile the section File
     * @return the SectionWrapper read from the file
     */
    public SectionWrapper importSectionFromFile(File sectionFile) {
        // Read the content
        try {
            FileInputStream fis = new FileInputStream(sectionFile);
            JSONTokener tokener = new JSONTokener(fis);
            JSONObject jsonContent = new JSONObject(tokener);

            // Create the section by de-serialization
            SectionWrapper importedSection = SectionWrapper.fromJson(jsonContent);

            fis.close();

            return importedSection;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Return the File of the Section associated with the launchpad.
     * If a section is not available, it generates an empty one and then
     * writes it to the file.
     *
     * @return the File of the Section associated with the launchpad.
     */
    private File getLaunchpadSectionFile() {
        // Get the section directory
        File userSectionDir = CacheManager.getInstance().getSectionDir();

        // Get the section file
        File sectionFile = new File(userSectionDir, "launchpad.json");

        // If the file doesn't exist fill it with an empty section.
        if (!sectionFile.isFile()) {
            // Check if a template is available
            Section section = generateEmptyLaunchpadSection();

            // Write the section to the file
            writeSectionToFile(section, sectionFile);
        }

        return sectionFile;
    }

    /**
     * Return the File of the Section associated with the system.
     * If a section is not available, it copies the default one and then
     * writes it to the file.
     *
     * @return the File of the Section associated with the launchpad.
     */
    private File getSystemSectionFile() {
        // Get the section directory
        File userSectionDir = CacheManager.getInstance().getSectionDir();

        // Get the section file
        File sectionFile = new File(userSectionDir, "system.json");

        // If the file doesn't exist fill it with an empty section.
        if (!sectionFile.isFile()) {
            // Load the default one
            File defaultSection = ResourceUtils.getResource("/sections/system.json");

            // Copy the default one
            if (defaultSection != null) {
                try {
                    FileUtils.copyFile(defaultSection, sectionFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sectionFile;
    }

    /**
     * Return the File of the Section associated with the given appPath.
     * If a section is not available, it generates an empty one and then
     * writes it to the file.
     *
     * @param appPath the application path.
     * @return the File of the Section associated with the application.
     */
    private File getAppSectionFile(String appPath) {
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

        // Add an empty page
        Page firstPage = new Page();
        firstPage.setTitle("Page 1");
        firstPage.setColCount(DEFAULT_PAGE_COLS);
        firstPage.setRowCount(DEFAULT_PAGE_ROWS);
        section.addPage(firstPage);

        return section;
    }

    /**
     * Generate an empty section for the launchpad
     *
     * @return a Launchpad Section.
     */
    private Section generateEmptyLaunchpadSection() {
        // Create an empty section
        Section section = new Section();
        section.setSectionType(SectionType.LAUNCHPAD);
        section.setLastEdit(System.currentTimeMillis());

        Page firstPage = new Page();
        firstPage.setTitle("Launchpad");
        firstPage.setColCount(4);
        firstPage.setRowCount(4);

        section.addPage(firstPage);
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
        File templateFile = ResourceUtils.getResource("/sections/"+templateFilename);

        // Read the section
        Section section = getSectionFromFile(templateFile);

        // Replace the relatedAppID with the correct path and the correct modified id
        if (section != null) {
            section.setRelatedAppId(appPath);
            section.setLastEdit(System.currentTimeMillis());
        }

        return section;
    }

    /**
     * Write the given section to the given file
     *
     * @param section the Section to save.
     * @param dest    the destination File.
     * @return true if succeeded, false otherwise.
     */
    public synchronized boolean writeSectionToFile(Section section, File dest) {
        try {
            // Write the json section to the file
            FileOutputStream fos = new FileOutputStream(dest);
            PrintWriter pw = new PrintWriter(fos);
            JSONObject sectionJson = section.json();  // Get the section json
            pw.write(sectionJson.toString());
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
     * Write the given sectionWrapper to the given file.
     *
     * @param sectionWrapper the SectionWrapper to save.
     * @param dest    the destination File.
     * @return true if succeeded, false otherwise.
     */
    public synchronized boolean exportSectionToFile(SectionWrapper sectionWrapper, File dest) {
        try {
            // Write the json sectionWrapper to the file
            FileOutputStream fos = new FileOutputStream(dest);
            PrintWriter pw = new PrintWriter(fos);
            JSONObject sectionJson = sectionWrapper.json();
            pw.write(sectionJson.toString());
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
        File templateDb = ResourceUtils.getResource("/sections/"+TEMPLATE_DB_FILENAME);

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
