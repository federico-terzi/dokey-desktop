package system.image.sources

import javafx.scene.image.Image
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import system.ResourceUtils
import system.context.ImageSourceContext
import system.image.ImageResolver
import system.image.annotations.RegisterSource
import system.web.WebResolver
import java.io.File
import java.net.URI
import java.net.URISyntaxException



@RegisterSource(scheme = "url", useAnotherThread = true)
class UrlImageSource(context: ImageSourceContext) : AbstractImageSource(context) {
    override fun resolveImageInternal(id: String, size: Int): Image? {
        // Calculate the hash of the requested url
        val hash = DigestUtils.md5Hex(id)

        // Check if the image has already been cached
        val cachedFile = File(context.storageManager.webCacheDir, "$hash.png")
        if (cachedFile.isFile) {
            return ImageResolver.getImage(cachedFile, size)
        }

        // Check if the image was already requested before but not found
        // Used to avoid requesting the same image over and over when not available
        if (File(context.storageManager.webCacheDir, "$hash.notfound").isFile) {
            // Falling back to the default
            return ImageResolver.getImage(UrlImageSource::class.java.getResourceAsStream("/assets/world_black.png"), size)
        }

        // Check if an high resolution icon for this website is available
        val highResImage = resolveHighResolutionIcon(id)
        if (highResImage != null) {
            // Copy the found image to the cache file
            FileUtils.copyFile(highResImage, cachedFile)
            return ImageResolver.getImage(cachedFile, size)        }

        // Not in the cache, request it
        val downloadedImageFile = WebResolver.extractImageFromUrl(id)

        if (downloadedImageFile == null) {  // No image found, falling back to the default
            // Create a file that is used in followings request to avoid requesting the image over and over
            // when not available
            File(context.storageManager.webCacheDir, "$hash.notfound").createNewFile()
            return ImageResolver.getImage(UrlImageSource::class.java.getResourceAsStream("/assets/world_black.png"), size)
        }else{ // Image found!
            // Copy the found image to the right file
            FileUtils.copyFile(downloadedImageFile, cachedFile)
            // Delete the temporary download file
            downloadedImageFile.delete()

            // And finally, return the right image
            return ImageResolver.getImage(cachedFile, size)
        }

        return null
    }

    private fun resolveHighResolutionIcon(url: String) : File? {
        try {
            val uri = URI(url)
            val subNames = uri.getHost().split(".")
            val name = subNames[subNames.size - 2]
            return ResourceUtils.getResource("/webicons/$name.png")
        } catch (ex: URISyntaxException) {
            ex.printStackTrace()
        }

        return null
    }
}