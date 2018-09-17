package system.image.model

interface ListableSource {
    fun list() : List<ImageListing>
}