package artwork.authenticator.service;

import artwork.authenticator.dto.ArtworkDetailDto;
import artwork.authenticator.dto.ArtworkResultDto;

public interface ArtworkService {

  ArtworkResultDto analyse(ArtworkDetailDto artwork);
}
