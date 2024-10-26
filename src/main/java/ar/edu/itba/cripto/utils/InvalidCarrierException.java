package ar.edu.itba.cripto.utils;

public class InvalidCarrierException extends IllegalArgumentException {
    public InvalidCarrierException(int requiredSize) {
        super("Carrier is too small to embed the payload. To embed the payload, the carrier must be at least " + FileUtils.getHumanReadableSize(requiredSize) + " long");
    }
}
