package system.image.sources

import javafx.scene.image.Image
import system.context.ImageSourceContext
import system.image.ImageResolver
import system.image.annotations.RegisterSource

@RegisterSource(scheme = "app")
class ApplicationImageSource(context: ImageSourceContext) : AbstractImageSource(context) {
    override fun resolveImageInternal(id: String, size: Int): Image? {
        val imageFile = context.applicationManager.getApplicationIcon(id)
        if (imageFile != null) {
            return  ImageResolver.getImage(imageFile, size)
        }

        return null
    }
}