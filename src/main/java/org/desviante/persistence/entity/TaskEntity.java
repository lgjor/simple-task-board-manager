package org.desviante.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "tasks")
@Getter
@Setter
public class TaskEntity {
    // Getters e setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String listTitle;
    private String title;
    private OffsetDateTime due;
    private String notes;
    private boolean sent;
    @ManyToOne
    @JoinColumn(name = "card_id")
    private CardEntity card;

}