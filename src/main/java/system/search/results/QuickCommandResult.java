package system.search.results;

import javafx.scene.image.Image;
import system.model.ApplicationManager;
import system.quick_commands.QuickCommand;
import system.quick_commands.model.DependencyResolver;
import system.search.SearchEngine;

import java.util.ResourceBundle;

public class QuickCommandResult extends AbstractResult {
    private QuickCommand quickCommand;
    private DependencyResolver dependencyResolver;

    public QuickCommandResult(SearchEngine searchEngine, ResourceBundle resourceBundle, QuickCommand quickCommand,
                              DependencyResolver dependencyResolver, Image defaultImage) {
        super(searchEngine, resourceBundle, defaultImage);
        this.quickCommand = quickCommand;
        this.dependencyResolver = dependencyResolver;
    }

    @Override
    public String getTitle() {
        return quickCommand.getCommand();
    }

    @Override
    public String getDescription() {
        return quickCommand.getAction().getDisplayText(dependencyResolver, resourceBundle);
    }

    @Override
    public void requestImage(OnImageAvailableListener listener) {
        if (listener != null) {
            new Thread(() -> {
                new Thread(() -> {
                    listener.onImageAvailable(defaultImage, null);
                }).start();
            }).start();
        }
    }

    @Override
    public void executeAction() {
        quickCommand.getAction().executeAction(dependencyResolver);
    }
}
