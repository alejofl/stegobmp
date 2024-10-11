package ar.edu.itba.cripto.steganography;

import java.util.Arrays;

public class LSBI implements SteganographyMethod{
    static private int BITS_IN_BYTE = 8;
    static private int BITS_PREFIX = 4;
    static private int BITS_SIZE = 4;
    static private byte MASK_PAYLOAD = (byte) 0b00000001;
    static private byte MASK_PREFIX = (byte) 0b00000110;
    static private byte MASK_CARRIER = (byte) 0b11111110;

    @Override
    public byte[] embed(byte[] carrier, byte[] payload) {
        if(!canEmbed(carrier, payload)){
            throw new IllegalArgumentException();
        }
        int[] prefixReplaced = {0, 0, 0, 0}; // {00, 01, 10, 11}
        int prefix;

        // Arranco desde BITS_PREFIX porque tengo que dejar 4 libres para los prefijos
        for(int i = BITS_PREFIX; i < payload.length * BITS_IN_BYTE + BITS_PREFIX; i++) {
            prefix = (byte) ((carrier[i] & MASK_PREFIX) >> 1);

            int byteIndex = (i-BITS_PREFIX) / 8;
            int bitPosition = 7 - ((i-BITS_PREFIX) % 8);  // De MSB a LSB
            byte mask = (byte) (1 << bitPosition);

            byte payloadBit = (byte) ((payload[byteIndex] & mask) >> bitPosition);
            byte carrierBit = (byte) (carrier[i] & MASK_PAYLOAD);

            if (payloadBit == carrierBit) {
                prefixReplaced[prefix] += 1; // si son iguales sumo
            } else {
                prefixReplaced[prefix] -= 1;
                carrier[i] = (byte) ((carrier[i] & MASK_CARRIER) | payloadBit);
            }
        }

        // Pone los headers de que prefijos voy a reeemplazar
        for(int i = 0; i < prefixReplaced.length; i++){
            if(prefixReplaced[i] > 0){
                carrier[i] = (byte) ((carrier[i] & MASK_CARRIER) | 1);
            }else{
                carrier[i] = (byte) ((carrier[i] & MASK_CARRIER) | 0);
            }
        }

        // Hacer mas performante
        for(int i = BITS_PREFIX; i < payload.length * BITS_IN_BYTE + BITS_PREFIX; i++) {
            prefix = (byte) ((carrier[i] & MASK_PREFIX) >> 1);

            if (prefixReplaced[prefix] > 0) {
                if((carrier[i] & MASK_PAYLOAD) == 1){
                    carrier[i] = (byte) ((carrier[i] & MASK_CARRIER) | 0);
                }else{
                    carrier[i] = (byte) ((carrier[i] & MASK_CARRIER) | 1);
                }
            }
        }

        return carrier;
    }

    @Override
    public byte[] extract(byte[] carrier) {
        boolean[] prefixReplaced = {true, true, true, true}; // {00, 01, 10, 11}

        for(int i = 0; i< BITS_PREFIX; i++){
            if((carrier[i] & MASK_PAYLOAD) == 0){
                prefixReplaced[i] = false;
            }
        }

        byte[] sizeAux = new byte[BITS_SIZE];
        int k = 0;
        int i = BITS_PREFIX;
        byte MASK_PAYLOAD = (byte) 0b00000001;
        int bytesNeeded = 8;
        while (i < (bytesNeeded * BITS_SIZE+ BITS_PREFIX) ) {
            byte data;
            byte prefix;
            byte payloadByte = 0;
            for (int j = 0; j < bytesNeeded; j++) {
                prefix = (byte) ((carrier[i] & 0b00000110) >> 1);
                if (prefixReplaced[prefix]){
                    data = (byte) (((carrier[i] & MASK_PAYLOAD) ^ 0b00000001) << (8 - (j + 1)));
                }else{
                    data = (byte) ((carrier[i] & MASK_PAYLOAD) << (8 - (j + 1)));
                }
                payloadByte = (byte) (payloadByte | data);
                i++;
            }
            sizeAux[k++] = payloadByte;
        }

        int number = ((sizeAux[0] & 0xFF) << 24) | // Byte más significativo
                    ((sizeAux[1] & 0xFF) << 16) | // Segundo byte
                    ((sizeAux[2] & 0xFF) << 8)  | // Tercer byte
                    (sizeAux[3] & 0xFF);         // Byte menos significativo

        byte[] payload = new byte[BITS_SIZE + number];

        for(int j = 0; j<BITS_SIZE; j++){
            payload[j] = sizeAux[j];
        }

        k = BITS_SIZE;
        i = BITS_SIZE * BITS_IN_BYTE + BITS_PREFIX;
        while (i < (BITS_SIZE + number) * BITS_IN_BYTE + BITS_PREFIX) {
            byte data;
            byte prefix;
            byte payloadByte = 0;
            for (int j = 0; j < bytesNeeded; j++) {
                prefix = (byte) ((carrier[i] & 0b00000110) >> 1);
                if (prefixReplaced[prefix]){
                    data = (byte) (((carrier[i] & MASK_PAYLOAD) ^ 0b00000001) << (8 -  (j + 1)));
                }else{
                    data = (byte) ((carrier[i] & MASK_PAYLOAD) << (8 - (j + 1)));
                }
                payloadByte = (byte) (payloadByte | data);
                i++;
            }
            payload[k++] = payloadByte;
        }

        return payload;
    }

    @Override
    public boolean canEmbed(byte[] carrier, byte[] payload) {
        int bitsCarrier = carrier.length * BITS_IN_BYTE; // (carrier.length - PAYLOAD_INDEX) * BITS_IN_BYTE;
        int bitsPayloadNeeded = ((payload.length * BITS_IN_BYTE) + BITS_PREFIX) * BITS_IN_BYTE;

        return bitsPayloadNeeded <= bitsCarrier;
    }

    public static void main(String[] args) {
        byte[] carrier = {
                //0,1,2,3,4,5,6,7,8,9,
                //10,11,12,13,14,15,16,17,18,19,
                //20,21,22,23,24,25,26,27,28,29,
                //30,31,32,33,34,35,36,37,38,39,
                //40,41,42,43,44,45,46,47,48,49,
                //50,51,52,53,

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
        byte[] plain = aux.extract(embed);

        for (byte b : plain) {
            System.out.println(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }
        System.out.println(Arrays.toString(plain));
        String texto = new String(plain);
        System.out.println(texto);
    }

}
