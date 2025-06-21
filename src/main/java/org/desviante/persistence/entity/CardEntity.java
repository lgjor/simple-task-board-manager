package org.desviante.persistence.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
public class CardEntity {
    private Long id;
    private String title;
    private String description;
    private boolean blocked;
    private String blockReason;
    private String unblockReason;
    private BoardColumnEntity boardColumn;
    private LocalDateTime creationDate = LocalDateTime.now();
    @Getter
    @Setter
    private LocalDateTime lastUpdateDate;
    @Getter
    @Setter
    private LocalDateTime completionDate;

    public BoardEntity getBoard() {
        // Implemente a lógica para retornar o board
        // Isso pode envolver buscar o board através da coluna
        if (this.boardColumn != null) {
            return this.boardColumn.getBoard();
        }
        return null;
    }
}