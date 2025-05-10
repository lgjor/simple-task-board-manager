package br.com.dio.persistence.entity;

import lombok.Data;
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
}