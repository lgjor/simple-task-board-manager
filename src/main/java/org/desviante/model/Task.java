package org.desviante.model;

import lombok.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Task {
    private Long id;

    private String listTitle;

    private String title;

    private OffsetDateTime due;

    private String notes;

    private String googleTaskId;

    private boolean sent;

    private Long cardId;

    // --- Campos de Auditoria ---
    private LocalDateTime creationDate;

    private LocalDateTime lastUpdateDate;
}