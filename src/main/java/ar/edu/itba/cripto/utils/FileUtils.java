package ar.edu.itba.cripto.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {
    public static byte[] readBytes(String path) throws IOException {
        return Files.readAllBytes(Paths.get(path));
    }

    public static Path createFile(String path) throws IOException {
        Path p = Paths.get(path);
        Files.deleteIfExists(p);
        return Files.createFile(p);
    }

    public static void writeBytes(Path path, byte[] bytes) throws IOException {
        Files.write(path, bytes);
    }

    public static String getExtension(String path) {
        return path.substring(path.lastIndexOf('.'));
    }
}
