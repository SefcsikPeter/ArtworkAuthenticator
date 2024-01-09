package artwork.authenticator.mapper;

import artwork.authenticator.dto.ArtworkResultDto;
import artwork.authenticator.entity.ArtworkResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

@Component
public class ArtworkResultMapper {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  public ArtworkResultMapper(){}

  public ArtworkResultDto entityToDto(ArtworkResult artworkResult) {
    LOG.trace("entityToDto({})", artworkResult);
    if (artworkResult == null) {
      return null;
    }
    return new ArtworkResultDto(
        artworkResult.getId(),
        artworkResult.getArtworkId(),
        artworkResult.getNeuralNetResult(),
        artworkResult.getGptResult()
    );
  }
}
