package artwork.authenticator.service.impl;

import artwork.authenticator.dto.ArtworkDetailDto;
import artwork.authenticator.dto.ArtworkResultDto;
import artwork.authenticator.entity.Artwork;
import artwork.authenticator.entity.ArtworkResult;
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

  public ArtworkServiceImpl(ArtworkDao artworkDao, ArtworkResultDao artworkResultDao) {
    this.artworkDao = artworkDao;
    this.artworkResultDao = artworkResultDao;
  }

  @Override
  public ArtworkResultDto analyse(ArtworkDetailDto artwork) {
    LOG.trace("create({})", artwork);
    // TODO: do the image analysis

    // Save artwork
    Artwork analysedArtwork = artworkDao.create(artwork);
    return null;
  }
}
