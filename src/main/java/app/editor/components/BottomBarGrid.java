package app.editor.components;

import app.editor.listeners.OnSectionModifiedListener;
import app.editor.model.ScreenOrientation;
import section.model.Component;
import section.model.Item;
import section.model.Section;
import system.WebLinkResolver;
import system.model.ApplicationManager;
import system.ShortcutIconManager;

import java.util.ArrayList;
import java.util.List;

public class BottomBarGrid extends ComponentGrid implements ComponentGrid.OnComponentSelectedListener {
    private int colCount;
    private Section section;
    private OnSectionModifiedListener sectionModifiedListener;

    public BottomBarGrid(ApplicationManager applicationManager, ShortcutIconManager shortcutIconManager, WebLinkResolver webLinkResolver,
                         int colCount, Section section, ScreenOrientation screenOrientation) {
        super(applicationManager, shortcutIconManager, webLinkResolver, generateMatrix(section.getBottomBarItems(), colCount), screenOrientation);
        this.colCount = colCount;
        this.section = section;

        setOnComponentSelectedListener(this);

        // Customize the size based on the orientation
        if (screenOrientation == ScreenOrientation.PORTRAIT) {
            setHeight(SectionGridController.PORTRAIT_BOTTOM_BAR_HEIGHT);
            setWidth(SectionGridController.PORTRAIT_WIDTH);
            getStyleClass().add("bottombar-portrait");
        }else{
            setHeight(SectionGridController.LANDSCAPE_HEIGHT);
            setWidth(SectionGridController.LANDSCAPE_BOTTOM_BAR_WIDTH);
            getStyleClass().add("bottombar-landscape");
        }
    }

    private static Component[][] generateMatrix(List<Item> items, int colCount) {
        // If there are more items than column count, resize the column count.
        if (items.size() > colCount) {
            colCount = items.size();
        }

        // Create the matrix
        Component[][] componentMatrix = new Component[colCount][1];

        int currentIndex = 0;
        // Add all the items
        for (Item item : items) {
            Component component = new Component();
            component.setItem(item);
            component.setY(currentIndex);
            component.setX(0);

            // Add the component to the matrix
            componentMatrix[currentIndex][0] = component;

            currentIndex++;
        }

        return componentMatrix;
    }

    /**
     * Propagate the items to the section button bar
     */
    private void updateSectionButtonBar() {
        List<Item> newButtonBar = new ArrayList<>();

        // For each element of the component matrix, update the items
        for (int i = 0; i<componentMatrix.length; i++) {
            if (componentMatrix[i][0] != null) {
                newButtonBar.add(componentMatrix[i][0].getItem());
            }
        }

        // Set the new items
        section.setBottomBarItems(newButtonBar);
    }

    @Override
    public void onNewComponentRequested(Component component) {
        // Update the section button bar
        updateSectionButtonBar();

        // Save the section
        if (sectionModifiedListener != null) {
            sectionModifiedListener.onSectionModified(section);
        }
    }

    @Override
    public void onDeleteComponentRequested(Component component) {
        // Update the section button bar
        updateSectionButtonBar();

        // Save the section
        if (sectionModifiedListener != null) {
            sectionModifiedListener.onSectionModified(section);
        }
    }

    @Override
    public void onEditComponentRequested(Component component) {
        // Update the section button bar
        updateSectionButtonBar();

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
