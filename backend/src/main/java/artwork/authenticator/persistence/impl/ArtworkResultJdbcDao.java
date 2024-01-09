package artwork.authenticator.persistence.impl;

import artwork.authenticator.dto.ArtworkDetailDto;
import artwork.authenticator.dto.ArtworkResultDto;
import artwork.authenticator.entity.Artwork;
import artwork.authenticator.entity.ArtworkResult;
import artwork.authenticator.exception.FatalException;
import artwork.authenticator.persistence.ArtworkResultDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.lang.invoke.MethodHandles;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;

@Repository
public class ArtworkResultJdbcDao implements ArtworkResultDao {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final String TABLE_NAME = "artwork_result";
  private static final String SQL_CREATE = "INSERT INTO " + TABLE_NAME
      + " (artwork_id, neural_net_result, gpt_result) VALUES (?, ?, ?)";
  private final JdbcTemplate jdbcTemplate;

  public ArtworkResultJdbcDao(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public ArtworkResult create(ArtworkResultDto result) {
    LOG.trace("create({})", result);

    GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(con -> {
      PreparedStatement stmt = con.prepareStatement(SQL_CREATE, Statement.RETURN_GENERATED_KEYS);
      stmt.setLong(1, result.artworkId());
      if (result.neuralNetResult() != null) {
        stmt.setString(2, result.neuralNetResult());
      } else {
        stmt.setNull(2, Types.VARCHAR);
      }
      if (result.gptResult() != null) {
        stmt.setString(3, result.gptResult());
      } else {
        stmt.setNull(3, Types.VARCHAR);
      }
      return stmt;
    }, keyHolder);
    Number key = keyHolder.getKey();
    if (key == null) {
      throw new FatalException("Could not extract key for newly added artwork. There is probably a programming error…");
    }

    return new ArtworkResult()
        .setId(key.longValue())
        .setArtworkId(result.artworkId())
        .setNeuralNetResult(result.gptResult())
        .setGptResult(result.gptResult())
        ;
  }
}
