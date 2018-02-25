package app.editor.listcells;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;
import section.model.Item;

public class ItemListCell extends ListCell<Item> {

    private GridPane grid = new GridPane();
    private Label name = new Label();

    private OnContextMenuListener onContextMenuListener;

    public ItemListCell() {
        configureGrid();
        addControlsToGrid();
    }

    private void configureGrid() {
        grid.setHgap(10);
        grid.setVgap(4);
        grid.setPadding(new Insets(0, 0, 0, 0));
    }
    private void addControlsToGrid() {
        grid.add(name, 0, 0);
    }

    private void clearContent() {
        setText(null);
        setGraphic(null);
    }

    private void addContent(Item item) {
        name.setText(item.getTitle());
        setGraphic(grid);
    }

    @Override
    protected void updateItem(Item item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            clearContent();
        }else{
            addContent(item);
        }
    }

    public void setOnContextMenuListener(OnContextMenuListener onContextMenuListener) {
        this.onContextMenuListener = onContextMenuListener;
    }

    public interface OnContextMenuListener {
        void onDeleteApplication(Item item);
    }
}
