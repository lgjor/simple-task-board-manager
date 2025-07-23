package org.desviante.service.dto;

/**
 * DTO para o resumo de um Board, exibido na tabela principal.
 * AGORA INCLUI OS PERCENTUAIS DE STATUS.
 */
public record BoardSummaryDTO(
        Long id,
        String name,
        Integer percentInitial,
        Integer percentPending,
        Integer percentFinal,
        String status
) {
}