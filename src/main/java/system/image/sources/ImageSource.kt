package system.image.sources

import javafx.scene.image.Image

interface ImageSource {
    fun resolveImage(id: String, size: Int) : Image?
    fun resolveImageFromCache(id: String, size: Int) : Image?
}