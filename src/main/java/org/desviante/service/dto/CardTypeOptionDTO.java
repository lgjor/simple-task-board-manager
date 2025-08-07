package org.desviante.service.dto;

/**
 * DTO para representar uma opção de tipo de card na interface.
 * 
 * <p>Representa um tipo de card que pode ser selecionado
 * pelo usuário na interface.
 * incluindo os tipos padrão pré-cadastrados pelo data initializer.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
public record CardTypeOptionDTO(
    String id,           // ID único do tipo de card
    String name,         // Nome do tipo
    String unitLabel,    // Label da unidade
    Long cardTypeId    // ID do tipo de card no banco
) {
    
    /**
     * Cria uma opção a partir de um tipo de card.
     *
     * @param cardType DTO do tipo de card
     * @return CardTypeOptionDTO representando o tipo de card
     */
    public static CardTypeOptionDTO fromCardType(CardTypeDTO cardType) {
        return new CardTypeOptionDTO(
            cardType.id().toString(),
            cardType.name(),
            cardType.unitLabel(),
            cardType.id()
        );
    }
    
    /**
     * Retorna o nome formatado para exibição na interface.
     *
     * @return nome formatado com a unidade
     */
    public String getDisplayName() {
        return name + " (" + unitLabel + ")";
    }
} 