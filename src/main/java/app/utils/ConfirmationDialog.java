package app.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * This utility speed up the creation of a confirmation dialog
 */
public class ConfirmationDialog extends Alert {
    public ConfirmationDialog(String title, String headerText) {
        super(AlertType.CONFIRMATION);

        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(ConfirmationDialog.class.getResourceAsStream("/assets/icon.png")));
        setTitle(title);
        setHeaderText(headerText);
    }

    /**
     * Display the Dialog and trigger the callback when clicked
     * @param onConfirmationResponseListener
     */
    public void display(OnConfirmationResponseListener onConfirmationResponseListener) {
        Optional<ButtonType> result = showAndWait();

        if (onConfirmationResponseListener != null) {
            if (result.get() == ButtonType.OK) {
                onConfirmationResponseListener.onSuccess();
                return;
            }

            onConfirmationResponseListener.onCancel();
            return;
        }
    }

    public interface OnConfirmationResponseListener {
        void onSuccess();
        void onCancel();
    }

    /**
     * Display the dialog and trigger the callback when the OK button is clicked.
     * @param listener that will handle the callback
     */
    public void success(OnConfirmationSuccessListener listener) {
        Optional<ButtonType> result = showAndWait();

        if (listener != null && result.get() == ButtonType.OK) {
            listener.onSuccess();
        }
    }

    public interface OnConfirmationSuccessListener {
        void onSuccess();
    }
}
