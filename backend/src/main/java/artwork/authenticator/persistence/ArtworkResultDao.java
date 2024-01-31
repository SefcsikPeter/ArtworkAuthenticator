package artwork.authenticator.persistence;

import artwork.authenticator.dto.ArtworkResultDto;
import artwork.authenticator.entity.ArtworkResult;
import artwork.authenticator.exception.NotFoundException;

import java.util.List;

public interface ArtworkResultDao {
  ArtworkResult create(ArtworkResultDto result);

  ArtworkResult getById(Long id) throws NotFoundException;

  List<ArtworkResult> getAll();
}
