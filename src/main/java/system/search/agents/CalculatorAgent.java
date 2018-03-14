package system.search.agents;

import com.udojava.evalex.Expression;
import system.model.ApplicationManager;
import system.search.SearchEngine;
import system.search.results.AbstractResult;
import system.search.results.CalculatorResult;
import utils.ImageResolver;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class CalculatorAgent extends AbstractAgent {
    public CalculatorAgent(SearchEngine searchEngine, ResourceBundle resourceBundle) {
        super(searchEngine, resourceBundle);

        this.defaultImage = ImageResolver.getInstance().getImage(CalculatorAgent.class.getResourceAsStream("/assets/light.png"), 32);

    }

    @Override
    public boolean validate(String query) {
        return query.matches(".*\\d+.*");  // Check if query contains numbers
    }

    @Override
    public List<? extends AbstractResult> getResults(String query) {
        ArrayList<CalculatorResult> results = new ArrayList<>(1);

        // Evaluate the expression
        Expression expression = new Expression(query);
        try {
            BigDecimal expressionResult = expression.eval();
            CalculatorResult result = new CalculatorResult(searchEngine, resourceBundle, query, expressionResult, defaultImage);
            results.add(result);
        }catch (Exception e) {}
        return results;
    }
}
