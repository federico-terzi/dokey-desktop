package app.editor.controllers;

import app.editor.listcells.ItemListCell;
import app.editor.listcells.SystemListCell;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import section.model.Item;
import section.model.SystemCommands;

public class ImportDialogController {
    public ImageView targetImageView;
    public Label sectionTitleLabel;
    public Label sectionDescriptionLabel;
    public Button changeTargetBtn;
    public ListView invalidItemsListView;
    public CheckBox avoidInvalidItemsCheckBox;
    public CheckBox compatibilityModeCheckBox;
    public ProgressBar progressBar;
    public Button cancelBtn;
    public Button importBtn;
    public TitledPane optionsPanel;
    public TitledPane invalidItemsPanel;
    public TitledPane targetPanel;

    public void initialize() {
        // Default: avoid invalid items true
        avoidInvalidItemsCheckBox.setSelected(true);

        // Initially show only the progress bar
        optionsPanel.setManaged(false);
        invalidItemsPanel.setManaged(false);
        targetPanel.setManaged(false);

        // Disable the change btn
        changeTargetBtn.setDisable(true);

        // Setup the listview cell factory to display items
        invalidItemsListView.setCellFactory(new Callback<ListView<Item>, ListCell<Item>>() {
            @Override
            public ListCell<Item> call(ListView<Item> param) {
                return new ItemListCell();
            }
        });
    }
}
