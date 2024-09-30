package ar.edu.itba.cripto;

import ar.edu.itba.cripto.encryption.EncryptionAlgorithm;
import ar.edu.itba.cripto.encryption.EncryptionMode;
import ar.edu.itba.cripto.steganography.SteganographyAlgorithm;
import ar.edu.itba.cripto.utils.FileUtils;
import ar.edu.itba.cripto.utils.StegobmpCLI;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        StegobmpCLI cli = new StegobmpCLI();
        try {
            CommandLine parsed = cli.parse(args);

            SteganographyAlgorithm steganographyAlgorithm = SteganographyAlgorithm.valueOf(parsed.getOptionValue("steg"));
            EncryptionAlgorithm encryptionAlgorithm = EncryptionAlgorithm.valueOf(parsed.getOptionValue("a", "AES_128"));
            EncryptionMode encryptionMode = EncryptionMode.valueOf(parsed.getOptionValue("m", "CBC"));
            String password = parsed.getOptionValue("pass");
            byte[] carrier = FileUtils.readBytes(parsed.getOptionValue("p"));
            Path output = FileUtils.createFile(parsed.getOptionValue("out"));

            if (parsed.hasOption("embed") && parsed.hasOption("in")) {
                byte[] payload = FileUtils.readBytes(parsed.getOptionValue("in"));
                // TODO
            } else if (parsed.hasOption("extract")) {
                // TODO
            } else {
                throw new ParseException("Mode not specified");
            }
        } catch (Exception e) {
            System.err.println("Argument Error: " + e.getMessage());
            cli.printHelp();
            System.exit(1);
        }
    }
}
