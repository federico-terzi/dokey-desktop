import app.notifications.NotificationFactory;
import app.search.stages.SearchStage;
import com.sun.jna.Library;
import com.sun.jna.Native;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class Test2Main extends Application{
    public static void main(String[] args) throws IOException {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        SearchStage stage = new SearchStage(null);
        stage.show();
    }
}