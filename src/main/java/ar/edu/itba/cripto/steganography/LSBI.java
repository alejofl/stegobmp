package ar.edu.itba.cripto.steganography;

import java.nio.ByteBuffer;

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

        for(int i = BITS_INFORMATION; i < carrier.length; i++) {
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
        for(int i = BITS_INFORMATION; i < carrier.length; i++) {
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
                data = (byte) ((carrier[i] & MASK_PAYLOAD) << (8 - bitsToHide * (j + 1)));
                if (!prefixReplaced[prefix]) {
                    data = (byte) (data ^ 0b00000001); // uso XOR
                }
                payloadByte = (byte) (payloadByte | data);
                i++;
            }
            sizeAux[k++] = payloadByte;
        }

        int number = ((sizeAux[3] & 0xFF) << 24) | // Byte más significativo
                    ((sizeAux[2] & 0xFF) << 16) | // Segundo byte
                    ((sizeAux[1] & 0xFF) << 8)  | // Tercer byte
                    (sizeAux[0] & 0xFF);         // Byte menos significativo

        // Mostrar el número
        System.out.println("El número DWORD es: " + number);
        System.out.println();

        return new byte[0];
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
                (byte) 0b00000000,
                (byte) 0b00000000,
                (byte) 0b00000000,
                (byte) 0b00000000,
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
        };

        byte[] payload = {
                (byte) 0b10010001,
        };
        LSBI aux = new LSBI();

        aux.extract(carrier);
        //aux.embed(carrier, payload);
    }
}
