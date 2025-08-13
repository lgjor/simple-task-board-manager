package org.desviante.service.progress;

import org.desviante.model.enums.ProgressType;

/**
 * Interface que define o contrato para diferentes estratégias de progresso.
 * Segue o padrão Strategy para permitir diferentes implementações de progresso.
 */
public interface ProgressStrategy {
    
    /**
     * Verifica se este tipo de progresso está habilitado.
     * 
     * @return true se o progresso está habilitado, false caso contrário
     */
    boolean isEnabled();
    
    /**
     * Retorna o nome de exibição para este tipo de progresso.
     * 
     * @return nome de exibição
     */
    String getDisplayName();
    
    /**
     * Retorna o tipo de progresso associado a esta estratégia.
     * 
     * @return tipo de progresso
     */
    ProgressType getType();
    
    /**
     * Configura a interface do usuário para este tipo de progresso.
     * 
     * @param config configuração da UI
     */
    void configureUI(ProgressUIConfig config);
    
    /**
     * Atualiza a exibição do progresso com os dados fornecidos.
     * 
     * @param data dados para atualização da exibição
     */
    void updateDisplay(ProgressDisplayData data);
    
    /**
     * Valida os dados de entrada para este tipo de progresso.
     * 
     * @param input dados de entrada para validação
     * @return resultado da validação
     */
    ProgressValidationResult validate(ProgressInputData input);
}
