package app.editor.components;

import app.editor.listeners.OnSectionModifiedListener;
import app.editor.model.ScreenOrientation;
import section.model.Component;
import section.model.Page;
import section.model.Section;
import system.WebLinkResolver;
import system.model.ApplicationManager;
import system.ShortcutIconManager;

public class PageGrid extends ComponentGrid implements ComponentGrid.OnComponentSelectedListener {
    private Page page;
    private Section section;
    private OnSectionModifiedListener sectionModifiedListener;

    public PageGrid(ApplicationManager applicationManager, ShortcutIconManager shortcutIconManager, WebLinkResolver webLinkResolver,
                    Page page, Section section, ScreenOrientation screenOrientation) {
        super(applicationManager, shortcutIconManager, webLinkResolver, generateMatrix(page), screenOrientation);
        this.page = page;
        this.section = section;
        setOnComponentSelectedListener(this);

        if (screenOrientation == ScreenOrientation.PORTRAIT) {
            getStyleClass().add("page-grid-portrait");
        }else{
            getStyleClass().add("page-grid-landscape");
        }

        // Customize the size based on the orientation
        setHeight(SectionGridController.getHeight(screenOrientation));
        setWidth(SectionGridController.getWidth(screenOrientation));
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
        // Add the component to the page
        page.addComponent(component);

        // Save the section
        if (sectionModifiedListener != null) {
            sectionModifiedListener.onSectionModified(section);
        }
    }

    @Override
    public void onDeleteComponentRequested(Component component) {
        // Remove the component from the page
        page.getComponents().remove(component);

        // Save the section
        if (sectionModifiedListener != null) {
            sectionModifiedListener.onSectionModified(section);
        }
    }

    @Override
    public void onEditComponentRequested(Component component) {
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
