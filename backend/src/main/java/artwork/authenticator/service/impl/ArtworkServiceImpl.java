package artwork.authenticator.service.impl;

import artwork.authenticator.dto.ArtworkDetailAndResultDto;
import artwork.authenticator.dto.ArtworkDetailDto;
import artwork.authenticator.dto.ArtworkResultDto;
import artwork.authenticator.entity.Artwork;
import artwork.authenticator.entity.ArtworkResult;
import artwork.authenticator.mapper.ArtworkResultMapper;
import artwork.authenticator.persistence.ArtworkDao;
import artwork.authenticator.persistence.ArtworkResultDao;
import artwork.authenticator.service.ArtworkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;

@Service
public class ArtworkServiceImpl implements ArtworkService {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final ArtworkDao artworkDao;
  private final ArtworkResultDao artworkResultDao;
  private final ArtworkResultMapper mapper;

  public ArtworkServiceImpl(ArtworkDao artworkDao, ArtworkResultDao artworkResultDao, ArtworkResultMapper mapper) {
    this.artworkDao = artworkDao;
    this.artworkResultDao = artworkResultDao;
    this.mapper = mapper;
  }

  @Override
  public ArtworkDetailAndResultDto analyse(ArtworkDetailDto artwork) {
    LOG.trace("analyse({})", artwork);
    // Save artwork
    Artwork analysedArtwork = artworkDao.create(artwork);

    // TODO: do the image analysis
    Long artworkId = analysedArtwork.getId();
    String neuralNetResult = "this is a test for the net result";
    String gptResult = "this is a test for the gpt result";

    ArtworkResultDto resultDto = new ArtworkResultDto(artworkId, neuralNetResult, gptResult);
    ArtworkResult result = artworkResultDao.create(resultDto);

    return mapper.entityToDto(analysedArtwork, result);
  }
}
