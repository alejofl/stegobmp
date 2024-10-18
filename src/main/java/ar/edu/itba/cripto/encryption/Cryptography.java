package ar.edu.itba.cripto.encryption;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;

public class Cryptography {
    private static final String KEY_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final byte[] SALT = new byte[8];
    private static final int ITERATIONS = 10000;

    private final EncryptionAlgorithm algorithm;
    private final EncryptionMode mode;
    private final Cipher encrypter;
    private final Cipher decrypter;

    public Cryptography(EncryptionAlgorithm algorithm, EncryptionMode mode, String password) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, InvalidKeyException {
        this.algorithm = algorithm;
        this.mode = mode;

        if (algorithm == EncryptionAlgorithm.NONE) {
            this.encrypter = null;
            this.decrypter = null;
            return;
        }

        SimpleEntry<byte[], byte[]> keyAndIV = getKeyAndIV(password, algorithm.keySize, mode.requiresIV ? algorithm.ivSize : 0);
        SecretKey key = new SecretKeySpec(keyAndIV.getKey(), algorithm.algorithm);
        IvParameterSpec iv = mode.requiresIV ? new IvParameterSpec(keyAndIV.getValue()) : null;

        String transformation = new StringBuilder()
                .append(algorithm.algorithm)
                .append("/")
                .append(mode.mode)
                .append("/")
                .append(mode.requiresPadding ? "PKCS5Padding" : "NoPadding")
                .toString();
        this.encrypter = Cipher.getInstance(transformation);
        encrypter.init(Cipher.ENCRYPT_MODE, key, iv);
        this.decrypter = Cipher.getInstance(transformation);
        decrypter.init(Cipher.DECRYPT_MODE, key, iv);
    }

    private SimpleEntry<byte[], byte[]> getKeyAndIV(String password, int keySize, int ivSize) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), SALT, ITERATIONS, keySize + ivSize);
        int keyBytes = keySize / 8;
        int ivBytes = ivSize / 8;
        byte[] hash = factory.generateSecret(spec).getEncoded();
        byte[] key = Arrays.copyOf(hash, keyBytes);
        byte[] iv = Arrays.copyOfRange(hash, keyBytes, keyBytes + ivBytes);
        return new SimpleEntry<>(key, iv);
    }

    public byte[] encrypt(byte[] data) throws IllegalBlockSizeException, BadPaddingException {
        if (this.encrypter == null) {
            return data;
        }
        return this.encrypter.doFinal(data);
    }

    public byte[] decrypt(byte[] data) throws IllegalBlockSizeException, BadPaddingException {
        if (this.decrypter == null) {
            return data;
        }
        return this.decrypter.doFinal(data);
    }

    public EncryptionMode getMode() {
        return mode;
    }

    public EncryptionAlgorithm getAlgorithm() {
        return algorithm;
    }
}
