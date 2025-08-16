package org.desviante.service.progress;

/**
 * Dados de entrada para validação do progresso.
 * Encapsula os valores fornecidos pelo usuário.
 */
public class ProgressInputData {
    
    private final Integer totalUnits;
    private final Integer currentUnits;
    private final String title;
    private final String description;
    
    /**
     * Construtor para dados de entrada do progresso.
     * 
     * @param totalUnits unidades totais
     * @param currentUnits unidades atuais
     * @param title título do card
     * @param description descrição do card
     */
    public ProgressInputData(
            Integer totalUnits,
            Integer currentUnits,
            String title,
            String description) {
        
        this.totalUnits = totalUnits;
        this.currentUnits = currentUnits;
        this.title = title;
        this.description = description;
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
     * Obtém o título do card.
     * 
     * @return título do card
     */
    public String getTitle() { return title; }
    
    /**
     * Obtém a descrição do card.
     * 
     * @return descrição do card
     */
    public String getDescription() { return description; }
}
