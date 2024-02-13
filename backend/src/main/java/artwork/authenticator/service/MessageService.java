package artwork.authenticator.service;

import artwork.authenticator.dto.MessageListDto;
import artwork.authenticator.exception.NotFoundException;

import java.util.stream.Stream;

public interface MessageService {
  Stream<MessageListDto> getAllByResultId(Long resultId);
  void create(Long resultId, String userMessage) throws NotFoundException;
}
