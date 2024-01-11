package artwork.authenticator.service;

import artwork.authenticator.dto.ArtworkDetailAndResultDto;
import artwork.authenticator.dto.ArtworkDetailDto;
import artwork.authenticator.exception.NotFoundException;

public interface ArtworkService {

  /**
   * Generate predictions for {@code artwork} and store it plus the results in the database
   */
  ArtworkDetailAndResultDto analyse(ArtworkDetailDto artwork);

  /**
   * Returns {@code artwork} with the given id
   */
  ArtworkDetailDto getById(Long id) throws NotFoundException;
}
