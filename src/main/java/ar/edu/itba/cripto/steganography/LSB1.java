package ar.edu.itba.cripto.steganography;

import javax.xml.crypto.Data;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class LSB1 implements SteganographyMethod{
    static private final int HEADER_SIZE = 54;
    static private final int PAYLOAD_LENGTH_SIZE = 4;
    protected int BYTES_NEEDED;
    protected int BITS_TO_HIDE;
    protected byte MASK_PAYLOAD;
    protected byte MASK_CARRIER;

    public LSB1() {
        this.BYTES_NEEDED = 8;
        this.BITS_TO_HIDE = 1;
        this.MASK_PAYLOAD = (byte) 0b00000001;
        this.MASK_CARRIER = (byte) 0b11111110;
    }

    public byte[] embed(byte[] carrier, byte[] payload) {
        if (!canEmbed(carrier, payload)) {
            throw new IllegalArgumentException();
        }

        int i = 0;
        int k = 0;
        byte payloadByte = 0;

        while(i < payload.length * BYTES_NEEDED) {
            for (int j = 0; j < BYTES_NEEDED; j++) {
                payloadByte = (byte) (payload[k] >> (8 - BITS_TO_HIDE * (j + 1)));
                payloadByte = (byte) (payloadByte & MASK_PAYLOAD);
                carrier[i] = (byte) ((carrier[i] & MASK_CARRIER) | payloadByte);
                i++;
            }
            k++;
        }
        return carrier;
    }

    @Override
    public byte[] extract(byte[] carrier, boolean isEncrypted) throws IOException {
        long payloadSize = 0;

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DataOutputStream writer = new DataOutputStream(output);

        for (int i = 0; i < BYTES_NEEDED * PAYLOAD_LENGTH_SIZE; i++) {
            int sizeBit = carrier[i] & MASK_PAYLOAD;
            sizeBit = sizeBit << (32 - BITS_TO_HIDE * (i + 1));
            payloadSize = payloadSize | sizeBit;
        }

        int i = 0;
        byte payloadByte;
        for ( ; i < BYTES_NEEDED * (payloadSize + PAYLOAD_LENGTH_SIZE); i += BYTES_NEEDED) {
            payloadByte = cicle(carrier, i);
            writer.write(payloadByte);
        }

        if (!isEncrypted) {
            payloadByte = 1;
            for ( ; i < carrier.length && payloadByte != 0; i += BYTES_NEEDED) {
                payloadByte = cicle(carrier, i);
                writer.write(payloadByte);
            }
        }

        return output.toByteArray();
    }

    protected byte cicle(byte[] carrier, int indexCarrier) {
        byte payloadByte = 0;
        for (int j = 0; j < BYTES_NEEDED; j++) {
            byte data = (byte) ((carrier[indexCarrier] & MASK_PAYLOAD) << (8 - BITS_TO_HIDE * (j + 1)));
            payloadByte = (byte) (payloadByte | data);
            indexCarrier++;
        }
        return payloadByte;
    }

    @Override
    public boolean canEmbed(byte[] carrier, byte[] payload) {
        long carrierBytes = carrier.length;
        long payloadBytesNeeded = (long) payload.length * BYTES_NEEDED;

        return payloadBytesNeeded <= carrierBytes;
    }

    public static void main(String[] args) throws IOException {
        byte[] carrier = {
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
        };

        // Array de bytes cuyos últimos bits se modificarán
        byte[] payload = {
                (byte) 0, // 54
                (byte) 0, // 62
                (byte) 0, // 70
                (byte) 40, // 78
                (byte) 0b10010001, // -111 pos 86
                (byte) 20,  // 94
                (byte) 127, //102
                (byte) 128, // -> Al ser 0b10000000 entonces tenemos que es -128  POS 110
                (byte) 129, // -> Al ser 0b10000001 entonces tenemos que es -127  POS 118
                (byte) 16, // 126
                (byte) 40, // 134
                (byte) 255, // -> Al ser 0b11111111 entonces tenemos que es -1 POS 142
                (byte) 0, // 150
                (byte) 69, //158
                (byte) 0b10010001, // -111 pos 86
                (byte) 20,  // 94
                (byte) 127, //102
                (byte) 128, // -> Al ser 0b10000000 entonces tenemos que es -128  POS 110
                (byte) 129, // -> Al ser 0b10000001 entonces tenemos que es -127  POS 118
                (byte) 16, // 126
                (byte) 40, // 134
                (byte) 255, // -> Al ser 0b11111111 entonces tenemos que es -1 POS 142
                (byte) 0, // 150
                (byte) 69, //158
                (byte) 0b10010001, // -111 pos 86
                (byte) 20,  // 94
                (byte) 127, //102
                (byte) 128, // -> Al ser 0b10000000 entonces tenemos que es -128  POS 110
                (byte) 129, // -> Al ser 0b10000001 entonces tenemos que es -127  POS 118
                (byte) 16, // 126
                (byte) 40, // 134
                (byte) 255, // -> Al ser 0b11111111 entonces tenemos que es -1 POS 142
                (byte) 0, // 150
                (byte) 69, //158
                (byte) 0b10010001, // -111 pos 86
                (byte) 20,  // 94
                (byte) 127, //102
                (byte) 128, // -> Al ser 0b10000000 entonces tenemos que es -128  POS 110
                (byte) 129, // -> Al ser 0b10000001 entonces tenemos que es -127  POS 118
                (byte) 16, // 126
                (byte) 40, // 134
                (byte) 255, // -> Al ser 0b11111111 entonces tenemos que es -1 POS 142
                (byte) 0, // 150
                (byte) 69, //158
                (byte) '.', // 166 //0b0010 1110
                (byte) 'a', //ASCII 97 0b0110 0001 QUE ES  174
                (byte) 'a', // 182
                (byte) 'a', // 190
                (byte) 'a', //198
                (byte) '\0', // 206

        };
        LSB1 aux = new LSB1();

        System.out.println("LSB1 " + aux.BYTES_NEEDED);

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
        int i = 0;
        for (byte b : carrier) {
            System.out.println(i + String.format("- %8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
            i++;
        }

        System.out.println("\nVamos a obtener el payload a ver si podemos:");

        byte[] ans = aux.extract(carrier, false);

        System.out.println(Arrays.toString(ans));

    }
}
