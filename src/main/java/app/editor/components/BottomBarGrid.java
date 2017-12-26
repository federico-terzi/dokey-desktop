package app.editor.components;

import app.editor.listeners.OnSectionModifiedListener;
import app.editor.model.ScreenOrientation;
import app.editor.stages.EditorStage;
import section.model.Component;
import section.model.Item;
import section.model.Section;
import system.model.ApplicationManager;
import system.sicons.ShortcutIconManager;

import java.util.List;

public class BottomBarGrid extends ComponentGrid implements ComponentGrid.OnComponentSelectedListener {
    private List<Item> items;
    private int colCount;
    private Section section;
    private OnSectionModifiedListener sectionModifiedListener;

    public BottomBarGrid(ApplicationManager applicationManager, ShortcutIconManager shortcutIconManager, List<Item> items, int colCount, Section section, ScreenOrientation screenOrientation) {
        super(applicationManager, shortcutIconManager, generateMatrix(items, colCount), screenOrientation);
        this.items = items;
        this.colCount = colCount;
        this.section = section;



        setOnComponentSelectedListener(this);
        setForceDiscardSpan(true);  // Discard the span

        // Customize the size based on the orientation
        if (screenOrientation == ScreenOrientation.PORTRAIT) {
            setHeight(EditorStage.PORTRAIT_BOTTOM_BAR_HEIGHT);
            setWidth(EditorStage.PORTRAIT_WIDTH);
            getStyleClass().add("bottombar-portrait");
        }else{
            setHeight(EditorStage.LANDSCAPE_HEIGHT);
            setWidth(EditorStage.LANDSCAPE_BOTTOM_BAR_WIDTH);
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
