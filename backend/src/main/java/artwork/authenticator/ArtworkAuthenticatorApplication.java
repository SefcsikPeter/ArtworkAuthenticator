package artwork.authenticator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@SpringBootApplication
public class ArtworkAuthenticatorApplication {

  public static void main(String[] args) {
    generateRsaKeys();
    SpringApplication.run(ArtworkAuthenticatorApplication.class, args);
  }

  private static void generateRsaKeys() {
    // Generate the key pair
    try {
      KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
      keyGen.initialize(2048);
      KeyPair pair = keyGen.generateKeyPair();

      // Get the public and private keys
      PublicKey publicKey = pair.getPublic();
      PrivateKey privateKey = pair.getPrivate();

      try {
        // Save the public key in PEM format
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey.getEncoded());
        String publicKeyPEM = "-----BEGIN PUBLIC KEY-----\n"
            + Base64.getEncoder().encodeToString(x509EncodedKeySpec.getEncoded())
            + "\n-----END PUBLIC KEY-----";
        Files.write(Paths.get("public_key.pem"), publicKeyPEM.getBytes(), StandardOpenOption.CREATE);

        // Save the private key in PEM format
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKey.getEncoded());
        String privateKeyPEM = "-----BEGIN PRIVATE KEY-----\n"
            + Base64.getEncoder().encodeToString(pkcs8EncodedKeySpec.getEncoded())
            + "\n-----END PRIVATE KEY-----";
        Files.write(Paths.get("private_key.pem"), privateKeyPEM.getBytes(), StandardOpenOption.CREATE);
      } catch (IOException e) {
        System.out.println("Could not save RSA keys");
      }

      System.out.println("RSA keys generated and saved to files.");
    } catch (NoSuchAlgorithmException e) {
      System.out.println("KeyPairGenerator not available");
    }
  }

}
