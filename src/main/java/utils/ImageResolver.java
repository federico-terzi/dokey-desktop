package utils;

import javafx.scene.image.Image;

import java.io.File;
import java.io.InputStream;

/**
 * This class is used to get the correct image based on the current screen resolution.
 */
public class ImageResolver {
    public static final int DPI_THRESHOLD = 10; // Always high resolution

    private double dpi = -1;

    private static ImageResolver instance = null;

    public static ImageResolver getInstance() {
        if (instance == null)
            instance = new ImageResolver();

        return instance;
    }

    public Image getImage(InputStream stream, int size) {
        if (dpi < 0)
            throw new DPINotInitializedException("The image DPI must me initialized beforehand");

        // Resize the image if the resolution is high
        int imageSize = size;
        if (dpi > DPI_THRESHOLD)
            imageSize = imageSize*2;

        return new Image(stream, imageSize, imageSize, true, true);
    }

    public Image getImage(File imageFile, int size) {
        if (dpi < 0)
            throw new DPINotInitializedException("The image DPI must me initialized beforehand");

        // Resize the image if the resolution is high
        int imageSize = size;
        if (dpi > DPI_THRESHOLD)
            imageSize = imageSize*2;

        return new Image(imageFile.toURI().toString(), imageSize, imageSize, true, true);
    }

    public double getDpi() {
        return dpi;
    }

    public void setDpi(double dpi) {
        this.dpi = dpi;
    }

    public static class DPINotInitializedException extends RuntimeException {
        public DPINotInitializedException(String message) {
            super(message);
        }
    }
}
