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
    @Getter
    @Setter
    private boolean blocked;
    private String blockReason;
    private String unblockReason;
    @Getter
    @Setter
    private BoardEntity board;
    private BoardColumnEntity boardColumn;
    private LocalDateTime creationDate = LocalDateTime.now();
    @Getter
    @Setter
    private LocalDateTime lastUpdateDate;
    @Getter
    @Setter
    private LocalDateTime completionDate;

}