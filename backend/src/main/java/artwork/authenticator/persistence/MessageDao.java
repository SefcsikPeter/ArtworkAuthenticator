package artwork.authenticator.persistence;

import artwork.authenticator.dto.MessageDto;
import artwork.authenticator.entity.Message;

import java.util.List;

public interface MessageDao {
  List<Message> getAllByResultId(Long resultId);
  Message create(MessageDto message);
}
