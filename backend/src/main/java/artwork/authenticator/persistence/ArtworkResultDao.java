package artwork.authenticator.persistence;

import artwork.authenticator.dto.ArtworkResultDto;
import artwork.authenticator.entity.ArtworkResult;

import java.sql.SQLException;

public interface ArtworkResultDao {
  ArtworkResult create(ArtworkResultDto result);

  ArtworkResult getById(Long id) throws SQLException;
}
