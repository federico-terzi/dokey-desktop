package system.external.photoshop

abstract class PhotoshopEngine {
    // METHODS
    abstract fun executeJavascript(code: String, params: Array<Double>): Boolean
}