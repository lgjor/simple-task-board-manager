package org.desviante.service;

import org.desviante.dto.CardDetailsDTO;
import org.desviante.persistence.dao.CardDAO;
import org.desviante.persistence.entity.CardEntity;

import java.sql.SQLException;
import java.util.Optional;

public class CardQueryService {

    private final CardDAO cardDAO;

    public CardQueryService(CardDAO cardDAO) {
        this.cardDAO = cardDAO;
    }

    public Optional<CardEntity> findById(final Long id) throws SQLException {
        return cardDAO.findById(id);
    }
}
