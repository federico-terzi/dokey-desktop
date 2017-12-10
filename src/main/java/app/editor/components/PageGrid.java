package app.editor.components;

import app.editor.OnSectionModifiedListener;
import section.model.Component;
import section.model.Page;
import section.model.Section;
import system.model.ApplicationManager;

public class PageGrid extends ComponentGrid implements ComponentGrid.OnComponentSelectedListener {
    private Page page;
    private Section section;

    private OnSectionModifiedListener sectionModifiedListener;

    public PageGrid(ApplicationManager applicationManager, Page page, Section section) {
        super(applicationManager, generateMatrix(page));
        this.page = page;
        this.section = section;
        setOnComponentSelectedListener(this);
    }

    private static Component[][] generateMatrix(Page page) {
        // Create the matrix
        Component[][] componentMatrix = new Component[page.getColCount()][page.getRowCount()];

        // Add all the components
        for (Component component : page.getComponents()) {
            // Add the component to the matrix
            componentMatrix[component.getY()][component.getX()] = component;
        }

        return componentMatrix;
    }

    @Override
    public void onNewComponentRequested(Component component) {
        page.addComponent(component);

        // Save the section
        if (sectionModifiedListener != null) {
            sectionModifiedListener.onSectionModified(section);
        }
    }

    public OnSectionModifiedListener getSectionModifiedListener() {
        return sectionModifiedListener;
    }

    public void setSectionModifiedListener(OnSectionModifiedListener sectionModifiedListener) {
        this.sectionModifiedListener = sectionModifiedListener;
    }
}
