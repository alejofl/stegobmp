package ar.edu.itba.cripto.utils;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class FileUtils {
    public static byte[] readBytes(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        byte[] offset = new byte[4];
        for (int i = 0; i < 4; i++) {
            offset[3 - i] = bytes[i + 10];
        }
        int offsetValue = new BigInteger(offset).intValue();
        return Arrays.copyOfRange(bytes, offsetValue, bytes.length);
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
