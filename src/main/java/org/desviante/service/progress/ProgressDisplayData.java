package org.desviante.service.progress;

/**
 * Dados para exibição do progresso.
 * Encapsula os valores necessários para atualizar a interface.
 */
public class ProgressDisplayData {
    
    private final Integer totalUnits;
    private final Integer currentUnits;
    private final Double progressPercentage;
    private final String statusText;
    private final String statusCssClass;
    
    public ProgressDisplayData(
            Integer totalUnits,
            Integer currentUnits,
            Double progressPercentage,
            String statusText,
            String statusCssClass) {
        
        this.totalUnits = totalUnits;
        this.currentUnits = currentUnits;
        this.progressPercentage = progressPercentage;
        this.statusText = statusText;
        this.statusCssClass = statusCssClass;
    }
    
    // Getters
    public Integer getTotalUnits() { return totalUnits; }
    public Integer getCurrentUnits() { return currentUnits; }
    public Double getProgressPercentage() { return progressPercentage; }
    public String getStatusText() { return statusText; }
    public String getStatusCssClass() { return statusCssClass; }
}
