package artwork.authenticator.mapper;

import artwork.authenticator.dto.ArtworkDetailDto;
import artwork.authenticator.dto.ArtworkResultDto;
import artwork.authenticator.dto.ResultListDto;
import artwork.authenticator.entity.ArtworkResult;
import artwork.authenticator.exception.FatalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.Map;

@Component
public class ArtworkResultMapper {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  public ArtworkResultMapper(){}

  public ArtworkResultDto entityToDto(ArtworkResult result) {
    LOG.trace("entityToDto({})", result);
    if (result == null) {
      return null;
    }
    return new ArtworkResultDto(
        result.getArtworkId(),
        result.getNeuralNetResult(),
        result.getGptResult()
    );
  }

  public ResultListDto entityToListDto(ArtworkResult result, Map<Long, ArtworkDetailDto> artworks) {
    LOG.trace("entityToListDto({})", result);
    if (result == null) {
      return null;
    }
    return new ResultListDto(
        result.getId(),
        result.getArtworkId(),
        getTitle(result, artworks),
        result.getNeuralNetResult(),
        result.getGptResult()
    );
  }

  private String getTitle(ArtworkResult result, Map<Long, ArtworkDetailDto> artworks) {
    LOG.trace("getTitle({}, {})", result, artworks);
    String title = null;
    var artworkId = result.getArtworkId();
    if (artworkId != null) {
      if (!artworks.containsKey(artworkId)) {
        throw new FatalException("Given owner map does not contain owner of this Horse (%d)".formatted(result.getId()));
      }
      title = artworks.get(artworkId).title();
    }
    return title;
  }
}
