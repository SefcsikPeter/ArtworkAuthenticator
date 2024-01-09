package artwork.authenticator.mapper;

import artwork.authenticator.dto.ArtworkDetailDto;
import artwork.authenticator.entity.Artwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

@Component
public class ArtworkMapper {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  public ArtworkMapper(){}

  public ArtworkDetailDto entityToDto(Artwork artwork) {
    LOG.trace("entityToDto({})", artwork);
    if (artwork == null) {
      return null;
    }
    return new ArtworkDetailDto(
        artwork.getId(),
        artwork.getTitle(),
        artwork.getArtist(),
        artwork.getGallery(),
        artwork.getPrice(),
        artwork.getDescription(),
        artwork.getImage()
    );
  }
}
