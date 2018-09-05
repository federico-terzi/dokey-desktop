package system.image.sources

import system.ResourceUtils
import system.context.ImageSourceContext
import system.image.ImageResolver
import system.image.annotations.RegisterSource
import java.io.File

@RegisterSource(scheme = "asset")
class AssetImageSource(context: ImageSourceContext) : AbstractImageSource(context) {
    override fun resolveFileInternal(id: String): File? {
        val imageFile = ResourceUtils.getResource("/assets/$id.png");
        if (imageFile != null) {
            return imageFile
        }
        return null
    }
}