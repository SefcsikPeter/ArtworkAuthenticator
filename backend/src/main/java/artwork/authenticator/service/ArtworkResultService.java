package artwork.authenticator.service;

import artwork.authenticator.dto.ArtworkResultDto;
import artwork.authenticator.exception.NotFoundException;

public interface ArtworkResultService {
  ArtworkResultDto getById(Long id) throws NotFoundException;
}
