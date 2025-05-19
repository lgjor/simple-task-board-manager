package org.desviante.service;

import org.desviante.dto.CardDetailsDTO;
import org.desviante.persistence.dao.CardDAO;
import lombok.AllArgsConstructor;

import java.sql.SQLException;
import java.util.Optional;

@AllArgsConstructor
public class CardQueryService {

    public Optional<CardDetailsDTO> findById(final Long id) throws SQLException {
        var dao = new CardDAO();
        return dao.findById(id);
    }

}
