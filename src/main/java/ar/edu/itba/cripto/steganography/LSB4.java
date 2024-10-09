package ar.edu.itba.cripto.steganography;

import java.util.Arrays;

public class LSB4 implements SteganographyMethod{
    static private int HEADER_SIZE = 54;
    static private int SIZE_STORAGE = 4;
    static private int BYTES_NEEDED = 2;
    static private int BITS_TO_HIDE = 4;
    static private byte MASK_PAYLOAD = (byte) 0b00001111;
    static private byte MASK_CARRIER = (byte) 0b11110000;

    @Override
    public byte[] embed(byte[] carrier, byte[] payload) {

        if (!canEmbed(carrier, payload)) {
            throw new IllegalArgumentException();
        }

        return carrierTransform(carrier, payload, BYTES_NEEDED, BITS_TO_HIDE, MASK_PAYLOAD, MASK_CARRIER);
    }

    private byte[] carrierTransform(byte[] carrier, byte[] payload, int bytesNeeded, int bitsToHide, byte maskPayload, byte maskCarrier) {
        int i = HEADER_SIZE;
        int k = 0;
        byte payloadByte;
        while(i < HEADER_SIZE + (payload.length * bytesNeeded)) {
            for (int j = 0; j < bytesNeeded; j++) {
                payloadByte = (byte) (payload[k] >> (8 - bitsToHide * (j + 1)));
                payloadByte = (byte) (payloadByte & maskPayload);
                carrier[i] = (byte) ((carrier[i] & maskCarrier) | payloadByte);
                i++;
            }
            k++;
        }
        return carrier;
    }

    @Override
    public byte[] extract(byte[] carrier) {
        long payloadSize = 0;

        for (int i = HEADER_SIZE; i < (HEADER_SIZE + (BYTES_NEEDED * SIZE_STORAGE)); i++) {
            int sizeBit = carrier[i] & 15;
            payloadSize += sizeBit * (int) Math.pow(2, BYTES_NEEDED * SIZE_STORAGE - 1 - i + HEADER_SIZE);
        }
        byte[] payload = new byte[(int) (payloadSize + SIZE_STORAGE)];

        int i = HEADER_SIZE;
        int k = 0;
        while (i < (HEADER_SIZE + BYTES_NEEDED * (SIZE_STORAGE + payloadSize)) ) {
            byte data;
            byte payloadByte = 0;
            for (int j = 0; j < BYTES_NEEDED; j++) {
                data = (byte) ((carrier[i] & 15) << (int) Math.pow(2, BYTES_NEEDED) * (1 - j));
                payloadByte = (byte) (payloadByte | data);
                i++;
            }
            payload[k++] = payloadByte;
        }
        return payload;
    }

    @Override
    public boolean canEmbed(byte[] carrier, byte[] payload) {
        long carrierBytes = carrier.length - HEADER_SIZE;
        long payloadBytesNeeded = payload.length * BYTES_NEEDED;

        return payloadBytesNeeded <= carrierBytes;
    }

    public static void main(String[] args) {

        byte number3 = 1;
        number3 = (byte) (number3 << 4);

        byte number = -112;
        number = (byte) (number >> 4);

        byte number2= -116;
        number2 = (byte) (number2 >> 4);
        System.out.println(String.format("%8s", Integer.toBinaryString(number & 0xFF)).replace(' ', '0'));


        byte[] carrier = {
                0,1,2,3,4,5,6,7,8,9,
                0,1,2,3,4,5,6,7,8,9,
                0,1,2,3,4,5,6,7,8,9,
                0,1,2,3,4,5,6,7,8,9,
                0,1,2,3,4,5,6,7,8,9,
                0,1,2,3,

                // 4 * 2 = 8 bytes para guardar el tamanio
                (byte) 0b11110001,
                (byte) 0b00000001,
                (byte) 0b00000001,
                (byte) 0b00000001,
                (byte) 0b00001001,
                (byte) 0b00001001,
                (byte) 0b00001001,
                (byte) 0b00000001,

                // 1 byte se guardan en 2 bytes
                (byte) 0b01000001,
                (byte) 0b00100000,
                (byte) 0b01000001,
                (byte) 0b00100000,
                (byte) 0b01000001,
                (byte) 0b00100000,
                (byte) 0b01000001,
                (byte) 0b00100000,
                (byte) 0b01000001,
                (byte) 0b00100000,
                (byte) 0b01000001,
                (byte) 0b00100000,
                (byte) 0b01000001,
                (byte) 0b00100000,
                (byte) 0b01000001,
                (byte) 0b00100000,
                (byte) 0b01000001,
                (byte) 0b00100000,
                (byte) 0b01000001,
                (byte) 0b00100000,
                (byte) 0b01000001,
                (byte) 0b00100000,
                (byte) 0b01000001,
                (byte) 0b00100000,
                (byte) 0b01000001,
                (byte) 0b00100000,
                (byte) 0b01000001,
                (byte) 0b00100000,

        };

        // Array de bytes cuyos últimos bits se modificarán
        byte[] payload = {
                (byte) 0,
                (byte) 0,
                (byte) 0,
                (byte) 11,
                (byte) 0b10010101, // -107
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
        LSB4 aux = new LSB4();
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

//        for (byte b : ans) {
//            System.out.println(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
//        }

    }
}
