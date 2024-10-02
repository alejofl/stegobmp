package ar.edu.itba.cripto.steganography;

public class LSB1 implements SteganographyMethod{

    @Override
    public byte[] embed(byte[] carrier, byte[] payload) {
        return new byte[0];
    }

    @Override
    public byte[] extract(byte[] carrier) {
        return new byte[0];
    }
}
