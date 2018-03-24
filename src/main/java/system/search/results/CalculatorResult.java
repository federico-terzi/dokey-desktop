package system.search.results;

import javafx.scene.image.Image;
import system.search.SearchEngine;

import java.math.BigDecimal;
import java.util.ResourceBundle;

public class CalculatorResult extends AbstractResult {
    // This field is used in the search bar to display the filter label
    // It refers to the resource bundle id
    public static final String SEARCH_FILDER_RESOURCE_ID = "calculator_category";

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
        return resourceBundle.getString("result_of_expression") + " " + result;
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
