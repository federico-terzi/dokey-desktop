package system.search.results;

import javafx.scene.image.Image;
import system.search.SearchEngine;
import utils.ImageResolver;

import java.math.BigDecimal;
import java.util.ResourceBundle;

public class CalculatorResult extends AbstractResult {
    private String expression;
    private BigDecimal result;

    public CalculatorResult(SearchEngine searchEngine, ResourceBundle resourceBundle, String query, BigDecimal result,
                            Image defaultImage) {
        super(searchEngine, resourceBundle, defaultImage);
        this.expression = query;
        this.result = result;
    }

    @Override
    public String getTitle() {
        return expression + " = "+result;
    }

    @Override
    public String getDescription() {
        return "The result of the expression is: "+result;  // TODO: i18n
    }

    @Override
    public void requestImage(OnImageAvailableListener listener) {
        if (listener != null) {
            new Thread(() -> {
                listener.onImageAvailable(defaultImage, null);
            }).start();
        }
    }

    @Override
    public void executeAction() {

    }
}
