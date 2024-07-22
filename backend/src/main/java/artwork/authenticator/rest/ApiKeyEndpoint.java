package artwork.authenticator.rest;

import artwork.authenticator.dto.ApiKeyDto;
import artwork.authenticator.service.ArtworkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@RestController
@RequestMapping(path = ApiKeyEndpoint.BASE_PATH)
public class ApiKeyEndpoint {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  static final String BASE_PATH = "/apikey";
  private final ArtworkService artworkService;

  public ApiKeyEndpoint(ArtworkService artworkService){this.artworkService = artworkService;}

  @GetMapping
  public ResponseEntity<String> getPublicKey() {
    try {
      String key = new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/public_key.pem")));
      return ResponseEntity.ok(key);
    } catch (IOException e) {
      LOG.error("Failed to retrieve public key", e);
      return ResponseEntity.status(500).body("Failed to retrieve public key");
    }
  }

  @PostMapping
  public ResponseEntity<String> setApiKey(@RequestBody ApiKeyDto encryptedApiKey) {
    try {
      LOG.info("Encrypted API key: " + encryptedApiKey.encryptedApiKey());
      PrivateKey privateKey = this.getPrivateKey();
      String decryptedApiKey = this.decrypt(encryptedApiKey.encryptedApiKey(), privateKey);
      artworkService.setApiKey(decryptedApiKey);
      System.out.println(decryptedApiKey);
      return ResponseEntity.ok("API key set successfully");
    } catch (Exception e) {
      LOG.error("Failed to set API key", e);
      return ResponseEntity.status(500).body("Failed to set API key");
    }
  }

  private PrivateKey getPrivateKey() throws Exception {
    try {
      // Read the PEM file content
      String key = new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "/private_key.pem")));

      // Remove the PEM file headers and footers
      key = key.replaceAll("-----BEGIN PRIVATE KEY-----", "")
          .replaceAll("-----END PRIVATE KEY-----", "")
          .replaceAll("\\s+", "");

      LOG.info("Private Key String: " + key);

      // Decode the base64 encoded string
      byte[] keyBytes = Base64.getDecoder().decode(key);

      // Reconstruct the private key
      PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");

      // Return the private key
      return keyFactory.generatePrivate(keySpec);
    } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new Exception("Failed to read private key.", e);
    }
  }

  private String decrypt(String encryptedKey, PrivateKey privateKey)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
    byte[] encryptedBytes = Base64.getDecoder().decode(encryptedKey);
    Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
    cipher.init(Cipher.DECRYPT_MODE, privateKey);
    byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
    return new String(decryptedBytes);
  }
}
