package app.editor.components;

import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import section.model.Component;
import section.model.Page;
import system.model.ApplicationManager;

public class PageGrid extends ComponentGrid implements ComponentGrid.OnComponentSelectedListener {
    private Page page;

    public PageGrid(ApplicationManager applicationManager, Page page) {
        super(applicationManager, generateMatrix(page));
        this.page = page;
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

    }
}
