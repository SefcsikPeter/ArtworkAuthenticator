package artwork.authenticator.rest;

import artwork.authenticator.service.ArtworkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
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
  public ResponseEntity<String> getPublicKey(){
    try {
      // Read the PEM file content
      String key = new String(Files.readAllBytes(Paths.get(System.getProperty("user.dir") + "\\public_key.pem")));

      // Remove the PEM file headers and footers
      key = key.replaceAll("-----BEGIN PUBLIC KEY-----", "")
          .replaceAll("-----END PUBLIC KEY-----", "")
          .replaceAll("\\s+", "");

      // Decode the base64 encoded string
      byte[] keyBytes = Base64.getDecoder().decode(key);

      // Reconstruct the public key
      X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      PublicKey publicKey = keyFactory.generatePublic(keySpec);

      // Return the base64 encoded public key
      return ResponseEntity.ok(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
    } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
      return ResponseEntity.status(500).body("Failed to retrieve public key");
    }
  }

  @PostMapping
  public Boolean setApiKey(){
    return null;
  }



}
