package org.desviante.persistence.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

public class TaskEntity {
    // Getters e setters
    @Setter
    @Getter
    private Long id;
    private String title;
    private String due;
    private String notes;
    private boolean sent;
    private CardEntity card;

    public LocalDateTime getDue() { return due; }
    public void setDue(LocalDateTime due) { this.due = due; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isSent() { return sent; }
    public void setSent(boolean sent) { this.sent = sent; }

    public CardEntity getCard() { return card; }
    public void setCard(CardEntity card) { this.card = card; }
}