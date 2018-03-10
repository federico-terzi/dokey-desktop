package system.search.results;

import javafx.scene.image.Image;
import system.model.Application;

import java.io.File;

public class ApplicationResult extends AbstractResult {
    private Application application;

    public ApplicationResult(Application application) {
        this.application = application;
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
                    Image appImage = new Image(new File(application.getIconPath()).toURI().toString(), 32, 32, true, true);
                    listener.onImageAvailable(appImage);
                }
            }).start();
        }
    }
}
