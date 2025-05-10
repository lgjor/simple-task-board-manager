package br.com.dio.service;

import br.com.dio.dto.BoardDetailsDTO;
import br.com.dio.persistence.dao.BoardColumnDAO;
import br.com.dio.persistence.dao.BoardDAO;
import br.com.dio.persistence.entity.BoardEntity;
import lombok.AllArgsConstructor;
import java.util.ArrayList;
import java.util.List;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

@AllArgsConstructor
public class BoardQueryService {

    private final Connection connection;

    public List<BoardEntity> findAll() throws SQLException {
        String sql = "SELECT id, name FROM boards"; // Defina a consulta SQL
        try (var statement = connection.createStatement();
             var resultSet = statement.executeQuery(sql)) {
            List<BoardEntity> boards = new ArrayList<>();
            while (resultSet.next()) {
                BoardEntity board = new BoardEntity();
                board.setId(resultSet.getLong("id"));
                board.setName(resultSet.getString("name"));
                boards.add(board);
            }
            System.out.println("Boards encontrados: " + boards.size()); // Log para verificar o tamanho da lista
            return boards;
        }
    }

    public Optional<BoardEntity> findById(final Long id) throws SQLException {
        var dao = new BoardDAO(connection);
        var boardColumnDAO = new BoardColumnDAO(connection);
        var optional = dao.findById(id);
        if (optional.isPresent()){
            var entity = optional.get();
            entity.setBoardColumns(boardColumnDAO.findByBoardId(entity.getId())); // Carrega as colunas do board
            return Optional.of(entity);
        }
        return Optional.empty();
    }

    public Optional<BoardDetailsDTO> showBoardDetails(final Long id) throws SQLException {
        var dao = new BoardDAO(connection);
        var boardColumnDAO = new BoardColumnDAO(connection);
        var optional = dao.findById(id);
        if (optional.isPresent()){
            var entity = optional.get();
            var columns = boardColumnDAO.findByBoardIdWithDetails(entity.getId());
            var dto = new BoardDetailsDTO(entity.getId(), entity.getName(), columns);
            return Optional.of(dto);
        }
        return Optional.empty();
    }

}
