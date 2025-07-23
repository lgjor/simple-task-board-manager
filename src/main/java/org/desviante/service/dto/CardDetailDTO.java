package org.desviante.service.dto;

/**
 * DTO para os detalhes de um Card a ser exibido na UI.
 */
public record CardDetailDTO(
        Long id,
        String title,
        String description,
        String creationDate,
        String lastUpdateDate,
        String completionDate
) {
}