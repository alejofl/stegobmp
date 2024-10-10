package ar.edu.itba.cripto.utils;

import org.apache.commons.cli.*;

public class StegobmpCLI {
    private final Options options = new Options();
    private final CommandLineParser parser = new DefaultParser();
    private final HelpFormatter formatter = new HelpFormatter();

    public StegobmpCLI() {
        Option embed = Option.builder()
                .option("embed")
                .desc("Sets mode to hiding information")
                .build();
        Option extract = Option.builder()
                .option("extract")
                .desc("Sets mode to extracting information")
                .build();
        Option payload = Option.builder()
                .option("in")
                .argName("file")
                .hasArg()
                .desc("Input file to hide")
                .build();
        Option carrier = Option.builder()
                .option("p")
                .argName("file")
                .hasArg()
                .required()
                .desc("Carrier file")
                .build();
        Option output = Option.builder()
                .option("out")
                .argName("file")
                .hasArg()
                .required()
                .desc("Output file")
                .build();
        Option steganographyMode = Option.builder()
                .option("steg")
                .argName("mode")
                .hasArg()
                .required()
                .desc("Steganography mode. Options: LSB1, LSB4, LSBI")
                .build();
        Option encryptionAlgorithm = Option.builder()
                .option("a")
                .argName("algorithm")
                .hasArg()
                .desc("Encryption algorithm. Options: AES_128, AES_192, AES_256, DES_3")
                .build();
        Option encryptionMode = Option.builder()
                .option("m")
                .argName("mode")
                .hasArg()
                .desc("Encryption mode. Options: ECB, CBC, OFB, CFB")
                .build();
        Option password = Option.builder()
                .option("pass")
                .argName("password")
                .hasArg()
                .desc("Password")
                .build();
        options.addOption(embed);
        options.addOption(extract);
        options.addOption(payload);
        options.addOption(carrier);
        options.addOption(output);
        options.addOption(steganographyMode);
        options.addOption(encryptionAlgorithm);
        options.addOption(encryptionMode);
        options.addOption(password);
    }

    public CommandLine parse(String[] args) throws ParseException {
        return parser.parse(options, args);
    }

    public void printHelp() {
        formatter.printHelp("stegobmp", options);
    }
}
