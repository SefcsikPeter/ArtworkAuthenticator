package artwork.authenticator.rest;

import artwork.authenticator.dto.ArtworkDetailAndResultDto;
import artwork.authenticator.dto.ArtworkDetailDto;
import artwork.authenticator.exception.NotFoundException;
import artwork.authenticator.service.ArtworkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.lang.invoke.MethodHandles;

@RestController
@RequestMapping(path = ArtworkEndpoint.BASE_PATH)
public class ArtworkEndpoint {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  static final String BASE_PATH = "/artwork";
  private final ArtworkService service;

  public ArtworkEndpoint(ArtworkService artworkService) {
    this.service = artworkService;
  }

  @PostMapping
  public ArtworkDetailAndResultDto analyse(@RequestBody ArtworkDetailDto artwork) {
    LOG.info("POST " + BASE_PATH);
    LOG.trace("Body of request:\n{}", artwork);
    return this.service.analyse(artwork);
  }

  @GetMapping("{id}")
  public ArtworkDetailDto getById(@PathVariable long id) {
    LOG.info("GET " + BASE_PATH + "/{}", id);
    try {
      return service.getById(id);
    } catch (NotFoundException e) {
      HttpStatus status = HttpStatus.NOT_FOUND;
      logClientError(status, "Artwork with id %d not found".formatted(id), e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    }
  }

  private void logClientError(HttpStatus status, String message, Exception e) {
    LOG.warn("{} {}: {}: {}", status.value(), message, e.getClass().getSimpleName(), e.getMessage());
  }
}
