package app.editor.animations;

import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.scene.control.SplitPane;
import javafx.util.Duration;

public class DividerTransition extends Transition {
    private static final double DURATION = 300;


    private final double initialPosition;
    private SplitPane splitPane;
    private double targetPosition;

    public DividerTransition(SplitPane splitPane, double targetPosition) {
        super();
        setCycleDuration(Duration.millis(DURATION));
        setInterpolator(Interpolator.EASE_OUT);

        this.splitPane = splitPane;
        this.targetPosition = targetPosition;
        this.initialPosition = splitPane.getDividerPositions()[0];
    }

    @Override
    protected void interpolate(double frac) {
        splitPane.setDividerPosition(0, initialPosition+(targetPosition - initialPosition)*frac);
    }
}
