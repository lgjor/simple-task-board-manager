package org.desviante.service.progress;

import org.desviante.model.enums.ProgressType;

/**
 * Estratégia para progresso baseado em checklist.
 * Exemplo de como implementar um novo tipo de progresso.
 * 
 * Esta implementação seria expandida para incluir:
 * - Lista de itens do checklist
 * - Estado de cada item (concluído/pendente)
 * - Cálculo de progresso baseado em itens concluídos
 * - Interface específica para gerenciar itens
 */
public class ChecklistProgressStrategy implements ProgressStrategy {
    
    @Override
    public boolean isEnabled() {
        // Para checklist, o progresso é calculado automaticamente baseado nos itens
        // Não depende de valores de spinners, então sempre retorna true
        return true;
    }
    
    @Override
    public String getDisplayName() {
        return "Checklist";
    }
    
    @Override
    public ProgressType getType() {
        return ProgressType.CHECKLIST;
    }
    
    @Override
    public void configureUI(ProgressUIConfig config) {
        // Mostrar seção de checklist
        config.getProgressContainer().setVisible(true);
        config.getProgressContainer().setManaged(true);
        
        // Para checklist, ocultar os spinners (Total/Atual) pois o progresso
        // será calculado automaticamente baseado nos itens do checklist
        config.getTotalLabel().setVisible(false);
        config.getTotalSpinner().setVisible(false);
        config.getCurrentLabel().setVisible(false);
        config.getCurrentSpinner().setVisible(false);
        
        // Ocultar o label "Progresso:" para checklist (só faz sentido em PERCENTAGE)
        config.getProgressLabel().setVisible(false);
        config.getProgressLabel().setManaged(false);
        
        // Configurar labels específicos para checklist
        config.getTotalLabel().setText("Total de itens:");
        config.getCurrentLabel().setText("Itens concluídos:");
        
        // Aqui seria adicionada a interface específica do checklist:
        // - Lista de itens
        // - Checkboxes para cada item
        // - Botões para adicionar/remover itens
    }
    
    @Override
    public void updateDisplay(ProgressDisplayData data) {
        // Este método seria implementado pelo ProgressContext
        // com lógica específica para checklist
    }
    
    @Override
    public ProgressValidationResult validate(ProgressInputData input) {
        // Validar título
        if (input.getTitle() == null || input.getTitle().trim().isEmpty()) {
            return ProgressValidationResult.error("O título não pode estar vazio.");
        }
        
        // Para checklist, aceitar qualquer valor de total/current pois o progresso
        // será calculado automaticamente baseado nos itens do checklist
        // Não validar valores de spinners para checklist
        
        return ProgressValidationResult.success();
    }
    
    /**
     * Métodos específicos para checklist que seriam implementados:
     */
    
    /**
     * Adiciona um novo item ao checklist.
     * 
     * @param itemText texto do item
     * @return true se adicionado com sucesso
     */
    public boolean addChecklistItem(String itemText) {
        // Implementação para adicionar item
        return true;
    }
    
    /**
     * Remove um item do checklist.
     * 
     * @param itemIndex índice do item
     * @return true se removido com sucesso
     */
    public boolean removeChecklistItem(int itemIndex) {
        // Implementação para remover item
        return true;
    }
    
    /**
     * Marca um item como concluído.
     * 
     * @param itemIndex índice do item
     * @param completed true se concluído, false caso contrário
     */
    public void setItemCompleted(int itemIndex, boolean completed) {
        // Implementação para marcar item como concluído
    }
    
    /**
     * Obtém a lista de itens do checklist.
     * 
     * @return lista de itens
     */
    public java.util.List<ChecklistItem> getChecklistItems() {
        // Implementação para retornar itens
        return new java.util.ArrayList<>();
    }
    
    /**
     * Classe interna para representar um item do checklist.
     */
    public static class ChecklistItem {
        private final String text;
        private boolean completed;
        
        public ChecklistItem(String text) {
            this.text = text;
            this.completed = false;
        }
        
        public String getText() { return text; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
    }
}
