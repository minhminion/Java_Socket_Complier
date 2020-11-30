package socket.server.ServerHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;

public class MyKeyGenerator {
    private KeyPairGenerator keyGen;
    private KeyPair pair;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public MyKeyGenerator(int keyLength) throws NoSuchAlgorithmException, NoSuchProviderException {
        this.keyGen = KeyPairGenerator.getInstance("RSA");
        this.keyGen.initialize(keyLength);
    }
    public void createKeys() {
        this.pair = this.keyGen.generateKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
    }
    public PrivateKey getPrivateKey() {
        return this.privateKey;
    }
    public PublicKey getPublicKey() {
        return this.publicKey;
    }
}
