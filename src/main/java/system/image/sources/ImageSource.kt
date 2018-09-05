package system.image.sources

import javafx.scene.image.Image
import java.io.File

interface ImageSource {
    fun resolveFile(id: String) : File?
    fun resolveFileFromCache(id: String) : File?

    fun resolveImage(id: String, size: Int) : Image?
    fun resolveImageFromCache(id: String, size: Int) : Image?
}