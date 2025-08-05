package org.desviante.service.dto;

import org.desviante.model.BoardGroup;

/**
 * DTO para o resumo de um Board, exibido na tabela principal.
 * AGORA INCLUI OS PERCENTUAIS DE STATUS E INFORMAÇÕES DO GRUPO.
 */
public record BoardSummaryDTO(
        Long id,
        String name,
        Integer percentInitial,
        Integer percentPending,
        Integer percentFinal,
        String status,
        BoardGroup group
) {
}