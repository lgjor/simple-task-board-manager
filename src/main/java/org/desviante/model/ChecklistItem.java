package org.desviante.model;

import java.time.LocalDateTime;

/**
 * Representa um item de um checklist.
 * 
 * <p>Cada item tem um texto, estado de conclusão e informações de rastreamento.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
public class ChecklistItem {
    
    private Long id;
    private Long cardId;
    private String text;
    private boolean completed;
    private int orderIndex;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    
    /**
     * Construtor padrão.
     */
    public ChecklistItem() {
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * Construtor com texto do item.
     * 
     * @param text texto do item
     */
    public ChecklistItem(String text) {
        this();
        this.text = text;
        this.completed = false;
    }
    
    /**
     * Construtor completo.
     * 
     * @param id identificador único
     * @param cardId identificador do card
     * @param text texto do item
     * @param completed se está concluído
     * @param orderIndex posição na lista
     */
    public ChecklistItem(Long id, Long cardId, String text, boolean completed, int orderIndex) {
        this();
        this.id = id;
        this.cardId = cardId;
        this.text = text;
        this.completed = completed;
        this.orderIndex = orderIndex;
    }
    
    // Getters e Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getCardId() {
        return cardId;
    }
    
    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
        if (completed && this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        } else if (!completed) {
            this.completedAt = null;
        }
    }
    
    public int getOrderIndex() {
        return orderIndex;
    }
    
    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    @Override
    public String toString() {
        return "ChecklistItem{" +
                "id=" + id +
                ", cardId=" + cardId +
                ", text='" + text + '\'' +
                ", completed=" + completed +
                ", orderIndex=" + orderIndex +
                '}';
    }
}
