package app.editor.comparators;

import section.model.Section;
import section.model.SectionType;
import system.model.Application;
import system.model.ApplicationManager;

import java.util.Comparator;

public class SectionComparator implements Comparator<Section> {
    private ApplicationManager applicationManager;

    public SectionComparator(ApplicationManager applicationManager) {
        super();
        this.applicationManager = applicationManager;
    }

    @Override
    public int compare(Section o1, Section o2) {
        if (o1.getSectionType() == SectionType.LAUNCHPAD && o2.getSectionType() == SectionType.SHORTCUTS) {
            return -1;
        }else if (o1.getSectionType() == SectionType.SHORTCUTS && o2.getSectionType() == SectionType.LAUNCHPAD) {
            return 1;
        }else{
            Application a1 = applicationManager.getApplication(o1.getRelatedAppId());
            Application a2 = applicationManager.getApplication(o2.getRelatedAppId());
            return a1.getName().toLowerCase().compareTo(a2.getName().toLowerCase());
        }
    }
}
