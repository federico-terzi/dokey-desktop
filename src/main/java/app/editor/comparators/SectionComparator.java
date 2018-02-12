package app.editor.comparators;

import section.model.Section;
import section.model.SectionType;
import system.model.Application;
import system.model.ApplicationManager;

import java.util.Comparator;
import java.util.HashMap;

public class SectionComparator implements Comparator<Section> {
    private ApplicationManager applicationManager;

    private HashMap<SectionType, Integer> priorityMap = new HashMap<>();

    public SectionComparator(ApplicationManager applicationManager) {
        super();
        this.applicationManager = applicationManager;

        priorityMap.put(SectionType.LAUNCHPAD, 0);
        priorityMap.put(SectionType.SYSTEM, 1);
        priorityMap.put(SectionType.SHORTCUTS, 2);
    }

    @Override
    public int compare(Section o1, Section o2) {
        if (o1.getSectionType() == SectionType.LAUNCHPAD || o2.getSectionType() == SectionType.LAUNCHPAD ||
                o1.getSectionType() == SectionType.SYSTEM || o2.getSectionType() == SectionType.SYSTEM) {
            return Integer.compare(priorityMap.get(o1.getSectionType()), priorityMap.get(o2.getSectionType()));
        }else{
            Application a1 = applicationManager.getApplication(o1.getRelatedAppId());
            Application a2 = applicationManager.getApplication(o2.getRelatedAppId());
            return a1.getName().toLowerCase().compareTo(a2.getName().toLowerCase());
        }
    }
}
