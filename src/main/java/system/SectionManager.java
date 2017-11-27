package system;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;
import org.json.JSONTokener;
import section.model.Section;
import section.model.SectionType;

import java.io.*;

/**
 * The SectionManager is used to manage sections.
 */
public class SectionManager {
    public static final String SECTION_FOLDER_NAME = "sections";

    public Section getShortcutSection(String appPath) {
        // Get the section file
        File sectionFile = getSectionFile(appPath);

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

        // If the file doesn't exist, fill it with an empty section
        if (!sectionFile.isFile()) {
            // Generate an empty section
            Section emptySection = generateEmptyShortcutSection(appPath);

            // Write the section to the file
            writeSectionToFile(emptySection, sectionFile);
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
}
