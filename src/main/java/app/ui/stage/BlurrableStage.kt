package app.ui.stage

import javafx.stage.Stage

abstract class BlurrableStage : Stage() {
    abstract val parent : BlurrableStage?
    abstract fun blurIn()
    abstract fun blurOut()
}