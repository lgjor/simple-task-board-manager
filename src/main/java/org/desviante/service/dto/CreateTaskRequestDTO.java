package org.desviante.service.dto;

import java.time.LocalDateTime;

// Este record servirá para passar os dados do diálogo para o backend.
public record CreateTaskRequestDTO(
        String listTitle,
        String title,
        String notes,
        LocalDateTime due,
        Long cardId
) {
}