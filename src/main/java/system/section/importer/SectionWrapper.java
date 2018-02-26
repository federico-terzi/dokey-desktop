package system.section.importer;

import json.JSONObject;
import net.model.DeviceInfo;
import section.model.Section;

/**
 * This class represents an imported/exported section, a section with optional parameters.
 */
public class SectionWrapper {
    private Section section;
    private DeviceInfo.OS os;

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    public DeviceInfo.OS getOs() {
        return os;
    }

    public void setOs(DeviceInfo.OS os) {
        this.os = os;
    }

    public JSONObject json() {
        JSONObject jsonObject = section.json();
        jsonObject.put("os", os);
        return jsonObject;
    }

    public static SectionWrapper fromJson(JSONObject jsonContent) {
        Section section = Section.fromJson(jsonContent);

        // Check if the OS is available
        DeviceInfo.OS os = null;
        String osToken = jsonContent.optString("os", null);
        if (osToken != null) {
            os = DeviceInfo.OS.valueOf(osToken);
        }

        // Create the imported section
        SectionWrapper importedSection = new SectionWrapper();
        importedSection.setSection(section);
        importedSection.setOs(os);

        return importedSection;
    }
}
