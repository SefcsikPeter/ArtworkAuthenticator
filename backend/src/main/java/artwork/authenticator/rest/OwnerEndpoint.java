package artwork.authenticator.rest;

import artwork.authenticator.dto.OwnerDto;
import artwork.authenticator.dto.OwnerSearchDto;
import artwork.authenticator.service.OwnerService;
import java.lang.invoke.MethodHandles;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(OwnerEndpoint.BASE_PATH)
public class OwnerEndpoint {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  static final String BASE_PATH = "/owners";

  private final OwnerService service;

  public OwnerEndpoint(OwnerService service) {
    this.service = service;
  }

  @GetMapping
  public Stream<OwnerDto> search(OwnerSearchDto searchParameters) {
    LOG.info("GET " + BASE_PATH + " query parameters: {}", searchParameters);
    return service.search(searchParameters);
  }

}
