package app.ui.animation

import javafx.animation.Interpolator
import javafx.animation.Transition
import javafx.scene.control.Control
import javafx.util.Duration

class WidthTransition(val control : Control, val duration: Duration, val finalWidth: Double) : Transition() {
    private val initialWidth = control.width

    init {
        this.cycleDuration = duration
        this.interpolator = Interpolator.EASE_BOTH
    }

    override fun interpolate(frac: Double) {
        control.prefWidth = initialWidth + (finalWidth - initialWidth)*frac
    }
}