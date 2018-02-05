package system;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * This class is used to get the attributes from a given url.
 */
public class WebLinkResolver {
    public static final String WEB_CACHE_DIRNAME = "webcache";

    /**
     * Request the attributes ( title and image url ) for the specified link.
     *
     * @param url the URL to parse.
     * @return the Result if succeeded, null otherwise.
     */
    public static Result getAttributes(String url) {
        // Download and parse the web page
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
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
        }

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
                    // If the url is relative, get the absolute one.
                    if (!maxUrl.startsWith("http")) {
                        try {
                            URL baseUrl = baseUrl = new URL(url);
                            URL abs = new URL(baseUrl, maxUrl);
                            maxUrl = abs.toURI().toURL().toString();
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }

                    result.imageUrl = maxUrl;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Download the image to the cache
        if (result.imageUrl != null) {
            saveImage(result.imageUrl);
        }

        return result;
    }

    /**
     * Get the image file from the cache if present, null otherwise.
     * @param imageUrl the URL of the image.
     * @return the image File if present, null otherwise.
     */
    public static File getImage(String imageUrl) {
        File imageFile = getImageFromCache(imageUrl);
        if (imageFile.isFile()) {
            return imageFile;
        } else {
            return null;
        }
    }

    /**
     * Save the specified image to the web cache.
     *
     * @param imageUrl the URL of the image to save.
     */
    private static void saveImage(String imageUrl) {
        File imageFile = getImageFromCache(imageUrl);

        // If it doesn't already exist, create it
        if (!imageFile.isFile()) {
            // Download the image
            try {
                System.out.println(imageUrl);
                URL image = new URL(imageUrl);
                FileUtils.copyURLToFile(image, imageFile, 3000, 3000);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
