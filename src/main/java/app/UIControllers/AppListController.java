package app.UIControllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;


public class AppListController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button addExternalAppBtn;

    @FXML
    private ListView<?> appListView;

    @FXML
    private Button selectBtn;

    public Button getAddExternalAppBtn() {
        return addExternalAppBtn;
    }

    public void setAddExternalAppBtn(Button addExternalAppBtn) {
        this.addExternalAppBtn = addExternalAppBtn;
    }

    public ListView<?> getAppListView() {
        return appListView;
    }

    public void setAppListView(ListView<?> appListView) {
        this.appListView = appListView;
    }

    public Button getSelectBtn() {
        return selectBtn;
    }

    public void setSelectBtn(Button selectBtn) {
        this.selectBtn = selectBtn;
    }
}
