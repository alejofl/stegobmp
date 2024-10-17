package ar.edu.itba.cripto.steganography;

import java.io.IOException;
import java.util.Arrays;

public class LSB4 extends LSB1 {

    public LSB4() {
        this.BYTES_NEEDED = 2;
        this.BITS_TO_HIDE = 4;
        this.MASK_PAYLOAD = (byte) 0b00001111;
        this.MASK_CARRIER = (byte) 0b11110000;
    }

    public static void main(String[] args) throws IOException {
        byte[] carrier = {
                0,1,2,3,4,5,6,7,8,9,
                0,1,2,3,4,5,6,7,8,9,
                0,1,2,3,4,5,6,7,8,9,
                0,1,2,3,4,5,6,7,8,9,
                0,1,2,3,4,5,6,7,8,9,
                0,1,2,3,

                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0

        };

        // Array de bytes cuyos últimos bits se modificarán
        byte[] payload = {
                (byte) 0,
                (byte) 0,
                (byte) 0,
                (byte) 40,
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
                (byte) 2,
                (byte) 1,
                (byte) 2,
                (byte) 1,
                (byte) 2,
                (byte) 1,
                (byte) 2,
                (byte) 1,
                (byte) 2,
                (byte) 1,
                (byte) 2,
                (byte) 1,
                (byte) 2,
                (byte) 1,
                (byte) 2,
                (byte) 1,
                (byte) 2,
                (byte) 1,
                (byte) 2,
                (byte) 1,
                (byte) 2,
                (byte) 1,
                (byte) 2,
                (byte) 1,
                (byte) 2,
                (byte) 1,
                (byte) 2,
                (byte) 1,
                (byte) 2,
                (byte) '.', // 0b0010 1110
                (byte) 'a', //ASCII 97 0b0110 0001
                (byte) 'a',
                (byte) 'a',
                (byte) 'a',
                (byte) 'a',
                (byte) 'a',
                (byte) 'a',
                (byte) '\0',
        };
        LSB4 aux = new LSB4();

        System.out.println("LSB4 " + aux.BYTES_NEEDED);

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

        byte[] ans = aux.extract(carrier, false);

        System.out.println(Arrays.toString(ans));
    }
}
