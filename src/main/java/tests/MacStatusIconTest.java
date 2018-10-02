package tests;

import com.sun.jna.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class MacStatusIconTest extends Application {
    private static boolean highlighted = false;
    private static JavaMacNativeUI.StatusItemClickCallback callback = new JavaMacNativeUI.StatusItemClickCallback() {
        @Override
        public void invoke() {
            System.out.println("click");
            int high = highlighted ? 0 : 1;
            highlighted = !highlighted;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    JavaMacNativeUI.INSTANCE.setStatusItemHighlighted(high);


                }
            });

        }
    };

    public static void main(String[] args) {
        System.setProperty("jna.library.path", "/Users/freddy/Documents");
        // La soluzione giusta potrebbe essere un metodo "createStatusIcon" che ritorna un puntatore
        // a NSStatusItem a cui, tramite una serie di funzioni di manipolazione che prendono in ingresso
        // il puntatore, vengono impostati tutti i parametri.

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        JavaMacNativeUI.INSTANCE.initializeStatusItem();
        JavaMacNativeUI.INSTANCE.setStatusItemImage("/Users/freddy/Documents/google.png");
        JavaMacNativeUI.INSTANCE.setStatusItemTooltip("Dokey\nConnected");
        JavaMacNativeUI.INSTANCE.setStatusItemAction(callback);
    }

    interface JavaMacNativeUI extends Library
    {
        JavaMacNativeUI INSTANCE = Native.loadLibrary("JavaMacNativeUI", JavaMacNativeUI.class);

        void initializeStatusItem();
        void setStatusItemImage(String imagePath);
        void setStatusItemTooltip(String tooltip);
        void setStatusItemHighlighted(int highlighted);
        void setStatusItemAction(StatusItemClickCallback callback);

        interface StatusItemClickCallback extends Callback {
            void invoke();
        }
    }
}
