package artwork.authenticator.persistence;

import artwork.authenticator.dto.ArtworkResultDto;
import artwork.authenticator.entity.ArtworkResult;

public interface ArtworkResultDao {
  ArtworkResult create(ArtworkResultDto result);
}
