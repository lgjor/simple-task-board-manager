package org.desviante.service.dto;

import java.time.LocalDateTime;

/**
 * DTO para transferência de dados de itens do checklist.
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
public record ChecklistItemDTO(
    Long id,
    Long cardId,
    String text,
    boolean completed,
    int orderIndex,
    LocalDateTime createdAt,
    LocalDateTime completedAt
) {
    
    /**
     * Cria um DTO para um novo item.
     * 
     * @param cardId identificador do card
     * @param text texto do item
     * @param orderIndex posição na lista
     * @return DTO do item
     */
    public static ChecklistItemDTO createNew(Long cardId, String text, int orderIndex) {
        return new ChecklistItemDTO(
            null, // id será gerado pelo banco
            cardId,
            text,
            false, // sempre não concluído inicialmente
            orderIndex,
            LocalDateTime.now(),
            null // completedAt será null até ser concluído
        );
    }
    
    /**
     * Cria uma cópia do DTO com o estado de conclusão alterado.
     * 
     * @param completed novo estado de conclusão
     * @return nova instância do DTO
     */
    public ChecklistItemDTO withCompleted(boolean completed) {
        return new ChecklistItemDTO(
            id,
            cardId,
            text,
            completed,
            orderIndex,
            createdAt,
            completed ? LocalDateTime.now() : null
        );
    }
    
    /**
     * Cria uma cópia do DTO com o texto alterado.
     * 
     * @param newText novo texto do item
     * @return nova instância do DTO
     */
    public ChecklistItemDTO withText(String newText) {
        return new ChecklistItemDTO(
            id,
            cardId,
            newText,
            completed,
            orderIndex,
            createdAt,
            completedAt
        );
    }
    
    /**
     * Cria uma cópia do DTO com a posição alterada.
     * 
     * @param newOrderIndex nova posição na lista
     * @return nova instância do DTO
     */
    public ChecklistItemDTO withOrderIndex(int newOrderIndex) {
        return new ChecklistItemDTO(
            id,
            cardId,
            text,
            completed,
            newOrderIndex,
            createdAt,
            completedAt
        );
    }
}
