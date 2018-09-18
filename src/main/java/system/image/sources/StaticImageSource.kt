package system.image.sources

import system.ResourceUtils
import system.context.ImageSourceContext
import system.image.ImageResolver
import system.image.annotations.RegisterSource
import system.image.model.ImageListing
import system.image.model.ListableSource
import java.io.File

@RegisterSource(scheme = "static")
class StaticImageSource(context: ImageSourceContext) : AbstractImageSource(context), ListableSource {
    private val listing : List<ImageListing> by lazy {
        val iconDir = ResourceUtils.getResource("/sicons/")
        val output = mutableListOf<ImageListing>()
        for (file in iconDir.listFiles()) {
            val identifier = file.name.removeSuffix(".png")
            val name = identifier.replace("_", " ")
            val id = "static:$identifier"
            output.add(ImageListing(name, id))
        }
        output
    }

    override fun resolveFileInternal(id: String): File? {
        var identifier : String = id // Initially, if no theme is specified, the identifier is equal to the id

        val imageFile = ResourceUtils.getResource("/sicons/$identifier.png")
        if (imageFile != null) {
            return imageFile
        }
        return null
    }

    override fun list(): List<ImageListing> {
        return listing
    }
}