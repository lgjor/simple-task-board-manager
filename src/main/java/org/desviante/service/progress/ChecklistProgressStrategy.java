package org.desviante.service.progress;

import org.desviante.model.enums.ProgressType;

/**
 * Estratégia de progresso baseada em checklist.
 * 
 * <p>Implementa o cálculo de progresso baseado em itens de checklist,
 * onde cada item pode estar marcado como concluído ou pendente.
 * O progresso é calculado automaticamente como a porcentagem de
 * itens concluídos em relação ao total.</p>
 * 
 * <p>Características principais:</p>
 * <ul>
 *   <li>Lista de itens do checklist</li>
 *   <li>Estado de cada item (concluído/pendente)</li>
 *   <li>Cálculo de progresso baseado em itens concluídos</li>
 *   <li>Interface específica para gerenciar itens</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
public class ChecklistProgressStrategy implements ProgressStrategy {
    
    /**
     * Construtor padrão da estratégia de progresso de checklist.
     * 
     * <p>Esta estratégia não requer inicialização especial.</p>
     */
    public ChecklistProgressStrategy() {
        // Estratégia padrão para checklist
    }
    
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
     * 
     * <p>Representa um item individual do checklist com seu texto
     * e estado de conclusão.</p>
     * 
     * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
     * @version 1.0
     * @since 1.0
     */
    public static class ChecklistItem {
        private final String text;
        private boolean completed;
        
        /**
         * Construtor para criar um novo item de checklist.
         * 
         * @param text texto descritivo do item
         */
        public ChecklistItem(String text) {
            this.text = text;
            this.completed = false;
        }
        
        /**
         * Obtém o texto do item.
         * 
         * @return texto descritivo do item
         */
        public String getText() { return text; }
        
        /**
         * Verifica se o item está concluído.
         * 
         * @return true se o item está concluído, false caso contrário
         */
        public boolean isCompleted() { return completed; }
        
        /**
         * Define o estado de conclusão do item.
         * 
         * @param completed true para marcar como concluído, false para marcar como pendente
         */
        public void setCompleted(boolean completed) { this.completed = completed; }
    }
}
