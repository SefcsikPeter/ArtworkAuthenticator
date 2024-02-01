package artwork.authenticator.persistence;

import artwork.authenticator.dto.ArtworkDetailDto;
import artwork.authenticator.entity.Artwork;
import artwork.authenticator.exception.NotFoundException;

import java.util.Collection;

public interface ArtworkDao {
  Artwork create(ArtworkDetailDto artwork);

  Artwork getById(Long id) throws NotFoundException;

  Collection<Artwork> getAllById(Collection<Long> ids);
}
