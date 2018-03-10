import app.search.stages.SearchStage;
import com.tulskiy.keymaster.common.HotKey;
import com.tulskiy.keymaster.common.HotKeyListener;
import com.tulskiy.keymaster.common.Provider;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.IOException;

public class Test2Main extends Application{
    private Provider provider;
    private SearchStage stage;

    public static void main(String[] args) throws IOException {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Platform.setImplicitExit(false);

        stage = new SearchStage(null, null);
        stage.show();

        provider = Provider.getCurrentProvider(false);

        provider.register(KeyStroke.getKeyStroke("alt SPACE"), new HotKeyListener() {
            @Override
            public void onHotKey(HotKey hotKey) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (stage == null) {
                            try {
                                stage = new SearchStage(null, null);
                                stage.show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }else{
                            stage.hide();
                            stage = null;
                        }
                    }
                });
            }
        });
    }
}