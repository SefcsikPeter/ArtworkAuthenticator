package artwork.authenticator.service.impl;

import artwork.authenticator.dto.ArtworkDetailDto;
import artwork.authenticator.dto.ArtworkResultDto;
import artwork.authenticator.dto.ResultListDto;
import artwork.authenticator.entity.ArtworkResult;
import artwork.authenticator.exception.FatalException;
import artwork.authenticator.exception.NotFoundException;
import artwork.authenticator.mapper.ArtworkResultMapper;
import artwork.authenticator.persistence.ArtworkResultDao;
import artwork.authenticator.service.ArtworkResultService;
import artwork.authenticator.service.ArtworkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ArtworkResultServiceImpl implements ArtworkResultService {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final ArtworkResultMapper mapper;
  private final ArtworkResultDao dao;
  private final ArtworkService artworkService;

  public ArtworkResultServiceImpl(ArtworkResultMapper mapper, ArtworkResultDao dao, ArtworkService artworkService) {
    this.mapper = mapper;
    this.dao = dao;
    this.artworkService = artworkService;
  }
  @Override
  public ArtworkResultDto getById(Long id) throws NotFoundException {
    LOG.trace("getById({})", id);
    ArtworkResult result = dao.getById(id);
    return mapper.entityToDto(result);
  }

  @Override
  public Stream<ResultListDto> getAll() {
    LOG.trace("getAll()");
    List<ArtworkResult> results = dao.getAll();
    var artworkIds = results.stream()
        .map(ArtworkResult::getArtworkId)
        .filter(Objects::nonNull)
        .collect(Collectors.toUnmodifiableSet());
    Map<Long, ArtworkDetailDto> artworkMap;
    try {
      artworkMap = artworkService.getAllById(artworkIds);
    } catch (NotFoundException e) {
      throw new FatalException("Result references non-existent artwork", e);
    }
    return results.stream().map(result -> mapper.entityToListDto(result, artworkMap));
  }
}
