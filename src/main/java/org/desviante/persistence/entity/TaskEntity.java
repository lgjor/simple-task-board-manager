package org.desviante.persistence.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

public class TaskEntity {
    // Getters e setters
    @Setter
    @Getter
    private Long id;
    @Setter
    @Getter
    private String listTitle;
    @Setter
    @Getter
    private String title;
    @Setter
    @Getter
    private OffsetDateTime due;
    @Setter
    @Getter
    private String notes;
    @Setter
    @Getter
    private boolean sent;
    @Setter
    @Getter
    private CardEntity card;

}