package org.desviante.service.dto;

import java.util.List;

/**
 * DTO para os detalhes de uma Coluna, incluindo seus Cards.
 */
public record BoardColumnDetailDTO(Long id, String name, List<CardDetailDTO> cards) {
}