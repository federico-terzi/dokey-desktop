package app.notifications;

import javafx.animation.Transition;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Used to delay a transition
 */
public class DelayTransition extends Transition {
    public DelayTransition(double delay) {
        super();
        setDelay(Duration.seconds(delay));
    }

    @Override
    protected void interpolate(double frac) {
    }
}
