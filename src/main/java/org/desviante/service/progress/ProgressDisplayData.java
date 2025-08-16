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
    
    /**
     * Construtor para dados de exibição do progresso.
     * 
     * @param totalUnits unidades totais
     * @param currentUnits unidades atuais
     * @param progressPercentage percentual de progresso
     * @param statusText texto do status
     * @param statusCssClass classe CSS do status
     */
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
    
    /**
     * Obtém o número total de unidades.
     * 
     * @return número total de unidades
     */
    public Integer getTotalUnits() { return totalUnits; }
    
    /**
     * Obtém o número de unidades atuais.
     * 
     * @return número de unidades atuais
     */
    public Integer getCurrentUnits() { return currentUnits; }
    
    /**
     * Obtém o percentual de progresso.
     * 
     * @return percentual de progresso
     */
    public Double getProgressPercentage() { return progressPercentage; }
    
    /**
     * Obtém o texto do status.
     * 
     * @return texto do status
     */
    public String getStatusText() { return statusText; }
    
    /**
     * Obtém a classe CSS do status.
     * 
     * @return classe CSS do status
     */
    public String getStatusCssClass() { return statusCssClass; }
}
