package ar.edu.itba.cripto.encryption;

public enum EncryptionAlgorithm {
    AES_128("AES", 128, 128),
    AES_192("AES", 192, 128),
    AES_256("AES", 256, 128),
    DES_3("DESede", 192, 64),
    NONE(null, null, null);

    final String algorithm;
    final Integer keySize;
    final Integer ivSize;

    EncryptionAlgorithm(String algorithm, Integer keySize, Integer ivSize) {
        this.algorithm = algorithm;
        this.keySize = keySize;
        this.ivSize = ivSize;
    }
}
