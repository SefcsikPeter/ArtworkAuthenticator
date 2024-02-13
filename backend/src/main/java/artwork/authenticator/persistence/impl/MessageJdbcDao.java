package artwork.authenticator.persistence.impl;

import artwork.authenticator.dto.MessageDto;
import artwork.authenticator.entity.Message;
import artwork.authenticator.exception.FatalException;
import artwork.authenticator.persistence.MessageDao;
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
public class MessageJdbcDao implements MessageDao {
  private static final String TABLE_NAME = "message";
  private static final String SQL_CREATE = "INSERT INTO " + TABLE_NAME
      + " (result_id, t, user_message, gpt_response) VALUES (?, CURRENT_TIMESTAMP, ?, ?)";
  private static final String SQL_SELECT_ALL_BY_result_ID = "SELECT * FROM " + TABLE_NAME + " WHERE result_id = ? ORDER BY t ASC";
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final JdbcTemplate jdbcTemplate;
  public MessageJdbcDao(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }
  @Override
  public List<Message> getAllByResultId(Long resultId) {
    LOG.trace("getAllByResultId({})", resultId);

    List<Message> messages;
    messages = jdbcTemplate.query(SQL_SELECT_ALL_BY_result_ID, this::mapRow, resultId);
    return messages;
  }

  @Override
  public Message create(MessageDto message) {
    LOG.trace("create({})", message);

    GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(con -> {
      PreparedStatement stmt = con.prepareStatement(SQL_CREATE, Statement.RETURN_GENERATED_KEYS);
      stmt.setLong(1, message.resultId());
      if (message.userMessage() != null) {
        stmt.setString(2, message.userMessage());
      } else {
        stmt.setNull(2, Types.VARCHAR);
      }
      if (message.gptResponse() != null) {
        stmt.setString(3, message.gptResponse());
      } else {
        stmt.setNull(3, Types.VARCHAR);
      }
      return stmt;
    }, keyHolder);

    Number key = keyHolder.getKey();
    if (key == null) {
      throw new FatalException("Could not extract key for newly added artwork. There is probably a programming error…");
    }

    return new Message()
        .setId(key.longValue())
        .setResultId(message.resultId())
        .setUserMessage(message.userMessage())
        .setGptResponse(message.gptResponse());
  }

  private Message mapRow(ResultSet result, int rownum) throws SQLException {
    LOG.trace("mapRow({}, {})", result, rownum);
    return new Message()
        .setId(result.getLong("id"))
        .setResultId(result.getLong("result_id"))
        .setUserMessage(result.getString("user_message"))
        .setGptResponse(result.getString("gpt_message"));
  }
}
