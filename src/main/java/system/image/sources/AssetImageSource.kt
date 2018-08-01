package system.image.sources

import javafx.scene.image.Image
import system.ResourceUtils
import system.context.ImageSourceContext
import system.image.ImageResolver
import system.image.annotations.RegisterSource

@RegisterSource(scheme = "asset")
class AssetImageSource(context: ImageSourceContext) : AbstractImageSource(context) {
    override fun resolveImageInternal(id: String, size: Int): Image? {
        val imageFile = ResourceUtils.getResource("/assets/$id.png");
        if (imageFile != null) {
            return ImageResolver.getImage(imageFile, size)
        }
        return null
    }
}