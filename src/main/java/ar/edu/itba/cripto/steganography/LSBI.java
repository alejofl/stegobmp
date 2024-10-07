package ar.edu.itba.cripto.steganography;

public class LSBI implements SteganographyMethod{
    static private int PAYLOAD_INDEX = 54;
    static private int BITS_IN_BYTE = 8;
    static private int BITS_INFORMATION = 4;
    @Override
    public byte[] embed(byte[] carrier, byte[] payload) {
        if(!canEmbed(carrier, payload)){
            throw new IllegalArgumentException();
        }
        int[] prefixReplaced = {0, 0, 0, 0};

        for(int i = BITS_INFORMATION; i < carrier.length; i++){
            int prefix = getBitValue(carrier, i*BITS_IN_BYTE+6) + getBitValue(carrier, i*BITS_IN_BYTE+5) * 2;

            int payloadBit = getBitValue(payload, i - BITS_INFORMATION);
            int carrierBit = getBitValue(carrier, i*BITS_IN_BYTE+7);
            if( payloadBit == carrierBit){
                prefixReplaced[prefix] += 1;
            }else{
                prefixReplaced[prefix] -= 1;
            }
            replaceBit(carrier, i*BITS_IN_BYTE+7, payloadBit);
        }

        System.out.println("\nDespues del LSB1:");
        for (byte b : carrier) {
            System.out.println(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }

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
        return new byte[0];
    }
    /*
    @Override
    public byte[] extract(byte[] carrier) {
        int[]
        boolean[] prefixReplaced = {true, true, true, true};
        for (int i=0; i<prefixReplaced.length; i++){
            prefixReplaced[i] = (getBitValue(carrier, i * BITS_IN_BYTE + 7) == 1);
        }
        for(int i = BITS_INFORMATION; i < carrier.length; i++) {
            int prefix = getBitValue(carrier, i * BITS_IN_BYTE + 6) + getBitValue(carrier, i * BITS_IN_BYTE + 5) * 2;
            if (prefixReplaced[prefix]) {
                if(getBitValue(carrier, i * BITS_IN_BYTE + 7) == 1){
                    byte
                }else{
                    replaceBit(carrier, i * BITS_IN_BYTE + 7, 1);
                }
            }
        }


        return new byte[0];
    }
    */

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
                // headers
                (byte) 0b11111100,
                (byte) 0b00011011,
                (byte) 0b10100101,
                (byte) 0b01010101,

                // payload
                (byte) 0b01101101,
                (byte) 0b10101010,
                (byte) 0b11110001,
                (byte) 0b01101100,
                (byte) 0b10101011,
                (byte) 0b11110000,
                (byte) 0b01101101,
                (byte) 0b10101011
        };

        byte[] payload = {
                (byte) 0b10010001,
        };
        LSBI aux = new LSBI();

        aux.embed(carrier, payload);
    }
}
