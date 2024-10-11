package ar.edu.itba.cripto.steganography;

import java.util.Arrays;

public class LSB1 implements SteganographyMethod{
    static private final int HEADER_SIZE = 54;
    static private final int SIZE_STORAGE = 4;
    static private final int BYTES_NEEDED = 8;
    static private final int BITS_TO_HIDE = 1;
    static private final byte MASK_PAYLOAD = (byte) 0b00000001;
    static private final byte MASK_CARRIER = (byte) 0b11111110;

    public byte[] embed(byte[] carrier, byte[] payload) {

        if (!canEmbed(carrier, payload)) {
            throw new IllegalArgumentException();
        }

        return carrierTransform(carrier, payload, HEADER_SIZE, BYTES_NEEDED, BITS_TO_HIDE, MASK_PAYLOAD, MASK_CARRIER);
    }

    @Override
    public byte[] extract(byte[] carrier) {
        long payloadSize = 0;

        for (int i = HEADER_SIZE; i < (HEADER_SIZE + (BYTES_NEEDED * SIZE_STORAGE)); i++) {
            int sizeBit = carrier[i] & 1;
            payloadSize += sizeBit * (int) Math.pow(2, BYTES_NEEDED * SIZE_STORAGE - 1 - i + HEADER_SIZE);
        }
        System.out.println("pedrooo: " + payloadSize);
        byte[] payload = new byte[(int) (payloadSize + SIZE_STORAGE)];

        return carrierExtract(carrier, payload, HEADER_SIZE, BYTES_NEEDED, BITS_TO_HIDE, MASK_PAYLOAD);
    }

    @Override
    public boolean canEmbed(byte[] carrier, byte[] payload) {
        long carrierBytes = carrier.length - HEADER_SIZE;
        long payloadBytesNeeded = (long) payload.length * BYTES_NEEDED;

        return payloadBytesNeeded <= carrierBytes;
    }

    public static void main(String[] args) {
        byte[] carrier = {
                0,1,2,3,4,5,6,7,8,9,
                0,1,2,3,4,5,6,7,8,9,
                0,1,2,3,4,5,6,7,8,9,
                0,1,2,3,4,5,6,7,8,9,
                0,1,2,3,4,5,6,7,8,9,
                0,1,2,3,

                // 4 * 8 = 32 bytes para guardar el tamanio
                (byte) 0b00000001,
                (byte) 0b00000001,
                (byte) 0b00000001,
                (byte) 0b00000001,
                (byte) 0b00001001,
                (byte) 0b00001001,
                (byte) 0b00001001,
                (byte) 0b00000001,

                (byte) 0b00001001,
                (byte) 0b00001001,
                (byte) 0b00001001,
                (byte) 0b00000001,
                (byte) 0b00000001,
                (byte) 0b00000001,
                (byte) 0b00000001,
                (byte) 0b00000001,

                (byte) 0b00000001,
                (byte) 0b00000001,
                (byte) 0b00000001,
                (byte) 0b00000001,
                (byte) 0b00000001,
                (byte) 0b00000001,
                (byte) 0b00000001,
                (byte) 0b00000000,

                (byte) 0b00000001,
                (byte) 0b00000000,
                (byte) 0b00000001,
                (byte) 0b00000000,
                (byte) 0b00000001,
                (byte) 0b00000001,
                (byte) 0b00000001,
                (byte) 0b00000000,


                // 12 bytes de un dato de 1byte=8bits Uso solamente 8 bytes del carrier
                (byte) 0b01000001,
                (byte) 0b00100000,
                (byte) 0b00010001,
                (byte) 0b00001000,
                (byte) 0b00000101,
                (byte) 0b00000010,
                (byte) 0b00000001,
                (byte) 0b00000000,

                (byte) 0b01000001,
                (byte) 0b00100000,
                (byte) 0b00010001,
                (byte) 0b00001000,
                (byte) 0b00000101,
                (byte) 0b00000010,
                (byte) 0b00000001,
                (byte) 0b00000000,

                (byte) 0b01000001,
                (byte) 0b00100000,
                (byte) 0b00010001,
                (byte) 0b00001000,
                (byte) 0b00000101,
                (byte) 0b00000010,
                (byte) 0b00000001,
                (byte) 0b00000000,

                (byte) 0b01000001,
                (byte) 0b00100000,
                (byte) 0b00010001,
                (byte) 0b00001000,
                (byte) 0b00000101,
                (byte) 0b00000010,
                (byte) 0b00000001,
                (byte) 0b00000000,

                (byte) 0b01000001,
                (byte) 0b00100000,
                (byte) 0b00010001,
                (byte) 0b00001000,
                (byte) 0b00000101,
                (byte) 0b00000010,
                (byte) 0b00000001,
                (byte) 0b00000000,

                (byte) 0b01000001,
                (byte) 0b00100000,
                (byte) 0b00010001,
                (byte) 0b00001000,
                (byte) 0b00000101,
                (byte) 0b00000010,
                (byte) 0b00000001,
                (byte) 0b00000000,

                (byte) 0b01000001,
                (byte) 0b00100000,
                (byte) 0b00010001,
                (byte) 0b00001000,
                (byte) 0b00000101,
                (byte) 0b00000010,
                (byte) 0b00000001,
                (byte) 0b00000000,

                (byte) 0b01000001,
                (byte) 0b00100000,
                (byte) 0b00010001,
                (byte) 0b00001000,
                (byte) 0b00000101,
                (byte) 0b00000010,
                (byte) 0b00000001,
                (byte) 0b00000000,

                (byte) 0b01000001,
                (byte) 0b00100000,
                (byte) 0b00010001,
                (byte) 0b00001000,
                (byte) 0b00000101,
                (byte) 0b00000010,
                (byte) 0b00000001,
                (byte) 0b00000000,

                (byte) 0b01000001,
                (byte) 0b00100000,
                (byte) 0b00010001,
                (byte) 0b00001000,
                (byte) 0b00000101,
                (byte) 0b00000010,
                (byte) 0b00000001,
                (byte) 0b00000000,

                (byte) 0b01000001,
                (byte) 0b00100000,
                (byte) 0b00010001,
                (byte) 0b00001000,
                (byte) 0b00000101,
                (byte) 0b00000010,
                (byte) 0b00000001,
                (byte) 0b00000000,

                (byte) 0b01000001,
                (byte) 0b00100000,
                (byte) 0b00010001,
                (byte) 0b00001000,
                (byte) 0b00000101,
                (byte) 0b00000010,
                (byte) 0b00000001,
                (byte) 0b00000000,

                (byte) 0b01000001,
                (byte) 0b00100000,
                (byte) 0b00010001,
                (byte) 0b00001000,
                (byte) 0b00000101,
                (byte) 0b00000010,
                (byte) 0b00000001,
                (byte) 0b00000000,

                (byte) 0b01000001,
                (byte) 0b00100000,
                (byte) 0b00010001,
                (byte) 0b00001000,
                (byte) 0b00000101,
                (byte) 0b00000010,
                (byte) 0b00000001,
                (byte) 0b00000000,

                (byte) 0b01000001,
                (byte) 0b00100000,
                (byte) 0b00010001,
                (byte) 0b00001000,
                (byte) 0b00000101,
                (byte) 0b00000010,
                (byte) 0b00000001,
                (byte) 0b00000000,

                (byte) 0b00000001,
                (byte) 0b00000000,
                (byte) 0b00000001,
                (byte) 0b00000000,
        };

        // Array de bytes cuyos últimos bits se modificarán
        byte[] payload = {
                (byte) 0,
                (byte) 0,
                (byte) 0,
                (byte) 10,
                (byte) 0b10010001, // -111
                (byte) 20,
                (byte) 127,
                (byte) 128, // -> Al ser 0b10000000 entonces tenemos que es -128
                (byte) 129, // -> Al ser 0b10000001 entonces tenemos que es -127
                (byte) 16,
                (byte) 40,
                (byte) 255, // -> Al ser 0b11111111 entonces tenemos que es -1
                (byte) 0,
                (byte) 69,
                (byte) 1,

        };
        LSB1 aux = new LSB1();
        // Mostrar los arrays originales
        System.out.println("Carrier original:");
        for (byte b : carrier) {
            System.out.println(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }
        System.out.println("\nPayload original:");
        for (byte b : payload) {
            System.out.println(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }

        System.out.println("\nAplicando máscaras y modificando bits:");
        aux.embed(carrier, payload);

        // Mostrar el carrier final modificado
        System.out.println("\nCarrier final modificado:");
        for (byte b : carrier) {
            System.out.println(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }

        System.out.println("\nVamos a obtener el payload a ver si podemos:");

        byte[] ans = aux.extract(carrier);

        System.out.println(Arrays.toString(ans));

    }
}
