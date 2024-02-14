package artwork.authenticator.rest;

import artwork.authenticator.dto.GPTResponseDto;
import artwork.authenticator.dto.UserMessageDto;
import artwork.authenticator.exception.NotFoundException;
import artwork.authenticator.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.lang.invoke.MethodHandles;

@RestController
@RequestMapping(path = MessageEndpoint.BASE_PATH)
public class MessageEndpoint {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  public static final String BASE_PATH = "/messages";
  private final MessageService service;

  public MessageEndpoint(MessageService service) {
    this.service = service;
  }

  @PostMapping
  public GPTResponseDto create(@RequestBody UserMessageDto userMessage) {
    LOG.trace("create({})", userMessage);
    LOG.error(userMessage.userMessage());
    try {
      return service.create(userMessage);
    } catch (NotFoundException e) {
      HttpStatus status = HttpStatus.NOT_FOUND;
      logClientError(status, "Result with id %d not found".formatted(userMessage.resultId()), e);
      throw new ResponseStatusException(status, e.getMessage(), e);
    }
  }

  private void logClientError(HttpStatus status, String message, Exception e) {
    LOG.warn("{} {}: {}: {}", status.value(), message, e.getClass().getSimpleName(), e.getMessage());
  }
}
