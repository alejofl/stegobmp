package ar.edu.itba.cripto.steganography;

public class LSB1 implements SteganographyMethod{
    static private int PAYLOAD_INDEX = 54;
    static private int BITS_IN_BYTE = 8;

    public LSB1 () {

    }
    @Override
    public byte[] embed(byte[] carrier, byte[] payload) {
        int bitsCarrier = carrier.length * BITS_IN_BYTE; // (carrier.length - PAYLOAD_INDEX) * BITS_IN_BYTE;
        int bitsPayloadNeeded = payload.length * BITS_IN_BYTE * BITS_IN_BYTE;

        if (bitsPayloadNeeded > bitsCarrier) {
            throw new IllegalArgumentException();
        }

        byte c = 0;
        // Recorro mis amigos bits del carrier
        int i = 0;
        int k = 0;
        while (i < carrier.length) {
            // Recorro los 8 bytes del carrier y todos los bits de un byte del payload
            for (int j = 0; j < BITS_IN_BYTE; j++) {
                byte mask = (byte) (1 << (BITS_IN_BYTE - 1 - j));
                byte payloadByte = (byte) (payload[k] & mask);

                if (payloadByte != 0) {
                    carrier[i] = (byte) ((carrier[i] & 0b11111110) | (byte) 1);
                } else {
                    carrier[i] = (byte) ((carrier[i] & 0b11111110) | (byte) 0);
                }
                i++;
            }
            k++;
        }
        return carrier;
    }

    @Override
    public byte[] extract(byte[] carrier) {
        return new byte[0];
    }

    public static void main(String[] args) {
        byte[] carrier = {
                (byte) 0b01101101,
                (byte) 0b10101010,
                (byte) 0b11110001,
                (byte) 0b01101100,
                (byte) 0b10101011,
                (byte) 0b11110000,
                (byte) 0b01101101,
                (byte) 0b10101010
        };

        // Array de bytes cuyos últimos bits se modificarán
        byte[] payload = {
                (byte) 0b10010001,
        };
        LSB1 aux = new LSB1();
        // Mostrar los arrays originales
        System.out.println("Array1 original:");
        for (byte b : carrier) {
            System.out.println(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }
        System.out.println("\nArray2 original:");
        for (byte b : payload) {
            System.out.println(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }

        System.out.println("\nAplicando máscaras y modificando bits:");
        aux.embed(carrier, payload);

        // Mostrar el array2 final modificado
        System.out.println("\nArray2 final modificado:");
        for (byte b : carrier) {
            System.out.println(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }

    }
}
