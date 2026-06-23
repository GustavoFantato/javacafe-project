package util;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import java.io.File;
import java.net.URL;

public final class ProductImageResolver {

    private static final String DEFAULT_IMAGE_PATH = "images/products/default.jpg";

    private ProductImageResolver() {
    }

    public static Image load(String imagePath, double requestedWidth, double requestedHeight) {
        String resolvedPath = normalizePath(imagePath);
        Image image = tryLoad(resolvedPath, requestedWidth, requestedHeight);
        if (image != null) {
            return image;
        }
        Image fallback = tryLoad(DEFAULT_IMAGE_PATH, requestedWidth, requestedHeight);
        return fallback != null ? fallback : new WritableImage(1, 1);
    }

    public static String defaultImagePath() {
        return DEFAULT_IMAGE_PATH;
    }

    private static String normalizePath(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {
            return DEFAULT_IMAGE_PATH;
        }
        String normalizedPath = imagePath.trim();
        if (normalizedPath.startsWith("/")) {
            return normalizedPath.substring(1);
        }
        return normalizedPath;
    }

    private static Image tryLoad(String imagePath, double requestedWidth, double requestedHeight) {
        try {
            if (imagePath.startsWith("http://") || imagePath.startsWith("https://") || imagePath.startsWith("file:")) {
                return buildImage(imagePath, requestedWidth, requestedHeight);
            }

            URL resourceUrl = ProductImageResolver.class.getResource("/" + imagePath);
            if (resourceUrl != null) {
                return buildImage(resourceUrl.toExternalForm(), requestedWidth, requestedHeight);
            }

            File file = new File(imagePath);
            if (file.exists()) {
                return buildImage(file.toURI().toString(), requestedWidth, requestedHeight);
            }
        } catch (Exception ignored) {
            // Usa a imagem padrao quando o caminho configurado falha.
        }
        return null;
    }

    private static Image buildImage(String source, double requestedWidth, double requestedHeight) {
        Image image = new Image(source, requestedWidth, requestedHeight, true, true, false);
        return image.isError() ? null : image;
    }
}
