package artwork.authenticator.rest;

import artwork.authenticator.dto.ArtworkResultDto;
import artwork.authenticator.dto.ResultListDto;
import artwork.authenticator.exception.NotFoundException;
import artwork.authenticator.service.ArtworkResultService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.lang.invoke.MethodHandles;
import java.util.stream.Stream;

@RestController
@RequestMapping(path = ResultEndpoint.BASE_PATH)
public class ResultEndpoint {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  public static final String BASE_PATH = "/results";
  private final ArtworkResultService service;

  public ResultEndpoint(ArtworkResultService service) {
    this.service = service;
  }

  @GetMapping("{id}")
  public ArtworkResultDto getById(@PathVariable long id) {
    LOG.info("GET " + BASE_PATH + "/{}", id);
    try {
      return service.getById(id);
    } catch (NotFoundException e) {
      HttpStatus status = HttpStatus.NOT_FOUND;
      logClientError(status, "Results with id %d not found".formatted(id), e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    }
  }

  @GetMapping()
  public Stream<ResultListDto> getAll() {
    LOG.info("GET " + BASE_PATH);
    return service.getAll();
  }

  private void logClientError(HttpStatus status, String message, Exception e) {
    LOG.warn("{} {}: {}: {}", status.value(), message, e.getClass().getSimpleName(), e.getMessage());
  }
}
