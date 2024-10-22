package ar.edu.itba.cripto.steganography;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class LSBI implements SteganographyMethod{
    static private final int BYTES_HEADER = 54;
    static private final int BITS_IN_BYTE = 8;
    static private final int BYTES_PREFIX = 4;
    static private int BYTES_SIZE = 4;
    static private final byte MASK_PAYLOAD = (byte) 0b00000001;
    static private final byte MASK_PREFIX = (byte) 0b00000110;
    static private byte MASK_CARRIER = (byte) 0b11111110;

    @Override
    public byte[] embed(byte[] carrier, byte[] payload) {
        if(!canEmbed(carrier, payload)){
            throw new IllegalArgumentException();
        }

        int[] prefixReplaced = {0, 0, 0, 0}; // {00, 01, 10, 11}
        int prefix;
        int byteIndex, bitPosition;
        byte mask, payloadBit, carrierBit;

        for(int payloadIndex = 0, carrierIndex = BYTES_HEADER + BYTES_PREFIX; payloadIndex < payload.length * BITS_IN_BYTE; carrierIndex++) {
            // El header no cuenta para los colores
            // 54 % 3 == 0 => eso no me jode
            // Azul - Verde - Rojo (no lo toco)
            if(carrierIndex%3!=2){

                prefix = (byte) ((carrier[carrierIndex] & MASK_PREFIX) >> 1);
                carrierBit = (byte) (carrier[carrierIndex] & MASK_PAYLOAD);

                byteIndex = payloadIndex / 8;
                bitPosition = 7 - (payloadIndex % 8);
                mask = (byte) (1 << bitPosition);
                payloadBit = (byte) ((payload[byteIndex] & mask) >> bitPosition);

                if (payloadBit == carrierBit) {
                    prefixReplaced[prefix] += 1; // si son iguales sumo
                } else {
                    prefixReplaced[prefix] -= 1; // si son diferentes resto y remplazo
                    carrier[carrierIndex] = (byte) ((carrier[carrierIndex] & MASK_CARRIER) | payloadBit);
                }
                payloadIndex++;
            }
        }

        // Pone los headers de que prefijos voy a reeemplazar
        for(int i = 0, carrierIndex; i < prefixReplaced.length; i++){
            carrierIndex = i + BYTES_HEADER;
            if(prefixReplaced[i] > 0){
                carrier[carrierIndex] = (byte) ((carrier[carrierIndex] & MASK_CARRIER) | 1);
            }else{
                carrier[carrierIndex] = (byte) ((carrier[carrierIndex] & MASK_CARRIER) | 0);
            }
        }

        // Hacer mas performante
        for(int payloadIndex = 0, carrierIndex = BYTES_HEADER + BYTES_PREFIX; payloadIndex < payload.length * BITS_IN_BYTE; carrierIndex++) {
            if(carrierIndex%3!=2){
                prefix = (byte) ((carrier[carrierIndex] & MASK_PREFIX) >> 1);

                if (prefixReplaced[prefix] > 0) {
                    if((carrier[carrierIndex] & MASK_PAYLOAD) == 1){
                        carrier[carrierIndex] = (byte) ((carrier[carrierIndex] & MASK_CARRIER) | 0);
                    }else{
                        carrier[carrierIndex] = (byte) ((carrier[carrierIndex] & MASK_CARRIER) | 1);
                    }
                }
                payloadIndex++;
            }
        }

        return carrier;
    }

    @Override
    public byte[] extract(byte[] carrier, boolean isEncrypted) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        DataOutputStream writer = new DataOutputStream(output);

        // SETEAR QUE PREFIJOS FUERON CAMBIADOS
        boolean[] prefixReplaced = {true, true, true, true}; // {00, 01, 10, 11}
        for(int i = 0, carrierIndex = BYTES_HEADER; i< BYTES_PREFIX; i++, carrierIndex++){
            if((carrier[carrierIndex] & MASK_PAYLOAD) == 0){
                prefixReplaced[i] = false;
            }
        }


        byte[] sizeAux = new byte[BYTES_SIZE];
        int k = 0;
        int i = 0;
        int carrierIndex = BYTES_PREFIX + BYTES_HEADER;
        byte prefix, carrierBit, payloadByte = 0;


        // OBTENER EL TAMAÑO DEL PAYLOAD
        while (i < BITS_IN_BYTE * BYTES_SIZE) {
            payloadByte = 0;
            for (int j = 0; j < BITS_IN_BYTE;) {
                if(carrierIndex%3!=2) {
                    prefix = (byte) ((carrier[carrierIndex] & MASK_PREFIX) >> 1);
                    if (prefixReplaced[prefix]) {
                        carrierBit = (byte) (((carrier[carrierIndex] & MASK_PAYLOAD) ^ MASK_PAYLOAD) << (7 - j));
                    } else {
                        carrierBit = (byte) ((carrier[carrierIndex] & MASK_PAYLOAD) << (7 - j));
                    }
                    payloadByte = (byte) (payloadByte | carrierBit);
                    i++;
                    j++;
                }
                carrierIndex++;
            }
            writer.write(payloadByte);
            sizeAux[k++] = payloadByte;
        }

        int size = ((sizeAux[0] & 0xFF) << 24) | // Byte más significativo
                ((sizeAux[1] & 0xFF) << 16) | // Segundo byte
                ((sizeAux[2] & 0xFF) << 8)  | // Tercer byte
                (sizeAux[3] & 0xFF);         // Byte menos significativo

        // OBTENER EL PAYLOAD
        i = 0;
        while (i < size * BITS_IN_BYTE || (!isEncrypted && payloadByte != 0)) {
            payloadByte = 0;
            for (int j = 0; j < BITS_IN_BYTE; ) {
                if(carrierIndex%3!=2) {
                    prefix = (byte) ((carrier[carrierIndex] & MASK_PREFIX) >> 1);
                    if (prefixReplaced[prefix]) {
                        carrierBit = (byte) (((carrier[carrierIndex] & MASK_PAYLOAD) ^ MASK_PAYLOAD) << (7 - j));
                    } else {
                        carrierBit = (byte) ((carrier[carrierIndex] & MASK_PAYLOAD) << (7 - j));
                    }
                    payloadByte = (byte) (payloadByte | carrierBit);
                    i++;
                    j++;
                }
                carrierIndex++;
            }
            writer.write(payloadByte);
        }

        return output.toByteArray();
    }

    @Override
    public boolean canEmbed(byte[] carrier, byte[] payload) {
        int bitsCarrier = carrier.length * BITS_IN_BYTE; // (carrier.length - PAYLOAD_INDEX) * BITS_IN_BYTE;
        int bitsPayloadNeeded = ((payload.length * BITS_IN_BYTE) + BYTES_PREFIX) * BITS_IN_BYTE;

        return bitsPayloadNeeded <= bitsCarrier;
    }

    public static void main(String[] args) throws IOException {
        byte[] carrier = {
                0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,

                // 4 para los prefijos
                (byte) 0b10000001,
                (byte) 0b10000001,
                (byte) 0b10000001,
                (byte) 0b10000001,

                // 4 => 6 para el tamanio
                (byte) 0b11111111,
                (byte) 0b11111111,
                (byte) 0b11111111,
                (byte) 0b11111111,
                (byte) 0b11111111,
                (byte) 0b11111111,
                (byte) 0b11111111,
                (byte) 0b11111111,

                (byte) 0b11111111,
                (byte) 0b11111111,
                (byte) 0b11111111,
                (byte) 0b11111111,
                (byte) 0b11111111,
                (byte) 0b11111111,
                (byte) 0b11111111,
                (byte) 0b11111111,

                (byte) 0b11111111,
                (byte) 0b11111111,
                (byte) 0b11111111,
                (byte) 0b11111111,
                (byte) 0b11111111,
                (byte) 0b11111111,
                (byte) 0b11111111,
                (byte) 0b11111111,

                (byte) 0b11111111,
                (byte) 0b11111111,
                (byte) 0b11111111,
                (byte) 0b11111111,
                (byte) 0b11111111,
                (byte) 0b11111111,
                (byte) 0b11111111,
                (byte) 0b11111111,

                (byte) 0b11111111,
                (byte) 0b11111111,
                (byte) 0b11111111,
                (byte) 0b11111111,
                (byte) 0b11111111,
                (byte) 0b11111111,
                (byte) 0b11111111,
                (byte) 0b11111111,

                (byte) 0b11111111,
                (byte) 0b11111111,
                (byte) 0b11111111,
                (byte) 0b11111111,
                (byte) 0b11111111,
                (byte) 0b11111111,
                (byte) 0b11111111,
                (byte) 0b11111111,
                // texto -> 28 bytes
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
                (byte) 0b01010100,

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
                (byte) 0b00000101, // 4
                (byte) 0b01101000, // 'h'
                (byte) 0b01101111, // 'o'
                (byte) 0b01101100, // 'l'
                (byte) 0b01100001,  // 'a'
                (byte) 0b00101110, // '.'
                (byte) 0b01110000, // 'p'
                (byte) 0b01101110, // 'n'
                (byte) 0b01100111, // 'g'
                (byte) 0b01100111, // 'g'
                (byte) 0b00000000 // '/0'
        };
        LSBI aux = new LSBI();

        byte[] embed = aux.embed(carrier, payload);
        int i =0;
        for (byte b : embed) {
            System.out.println(String.format("%d - %8s", i, Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
            i++;
        }

        byte[] plain = aux.extract(embed, false);

        for (byte b : plain) {
            System.out.println(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }
        System.out.println(Arrays.toString(plain));
        String texto = new String(plain);
        System.out.println(texto);
    }

}
