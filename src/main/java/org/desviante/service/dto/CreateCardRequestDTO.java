package org.desviante.service.dto;

/**
 * DTO para encapsular os dados necessários para criar um novo Card.
 * A UI preencherá este objeto e o enviará para a Fachada.
 */
public record CreateCardRequestDTO(
        String title,
        String description,
        Long parentColumnId
) {}