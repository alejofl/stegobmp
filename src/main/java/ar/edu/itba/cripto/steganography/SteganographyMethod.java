package ar.edu.itba.cripto.steganography;

public interface SteganographyMethod {
    byte[] embed(byte[] carrier, byte[] payload);
    byte[] extract(byte[] carrier);
}
