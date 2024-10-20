# stegobmp

## Overview

This project is a steganography tool that allows you to hide messages in 24-bit BMP v3 images.

The program supports three different steganography methods:
* **LSB1**: Hides one bit of the message in the least significant bit of the image.
* **LSB4**: Hides four bits of the message in the least significant bits of the image.
* **[LSBI](https://www.jatit.org/volumes/Vol80No2/16Vol80No2.pdf)**: Improvement of LSB1 method proposed by MOHAMMED ABDUL MAJEED & ROSSILAWATI SULAIMAN.

The program also supports encryption of the message before hiding it in the image. The following encryption algorithms are available:
* **AES**: Advanced Encryption Standard. Available key sizes are 128, 192 and 256 bits.
* **3DES**: Triple Data Encryption Standard.

For the encryption, the following modes are available:
* **ECB**: Electronic Codebook Mode.
* **CBC**: Cipher Block Chaining Mode.
* **OFB**: Output Feedback Mode.
* **CFB**: Cipher Feedback Mode.

The program also supports the extraction of messages from images, allowing the user to decrypt the message if it was encrypted.

In both cases, the encryption is done using a password provided by the user. The password is used to generate the key for the encryption algorithm, using PBKDF2 with HMAC SHA-256, $10 000$ iterations and `0x00000000` as the salt.

## Build

### Prerequisites

To compile the program, you need to have the following binaries installed:

* Java 21
* Maven

### Compiling

To compile the program, run the following command:

```bash
mvn clean package
```

## Run

To run the program, use the following command:

```bash
./stegobmp <options>
```

The following options are available:

* `-embed`: Indicates that the program should embed a message in an image.
* `-extract`: Indicates that the program should extract a message from an image.
* `-in <file>`: Specifies the input file path to hide. **Required for extraction mode**.
* `-p <file>`: Specifies the carrier image path. **Required for both modes**.
* `-out <file>`: Specifies the output file path. **Required for both modes**.
* `-steg <mode>`: Specifies the steganography method. Available options are `LSB1`, `LSB4` and `LSBI`. **Required for both modes**.
* `-pass <password>`: Specifies the password to use for key generation at encryption. **If not present, the payload will not be encrypted**.
* `-a <algorithm>`: Specifies the encryption algorithm. Available options are `AES_128`, `AES_192`, `AES_256` and `DES_3`. **If `-pass` is present, defaults to `AES_128`**.
* `-m <mode>`: Specifies the encryption mode. Available options are `ECB`, `CBC`, `OFB` and `CFB`. **If `-pass` is present, defaults to `CBC`**.

## Final Remarks

This project was done in an academic environment, as part of the curriculum of Cryptography & Security from Instituto Tecnológico de Buenos Aires (ITBA)

The project was carried out by:

* [Alejo Flores Lucey](https://github.com/alejofl)
* [Andrés Carro Wetzel](https://github.com/AndresCarro)
* [Gastón Ariel François](https://github.com/francoisgaston)
* [Nehuén Gabriel Llanos](https://github.com/NehuenLlanos)
