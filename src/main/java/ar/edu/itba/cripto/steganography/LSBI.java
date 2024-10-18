package ar.edu.itba.cripto.steganography;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class LSBI implements SteganographyMethod {
    static final private int BYTES_HEADER = 54;
    static final private int BITS_IN_BYTE = 8;
    static final private int BYTES_PREFIX = 4;
    static final private int BYTES_SIZE = 4;
    static final private byte MASK_PAYLOAD = (byte) 0b00000001;
    static final private byte MASK_PREFIX = (byte) 0b00000110;
    static final private byte MASK_CARRIER = (byte) 0b11111110;

    @Override
    public byte[] embed(byte[] carrier, byte[] payload) {
        if (!canEmbed(carrier, payload)) {
            throw new IllegalArgumentException();
        }

        int[] prefixReplaced = {0, 0, 0, 0}; // {00, 01, 10, 11}

        int prefix;
        int byteIndex, bitPosition;
        byte mask, payloadBit, carrierBit;
        for(int i = 0, carrierIndex; i < payload.length * BITS_IN_BYTE; i++) {
            if (i % 3 == 2) {
                continue;
            }

            carrierIndex = i + BYTES_HEADER + BYTES_PREFIX;

            prefix = (byte) ((carrier[carrierIndex] & MASK_PREFIX) >> 1);

            byteIndex = i / 8;
            bitPosition = 7 - (i % 8);  // De MSB a LSB
            mask = (byte) (1 << bitPosition);
            payloadBit = (byte) ((payload[byteIndex] & mask) >> bitPosition);

            carrierBit = (byte) (carrier[carrierIndex] & MASK_PAYLOAD);

            if (payloadBit == carrierBit) {
                prefixReplaced[prefix] += 1; // si son iguales sumo
            } else {
                prefixReplaced[prefix] -= 1; // si son diferentes resto y remplazo
                carrier[carrierIndex] = (byte) ((carrier[carrierIndex] & MASK_CARRIER) | payloadBit);
            }
        }

        // Pone los headers de que prefijos voy a reeemplazar
        for(int i = 0, carrierIndex; i < prefixReplaced.length; i++){
            if (i % 3 == 2) {
                continue;
            }

            carrierIndex = i + BYTES_HEADER;
            if(prefixReplaced[i] > 0){
                carrier[carrierIndex] = (byte) ((carrier[carrierIndex] & MASK_CARRIER) | 1);
            }else{
                carrier[carrierIndex] = (byte) ((carrier[carrierIndex] & MASK_CARRIER) | 0);
            }
        }

        // Hacer mas performante
        for(int i = 0, carrierIndex; i < payload.length * BITS_IN_BYTE; i++) {
            if (i % 3 == 2) {
                continue;
            }

            carrierIndex = i + BYTES_HEADER + BYTES_PREFIX;
            prefix = (byte) ((carrier[carrierIndex] & MASK_PREFIX) >> 1);

            if (prefixReplaced[prefix] > 0) {
                if((carrier[carrierIndex] & MASK_PAYLOAD) == 1){
                    carrier[carrierIndex] = (byte) ((carrier[carrierIndex] & MASK_CARRIER) | 0);
                }else{
                    carrier[carrierIndex] = (byte) ((carrier[carrierIndex] & MASK_CARRIER) | 1);
                }
            }
        }

        return carrier;
    }

    @Override
    public byte[] extract(byte[] carrier, boolean isEncrypted) throws IOException {
        boolean[] prefixReplaced = {true, true, true, true}; // {00, 01, 10, 11}
        int carrierIndex = BYTES_HEADER;

        for (int k = 0; k < BYTES_PREFIX; carrierIndex++, k++) {
            if ((carrier[carrierIndex] & MASK_PAYLOAD) == 0) {
                prefixReplaced[k] = false;
            }
        }

        System.out.println("CARRIER INDEX: " + carrierIndex);

        byte[] sizeAux = new byte[BYTES_SIZE];
        byte prefix, carrierBit, payloadByte;
        int k = 0;
        while (k < 4) {
            payloadByte = 0;
            for (int j = 0; j < BITS_IN_BYTE; ) {
                if ((carrierIndex - BYTES_HEADER) % 3 == 2) {
                    carrierIndex++;
                    continue;
                }

                prefix = (byte) ((carrier[carrierIndex] & 0b00000110) >> 1);
                if (prefixReplaced[prefix]) {
                    carrierBit = (byte) (((carrier[carrierIndex] & MASK_PAYLOAD) ^ 0b00000001) << (8 - (j + 1)));
                } else {
                    carrierBit = (byte) ((carrier[carrierIndex] & MASK_PAYLOAD) << (8 - (j + 1)));
                }
                payloadByte = (byte) (payloadByte | carrierBit);
                j++;
                carrierIndex++;
            }
            sizeAux[k++] = payloadByte;
        }

        int size = ((sizeAux[0] & 0xFF) << 24) | // Byte más significativo
                    ((sizeAux[1] & 0xFF) << 16) | // Segundo byte
                    ((sizeAux[2] & 0xFF) << 8)  | // Tercer byte
                    (sizeAux[3] & 0xFF);         // Byte menos significativo

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DataOutputStream writer = new DataOutputStream(output);
        writer.write(sizeAux);

        int readBytesFromPayload = 0;
        while (readBytesFromPayload < size) {
            payloadByte = 0;
            for (int j = 0; j < BITS_IN_BYTE; ) {
                if ((carrierIndex - BYTES_HEADER) % 3 == 2) {
                    carrierIndex++;
                    continue;
                }

                prefix = (byte) ((carrier[carrierIndex] & 0b00000110) >> 1);
                if (prefixReplaced[prefix]) {
                    carrierBit = (byte) (((carrier[carrierIndex] & MASK_PAYLOAD) ^ 0b00000001) << (8 -  (j + 1)));
                } else {
                    carrierBit = (byte) ((carrier[carrierIndex] & MASK_PAYLOAD) << (8 - (j + 1)));
                }
                payloadByte = (byte) (payloadByte | carrierBit);
                j++;
                carrierIndex++;
            }
            writer.writeByte(payloadByte);
            readBytesFromPayload++;
        }

        if (!isEncrypted) {
            do {
                payloadByte = 0;
                for (int j = 0; j < BITS_IN_BYTE; ) {
                    if ((carrierIndex - BYTES_HEADER) % 3 == 2) {
                        carrierIndex++;
                        continue;
                    }

                    prefix = (byte) ((carrier[carrierIndex] & 0b00000110) >> 1);
                    if (prefixReplaced[prefix]) {
                        carrierBit = (byte) (((carrier[carrierIndex] & MASK_PAYLOAD) ^ 0b00000001) << (8 -  (j + 1)));
                    } else {
                        carrierBit = (byte) ((carrier[carrierIndex] & MASK_PAYLOAD) << (8 - (j + 1)));
                    }
                    payloadByte = (byte) (payloadByte | carrierBit);
                    j++;
                    carrierIndex++;
                }
                writer.writeByte(payloadByte);
            } while (payloadByte != 0);
        }

        return output.toByteArray();
    }

    @Override
    public boolean canEmbed(byte[] carrier, byte[] payload) {
        int carrierAvailableSize = (carrier.length - BYTES_HEADER) * 2 / 3;
        int payloadSize = payload.length + BYTES_PREFIX + BYTES_SIZE;
        return carrierAvailableSize >= payloadSize;
    }

    public static void main(String[] args) throws IOException {
        byte[] carrier = {
                0,1,2,3,4,5,6,7,8,9,
                10,11,12,13,14,15,16,17,18,19,
                20,21,22,23,24,25,26,27,28,29,
                30,31,32,33,34,35,36,37,38,39,
                40,41,42,43,44,45,46,47,48,49,
                50,51,52,53,

                // 4 para los prefijos
                (byte) 0b00000001,
                (byte) 0b00000001,
                (byte) 0b00000001,
                (byte) 0b00000001,

                // 4 para el tamanio
                (byte) 0b00000000,
                (byte) 0b00000000,
                (byte) 0b00000000,
                (byte) 0b00000000,
                (byte) 0b00001000,
                (byte) 0b00001000,
                (byte) 0b00001000,
                (byte) 0b00000000,

                (byte) 0b00001000,
                (byte) 0b00001000,
                (byte) 0b00001000,
                (byte) 0b00000000,
                (byte) 0b00000000,
                (byte) 0b00000000,
                (byte) 0b00000000,
                (byte) 0b00000000,

                (byte) 0b00000000,
                (byte) 0b00000000,
                (byte) 0b00000000,
                (byte) 0b00000000,
                (byte) 0b00000000,
                (byte) 0b00000000,
                (byte) 0b00000000,
                (byte) 0b00000000,

                (byte) 0b00000000,
                (byte) 0b00000000,
                (byte) 0b00000000,
                (byte) 0b00000000,
                (byte) 0b00000001,
                (byte) 0b00000000,
                (byte) 0b00000001,
                (byte) 0b11111111,

                // texto -> 11 bytes
                (byte) 0b00011010,
                (byte) 0b00100100,
                (byte) 0b01011000,
                (byte) 0b00001111,
                (byte) 0b10000001,
                (byte) 0b11110000,
                (byte) 0b00010101,
                (byte) 0b00111010,

                (byte) 0b01100001,
                (byte) 0b00000000,
                (byte) 0b00001101,
                (byte) 0b00011000,
                (byte) 0b10101010,
                (byte) 0b01010101,
                (byte) 0b11001100,
                (byte) 0b00100010,

                (byte) 0b11111111,
                (byte) 0b00010000,
                (byte) 0b01101100,
                (byte) 0b10001000,
                (byte) 0b00000011,
                (byte) 0b11100001,
                (byte) 0b00001111,
                (byte) 0b00000001,

                (byte) 0b01010101,
                (byte) 0b00001111,
                (byte) 0b11000011,
                (byte) 0b00111100,
                (byte) 0b10010010,
                (byte) 0b01110010,
                (byte) 0b00000001,
                (byte) 0b11110000,

                (byte) 0b00101010,
                (byte) 0b01000100,
                (byte) 0b00011111,
                (byte) 0b10000001,
                (byte) 0b01101110,
                (byte) 0b00111011,
                (byte) 0b00000000,
                (byte) 0b11111100,

                (byte) 0b11001010,
                (byte) 0b10100101,
                (byte) 0b01010000,
                (byte) 0b00111111,
                (byte) 0b00000010,
                (byte) 0b01111100,
                (byte) 0b11110101,
                (byte) 0b10000000,

                (byte) 0b00000100,
                (byte) 0b11111110,
                (byte) 0b00101000,
                (byte) 0b01011001,
                (byte) 0b10001110,
                (byte) 0b01100111,
                (byte) 0b00001000,
                (byte) 0b11011000,

                (byte) 0b01011110,
                (byte) 0b00010010,
                (byte) 0b10000000,
                (byte) 0b11001100,
                (byte) 0b00111101,
                (byte) 0b10101010,
                (byte) 0b01101011,
                (byte) 0b00000001,

                (byte) 0b11110011,
                (byte) 0b00001001,
                (byte) 0b01011100,
                (byte) 0b00100101,
                (byte) 0b10011010,
                (byte) 0b00000001,
                (byte) 0b01111111,
                (byte) 0b01010100,

                (byte) 0b11110011,
                (byte) 0b00001001,
                (byte) 0b01011100,
                (byte) 0b00100101,
                (byte) 0b10011010,
                (byte) 0b00000001,
                (byte) 0b01111111,
                (byte) 0b01010100,

                (byte) 0b11110011,
                (byte) 0b00001001,
                (byte) 0b01011100,
                (byte) 0b00100101,
                (byte) 0b10011010,
                (byte) 0b00000001,
                (byte) 0b01111111,
                (byte) 0b01010100,

                (byte) 0b11110011,
                (byte) 0b00001001,
                (byte) 0b01011100,
                (byte) 0b00100101,
                (byte) 0b10011010,
                (byte) 0b00000001,
                (byte) 0b01111111,
                (byte) 0b01010100,

                (byte) 0b11110011,
                (byte) 0b00001001,
                (byte) 0b01011100,
                (byte) 0b00100101,
                (byte) 0b10011010,
                (byte) 0b00000001,
                (byte) 0b01111111,
                (byte) 0b01010100
        };

        byte[] payload = {
                (byte) 0b00000000, // 0
                (byte) 0b00000000, // 0
                (byte) 0b00000000, // 0
                (byte) 0b00001001, // 4
                (byte) 0b01101000, // 'h'
                (byte) 0b01101111, // 'o'
                (byte) 0b01101100, // 'l'
                (byte) 0b01100001,  // 'a'
                (byte) 0b00101110, // '.'
                (byte) 0b01110000, // 'p'
                (byte) 0b01101110, // 'n'
                (byte) 0b01100111, // 'g'
                (byte) 0b00000000 // '/0'
        };
        LSBI aux = new LSBI();

        byte[] embed = aux.embed(carrier, payload);
        byte[] plain = aux.extract(embed, false);

        for (byte b : plain) {
            System.out.println(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }
        System.out.println(Arrays.toString(plain));
        String texto = new String(plain);
        System.out.println(texto);
    }

}
