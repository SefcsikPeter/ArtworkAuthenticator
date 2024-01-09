package artwork.authenticator.service;

import artwork.authenticator.dto.ArtworkDetailAndResultDto;
import artwork.authenticator.dto.ArtworkDetailDto;
import artwork.authenticator.dto.ArtworkResultDto;

public interface ArtworkService {

  ArtworkDetailAndResultDto analyse(ArtworkDetailDto artwork);
}
