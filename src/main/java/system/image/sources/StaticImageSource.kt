package system.image.sources

import system.ResourceUtils
import system.context.ImageSourceContext
import system.image.ImageResolver
import system.image.annotations.RegisterSource
import java.io.File

@RegisterSource(scheme = "static")
class StaticImageSource(context: ImageSourceContext) : AbstractImageSource(context) {
    override fun resolveFileInternal(id: String): File? {
        var identifier : String = id // Initially, if no theme is specified, the identifier is equal to the id

        val imageFile = ResourceUtils.getResource("/sicons/$identifier.png")
        if (imageFile != null) {
            return imageFile
        }
        return null
    }
}