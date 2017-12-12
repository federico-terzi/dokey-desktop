package app.editor.components;

import app.editor.listeners.OnSectionModifiedListener;
import section.model.Component;
import section.model.Item;
import section.model.Section;
import system.model.ApplicationManager;

import java.util.List;

public class BottomBarGrid extends ComponentGrid implements ComponentGrid.OnComponentSelectedListener {
    private List<Item> items;
    private int colCount;
    private Section section;
    private OnSectionModifiedListener sectionModifiedListener;

    public BottomBarGrid(ApplicationManager applicationManager, List<Item> items, int colCount, Section section) {
        super(applicationManager, generateMatrix(items, colCount));
        this.items = items;
        this.colCount = colCount;
        this.section = section;

        getStyleClass().add("bottombar");

        setOnComponentSelectedListener(this);
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
            component.setXSpan(1);
            component.setYSpan(1);

            // Add the component to the matrix
            componentMatrix[currentIndex][0] = component;

            currentIndex++;
        }

        return componentMatrix;
    }

    @Override
    public void onNewComponentRequested(Component component) {
        // Add the item
        if (items.size() > component.getY()) {
            items.add(component.getY(), component.getItem());
        }else{
            items.add(component.getItem());
        }

        // Save the section
        if (sectionModifiedListener != null) {
            sectionModifiedListener.onSectionModified(section);
        }
    }

    @Override
    public void onDeleteComponentRequested(Component component) {
        // Remove the item
        items.remove(component.getItem());

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
