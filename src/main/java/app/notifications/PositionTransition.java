package app.notifications;

import javafx.animation.Transition;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Used to animate the position of a stage.
 */
public class PositionTransition extends Transition {
    private Stage stage;
    private double targetX;
    private double targetY;
    private double targetAlpha;

    private double initialX;
    private double initialY;
    private double initialAlpha;

    public PositionTransition(Stage stage, double initialX, double initialY, double initialAlpha, double targetX, double targetY, double targetAlpha) {
        super();
        setCycleDuration(Duration.seconds(0.3));

        this.stage = stage;
        this.initialX = initialX;
        this.initialY = initialY;
        this.initialAlpha = initialAlpha;
        this.targetAlpha = targetAlpha;
        this.targetX = targetX;
        this.targetY = targetY;
    }

    @Override
    protected void interpolate(double frac) {
        stage.setX(initialX + (targetX-initialX)*frac);
        stage.setY(initialY + (targetY-initialY)*frac);
        stage.setOpacity(initialAlpha + (targetAlpha-initialAlpha)*frac);
    }
}
