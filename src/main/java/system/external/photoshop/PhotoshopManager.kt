package system.external.photoshop

class PhotoshopManager(val engine: PhotoshopEngine) {
    val sliders = mutableMapOf<PhotoshopSliderAction, String>(
            PhotoshopSliderAction.LAYER_OPACITY to "app.activeDocument.activeLayer.opacity=arguments[0];"
    )

    fun moveSlider(sliderId: PhotoshopSliderAction, value: Double) {
        if (sliders.contains(sliderId)) {
            engine.executeJavascript(sliders[sliderId]!!, arrayOf(value))
        }
    }
}