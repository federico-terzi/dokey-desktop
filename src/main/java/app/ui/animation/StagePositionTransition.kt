package app.ui.animation

import javafx.animation.Interpolator
import javafx.animation.Transition
import javafx.stage.Stage
import javafx.util.Duration

class StagePositionTransition(val duration: Duration, val stage: Stage) : Transition() {
    var fromY : Double = stage.y
    var toY : Double = 0.0

    init {
        this.cycleDuration = duration
        this.interpolator = Interpolator.EASE_BOTH
    }

    override fun interpolate(frac: Double) {
        stage.y = fromY + (toY - fromY)*frac
    }
}