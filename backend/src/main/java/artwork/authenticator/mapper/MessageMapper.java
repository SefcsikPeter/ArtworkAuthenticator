package artwork.authenticator.mapper;

import artwork.authenticator.dto.MessageListDto;
import artwork.authenticator.entity.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

@Component
public class MessageMapper {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  public MessageMapper(){}

  public MessageListDto entityToListDto(Message message) {
    LOG.trace("entityToListDto({})", message);

    if (message == null) {
      return null;
    }

    return new MessageListDto(
        message.getUserMessage(),
        message.getGptResponse()
    );
  }
}
