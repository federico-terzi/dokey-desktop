package system.web

import app.MainApp
import net.sf.image4j.codec.ico.ICODecoder
import org.apache.commons.io.FileUtils
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import system.bookmarks.BookmarkImportAgent.LOG
import java.awt.image.BufferedImage
import java.io.EOFException
import java.io.File
import java.io.IOException
import java.net.*
import java.util.*
import java.util.function.ToIntFunction
import java.util.logging.Logger
import javax.imageio.ImageIO


class WebResolver {
    companion object {
        private val LOG = Logger.getGlobal()

        fun extractTitleFromUrl(url: String) : String {
            // Download and parse the web page
            var doc: Document? = null
            try {
                doc = Jsoup.connect(url).ignoreHttpErrors(false).get()
            } catch (e: HttpStatusException) {
                return url  // Fallback to url
            } catch (e: IOException) {
                return url  // Fallback to url
            }

            // Get the title
            val titleElements = doc!!.getElementsByTag("title")
            if (titleElements.size > 0) {
                return titleElements.get(0).text()
            }

            return url  // Fallback to url
        }

        fun extractImageFromUrl(url : String, searchRecoursively : Boolean = true) : File? {
            // Download and parse the web page
            var doc: Document? = null
            try {
                doc = Jsoup.connect(url).ignoreHttpErrors(false).get()
            } catch (e: HttpStatusException) {  // In case of errors, if searchRecoursively is enabled, search in the base domain
                if (searchRecoursively) {
                    // Get the base domain
                    try {
                        val uri = URI(url)
                        val baseURL = uri.getScheme() + "://" + uri.getHost() + "/"

                        return extractImageFromUrl(baseURL, false)
                    } catch (ex: URISyntaxException) {
                        ex.printStackTrace()
                    }

                }
            } catch (e: IOException) {
                return null
            }

            var resultUrl : String? = null

            // Try to get the icon by looking at the "apple-touch-icon" tag.
            val linkElements = doc!!.getElementsByTag("link")
            for (link in linkElements) {
                if (link.hasAttr("rel") && link.attr("rel").contains("apple-touch-icon")) {
                    resultUrl = link.attr("href")
                    break
                }
            }

            // If not found with the apple-touch-icon, search for the "icon" attribute
            if (resultUrl == null) {
                try {
                    // Cycle through all icon elements and find the one with the biggest size
                    var maxSize = 0
                    var maxUrl: String? = null
                    for (link in linkElements) {
                        if (link.hasAttr("rel") && link.attr("rel").contains("icon")) {
                            if (link.hasAttr("sizes")) {
                                val size = Integer.parseInt(link.attr("sizes").split("x")[0])
                                if (size > maxSize) {
                                    maxUrl = link.attr("href")
                                    maxSize = size
                                }
                            } else {
                                if (link.attr("href").endsWith(".png")) {
                                    resultUrl = link.attr("href")
                                    break
                                }
                            }
                        }
                    }
                    if (maxUrl != null) {
                        resultUrl = maxUrl
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            // Image not found, fallback on the ICO favicon.
            if (resultUrl == null) {
                // Extract the favicon url
                try {
                    val uri = URI(url)
                    val faviconURLString = uri.getScheme() + "://" + uri.getHost() + "/favicon.ico"
                    // Check if it exists
                    val faviconURL = URL(faviconURLString)
                    val huc = faviconURL.openConnection() as HttpURLConnection
                    huc.setRequestMethod("HEAD")
                    if (huc.getResponseCode() === HttpURLConnection.HTTP_OK) {  // The icon exists
                        resultUrl = faviconURLString
                    }
                } catch (e: URISyntaxException) {
                    e.printStackTrace()
                } catch (e: MalformedURLException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }

            // The image couldn't be found, if searchRecoursively is enabled, search in the base domain
            if (resultUrl == null && searchRecoursively) {
                // Get the base domain
                try {
                    val uri = URI(url)
                    val baseURL = uri.getScheme() + "://" + uri.getHost() + "/"
                    val recursiveResult = extractImageFromUrl(baseURL, false)

                    if (recursiveResult != null) {
                        return recursiveResult
                    }
                } catch (e: URISyntaxException) {
                    e.printStackTrace()
                }

            }

            // Download the image as a file
            if (resultUrl != null) {
                // If the url is relative, get the absolute one.
                if (!resultUrl.startsWith("http")) {
                    try {
                        val baseUrl = URL(url)
                        val abs = URL(baseUrl, resultUrl)
                        resultUrl = abs.toURI().toURL().toString()
                    } catch (e: MalformedURLException) {
                        e.printStackTrace()
                    } catch (e: URISyntaxException) {
                        e.printStackTrace()
                    }

                }

                // Download the image
                return downloadImage(resultUrl)
            }

            return null
        }

        private fun downloadImage(imageUrl: String) : File? {
            val imageFile = File.createTempFile("dokey", "webicon.png")

            // Download the image
            try {
                LOG.fine("WLR: saving icon: $imageUrl");
                val image = URL(imageUrl)
                if (!imageUrl.endsWith(".ico")) {  // Not an ICO file, save directly
                    val conn = image.openConnection()
                    conn.setRequestProperty("User-Agent", "Dokey/" + MainApp.DOKEY_VERSION)
                    conn.connect()
                    FileUtils.copyInputStreamToFile(conn.getInputStream(), imageFile)
                    return imageFile
                } else {  // ICO file, must be converted beforehand
                    val tempFile = File.createTempFile("ico", "ico")
                    // Download the icon
                    val conn = image.openConnection()
                    conn.setRequestProperty("User-Agent", "Dokey/" + MainApp.DOKEY_VERSION)
                    conn.connect()
                    FileUtils.copyInputStreamToFile(conn.getInputStream(), tempFile)

                    // CONVERSION
                    val images = ICODecoder.read(tempFile)
                    if (images.size > 0) {
                        // Find the one with the highest resolution
                        val icoImage = images.maxBy { it.height }

                        // Save the image
                        ImageIO.write(icoImage, "png", imageFile)
                    }

                    tempFile.delete()  // Free the resources

                    return imageFile
                }
            } catch (e: MalformedURLException) {
                LOG.fine(e.toString())
            } catch (e: IOException) {
                LOG.fine(e.toString())
            }catch (e: EOFException) {
                LOG.fine(e.toString())
            }

            // Cannot download the image, delete the file
            imageFile.delete()

            return null
        }
    }
}