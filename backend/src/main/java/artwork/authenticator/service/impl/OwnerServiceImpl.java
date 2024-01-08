package artwork.authenticator.service.impl;

import artwork.authenticator.dto.OwnerCreateDto;
import artwork.authenticator.dto.OwnerDto;
import artwork.authenticator.dto.OwnerSearchDto;
import artwork.authenticator.exception.NotFoundException;
import artwork.authenticator.exception.ValidationException;
import artwork.authenticator.mapper.OwnerMapper;
import artwork.authenticator.persistence.OwnerDao;
import artwork.authenticator.service.OwnerService;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OwnerServiceImpl implements OwnerService {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final OwnerDao dao;
  private final OwnerMapper mapper;

  public OwnerServiceImpl(
      OwnerDao dao,
      OwnerMapper mapper) {
    this.dao = dao;
    this.mapper = mapper;
  }

  @Override
  public OwnerDto getById(long id) throws NotFoundException {
    LOG.trace("getById({})", id);
    return mapper.entityToDto(dao.getById(id));
  }

  @Override
  public Map<Long, OwnerDto> getAllById(Collection<Long> ids) throws NotFoundException {
    LOG.trace("getAllById({})", ids);
    Map<Long, OwnerDto> owners =
        dao.getAllById(ids).stream()
            .map(mapper::entityToDto)
            .collect(Collectors.toUnmodifiableMap(OwnerDto::id, Function.identity()));
    for (final var id : ids) {
      if (!owners.containsKey(id)) {
        throw new NotFoundException("Owner with ID %d not found".formatted(id));
      }
    }
    return owners;
  }

  @Override
  public Stream<OwnerDto> search(OwnerSearchDto searchParameters) {
    LOG.trace("search({})", searchParameters);
    return dao.search(searchParameters).stream()
        .map(mapper::entityToDto);
  }

  @Override
  public OwnerDto create(OwnerCreateDto newOwner) throws ValidationException {
    LOG.trace("create({})", newOwner);
    // TODO validation
    return mapper.entityToDto(dao.create(newOwner));
  }
}
