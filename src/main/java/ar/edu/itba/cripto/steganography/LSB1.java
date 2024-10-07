package ar.edu.itba.cripto.steganography;

import java.math.BigInteger;
import java.util.Arrays;

public class LSB1 implements SteganographyMethod{
    static private int HEADER_SIZE = 54;
    static private int SIZE_STORAGE = 4;
    static private int BYTES_NEEDED = 8;
    static private byte MASK = (byte) 0b11111110;

    public LSB1 () {

    }
    @Override
    public byte[] embed(byte[] carrier, byte[] payload) {

        if (!canEmbed(carrier, payload)) {
            throw new IllegalArgumentException();
        }

        long payloadSize = payload.length;

        int i = 0; // i = HEADER_SIZE -> porque tenemos que saltear el header
        int k = 0; // indice que recorre el payload
        while (i < (payloadSize * BYTES_NEEDED)) { // i < (HEADER_SIZE + payloadSize) * BYTES_NEEDED
            for (int j = 0; j < BYTES_NEEDED; j++) {
                byte mask = (byte) (1 << (BYTES_NEEDED - 1 - j));
                byte payloadByte = (byte) (payload[k] & mask);

                if (payloadByte != 0) {
                    carrier[i] = (byte) ((carrier[i] & MASK) | (byte) 1);
                } else {
                    carrier[i] = (byte) ((carrier[i] & MASK) | (byte) 0);
                }
                i++;
            }
            k++;
        }
        return carrier;
    }

    @Override
    public byte[] extract(byte[] carrier) {
        long payloadSize = 0;

        // OJO int i = HEADER_SIZE; -> indice que recorre el carrier posterior al header
        for (int i = 0; i < BYTES_NEEDED * SIZE_STORAGE; i++) {
            int sizeBit = carrier[i] & 1;
            payloadSize += sizeBit * (int) Math.pow(2, BYTES_NEEDED * SIZE_STORAGE - 1 - i);
        }
        byte[] payload = new byte[(int) (payloadSize + SIZE_STORAGE)];

        int i = 0; // OJO int i = HEADER_SIZE; -> indice que recorre el carrier posterior al header
        int k = 0; // -> indice que recorre el payload
        while (i < BYTES_NEEDED * (SIZE_STORAGE + payloadSize) ) { // BYTES_NEEDED * (HEADER_SIZE + CIF_SIZE + payloadSize)
            byte bit;
            byte payloadByte = 0;
            for (int j = 0; j < BYTES_NEEDED; j++) {
                bit = (byte) (carrier[i] & 1);
                payloadByte += (byte) (bit * Math.pow(2, BYTES_NEEDED - 1 - j));
                i++;
            }
            payload[k++] = payloadByte;
        }
        return payload;
    }

    @Override
    public boolean canEmbed(byte[] carrier, byte[] payload) {
        long carrierBytes = carrier.length; // carrier.length - HEADER_SIZE;
        long payloadBytesNeeded = payload.length * BYTES_NEEDED;

        return payloadBytesNeeded <= carrierBytes;
    }

    public static void main(String[] args) {
        byte[] carrier = {
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
                (byte) 0b00000001,

                (byte) 0b00000001,
                (byte) 0b00000001,
                (byte) 0b00000001,
                (byte) 0b00000001,
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
                (byte) 1,
                (byte) 0b10010001,
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

        for (byte b : ans) {
            System.out.println(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }

    }
}
