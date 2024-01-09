package artwork.authenticator.mapper;

import artwork.authenticator.dto.ArtworkDetailAndResultDto;
import artwork.authenticator.dto.ArtworkResultDto;
import artwork.authenticator.entity.Artwork;
import artwork.authenticator.entity.ArtworkResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

@Component
public class ArtworkResultMapper {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  public ArtworkResultMapper(){}

  public ArtworkDetailAndResultDto entityToDto(Artwork artwork, ArtworkResult artworkResult) {
    LOG.trace("entityToDto({}, {})", artwork, artworkResult);
    if (artworkResult == null) {
      return null;
    }
    return new ArtworkDetailAndResultDto(
        artwork.getId(),
        artworkResult.getId(),
        artwork.getTitle(),
        artwork.getArtist(),
        artwork.getGallery(),
        artwork.getPrice(),
        artwork.getDescription(),
        artwork.getImage(),
        artworkResult.getNeuralNetResult(),
        artworkResult.getGptResult()
    );
  }
}
