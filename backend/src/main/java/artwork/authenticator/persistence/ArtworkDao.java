package artwork.authenticator.persistence;

import artwork.authenticator.dto.ArtworkDetailDto;
import artwork.authenticator.entity.Artwork;

public interface ArtworkDao {
  Artwork create(ArtworkDetailDto artwork);
}
