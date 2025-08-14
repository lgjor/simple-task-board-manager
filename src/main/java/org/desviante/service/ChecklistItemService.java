package org.desviante.service;

import org.desviante.model.CheckListItem;
import org.desviante.repository.CheckListItemRepository;
import org.desviante.service.dto.ChecklistItemDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service para gerenciar a lógica de negócio dos itens do checklist.
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
@Service
public class ChecklistItemService {
    
    private final CheckListItemRepository checklistItemRepository;
    
    public ChecklistItemService(CheckListItemRepository checklistItemRepository) {
        this.checklistItemRepository = checklistItemRepository;
    }
    
    /**
     * Adiciona um novo item ao checklist de um card.
     * 
     * @param cardId identificador do card
     * @param text texto do item
     * @return DTO do item criado
     */
    public ChecklistItemDTO addItem(Long cardId, String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("O texto do item não pode estar vazio.");
        }
        
        // Obter a próxima posição disponível
        int nextOrderIndex = checklistItemRepository.countByCardId(cardId);
        
        // Criar o item
        CheckListItem item = new CheckListItem(text.trim());
        item.setCardId(cardId);
        item.setOrderIndex(nextOrderIndex);
        
        // Salvar no banco
        CheckListItem savedItem = checklistItemRepository.save(item);
        
        return convertToDTO(savedItem);
    }
    
    /**
     * Remove um item do checklist.
     * 
     * @param itemId identificador do item
     * @return true se removido com sucesso
     */
    public boolean removeItem(Long itemId) {
        return checklistItemRepository.deleteById(itemId);
    }
    
    /**
     * Atualiza o texto de um item.
     * 
     * @param itemId identificador do item
     * @param newText novo texto
     * @return DTO do item atualizado
     */
    public Optional<ChecklistItemDTO> updateItemText(Long itemId, String newText) {
        if (newText == null || newText.trim().isEmpty()) {
            throw new IllegalArgumentException("O texto do item não pode estar vazio.");
        }
        
        Optional<CheckListItem> itemOpt = checklistItemRepository.findById(itemId);
        if (itemOpt.isEmpty()) {
            return Optional.empty();
        }
        
        CheckListItem item = itemOpt.get();
        item.setText(newText.trim());
        
        boolean updated = checklistItemRepository.update(item);
        return updated ? Optional.of(convertToDTO(item)) : Optional.empty();
    }
    
    /**
     * Marca um item como concluído ou não concluído.
     * 
     * @param itemId identificador do item
     * @param completed estado de conclusão
     * @return true se atualizado com sucesso
     */
    public boolean toggleItemCompleted(Long itemId, boolean completed) {
        return checklistItemRepository.updateCompleted(itemId, completed);
    }
    
    /**
     * Move um item para uma nova posição.
     * 
     * @param itemId identificador do item
     * @param newOrderIndex nova posição
     * @return true se movido com sucesso
     */
    public boolean moveItem(Long itemId, int newOrderIndex) {
        if (newOrderIndex < 0) {
            throw new IllegalArgumentException("A posição deve ser maior ou igual a zero.");
        }
        
        return checklistItemRepository.updateOrderIndex(itemId, newOrderIndex);
    }
    
    /**
     * Busca todos os itens de um card.
     * 
     * @param cardId identificador do card
     * @return lista de DTOs dos itens
     */
    public List<ChecklistItemDTO> getItemsByCardId(Long cardId) {
        List<CheckListItem> items = checklistItemRepository.findByCardIdOrderByOrderIndex(cardId);
        return items.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Busca um item por ID.
     * 
     * @param itemId identificador do item
     * @return DTO do item ou empty
     */
    public Optional<ChecklistItemDTO> getItemById(Long itemId) {
        return checklistItemRepository.findById(itemId)
                .map(this::convertToDTO);
    }
    
    /**
     * Conta quantos itens um card tem.
     * 
     * @param cardId identificador do card
     * @return número de itens
     */
    public int getItemCount(Long cardId) {
        return checklistItemRepository.countByCardId(cardId);
    }
    
    /**
     * Conta quantos itens concluídos um card tem.
     * 
     * @param cardId identificador do card
     * @return número de itens concluídos
     */
    public int getCompletedItemCount(Long cardId) {
        return checklistItemRepository.countCompletedByCardId(cardId);
    }
    
    /**
     * Calcula o progresso percentual do checklist.
     * 
     * @param cardId identificador do card
     * @return percentual de conclusão (0-100)
     */
    public double getProgressPercentage(Long cardId) {
        int totalItems = getItemCount(cardId);
        if (totalItems == 0) {
            return 0.0;
        }
        
        int completedItems = getCompletedItemCount(cardId);
        return (double) completedItems / totalItems * 100.0;
    }
    
    /**
     * Remove todos os itens de um card.
     * 
     * @param cardId identificador do card
     * @return número de itens removidos
     */
    public int removeAllItemsFromCard(Long cardId) {
        return checklistItemRepository.deleteByCardId(cardId);
    }
    
    /**
     * Converte um ChecklistItem para ChecklistItemDTO.
     * 
     * @param item item a ser convertido
     * @return DTO do item
     */
    private ChecklistItemDTO convertToDTO(CheckListItem item) {
        return new ChecklistItemDTO(
            item.getId(),
            item.getCardId(),
            item.getText(),
            item.isCompleted(),
            item.getOrderIndex(),
            item.getCreatedAt(),
            item.getCompletedAt()
        );
    }
}
