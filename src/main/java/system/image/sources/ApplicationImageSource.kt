package system.image.sources

import system.context.ImageSourceContext
import system.image.ImageResolver
import system.image.annotations.RegisterSource
import java.io.File

@RegisterSource(scheme = "app")
class ApplicationImageSource(context: ImageSourceContext) : AbstractImageSource(context) {
    override fun resolveFileInternal(id: String): File? {
        val imageFile = context.applicationManager.getApplicationIcon(id)
        if (imageFile != null) {
            return imageFile
        }

        return null
    }
}