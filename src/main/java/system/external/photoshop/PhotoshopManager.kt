package system.external.photoshop

class PhotoshopManager(val engine: PhotoshopEngine) {
    val sliders = mutableMapOf<String, String>(
            "opacity" to "app.activeDocument.activeLayer.opacity=arguments[0];"
    )

    fun moveSlider(sliderId: String, value: Double) {
        if (sliders.contains(sliderId)) {
            engine.executeJavascript(sliders[sliderId]!!, arrayOf(value))
        }
    }
}