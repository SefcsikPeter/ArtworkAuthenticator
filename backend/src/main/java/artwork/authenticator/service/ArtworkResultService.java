package artwork.authenticator.service;

import artwork.authenticator.dto.ArtworkResultDto;

import java.sql.SQLException;

public interface ArtworkResultService {
  ArtworkResultDto getById(Long id) throws SQLException;
}
