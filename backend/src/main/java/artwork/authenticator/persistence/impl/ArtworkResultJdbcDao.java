package artwork.authenticator.persistence.impl;

import artwork.authenticator.dto.ArtworkResultDto;
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

@Repository
public class ArtworkResultJdbcDao implements ArtworkResultDao {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final String TABLE_NAME = "artwork_result";
  private static final String SQL_CREATE = "INSERT INTO " + TABLE_NAME
      + " (artwork_id, neural_net_result, gpt_result) VALUES (?, ?, ?)";
  private static final String SQL_SELECT_BY_ID = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
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
        .setNeuralNetResult(result.neuralNetResult())
        .setGptResult(result.gptResult())
        ;
  }

  @Override
  public ArtworkResult getById(Long id) throws SQLException {
    LOG.trace("getById({})", id);
    List<ArtworkResult> results;
    results = jdbcTemplate.query(SQL_SELECT_BY_ID, this::mapRow, id);
    if (results.size() == 0) {
      throw new SQLException("Result with id %d not found!".formatted(id));
    }
    if (results.size() > 1) {
      throw new FatalException("There are more results with id %d than one".formatted(id));
    }
    return results.get(0);
  }

  private ArtworkResult mapRow(ResultSet result, int rownum) throws SQLException {
    LOG.trace("mapRow({}, {})", result, rownum);
    return new ArtworkResult()
        .setId(result.getLong("id"))
        .setArtworkId(result.getLong("artwork_id"))
        .setNeuralNetResult(result.getString("neural_net_result"))
        .setGptResult(result.getString("gpt_result"))
        ;
  }
}
