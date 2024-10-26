package ar.edu.itba.cripto.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;

public class FileUtils {
    public static SimpleEntry<byte[], byte[]> loadCarrier(String path) throws IOException {
        byte[] bytes = FileUtils.readBytes(path);
        byte[] offset = new byte[4];
        for (int i = 0; i < 4; i++) {
            offset[3 - i] = bytes[i + 10];
        }
        int offsetValue = new BigInteger(offset).intValue();
        byte[] header = Arrays.copyOfRange(bytes, 0, offsetValue);
        byte[] carrier = Arrays.copyOfRange(bytes, offsetValue, bytes.length);
        return new SimpleEntry<>(header, carrier);
    }

    public static byte[] readBytes(String path) throws IOException {
        return Files.readAllBytes(Paths.get(path));
    }

    public static Path createFile(String path) throws IOException {
        Path p = Paths.get(path);
        Files.deleteIfExists(p);
        return Files.createFile(p);
    }

    public static void writeBytes(Path path, byte[] header, byte[] bytes) throws IOException {
        try (OutputStream out = Files.newOutputStream(path)) {
            if (header != null) {
                out.write(header);
            }
            out.write(bytes);
        }
    }

    public static String getExtension(String path) {
        return path.substring(path.lastIndexOf('.'));
    }
}
