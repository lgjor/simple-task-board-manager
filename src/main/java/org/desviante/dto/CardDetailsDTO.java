package org.desviante.dto;

import java.time.OffsetDateTime;

public record CardDetailsDTO(Long id,
                             String title,
                             String description,
                             boolean blocked,
                             OffsetDateTime blockedAt,
                             String blockReason,
                             int blocksAmount,
                             Long columnId,
                             String columnName,
                             Long boardId,
                             String boardName
) {
    public Long boardColumnId() {
        return columnId;
    }

    public String boardColumnName() {
        return columnName;
    }
}
