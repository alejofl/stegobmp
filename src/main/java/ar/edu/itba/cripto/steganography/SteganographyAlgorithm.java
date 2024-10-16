package ar.edu.itba.cripto.steganography;

public enum SteganographyAlgorithm {
    LSB1{
        @Override
        public SteganographyMethod getInstance() {
            return new LSB1();
        }
    },
    LSB4{
        @Override
        public SteganographyMethod getInstance() {
            return new LSB4();
        }
    },
    LSBI{
        @Override
        public SteganographyMethod getInstance() {
            return new LSBI();
        }
    };

    public abstract SteganographyMethod getInstance();
}
