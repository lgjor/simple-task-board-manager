package org.desviante.model.enums;

/**
 * Enum que define os tipos de cards suportados pelo sistema.
 *
 * <p>Cada tipo de card possui características específicas e pode ou não
 * suportar acompanhamento de progresso. O tipo CARD representa cards
 * simples sem progresso, enquanto outros tipos suportam progresso
 * através de unidades específicas.</p>
 *
 * <p>Nota: O tipo CARD foi escolhido para evitar confusão com a classe
 * Task que já existe para integração com Google Tasks API.</p>
 *
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see org.desviante.model.Task
 */
public enum CardType {
    CARD("card"),
    BOOK("páginas"), 
    VIDEO("minutos"),
    COURSE("aulas");
    
    private final String unitLabel;
    
    CardType(String unitLabel) {
        this.unitLabel = unitLabel;
    }
    
    /**
     * Retorna o label da unidade de progresso para este tipo de card.
     * 
     * @return label da unidade (ex: "páginas", "minutos", "aulas")
     */
    public String getUnitLabel() { 
        return unitLabel; 
    }
}