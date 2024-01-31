package artwork.authenticator.service;

import artwork.authenticator.dto.ArtworkResultDto;
import artwork.authenticator.exception.NotFoundException;
import java.util.stream.Stream;

public interface ArtworkResultService {
  ArtworkResultDto getById(Long id) throws NotFoundException;
  Stream<ArtworkResultDto> getAll();
}
