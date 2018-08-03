package system.image.sources

import javafx.scene.image.Image
import system.context.ImageSourceContext

abstract class AbstractImageSource(val context: ImageSourceContext) : ImageSource {
    // Used as a cache to keep previous images in cache
    val imageCache = mutableMapOf<String, Image>()

    /**
     * Resolve the image corresponding to the given ID.
     * Implement a caching mechanism in memory, to avoid requesting the same image twice.
     */
    override fun resolveImage(id: String, size: Int): Image? {
        // Create a global identifier of the requested image concatenating id and size
        val completeId = "$id$size"

        val image = resolveImageInternal(id, size)
        if (image != null) {
            imageCache[completeId] = image
            return image
        }

        return null
    }

    override fun resolveImageFromCache(id: String, size: Int): Image? {
        // Create a global identifier of the requested image concatenating id and size
        val completeId = "$id$size"

        if (imageCache.containsKey(completeId)) {
            return imageCache[completeId]
        }else{
            return resolveImageFromCacheInternal(id, size)
        }
    }

    /**
     * Must be implemented to the subclasses.
     * Return the image corresponding to the given id.
     */
    abstract fun resolveImageInternal(id: String, size: Int) : Image?

    protected open fun resolveImageFromCacheInternal(id: String, size: Int) : Image? = null
}