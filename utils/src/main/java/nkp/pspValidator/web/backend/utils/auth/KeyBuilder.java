package nkp.pspValidator.web.backend.utils.auth;

import java.io.StringWriter;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyBuilder {

    public String[] buildPublicPrivateKey() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048); // You can choose the key size you prefer
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        // Store keys in PEM files
        String publicKeyPem = storeKeyInPEMString(publicKey);
        String privateKeyPem = storeKeyInPEMString(privateKey);
        return new String[]{publicKeyPem, privateKeyPem};
    }

    private String storeKeyInPEMString(Key key) throws Exception {
        try (StringWriter writer = new StringWriter()) {
            writer.write("-----BEGIN " + (key instanceof PublicKey ? "PUBLIC" : "PRIVATE") + " KEY-----\n");
            writer.write(Base64.getEncoder().encodeToString(key.getEncoded()));
            writer.write("\n-----END " + (key instanceof PublicKey ? "PUBLIC" : "PRIVATE") + " KEY-----\n");
            return writer.toString();
        }
    }

    public PublicKey convertPemToPublicKey(String publicKeyPEM) throws Exception {
        String stripped = publicKeyPEM.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\n", "");
        System.out.println("public key stripped:");
        System.out.println(stripped);
        byte[] publicKeyBytes = Base64.getDecoder().decode(stripped);
        //byte[] publicKeyBytes = Base64.getUrlDecoder().decode(publicKeyPEM);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    public PrivateKey convertPemToPrivateKey(String privateKeyPEM) throws Exception {
        String stripped = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\n", "");
        System.out.println("private key stripped:");
        System.out.println(stripped);
        byte[] privateKeyBytes = Base64.getDecoder().decode(stripped);
        //byte[] privateKeyBytes = Base64.getUrlDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }


}
