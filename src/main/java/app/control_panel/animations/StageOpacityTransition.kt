package app.control_panel.animations

import javafx.animation.Interpolator
import javafx.animation.Transition
import javafx.stage.Stage
import javafx.util.Duration

class StageOpacityTransition(val duration: Duration, val stage: Stage) : Transition() {
    var to : Double = 1.0

    private val initial = stage.opacity

    init {
        this.cycleDuration = duration
        this.interpolator = Interpolator.EASE_BOTH
    }

    override fun interpolate(frac: Double) {
        stage.opacity = initial + (to-initial)*frac
    }
}