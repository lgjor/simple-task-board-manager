package org.desviante.service;

import org.desviante.persistence.dao.BoardColumnDAO;
import org.desviante.persistence.entity.BoardColumnEntity;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class BoardColumnQueryService {

    private final Connection connection;

    public BoardColumnQueryService(Connection connection) {
        this.connection = connection;
    }

    public Optional<BoardColumnEntity> findById(final Long id) throws SQLException {
        var dao = new BoardColumnDAO(connection);
        return dao.findById(id);
    }
}