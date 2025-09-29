package org.desviante.service.progress;

import org.desviante.model.enums.ProgressType;

/**
 * Estratégia para cards sem progresso.
 * 
 * <p>Gerencia a lógica para cards que não possuem progresso,
 * ocultando elementos de interface relacionados ao progresso
 * mas mantendo o status visível.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
public class NoProgressStrategy implements ProgressStrategy {
    
    /**
     * Construtor padrão da estratégia sem progresso.
     * 
     * <p>Esta estratégia não requer inicialização especial.</p>
     */
    public NoProgressStrategy() {
        // Estratégia padrão sem progresso
    }
    
    @Override
    public boolean isEnabled() {
        return false;
    }
    
    @Override
    public String getDisplayName() {
        return "Sem progresso";
    }
    
    @Override
    public ProgressType getType() {
        return ProgressType.NONE;
    }
    
    @Override
    public void configureUI(ProgressUIConfig config) {
        // Ocultar seção de progresso mas manter o status visível
        config.getProgressSection().setVisible(false);
        config.getProgressSection().setManaged(false);
        
        // Ocultar o label "Progresso:"
        config.getProgressLabel().setVisible(false);
        config.getProgressLabel().setManaged(false);
        
        // Manter o container de progresso visível para o status
        config.getProgressContainer().setVisible(true);
        config.getProgressContainer().setManaged(true);
        
        // Garantir que o status seja sempre visível
        if (config.getStatusValueLabel().getParent() != null) {
            config.getStatusValueLabel().getParent().setVisible(true);
            config.getStatusValueLabel().getParent().setManaged(true);
        }
    }
    
    @Override
    public void updateDisplay(ProgressDisplayData data) {
        // Este método será implementado pelo ProgressContext que tem acesso à UI
        // A estratégia apenas fornece a lógica de negócio, não manipula diretamente a UI
    }
    
    @Override
    public ProgressValidationResult validate(ProgressInputData input) {
        // Para cards sem progresso, apenas validar o título
        if (input.getTitle() == null || input.getTitle().trim().isEmpty()) {
            return ProgressValidationResult.error("O título não pode estar vazio.");
        }
        
        return ProgressValidationResult.success();
    }
}
