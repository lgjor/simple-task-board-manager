package org.desviante.service.dto;

import java.util.List;

/**
 * DTO para a visão completa e detalhada de um Board, com todas as suas colunas e cards.
 * Este é o objeto principal que a UI receberá ao abrir um board.
 */
public record BoardDetailDTO(Long id, String name, List<BoardColumnDetailDTO> columns) {
}