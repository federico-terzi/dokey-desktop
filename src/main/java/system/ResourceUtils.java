package system;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class ResourceUtils {
    /**
     * Get the file associated with the given resource path.
     * @param path path of the file or directory in the resources structure.
     *             For example: /icons/icon.png
     * @return the File associated with the given path, or null if not found.
     */
    public static File getResource(String path) {
        // Get the resource protocol.
        // This may vary if accessing it through JAR or when developing
        String protocol = ResourceUtils.class.getResource(path).getProtocol();

        if (protocol.equals("jar")) {  // JAR Request
            // The logic behind this is to get the path of the JAR file
            // that should be in the same dir as the resources directories.
            // Then the file is returned by loading it from his relative path
            // starting from the base directory of the JAR.
            String filepath = ResourceUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            try {
                // Remove the starting / from the path if needed
                if (path.startsWith("/")) {
                    path.substring(1);
                }
                String decodedPath = URLDecoder.decode(filepath, "UTF-8");
                File jarFile = new File(decodedPath);
                return new File(jarFile.getParentFile(), path);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }else if (protocol.equals("file")) {  // Normal file request
            String filepath = ResourceUtils.class.getResource(path).getFile();
            File file = new File(filepath);
            if (file.isFile() || file.isDirectory()) {
                return file;
            }
        }

        return null;
    }
}
