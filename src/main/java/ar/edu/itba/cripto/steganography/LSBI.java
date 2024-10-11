package ar.edu.itba.cripto.steganography;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class LSBI implements SteganographyMethod{
    static private int PAYLOAD_INDEX = 54;
    static private int BITS_IN_BYTE = 8;
    static private int BITS_INFORMATION = 4;
    static private int BITS_SIZE = 4;
    @Override
    public byte[] embed(byte[] carrier, byte[] payload) {
        if(!canEmbed(carrier, payload)){
            throw new IllegalArgumentException();
        }

        System.out.println("\nEntrada:");
        for (byte b : carrier) {
            System.out.println(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }

        int[] prefixReplaced = {0, 0, 0, 0}; // {00, 01, 10, 11}

        for(int i = BITS_INFORMATION; i < payload.length * BITS_IN_BYTE + BITS_INFORMATION; i++) {
            // 01010101 i*8+5 - i*8+6 - i*8+7
            int prefix = getBitValue(carrier, i * BITS_IN_BYTE + 6) + getBitValue(carrier, i * BITS_IN_BYTE + 5) * 2;

            int payloadBit = getBitValue(payload, i - BITS_INFORMATION);
            int carrierBit = getBitValue(carrier, i * BITS_IN_BYTE + 7);
            if (payloadBit == carrierBit) {
                prefixReplaced[prefix] += 1; // si son iguales sumo
            } else {
                prefixReplaced[prefix] -= 1;
                replaceBit(carrier, i * BITS_IN_BYTE + 7, payloadBit);
            }
        }

        System.out.println("\nDespues del LSB1:");
        for (byte b : carrier) {
            System.out.println(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }

        // Pone los headers de que prefijos voy a reeemplazar
        // x x x x 101010010202
        // 00 01 10 11
        // 1 0 1 1
        for(int i = 0; i < prefixReplaced.length; i++){
            if(prefixReplaced[i] > 0){
                System.out.println("El prefijo " + i + " entro");
                replaceBit(carrier, i * BITS_IN_BYTE + 7, 1);
            }else{
                replaceBit(carrier, i * BITS_IN_BYTE + 7, 0);
            }
        }

        // Hacer mas performante
        for(int i = BITS_INFORMATION; i < payload.length * BITS_IN_BYTE + BITS_INFORMATION; i++) {
            int prefix = getBitValue(carrier, i * BITS_IN_BYTE + 6) + getBitValue(carrier, i * BITS_IN_BYTE + 5) * 2;
            if (prefixReplaced[prefix] > 0) {
                if(getBitValue(carrier, i * BITS_IN_BYTE + 7) == 1){
                    replaceBit(carrier, i * BITS_IN_BYTE + 7, 0);
                }else{
                    replaceBit(carrier, i * BITS_IN_BYTE + 7, 1);
                }
            }
        }

        System.out.println("\nDespues de cambiar los invertir:");
        for (byte b : carrier) {
            System.out.println(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }

        return carrier;
    }

    @Override
    public byte[] extract(byte[] carrier) {
        boolean[] prefixReplaced = {true, true, true, true}; // {00, 01, 10, 11}

        for(int i = 0; i<BITS_INFORMATION; i++){
            int value = getBitValue(carrier, i * BITS_IN_BYTE + 7);
            if(value == 0){
                prefixReplaced[i] = false;
            }
        }

        System.out.println(prefixReplaced[0] + " " + prefixReplaced[1] + " " + prefixReplaced[2] + " " + prefixReplaced[3]);

        byte[] sizeAux = new byte[BITS_SIZE];
        int k = 0;
        int i = BITS_INFORMATION;
        int bitsToHide = 1;
        byte MASK_PAYLOAD = (byte) 0b00000001;
        int bytesNeeded = 8;
        while (i < (bytesNeeded * BITS_SIZE+ BITS_INFORMATION) ) {
            byte data;
            byte prefix;
            byte payloadByte = 0;
            for (int j = 0; j < bytesNeeded; j++) {
                prefix = (byte) ((carrier[i] & 0b00000110) >> 1);
                if (prefixReplaced[prefix]){
                    data = (byte) (((carrier[i] & MASK_PAYLOAD) ^ 0b00000001) << (8 - bitsToHide * (j + 1)));
                }else{
                    data = (byte) ((carrier[i] & MASK_PAYLOAD) << (8 - bitsToHide * (j + 1)));
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
        // Mostrar el número
        System.out.println("El número DWORD es: " + number);
        System.out.println();

        byte[] payload = new byte[BITS_SIZE + number];

        for(int j = 0; j<BITS_SIZE; j++){
            payload[j] = sizeAux[j];
        }

        k = BITS_SIZE;
        i = BITS_SIZE * BITS_IN_BYTE + BITS_INFORMATION;
        while (i < (BITS_SIZE + number) * BITS_IN_BYTE + BITS_INFORMATION ) {
            byte data;
            byte prefix;
            byte payloadByte = 0;
            for (int j = 0; j < bytesNeeded; j++) {
                prefix = (byte) ((carrier[i] & 0b00000110) >> 1);
                if (prefixReplaced[prefix]){
                    data = (byte) (((carrier[i] & MASK_PAYLOAD) ^ 0b00000001) << (8 - bitsToHide * (j + 1)));
                }else{
                    data = (byte) ((carrier[i] & MASK_PAYLOAD) << (8 - bitsToHide * (j + 1)));
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
        int bitsPayloadNeeded = ((payload.length * BITS_IN_BYTE) + BITS_INFORMATION) * BITS_IN_BYTE;

        return bitsPayloadNeeded <= bitsCarrier;
    }

    public static int getBitValue(byte[] array, int bitIndex) {
        // Calcular el índice del byte en el array y la posición del bit dentro del byte
        int byteIndex = bitIndex / 8;
        int bitPosition = 7 - (bitIndex % 8);  // De MSB a LSB

        // Crear la máscara para el bit deseado
        byte mask = (byte) (1 << bitPosition);

        // Realizar una operación AND entre el byte y la máscara, luego desplazar para obtener el bit como 0 o 1
        return (array[byteIndex] & mask) != 0 ? 1 : 0;
    }

    // 010101011 -> 2 y 1 -> 011101011
    public static void replaceBit(byte[] array, int bitIndex, int newValue) {
        // Calcular el índice del byte en el array y la posición del bit dentro del byte
        int byteIndex = bitIndex / 8;
        int bitPosition = 7 - (bitIndex % 8);  // De MSB a LSB

        // Crear la máscara para el bit a modificar
        byte mask = (byte) (1 << bitPosition);

        if (newValue == 1) {
            // Poner el bit en 1 usando OR
            array[byteIndex] |= mask;
        } else {
            // Poner el bit en 0 usando AND con la negación de la máscara
            array[byteIndex] &= ~mask;
        }
    }

    public static void main(String[] args) {
        byte[] carrier = {
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
                (byte) 0b00001010, // 4
                (byte) 0b01101000, // 'h'
                (byte) 0b01101111, // 'o'
                (byte) 0b01101100, // 'l'
                (byte) 0b01100001,  // 'a'
                (byte) 0b01100001,  // 'a'
                (byte) 0b00101110, // '.'
                (byte) 0b01110000, // 'p'
                (byte) 0b01101110, // 'n'
                (byte) 0b01100111, // 'g'
                (byte) 0b00000000 // '/0'
        };
        LSBI aux = new LSBI();

        //aux.extract(carrier);
        byte[] carrier_2 = aux.embed(carrier, payload);
        byte[] salida = aux.extract(carrier_2);

        for (byte b : salida) {
            System.out.println(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }
        System.out.println(Arrays.toString(salida));
    }

}
