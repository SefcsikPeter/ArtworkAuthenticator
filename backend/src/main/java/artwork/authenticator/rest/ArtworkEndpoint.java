package artwork.authenticator.rest;

import artwork.authenticator.service.ArtworkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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


}
