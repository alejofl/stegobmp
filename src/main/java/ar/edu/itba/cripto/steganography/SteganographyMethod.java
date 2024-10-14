package ar.edu.itba.cripto.steganography;

import ar.edu.itba.cripto.encryption.Cryptography;
import ar.edu.itba.cripto.encryption.EncryptionAlgorithm;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;

public interface SteganographyMethod {
    byte[] embed(byte[] carrier, byte[] payload);

    byte[] extract(byte[] carrier, boolean isEncrypted);

    boolean canEmbed(byte[] carrier, byte[] payload);

    default byte[] preprocessEmbedding(byte[] payload, String extension, Cryptography cryptography) throws IOException, IllegalBlockSizeException, BadPaddingException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DataOutputStream writer = new DataOutputStream(output);
        writer.writeInt(payload.length);
        writer.write(payload);
        writer.writeBytes(extension);
        writer.writeBytes("\0");

        if (cryptography.getAlgorithm() == EncryptionAlgorithm.NONE) {
            return output.toByteArray();
        }

        byte[] encrypted = cryptography.encrypt(output.toByteArray());
        output = new ByteArrayOutputStream();
        writer = new DataOutputStream(output);
        writer.writeInt(encrypted.length);
        writer.write(encrypted);
        return output.toByteArray();
    }

    default SimpleEntry<byte[], String> postprocessExtraction(byte[] extracted, Cryptography cryptography) throws IllegalBlockSizeException, BadPaddingException {
        byte[] data = extracted;
        if (cryptography.getAlgorithm() != EncryptionAlgorithm.NONE) {
            byte[] encryptedData = Arrays.copyOfRange(extracted, 4, extracted.length);
            data = cryptography.decrypt(encryptedData);
        }
        int payloadLength = new BigInteger(Arrays.copyOfRange(data, 0, 4)).intValue();
        byte[] payload = Arrays.copyOfRange(data, 4, 4 + payloadLength);
        String extension = new String(Arrays.copyOfRange(data, 4 + payloadLength, data.length - 1));
        return new SimpleEntry<>(payload, extension);
    }
}
