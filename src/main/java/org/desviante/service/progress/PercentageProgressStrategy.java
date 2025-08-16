package org.desviante.service.progress;

import org.desviante.model.enums.ProgressType;

/**
 * Estratégia para progresso percentual.
 * 
 * <p>Gerencia a lógica específica para progresso baseado em porcentagem,
 * incluindo validações de valores e configuração da interface de usuário.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
public class PercentageProgressStrategy implements ProgressStrategy {
    
    /**
     * Construtor padrão da estratégia de progresso percentual.
     * 
     * <p>Esta estratégia não requer inicialização especial.</p>
     */
    public PercentageProgressStrategy() {
        // Estratégia padrão para progresso percentual
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
    
    @Override
    public String getDisplayName() {
        return "Percentual";
    }
    
    @Override
    public ProgressType getType() {
        return ProgressType.PERCENTAGE;
    }
    
    @Override
    public void configureUI(ProgressUIConfig config) {
        // Mostrar toda a seção de progresso
        config.getProgressContainer().setVisible(true);
        config.getProgressContainer().setManaged(true);
        
        // Configurar labels padrão
        config.getTotalLabel().setText("Total:");
        config.getCurrentLabel().setText("Atual:");
        
        // Mostrar o label "Progresso:"
        config.getProgressLabel().setVisible(true);
        config.getProgressLabel().setManaged(true);
    }
    
    @Override
    public void updateDisplay(ProgressDisplayData data) {
        // Este método será implementado pelo ProgressContext que tem acesso à UI
        // A estratégia apenas fornece a lógica de negócio, não manipula diretamente a UI
    }
    
    @Override
    public ProgressValidationResult validate(ProgressInputData input) {
        // Validar título
        if (input.getTitle() == null || input.getTitle().trim().isEmpty()) {
            return ProgressValidationResult.error("O título não pode estar vazio.");
        }
        
        // Validar total
        if (input.getTotalUnits() == null || input.getTotalUnits() <= 0) {
            return ProgressValidationResult.error("O total deve ser maior que zero.");
        }
        
        // Validar valor atual
        if (input.getCurrentUnits() == null || input.getCurrentUnits() < 0) {
            return ProgressValidationResult.error("O valor atual não pode ser negativo.");
        }
        
        // Validar que valor atual não seja maior que total
        if (input.getCurrentUnits() > input.getTotalUnits()) {
            return ProgressValidationResult.error("O valor atual não pode ser maior que o total.");
        }
        
        // Validar intervalos
        if (input.getTotalUnits() > 9999) {
            return ProgressValidationResult.error("O total não pode ser maior que 9999.");
        }
        if (input.getCurrentUnits() > 9999) {
            return ProgressValidationResult.error("O valor atual não pode ser maior que 9999.");
        }
        
        return ProgressValidationResult.success();
    }
}
