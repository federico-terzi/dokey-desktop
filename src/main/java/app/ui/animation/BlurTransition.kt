package app.ui.animation

import javafx.animation.Transition
import javafx.scene.Node
import javafx.scene.effect.BoxBlur
import javafx.scene.effect.ColorAdjust
import javafx.util.Duration

class BlurTransition(val node: Node, val duration: Duration, val fromBlur: Double, val toBlur: Double,
                     val fromBrightness: Double, val toBrightness: Double) : Transition() {
    private val boxBlur = BoxBlur()
    private val darkenEffect = ColorAdjust()

    init {
        darkenEffect.input = boxBlur
        node.effect = darkenEffect

        boxBlur.width = fromBlur
        boxBlur.height = fromBlur
        boxBlur.iterations = 3

        cycleDuration = duration

    }

    override fun interpolate(frac: Double) {
        if (frac > 0.99 && toBlur < 0.01) {  // When finished, remove the effect
            node.effect = null
        } else {
            boxBlur.width = fromBlur + (toBlur - fromBlur) * frac
            boxBlur.height = fromBlur + (toBlur - fromBlur) * frac
            darkenEffect.brightness = fromBrightness + (toBrightness - fromBrightness) * frac
        }
    }
}