package ar.edu.itba.cripto.steganography;

public class LSBI implements SteganographyMethod{

    @Override
    public byte[] embed(byte[] carrier, byte[] payload) {
        return new byte[0];
    }

    @Override
    public byte[] extract(byte[] carrier) {
        return new byte[0];
    }

    @Override
    public boolean canEmbed(byte[] carrier, byte[] payload) {
        return false;
    }
}
