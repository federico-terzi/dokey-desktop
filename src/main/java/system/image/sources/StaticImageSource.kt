package system.image.sources

import system.ResourceUtils
import system.context.ImageSourceContext
import system.image.ImageResolver
import system.image.annotations.RegisterSource
import java.io.File

@RegisterSource(scheme = "static")
class StaticImageSource(context: ImageSourceContext) : AbstractImageSource(context) {
    override fun resolveFileInternal(id: String): File? {
        var theme : String = "DARK"  // DARK is the default theme
        var identifier : String = id // Initially, if no theme is specified, the identifier is equal to the id

        // Check if the static image has a theme specified
        if (id.contains(":")) {  // Images id should be in this format -> "identifier:theme" where theme is optional
            val tokens = id.split(":")
            theme = tokens[1]
            identifier = tokens[0]
        }
        val imageFile = ResourceUtils.getResource("/sicons/$theme/$identifier.png");
        if (imageFile != null) {
            return imageFile
        }
        return null
    }
}