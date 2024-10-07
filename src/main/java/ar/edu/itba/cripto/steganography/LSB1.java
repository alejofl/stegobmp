package ar.edu.itba.cripto.steganography;

public class LSB1 implements SteganographyMethod{
    static private int HEADER_SIZE = 54;
    static private int CIF_SIZE = 4;
    static private int BITS_IN_BYTE = 8;
    static private byte MASK = (byte) 0b11111110;

    public LSB1 () {

    }
    @Override
    public byte[] embed(byte[] carrier, byte[] payload) {
        if (canEmbed(carrier, payload)) {
            throw new IllegalArgumentException();
        }

        // i debería ser igual a HEADER_SIZE para que inicie donde empiezan los datos de la foto.
        int i = 0, k = 0;
        while (i < carrier.length) {
            for (int j = 0; j < BITS_IN_BYTE; j++) {
                byte mask = (byte) (1 << (BITS_IN_BYTE - 1 - j));
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
        int i = 0; // int i = HEADER_SIZE

        int payloadSize = 0;
        for (; i < BITS_IN_BYTE * CIF_SIZE; i++) {
            int sizeBit = carrier[i] & 1;
            payloadSize += sizeBit * (int) Math.pow(2, BITS_IN_BYTE * CIF_SIZE - 1 - i);
        }
        byte[] payload = new byte[payloadSize];

        int k = 0;
        while (i < (CIF_SIZE + payloadSize) ) { // (HEADER_SIZE + CIF_SIZE + payloadSize)
            int j = 0;
            byte bit = (byte) (carrier[i] & 0b00000001);
            byte payloadByte = 0;
            if (bit == 1) {
                payloadByte += (byte) ( - Math.pow(2, BITS_IN_BYTE - 1) );
            }
            j++; // incrementamos en 1 j para poder avanzar al siguiente
            while (j < BITS_IN_BYTE) {
                i++;
                bit = (byte) (carrier[i] & 0b00000001);
                payloadByte += (byte) (bit * Math.pow(2, BITS_IN_BYTE - 1 - j));
                j++;
            }
            payload[k++] = payloadByte;
        }
        return payload;
    }

    @Override
    public boolean canEmbed(byte[] carrier, byte[] payload) {
        int bitsCarrier = carrier.length * BITS_IN_BYTE; // (carrier.length - HEADER_SIZE) * BITS_IN_BYTE;
        int bitsPayloadNeeded = payload.length * BITS_IN_BYTE * BITS_IN_BYTE;

        return bitsPayloadNeeded < bitsCarrier;
    }

    public static void main(String[] args) {
        byte hola = (byte) 231;
        System.out.println(hola);

//        byte[] carrier = {
//                // 4 bytes del tamanio
//                (byte) 0b00000000,
//                (byte) 0b00000000,
//                (byte) 0b00000000,
//                (byte) 0b00000000,
//                (byte) 0b00000000,
//                (byte) 0b00000000,
//                (byte) 0b00000000,
//                (byte) 0b00000000,
//
//                (byte) 0b00000000,
//                (byte) 0b00000000,
//                (byte) 0b00000000,
//                (byte) 0b00000000,
//                (byte) 0b00000000,
//                (byte) 0b00000000,
//                (byte) 0b00000000,
//                (byte) 0b00000000,
//
//                (byte) 0b00000000,
//                (byte) 0b00000000,
//                (byte) 0b00000000,
//                (byte) 0b00000000,
//                (byte) 0b00000000,
//                (byte) 0b00000000,
//                (byte) 0b00000000,
//                (byte) 0b00000000,
//
//                (byte) 0b00000000,
//                (byte) 0b00000000,
//                (byte) 0b00000000,
//                (byte) 0b00000000,
//                (byte) 0b00000000,
//                (byte) 0b00000000,
//                (byte) 0b00000000,
//                (byte) 0b00000001,
//
//
//                // 12 bytes de un dato de 1byte=8bits Uso solamente 8 bytes del carrier
//                (byte) 0b00000001,
//                (byte) 0b00000000,
//                (byte) 0b00000001,
//                (byte) 0b00000000,
//                (byte) 0b00000001,
//                (byte) 0b00000000,
//                (byte) 0b00000001,
//                (byte) 0b00000000,
//                (byte) 0b00000001,
//                (byte) 0b00000000,
//                (byte) 0b00000001,
//                (byte) 0b00000000,
//        };
//
//        // Array de bytes cuyos últimos bits se modificarán
//        byte[] payload = {
//                (byte) 0,
//                (byte) 0,
//                (byte) 0,
//                (byte) 1,
//                (byte) 0b10010001,
//        };
//        LSB1 aux = new LSB1();
//        // Mostrar los arrays originales
//        System.out.println("Carrier original:");
//        for (byte b : carrier) {
//            System.out.println(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
//        }
//        System.out.println("\nPayload original:");
//        for (byte b : payload) {
//            System.out.println(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
//        }
//
//        System.out.println("\nAplicando máscaras y modificando bits:");
//        aux.embed(carrier, payload);
//
//        // Mostrar el array2 final modificado
//        System.out.println("\nCarrier final modificado:");
//        for (byte b : carrier) {
//            System.out.println(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
//        }
//
//        System.out.println("\nVamos a obtener el payload a ver si podemos:");
//
//        byte[] ans = aux.extract(carrier);
//
//        for (byte b : ans) {
//            System.out.println(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
//        }

    }
}
