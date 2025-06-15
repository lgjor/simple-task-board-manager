package org.desviante.persistence.entity;

import java.time.LocalDateTime;

public class ReminderEntity {
    private Long id;
    private LocalDateTime dateTime;
    private String message;
    private boolean sent;
    private CardEntity card;

    // Getters e setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isSent() { return sent; }
    public void setSent(boolean sent) { this.sent = sent; }

    public CardEntity getCard() { return card; }
    public void setCard(CardEntity card) { this.card = card; }
}