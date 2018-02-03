package app.editor.components;

import app.editor.stages.ShortcutDialogStage;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import section.model.Component;
import section.model.FolderItem;
import section.model.ShortcutItem;
import system.sicons.ShortcutIcon;
import system.sicons.ShortcutIconManager;

import java.io.File;
import java.io.IOException;

public class FolderButton extends ComponentButton {
    private FolderItem item;

    public FolderButton(Component component) {
        super(component);

        item = (FolderItem) associatedComponent.getItem();

        // Set up the layout
        setText(item.getTitle());

        Image folderImage = new Image(ComponentButton.class.getResourceAsStream("/assets/folder.png"));
        ImageView imageView = new ImageView(folderImage);
        imageView.setSmooth(true);
        imageView.setFitHeight(32);
        imageView.setFitWidth(32);
        setContentDisplay(ContentDisplay.TOP);
        setGraphic(imageView);

        // Create the tooltip
        final Tooltip tooltip = new Tooltip();
        tooltip.setText(item.getTitle()+"\n"+item.getPath());
        setTooltip(tooltip);
    }

    @Override
    public void showEditDialog() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose the Folder");
        File selectedDirectory = chooser.showDialog(null);

        // Create the component
        item.setPath(selectedDirectory.getAbsolutePath());
        item.setTitle(selectedDirectory.getName());

        // Notify the edit
        if (getOnComponentActionListener() != null) {
            getOnComponentActionListener().onComponentEdit();
        }
    }
}
