package ar.edu.itba.cripto;

import ar.edu.itba.cripto.encryption.Cryptography;
import ar.edu.itba.cripto.encryption.EncryptionAlgorithm;
import ar.edu.itba.cripto.encryption.EncryptionMode;
import ar.edu.itba.cripto.steganography.SteganographyAlgorithm;
import ar.edu.itba.cripto.steganography.SteganographyMethod;
import ar.edu.itba.cripto.utils.FileUtils;
import ar.edu.itba.cripto.utils.StegobmpCLI;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import java.nio.file.Path;
import java.util.AbstractMap.SimpleEntry;

public class Main {
    public static void main(String[] args) {
        StegobmpCLI cli = new StegobmpCLI();
        try {
            CommandLine parsed = cli.parse(args);

            SteganographyAlgorithm steganographyAlgorithm = SteganographyAlgorithm.valueOf(parsed.getOptionValue("steg"));
            EncryptionAlgorithm encryptionAlgorithm = EncryptionAlgorithm.valueOf(parsed.getOptionValue("a", "AES_128"));
            EncryptionMode encryptionMode = EncryptionMode.valueOf(parsed.getOptionValue("m", "CBC"));
            String password = parsed.hasOption("pass") ? parsed.getOptionValue("pass") : null;
            if (password == null) {
                encryptionAlgorithm = EncryptionAlgorithm.NONE;
            }
            SimpleEntry<byte[], byte[]> bmp = FileUtils.loadCarrier(parsed.getOptionValue("p"));
            byte[] header = bmp.getKey();
            byte[] carrier = bmp.getValue();

            Cryptography cryptography = new Cryptography(encryptionAlgorithm, encryptionMode, password);
            SteganographyMethod steganographyMethod = steganographyAlgorithm.getInstance();

            if (parsed.hasOption("embed") && parsed.hasOption("in")) {
                byte[] payload = FileUtils.readBytes(parsed.getOptionValue("in"));
                String payloadExtension = FileUtils.getExtension(parsed.getOptionValue("in"));
                byte[] data = steganographyMethod.preprocessEmbedding(payload, payloadExtension, cryptography);
                if (!steganographyMethod.canEmbed(carrier, data)) {
                    throw new IllegalArgumentException("Carrier is too small to embed the payload");
                }
                byte[] result = steganographyMethod.embed(carrier, data);
                Path output = FileUtils.createFile(parsed.getOptionValue("out"));
                FileUtils.writeBytes(output, header, result);
            } else if (parsed.hasOption("extract")) {
                byte[] extracted = steganographyMethod.extract(carrier, encryptionAlgorithm != EncryptionAlgorithm.NONE);
                SimpleEntry<byte[], String> data = steganographyMethod.postprocessExtraction(extracted, cryptography);
                Path output = FileUtils.createFile(parsed.getOptionValue("out") + data.getValue());
                FileUtils.writeBytes(output, null, data.getKey());
            } else {
                throw new ParseException("Mode not specified");
            }
        } catch (Exception e) {
            System.err.println("Something went wrong when running the program: " + e.getMessage());
            cli.printHelp();
            System.exit(1);
        }
    }
}
