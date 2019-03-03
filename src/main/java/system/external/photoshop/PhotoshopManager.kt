package system.external.photoshop

class PhotoshopManager(val engine: PhotoshopEngine) {
    fun setLayerOpacity(opacity: Double) {
        engine.executeJavascript("app.activeDocument.activeLayer.opacity=arguments[0];", arrayOf(opacity))
    }
}