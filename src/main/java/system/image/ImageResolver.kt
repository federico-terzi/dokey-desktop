package system.image

import javafx.animation.FadeTransition
import javafx.application.Platform
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.util.Duration
import org.reflections.Reflections
import system.context.ImageSourceContext
import system.image.annotations.RegisterSource
import system.image.model.ImageListing
import system.image.model.ListableSource
import system.image.sources.ImageSource
import java.io.File
import java.io.InputStream
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor

/**
 * Used to manage and retrieve images.
 * Every image in the system is associated with a scheme and an identifier.
 * Every scheme is associated with an ImageSource, to which the ImageResolver
 * will pass the relative id to find the image.
 */
class ImageResolver(context: ImageSourceContext) {
    val sourceMap = mutableMapOf<String, MetaImageSource>()

    private val executor : ThreadPoolExecutor = Executors.newFixedThreadPool(4) as ThreadPoolExecutor

    private val fallbackImage = ImageResolver.getImage("/assets/image.png", 96)

    /**
     * A wrapper around image source, used to add useful informations to the class
     */
    class MetaImageSource(val imageSource: ImageSource,
                          val listable: Boolean) : ImageSource by imageSource

    init {
        // Load all the image sources using reflection
        val reflections = Reflections("system.image.sources")
        val handlers = reflections.getTypesAnnotatedWith(RegisterSource::class.java)
        handlers.forEach {handlerClass ->
            val annotation = handlerClass.getAnnotation(RegisterSource::class.java)
            annotation as RegisterSource
            val imageSource = handlerClass.getConstructor(ImageSourceContext::class.java).newInstance(context) as ImageSource
            val listable = imageSource is ListableSource
            val metaImageSource = MetaImageSource(imageSource, listable)
            sourceMap[annotation.scheme] = metaImageSource
        }
    }

    /**
     * Return the File associated with the given image id, or null if not found.
     * The call will check the cache first and, if not found, make some potentially
     * expensive calculation to get it.
     */
    fun resolveImageFile(imageId: String) : File? {
        // Extract the scheme from the imageId
        val (scheme, id) = extractSchemeAndIdentifier(imageId)
        if (sourceMap.containsKey(scheme)) {
            val imageSource: MetaImageSource = sourceMap[scheme]!!

            val cachedImageFile = imageSource.resolveFileFromCache(id)
            if (cachedImageFile != null) {
                return cachedImageFile
            }else{
                return imageSource?.resolveFile(id)
            }
        }

        return null  // Not found
    }

    /**
     * Return the Image associated with the given image id, or null if not found.
     * The call will check the cache first and, if not found, make some potentially
     * expensive calculation to get it.
     */
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

    /**
     * Get the Image associated with the given image id, asynchronously.
     * The result will be delivered by the callback(), specifying if the call was
     * made from another thread, and thus requiring to move to the UI thread to use it.
     * The call will check the cache first and, if not found, make some potentially
     * expensive calculation to get it.
     */
    fun resolveImageAsync(imageId: String, size: Int, callback: (Image?, externalThread: Boolean) -> Unit) {
        // Extract the scheme from the imageId
        val (scheme, id) = extractSchemeAndIdentifier(imageId)
        if (sourceMap.containsKey(scheme)) {
            val imageSource: MetaImageSource = sourceMap[scheme]!!

            val cachedImage = imageSource.resolveImageFromCache(id, size)

            if (cachedImage != null) {
                callback(cachedImage, false)
            }else{
                executor.execute {
                    val image = imageSource?.resolveImage(id, size)
                    callback(image, true)
                }
            }
        }
    }

    private val imageViewTargetMap = mutableMapOf<ImageView, String>()

    fun loadInto(imageId: String?, size: Int, imageView: ImageView) {
        // Manage the case of null image id
        if (imageId == null) {
            imageView.image = fallbackImage
            return
        }

        // Setup the image view
        imageView.opacity = 0.0

        synchronized(imageViewTargetMap) {
            imageViewTargetMap[imageView] = imageId
        }

        resolveImageAsync(imageId, size) {resolvedImage, externalThread ->
            var isCorrect = true

            synchronized(imageViewTargetMap) {
                if (imageViewTargetMap[imageView] != imageId) {
                    isCorrect = false
                }else{
                    imageViewTargetMap.remove(imageView)
                }
            }

            val image = resolvedImage ?: fallbackImage

            if (isCorrect) {
                if (externalThread) {
                    Platform.runLater {
                        imageView.image = image

                        val transition = FadeTransition(Duration(100.0), imageView)
                        transition.toValue = 1.0
                        transition.play()
                    }
                }else{
                    imageView.image = image

                    val transition = FadeTransition(Duration(100.0), imageView)
                    transition.toValue = 1.0
                    transition.play()

                }
            }
        }
    }

    /**
     * Return the list of all available images for the Sources that implement the ListableSource interface.
     */
    fun list(scheme: String) : List<ImageListing>? {
        val imageSource = sourceMap[scheme]
        if (imageSource != null && imageSource.listable) {
            imageSource.imageSource as ListableSource
            return  imageSource.imageSource.list()
        }

        return null
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