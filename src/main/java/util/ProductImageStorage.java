package util;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;

public final class ProductImageStorage {

    private static final String INTERNAL_PREFIX = "images/products/";
    private static final String RESOURCES_DIR = "src/main/resources/images/products";
    private static final String RUNTIME_DIR = "target/classes/images/products";
    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    private ProductImageStorage() {
    }

    public static boolean isInternalPath(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {
            return false;
        }

        String normalized = imagePath.trim();
        if (normalized.startsWith("file:") || normalized.startsWith("http://") || normalized.startsWith("https://")) {
            return false;
        }

        Path path = Paths.get(normalized);
        if (path.isAbsolute()) {
            return false;
        }

        String withoutLeadingSlash = normalized.startsWith("/") ? normalized.substring(1) : normalized;
        return withoutLeadingSlash.startsWith(INTERNAL_PREFIX);
    }

    public static String importProductImage(Path source, int productId, String productName) throws IOException {
        if (source == null || !Files.isRegularFile(source)) {
            throw new IOException("Arquivo de imagem nao encontrado: " + source);
        }

        String extension = resolveExtension(source);
        String fileName = productId + "-" + slugify(productName) + extension;
        String relativePath = INTERNAL_PREFIX + fileName;

        Path resourcesTarget = Paths.get(System.getProperty("user.dir"), RESOURCES_DIR, fileName);
        Path runtimeTarget = Paths.get(System.getProperty("user.dir"), RUNTIME_DIR, fileName);

        Files.createDirectories(resourcesTarget.getParent());
        Files.createDirectories(runtimeTarget.getParent());

        Files.copy(source, resourcesTarget, StandardCopyOption.REPLACE_EXISTING);
        Files.copy(source, runtimeTarget, StandardCopyOption.REPLACE_EXISTING);

        return relativePath;
    }

    public static Path resolveSourcePath(String imagePath) throws IOException {
        if (imagePath == null || imagePath.isBlank()) {
            throw new IOException("Caminho da imagem nao informado.");
        }

        String normalized = imagePath.trim();

        if (normalized.startsWith("file:")) {
            return Paths.get(URI.create(normalized));
        }

        Path path = Paths.get(normalized);
        if (path.isAbsolute()) {
            return path;
        }

        Path relativeToProject = Paths.get(System.getProperty("user.dir")).resolve(normalized);
        if (Files.isRegularFile(relativeToProject)) {
            return relativeToProject;
        }

        throw new IOException("Arquivo de imagem nao encontrado: " + imagePath);
    }

    private static String resolveExtension(Path source) throws IOException {
        String fileName = source.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            throw new IOException("Extensao de imagem invalida: " + fileName);
        }

        String extension = fileName.substring(dotIndex).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IOException("Extensao nao suportada: " + extension);
        }

        return extension;
    }

    private static String slugify(String productName) {
        String normalized = Normalizer.normalize(productName == null ? "" : productName, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");

        return normalized.isBlank() ? "produto" : normalized;
    }
}
