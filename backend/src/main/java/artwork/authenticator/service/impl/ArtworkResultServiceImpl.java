package artwork.authenticator.service.impl;

import artwork.authenticator.dto.ArtworkResultDto;
import artwork.authenticator.entity.ArtworkResult;
import artwork.authenticator.mapper.ArtworkResultMapper;
import artwork.authenticator.persistence.ArtworkResultDao;
import artwork.authenticator.service.ArtworkResultService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.sql.SQLException;

@Component
public class ArtworkResultServiceImpl implements ArtworkResultService {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final ArtworkResultMapper mapper;
  private final ArtworkResultDao dao;

  public ArtworkResultServiceImpl(ArtworkResultMapper mapper, ArtworkResultDao dao) {
    this.mapper = mapper;
    this.dao = dao;
  }
  @Override
  public ArtworkResultDto getById(Long id) throws SQLException {
    LOG.trace("getById({})", id);
    ArtworkResult result = dao.getById(id);
    return mapper.entityToDto(result);
  }
}
