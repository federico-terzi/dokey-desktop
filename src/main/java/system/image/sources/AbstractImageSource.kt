package system.image.sources

import javafx.scene.image.Image
import system.context.ImageSourceContext
import system.image.ImageResolver
import java.io.File

abstract class AbstractImageSource(val context: ImageSourceContext) : ImageSource {
    // Used as a cache to keep already loaded files
    val fileCache = mutableMapOf<String, File>()

    // Used as a cache to keep previous images in cache
    val imageCache = mutableMapOf<String, Image>()

    override fun resolveFile(id: String): File? {
        val imageFile = resolveFileInternal(id)
        if (imageFile != null) {
            fileCache[id] = imageFile
            return imageFile
        }

        return null
    }

    override fun resolveFileFromCache(id: String): File? {
        if (fileCache.containsKey(id)) {
            return fileCache[id]
        }else{
            return resolveFileFromCacheInternal(id)
        }
    }

    /**
     * Resolve the image corresponding to the given ID.
     * Implement a caching mechanism in memory, to avoid requesting the same image twice.
     */
    override fun resolveImage(id: String, size: Int): Image? {
        // Create a global identifier of the requested image concatenating id and size
        val completeId = "$id$size"

        val imageFile = resolveFile(id)
        if (imageFile != null) {
            val image = ImageResolver.getImage(imageFile, size)
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
            val imageFile = resolveFileFromCacheInternal(id)
            if (imageFile != null) {
                val image = ImageResolver.getImage(imageFile, size)
                imageCache[completeId] = image
                return image
            }
        }

        return null
    }

    /**
     * Must be implemented to the subclasses.
     * Return the file image corresponding to the given id.
     */
    abstract fun resolveFileInternal(id: String): File?

    /**
     * Could be overridden in the subclasses.
     * It's used in those cases where a very expensive operation is necessary
     * to extract the image from the identifier ( such as a URL image ).
     * In those cases, the ImageSource could implement a file-level cache that will
     * be much faster than the slow one, but slower than the memory-level.
     * In those cases, to avoid flickering in the UI, this file-level cache can be used
     * by implementing this method.
     */
    protected open fun resolveFileFromCacheInternal(id: String): File? = null
}