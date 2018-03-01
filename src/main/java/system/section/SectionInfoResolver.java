package system.section;

import javafx.scene.image.Image;
import section.model.Section;
import section.model.SectionType;
import system.model.Application;
import system.model.ApplicationManager;

import java.io.File;
import java.util.ResourceBundle;

/**
 * This class is used to get a representation of the given section
 */
public class SectionInfoResolver {
    private ApplicationManager applicationManager;
    private ResourceBundle resourceBundle;

    public SectionInfoResolver(ApplicationManager applicationManager, ResourceBundle resourceBundle) {
        this.applicationManager = applicationManager;
        this.resourceBundle = resourceBundle;
    }

    /**
     * @param section     the Section to analyze.
     * @param application if the section has type SHORTCUTS, use this application for the info. Can be null.
     * @return the SectionInfo associated with the given section.
     */
    public SectionInfo getSectionInfo(Section section, int imageSize, Application application) {
        SectionInfo result = new SectionInfo();

        switch (section.getSectionType()) {
            case SHORTCUTS:
                // If the application is not specified, get it from the application manager
                if (application == null) {
                    application = applicationManager.getApplication(section.getRelatedAppId());
                }

                if (application != null && application.getIconPath() != null) {
                    result.image = new Image(new File(application.getIconPath()).toURI().toString(), imageSize, imageSize, true, true);
                    result.name = application.getName();
                    result.description = application.getExecutablePath();
                }
                break;
            case LAUNCHPAD:
                result.image = new Image(SectionInfoResolver.class.getResourceAsStream("/assets/apps.png"), imageSize, imageSize, true, true);
                result.name = resourceBundle.getString("launchpad");
                result.description = resourceBundle.getString("launchpad_desc");
                break;
            case SYSTEM:
                result.image = new Image(SectionInfoResolver.class.getResourceAsStream("/assets/shutdown.png"), imageSize, imageSize, true, true);
                result.name = resourceBundle.getString("system");
                result.description = resourceBundle.getString("system_desc");
                break;
        }

        return result;
    }

    /**
     * @param section     the Section to analyze.
     * @return the SectionInfo associated with the given section.
     */
    public SectionInfo getSectionInfo(Section section) {
        return getSectionInfo(section, 32, null);
    }

    public static class SectionInfo {
        public String name;
        public String description;
        public Image image;
    }
}
