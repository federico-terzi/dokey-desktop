package system;

import net.sf.image4j.codec.ico.ICODecoder;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.IconManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class is used to get the attributes from a given url.
 */
public class WebLinkResolver {
    public static final String WEB_CACHE_DIRNAME = "webcache";

    // Create the logger
    private final static Logger LOG = Logger.getGlobal();

    /**
     * Request the attributes ( title and image url ) for the specified link.
     *
     * @param url the URL to parse.
     * @return the Result if succeeded, null otherwise.
     */
    public static Result getAttributes(String url) {
        return getAttributes(url, true);
    }

    /**
     * Request the attributes ( title and image url ) for the specified link.
     *
     * @param url the URL to parse.
     * @param searchRecoursively if true, the algorithm will also search the subdomain for the image
     * @return the Result if succeeded, null otherwise.
     */
    public static Result getAttributes(String url, boolean searchRecoursively) {
        // Download and parse the web page
        Document doc = null;
        try {
            doc = Jsoup.connect(url).ignoreHttpErrors(false).get();
        } catch (HttpStatusException e) {  // In case of errors, if searchRecoursively is enabled, search in the base domain
            if (searchRecoursively) {
                // Get the base domain
                try {
                    URI uri = new URI(url);
                    String baseURL = uri.getScheme()+"://"+uri.getHost()+"/";
                    Result recursiveResult = getAttributes(baseURL, false);

                    return recursiveResult;
                } catch (URISyntaxException ex) {
                    ex.printStackTrace();
                }
            }
        } catch (IOException e) {
            return null;
        }

        // Set the URL of the result
        Result result = new Result();
        result.url = url;

        // Get the title
        Elements titleElements = doc.getElementsByTag("title");
        if (titleElements.size() > 0) {
            result.title = titleElements.get(0).text();
            LOG.fine("WLR: title: "+result.title);
        }

        // Check if a local high res icon is available
        String highResLocalIcon = IconManager.getInstance().resolveHighResWebIcon(url);
        if (highResLocalIcon != null) {
            result.imageUrl = highResLocalIcon;
        }else{
            // Try to get the icon by looking at the "apple-touch-icon" tag.
            Elements linkElements = doc.getElementsByTag("link");
            for (Element link : linkElements) {
                if (link.hasAttr("rel") && link.attr("rel").contains("apple-touch-icon")) {
                    result.imageUrl = link.attr("href");
                    break;
                }
            }

            // If not found with the apple-touch-icon, search for the "icon" attribute
            if (result.imageUrl == null) {
                try {
                    // Cycle through all icon elements and find the one with the biggest size
                    int maxSize = 0;
                    String maxUrl = null;
                    for (Element link : linkElements) {
                        if (link.hasAttr("rel") && link.attr("rel").contains("icon")) {
                            if (link.hasAttr("sizes")) {
                                int size = Integer.parseInt(link.attr("sizes").split("x")[0]);
                                if (size > maxSize) {
                                    maxUrl = link.attr("href");
                                    maxSize = size;
                                }
                            } else {
                                if (link.attr("href").endsWith(".png")) {
                                    result.imageUrl = link.attr("href");
                                    break;
                                }
                            }
                        }
                    }
                    if (maxUrl != null) {
                        result.imageUrl = maxUrl;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Image not found, fallback on the ICO favicon.
            if (result.imageUrl == null) {
                // Extract the favicon url
                try {
                    URI uri = new URI(url);
                    String faviconURLString = uri.getScheme()+"://"+uri.getHost()+"/favicon.ico";
                    // Check if it exists
                    URL faviconURL = new URL(faviconURLString);
                    HttpURLConnection huc =  (HttpURLConnection) faviconURL.openConnection();
                    huc.setRequestMethod("HEAD");
                    if (huc.getResponseCode() == HttpURLConnection.HTTP_OK) {  // The icon exists
                        result.imageUrl = faviconURLString;
                    }
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // The image couldn't be found, if searchRecoursively is enabled, search in the base domain
            if (result.imageUrl == null && searchRecoursively) {
                // Get the base domain
                try {
                    URI uri = new URI(url);
                    String baseURL = uri.getScheme()+"://"+uri.getHost()+"/";
                    Result recursiveResult = getAttributes(url, false);

                    if (recursiveResult.imageUrl != null) {
                        result.imageUrl = recursiveResult.imageUrl;
                    }
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }

            // Download the image to the cache
            if (result.imageUrl != null) {
                // If the url is relative, get the absolute one.
                if (!result.imageUrl.startsWith("http")) {
                    try {
                        URL baseUrl = new URL(url);
                        URL abs = new URL(baseUrl, result.imageUrl);
                        result.imageUrl = abs.toURI().toURL().toString();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }

                requestImage(result.imageUrl);
            }
        }

        return result;
    }

    /**
     * Get the image file from the cache if present, null otherwise.
     * @param imageUrl the URL of the image.
     * @return the image File if present, null otherwise.
     */
    public static File getImage(String imageUrl) {
        if (imageUrl.startsWith("http")) {  // Remote website icon
            File imageFile = getImageFromCache(imageUrl);
            if (imageFile.isFile()) {
                return imageFile;
            }
        }else{  // Local high res image
            return IconManager.getInstance().webIconMap.get(imageUrl);
        }
        return null;
    }

    /**
     * Request and save the specified image to the web cache.
     *
     * @param imageUrl the URL of the image to save.
     * @return true if succeeded, false otherwise.
     */
    public static boolean requestImage(String imageUrl) {
        File imageFile = getImageFromCache(imageUrl);

        // If it doesn't already exist, create it
        if (!imageFile.isFile()) {
            // Download the image
            try {
                LOG.fine("WLR: saving icon: "+imageUrl);
                URL image = new URL(imageUrl);
                if (!imageUrl.endsWith(".ico")) {  // Not an ICO file, save directly
                    FileUtils.copyURLToFile(image, imageFile, 3000, 3000);
                    return true;
                }else{  // ICO file, must be converted beforehand
                    File tempFile = File.createTempFile("ico", "ico");
                    // Download the icon
                    FileUtils.copyURLToFile(image, tempFile, 3000, 3000);

                    // CONVERSION
                    List<BufferedImage> images = ICODecoder.read(tempFile);
                    if (images.size() > 0) {
                        // Find the one with the highest resolution
                        BufferedImage icoImage = images.stream().max(Comparator.comparingInt(BufferedImage::getHeight)).get();

                        // Save the image
                        ImageIO.write(icoImage, "png", imageFile);
                    }

                    tempFile.delete();  // Free the resources

                    return true;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        return true;
    }

    /**
     * Translate the image url in the corresponding hashed file in the cache.
     * @param imageUrl the URL of the image.
     * @return the image File
     */
    private static File getImageFromCache(String imageUrl) {
        // Get the web cache folder
        File webCacheDir = CacheManager.getInstance().getWebCacheDir();

        // Generate the name
        String urlHash = DigestUtils.md5Hex(imageUrl);
        String filename = urlHash + ".png";

        return new File(webCacheDir, filename);
    }

    /**
     * This class will hold the search results.
     */
    public static class Result {
        public String url;
        public String title;
        public String imageUrl;
    }
}
