package ar.edu.itba.cripto.encryption;

public enum EncryptionMode {
    ECB("ECB", false, true),
    CBC("CBC", true, true),
    OFB("OFB", true, false),
    CFB("CFB8", true, false);

    final String mode;
    final boolean requiresIV;
    final boolean requiresPadding;

    EncryptionMode(String mode, boolean requiresIV, boolean requiresPadding) {
        this.mode = mode;
        this.requiresIV = requiresIV;
        this.requiresPadding = requiresPadding;
    }
}
