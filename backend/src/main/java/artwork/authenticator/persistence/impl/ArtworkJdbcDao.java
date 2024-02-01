package artwork.authenticator.persistence.impl;

import artwork.authenticator.dto.ArtworkDetailDto;
import artwork.authenticator.entity.Artwork;
import artwork.authenticator.exception.FatalException;
import artwork.authenticator.exception.NotFoundException;
import artwork.authenticator.persistence.ArtworkDao;
import artwork.authenticator.type.Artist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.lang.invoke.MethodHandles;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Repository
public class ArtworkJdbcDao implements ArtworkDao {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final String TABLE_NAME = "artwork";
  private static final String SQL_CREATE = "INSERT INTO " + TABLE_NAME
      + " (title, artist, gallery, price, description, image) VALUES (?, ?, ?, ?, ?, ?)";
  private static final String SQL_SELECT_BY_ID = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
  private static final String SQL_SELECT_ALL_BY_ID = "SELECT * FROM " + TABLE_NAME + " WHERE id IN (:ids)";
  private final JdbcTemplate jdbcTemplate;
  private final NamedParameterJdbcTemplate jdbcNamed;

  public ArtworkJdbcDao(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate jdbcNamed) {
    this.jdbcTemplate = jdbcTemplate;
    this.jdbcNamed = jdbcNamed;
  }

  @Override
  public Artwork create(ArtworkDetailDto artwork) {
    LOG.trace("create({})", artwork);

    GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(con -> {
      PreparedStatement stmt = con.prepareStatement(SQL_CREATE, Statement.RETURN_GENERATED_KEYS);
      stmt.setString(6, artwork.image());
      if (artwork.title() != null) {
        stmt.setString(1, artwork.title());
      } else {
        stmt.setNull(1, Types.VARCHAR);
      }
      if (artwork.artist() != null) {
        stmt.setString(2, artwork.artist().toString());
      } else {
        stmt.setNull(2, Types.VARCHAR);
      }
      if (artwork.gallery() != null) {
        stmt.setString(3, artwork.gallery());
      } else {
        stmt.setNull(3, Types.VARCHAR);
      }
      if (artwork.price() != null) {
        stmt.setString(4, artwork.price());
      } else {
        stmt.setNull(4, Types.VARCHAR);
      }
      if (artwork.title() != null) {
        stmt.setString(5, artwork.description());
      } else {
        stmt.setNull(5, Types.VARCHAR);
      }
      return stmt;
    }, keyHolder);
    Number key = keyHolder.getKey();
    if (key == null) {
      throw new FatalException("Could not extract key for newly added artwork. There is probably a programming error…");
    }

    return new Artwork()
        .setId(key.longValue())
        .setTitle(artwork.title())
        .setArtist(artwork.artist())
        .setGallery(artwork.gallery())
        .setPrice(artwork.price())
        .setDescription(artwork.description())
        .setImage(artwork.image())
        ;
  }

  @Override
  public Artwork getById(Long id) throws NotFoundException {
    LOG.trace("getById({})", id);
    List<Artwork> artworks;
    artworks = jdbcTemplate.query(SQL_SELECT_BY_ID, this::mapRow, id);

    if (artworks.isEmpty()) {
      throw new NotFoundException("Artwork with Id %d not found!".formatted(id));
    }
    if (artworks.size() > 1) {
      throw new FatalException("Too many artworks with Id %d! found".formatted(id));
    }
    return artworks.get(0);
  }

  @Override
  public Collection<Artwork> getAllById(Collection<Long> ids) {
    LOG.trace("getAllById({})", ids);
    var statementParams = Collections.singletonMap("ids", ids);
    return jdbcNamed.query(SQL_SELECT_ALL_BY_ID, statementParams, this::mapRow);
  }

  private Artwork mapRow(ResultSet result, int rownum) throws SQLException {
    LOG.trace("mapRow({}, {})", result, rownum);
    return new Artwork()
        .setId(result.getLong("id"))
        .setTitle(result.getString("title"))
        .setArtist(Artist.valueOf(result.getString("artist")))
        .setGallery(result.getString("gallery"))
        .setPrice(result.getString("price"))
        .setDescription(result.getString("description"))
        .setImage(result.getString("image"))
        ;
  }
}
