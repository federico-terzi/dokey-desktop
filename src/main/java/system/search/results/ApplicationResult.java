package system.search.results;

import javafx.scene.image.Image;
import system.model.Application;
import system.search.SearchEngine;
import utils.ImageResolver;

import java.util.ResourceBundle;

public class ApplicationResult extends AbstractResult {
    // This field is used in the search bar to display the filter label
    // It refers to the resource bundle id
    public static final String SEARCH_FILDER_RESOURCE_ID = "application_category";

    private Application application;

    public ApplicationResult(SearchEngine searchEngine, ResourceBundle resourceBundle, Application application) {
        super(searchEngine, resourceBundle, null);
        this.application = application;
        this.isIcon = false;
    }

    @Override
    public String getTitle() {
        return application.getName();
    }

    @Override
    public String getDescription() {
        return application.getExecutablePath();
    }

    @Override
    public void requestImage(OnImageAvailableListener listener) {
        if (listener != null) {
            new Thread(() -> {
                if (application.getIconPath() != null) {
                    Image appImage = ImageResolver.getInstance().getImage(application.getIconFile(), 32);
                    listener.onImageAvailable(appImage, application.getHashID());
                }
            }).start();
        }
    }

    @Override
    public String getHash() {
        return this.application.getHashID();
    }

    @Override
    public void executeAction() {
        searchEngine.getApplicationManager().openApplication(application.getExecutablePath());
    }
}
