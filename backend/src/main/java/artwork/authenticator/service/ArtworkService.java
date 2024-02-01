package artwork.authenticator.service;

import artwork.authenticator.dto.ArtworkDetailDto;
import artwork.authenticator.exception.NotFoundException;

import java.util.Collection;
import java.util.Map;

public interface ArtworkService {

  /**
   * Generate predictions for {@code artwork} and store it plus the results in the database
   * The result contains the data as follows: [top-1 class, top-1 percentage, percentage of selected class]
   */
  Long analyse(ArtworkDetailDto artwork);

  /**
   * Returns {@code artwork} with the given id
   */
  ArtworkDetailDto getById(Long id) throws NotFoundException;

  Map<Long, ArtworkDetailDto> getAllById(Collection<Long> ids) throws NotFoundException;
}
