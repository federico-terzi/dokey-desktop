package system.image

import javafx.scene.image.Image
import org.reflections.Reflections
import system.context.ImageSourceContext
import system.image.annotations.RegisterSource
import system.image.sources.ImageSource
import java.io.File
import java.io.InputStream
import java.util.*

class ImageResolver(context: ImageSourceContext) {
    val sourceMap = mutableMapOf<String, MetaImageSource>()

    /**
     * A wrapper around image source, used to add useful informations to the class
     */
    class MetaImageSource(imageSource: ImageSource, val useAnotherThread: Boolean) : ImageSource by imageSource

    init {
        // Load all the image sources using reflection
        val reflections = Reflections("system.image.sources")
        val handlers = reflections.getTypesAnnotatedWith(RegisterSource::class.java)
        handlers.forEach {handlerClass ->
            val annotation = handlerClass.getAnnotation(RegisterSource::class.java)
            annotation as RegisterSource
            val imageSource = handlerClass.getConstructor(ImageSourceContext::class.java).newInstance(context) as ImageSource
            val metaImageSource = MetaImageSource(imageSource, annotation.useAnotherThread)
            sourceMap[annotation.scheme] = metaImageSource
        }
    }

    fun resolveImage(imageId: String, size: Int) : Image? {
        // Extract the scheme from the imageId
        val (scheme, id) = extractSchemeAndIdentifier(imageId)
        if (sourceMap.containsKey(scheme)) {
            val imageSource: MetaImageSource = sourceMap[scheme]!!

            val cachedImage = imageSource.resolveImageFromCache(id, size)
            if (cachedImage != null) {
                return cachedImage
            }else{
                return imageSource?.resolveImage(id, size)
            }
        }

        return null  // Not found
    }

    fun resolveImageAsync(imageId: String, size: Int, callback: (Image?, externalThread: Boolean) -> Unit) {
        // Extract the scheme from the imageId
        val (scheme, id) = extractSchemeAndIdentifier(imageId)
        if (sourceMap.containsKey(scheme)) {
            val imageSource: MetaImageSource = sourceMap[scheme]!!

            val cachedImage = imageSource.resolveImageFromCache(id, size)
            if (cachedImage != null) {
                callback(cachedImage, false)
            }else{
                // Check if the call should be executed in another thread or not
                if (imageSource.useAnotherThread) {
                    Thread {
                        val image = imageSource?.resolveImage(id, size)
                        callback(image, true)
                    }.start()
                }else{
                    // Ask the image source to resolve the image
                    callback(imageSource?.resolveImage(id, size), false)
                }
            }
        }
    }

    companion object {
        /**
         * Get an Image object from the given InputStream, adapting to High DPI displays
         */
        fun getImage(stream: InputStream, size: Int): Image {
            // Double the image size to adapt to High DPI displays
            val imageSize = (size*2).toDouble()
            return Image(stream, imageSize, imageSize, true, true)
        }

        /**
         * Get an Image object from the given resource path, adapting to High DPI displays
         */
        fun getImage(resourcePath: String, size: Int): Image {
            return getImage(ImageResolver::class.java.getResourceAsStream(resourcePath), size)
        }

        /**
         * Get an Image object from the given File, adapting to High DPI displays
         */
        fun getImage(imageFile: File, size: Int): Image {
            // Double the image size to adapt to High DPI displays
            val imageSize = (size*2).toDouble()
            return Image(imageFile.toURI().toString(), imageSize, imageSize, true, true)
        }

        fun extractSchemeAndIdentifier(imageId: String) : Pair<String, String> {
            // Extract the scheme from the imageId
            // NOTE: the image id has this format "scheme:id"
            val tokenizer = StringTokenizer(imageId, ":")
            val scheme = tokenizer.nextToken()
            // Remove the scheme prefix and the : separator
            val id = imageId.substring(scheme.length+1)
            return Pair(scheme, id)
        }
    }
}