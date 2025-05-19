package org.desviante.persistence.dao;

import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;

import static org.desviante.persistence.converter.OffsetDateTimeConverter.toTimestamp;

@AllArgsConstructor
public class BlockDAO {

    public void block(final String reason, final Long cardId) throws SQLException {
        var sql = "INSERT INTO BLOCKS (blocked_at, block_reason, card_id) VALUES (?, ?, ?);";
        try (Connection connection = org.desviante.persistence.config.ConnectionConfig.getConnection();
             var statement = connection.prepareStatement(sql)) {
            var i = 1;
            statement.setTimestamp(i++, toTimestamp(OffsetDateTime.now()));
            statement.setString(i++, reason);
            statement.setLong(i, cardId);
            statement.executeUpdate();
            connection.commit();
        }
    }

    public void unblock(final String reason, final Long cardId) throws SQLException{
        var sql = "UPDATE BLOCKS SET unblocked_at = ?, unblock_reason = ? WHERE card_id = ? AND unblock_reason IS NULL;";
        try (Connection connection = org.desviante.persistence.config.ConnectionConfig.getConnection();
             var statement = connection.prepareStatement(sql)) {
            var i = 1;
            statement.setTimestamp(i ++, toTimestamp(OffsetDateTime.now()));
            statement.setString(i ++, reason);
            statement.setLong(i, cardId);
            statement.executeUpdate();
        }
    }
}
