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
}
