package artwork.authenticator.rest;

import artwork.authenticator.dto.ArtworkDetailDto;
import artwork.authenticator.service.ArtworkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;

@RestController
@RequestMapping(path = ArtworkEndpoint.BASE_PATH)
public class ArtworkEndpoint {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  static final String BASE_PATH = "/artwork";
  private ArtworkService service;

  public ArtworkEndpoint(ArtworkService artworkService) {
    this.service = artworkService;
  }

  @PostMapping
  public ResponseEntity<ArtworkDetailDto> analyse(@RequestBody ArtworkDetailDto artwork) {
    LOG.info("POST " + BASE_PATH);
    LOG.info("Body of request:\n{}", artwork);
    return null;
  }
}
