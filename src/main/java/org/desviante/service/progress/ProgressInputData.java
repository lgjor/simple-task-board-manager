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
    
    // Getters
    public Integer getTotalUnits() { return totalUnits; }
    public Integer getCurrentUnits() { return currentUnits; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
}
