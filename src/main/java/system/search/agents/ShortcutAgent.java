package system.search.agents;

import section.model.Item;
import section.model.Section;
import section.model.ShortcutItem;
import system.model.ApplicationManager;
import system.search.SearchEngine;
import system.search.results.AbstractResult;
import system.search.results.ApplicationResult;
import system.search.results.ShortcutResult;
import system.section.SectionInfoResolver;
import system.section.SectionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ShortcutAgent extends AbstractAgent {
    private ApplicationManager applicationManager;
    private SectionManager sectionManager;
    private SectionInfoResolver sectionInfoResolver;

    public ShortcutAgent(SearchEngine searchEngine, ResourceBundle resourceBundle, ApplicationManager applicationManager,
                         SectionManager sectionManager, SectionInfoResolver sectionInfoResolver) {
        super(searchEngine, resourceBundle);

        this.applicationManager = applicationManager;
        this.sectionManager = sectionManager;
        this.sectionInfoResolver = sectionInfoResolver;
    }

    @Override
    public boolean validate(String query) {
        return true;
    }

    @Override
    public List<? extends AbstractResult> getResults(String query) {
        String lowerCaseQuery = query.toLowerCase();
        List<ShortcutResult> output = new ArrayList<>(30);

        List<Section> sections = sectionManager.getSections();

        int currentSize = 0;
        sectionLoop: for (Section section : sections) {
            for (Item item : sectionManager.getSectionItems(section)) {
                if (item instanceof ShortcutItem && item.getTitle().toLowerCase().contains(lowerCaseQuery)) {
                    ShortcutResult result = new ShortcutResult(searchEngine, resourceBundle, applicationManager,
                            sectionInfoResolver, section, (ShortcutItem) item);
                    output.add(result);
                    currentSize++;
                }

                // Limit the size
                if (currentSize >= MAX_RESULTS_FOR_AGENT) {
                    break sectionLoop;
                }
            }
        }

        return output;
    }
}
