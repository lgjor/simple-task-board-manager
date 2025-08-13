package org.desviante.service.progress;

import org.desviante.model.enums.ProgressType;
import org.desviante.model.enums.BoardColumnKindEnum;

import java.util.HashMap;
import java.util.Map;

/**
 * Contexto que gerencia as estratégias de progresso.
 * Coordena a interface do usuário com as estratégias específicas.
 */
public class ProgressContext {
    
    private ProgressStrategy currentStrategy;
    private final Map<ProgressType, ProgressStrategy> strategies;
    private ProgressUIConfig uiConfig;
    
    public ProgressContext() {
        this.strategies = new HashMap<>();
        initializeStrategies();
    }
    
    /**
     * Inicializa as estratégias disponíveis.
     */
    private void initializeStrategies() {
        strategies.put(ProgressType.PERCENTAGE, new PercentageProgressStrategy());
        strategies.put(ProgressType.NONE, new NoProgressStrategy());
        strategies.put(ProgressType.CHECKLIST, new ChecklistProgressStrategy());
    }
    
    /**
     * Define a configuração da UI.
     * 
     * @param uiConfig configuração da UI
     */
    public void setUIConfig(ProgressUIConfig uiConfig) {
        this.uiConfig = uiConfig;
    }
    
    /**
     * Define a estratégia atual baseada no tipo de progresso.
     * 
     * @param progressType tipo de progresso
     */
    public void setStrategy(ProgressType progressType) {
        this.currentStrategy = strategies.get(progressType);
        if (this.currentStrategy == null) {
            // Fallback para NONE se a estratégia não existir
            this.currentStrategy = strategies.get(ProgressType.NONE);
        }
    }
    
    /**
     * Configura a interface do usuário para o tipo de progresso atual.
     */
    public void configureUI() {
        if (currentStrategy != null && uiConfig != null) {
            currentStrategy.configureUI(uiConfig);
        }
    }
    
    /**
     * Atualiza a exibição do progresso com os dados fornecidos.
     * 
     * @param totalUnits unidades totais
     * @param currentUnits unidades atuais
     * @param columnKind tipo da coluna
     */
    public void updateDisplay(Integer totalUnits, Integer currentUnits, BoardColumnKindEnum columnKind) {
        if (currentStrategy == null || uiConfig == null) {
            return;
        }
        
        // Calcular progresso percentual somente para tipo PERCENTAGE
        Double progressPercentage = null;
        if (currentStrategy.isEnabled()
                && currentStrategy.getType() == ProgressType.PERCENTAGE
                && totalUnits != null && totalUnits > 0) {
            int current = currentUnits != null ? currentUnits : 0;
            progressPercentage = Math.min(100.0, (double) current / totalUnits * 100);
        }
        
        // Determinar status baseado na coluna
        String statusText = getStatusText(columnKind);
        String statusCssClass = getStatusCssClass(columnKind);
        
        // Criar dados de exibição
        ProgressDisplayData displayData = new ProgressDisplayData(
            totalUnits, currentUnits, progressPercentage, statusText, statusCssClass
        );
        
        // Atualizar a UI
        updateUIFromData(displayData);
    }
    
    /**
     * Valida os dados de entrada.
     * 
     * @param input dados de entrada
     * @return resultado da validação
     */
    public ProgressValidationResult validate(ProgressInputData input) {
        if (currentStrategy == null) {
            return ProgressValidationResult.error("Estratégia de progresso não definida.");
        }
        
        return currentStrategy.validate(input);
    }
    
    /**
     * Verifica se o progresso atual está habilitado.
     * 
     * @return true se habilitado, false caso contrário
     */
    public boolean isProgressEnabled() {
        return currentStrategy != null && currentStrategy.isEnabled();
    }
    
    /**
     * Obtém o nome de exibição da estratégia atual.
     * 
     * @return nome de exibição
     */
    public String getCurrentDisplayName() {
        return currentStrategy != null ? currentStrategy.getDisplayName() : "Desconhecido";
    }
    
    /**
     * Atualiza a UI com os dados fornecidos.
     * 
     * @param data dados para atualização
     */
    private void updateUIFromData(ProgressDisplayData data) {
        // Atualizar valores dos spinners
        if (data.getTotalUnits() != null) {
            uiConfig.getTotalSpinner().getValueFactory().setValue(data.getTotalUnits());
        }
        if (data.getCurrentUnits() != null) {
            uiConfig.getCurrentSpinner().getValueFactory().setValue(data.getCurrentUnits());
        }
        
        // Atualizar label de progresso
        if (data.getProgressPercentage() != null) {
            uiConfig.getProgressValueLabel().setText(String.format("%.1f%%", data.getProgressPercentage()));
        } else {
            uiConfig.getProgressValueLabel().setText("");
        }
        
        // Atualizar status
        if (data.getStatusText() != null) {
            uiConfig.getStatusValueLabel().setText(data.getStatusText());
        }
        
        // Aplicar classe CSS do status
        if (data.getStatusCssClass() != null) {
            uiConfig.getStatusValueLabel().getStyleClass().removeAll(
                "status-not-started", "status-in-progress", "status-completed", "status-unknown"
            );
            uiConfig.getStatusValueLabel().getStyleClass().add(data.getStatusCssClass());
        }
    }
    
    /**
     * Obtém o texto do status baseado no tipo da coluna.
     * 
     * @param columnKind tipo da coluna
     * @return texto do status
     */
    private String getStatusText(BoardColumnKindEnum columnKind) {
        if (columnKind == null) {
            return "Desconhecido";
        }
        
        switch (columnKind) {
            case INITIAL: return "Não iniciado";
            case PENDING: return "Em andamento";
            case FINAL: return "Concluído";
            default: return "Desconhecido";
        }
    }
    
    /**
     * Obtém a classe CSS do status baseado no tipo da coluna.
     * 
     * @param columnKind tipo da coluna
     * @return classe CSS do status
     */
    private String getStatusCssClass(BoardColumnKindEnum columnKind) {
        if (columnKind == null) {
            return "status-unknown";
        }
        
        switch (columnKind) {
            case INITIAL: return "status-not-started";
            case PENDING: return "status-in-progress";
            case FINAL: return "status-completed";
            default: return "status-unknown";
        }
    }
}
