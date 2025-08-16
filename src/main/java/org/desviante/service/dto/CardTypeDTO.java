package org.desviante.service.dto;

import org.desviante.model.CardType;

import java.time.format.DateTimeFormatter;

/**
 * DTO para transferência de dados de tipos de card.
 * 
 * <p>Usado para enviar informações de tipos de card para a interface,
 * incluindo dados formatados para exibição.</p>
 * 
 * @param id identificador único do tipo de card
 * @param name nome do tipo de card
 * @param unitLabel label da unidade de progresso
 * @param creationDate data de criação formatada como string
 * @param lastUpdateDate data da última atualização formatada como string
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see CardType
 */
public record CardTypeDTO(
    Long id,                    // Identificador único do tipo de card
    String name,                // Nome do tipo de card
    String unitLabel,           // Label da unidade de progresso
    String creationDate,        // Data de criação formatada como string
    String lastUpdateDate       // Data da última atualização formatada como string
) {
    
    /**
     * Construtor que converte um CardType em CardTypeDTO.
     *
     * @param cardType tipo de card a ser convertido
     * @return DTO com os dados do tipo de card
     */
    public static CardTypeDTO from(CardType cardType) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        String creationDate = cardType.getCreationDate() != null 
            ? cardType.getCreationDate().format(formatter) 
            : "";
            
        String lastUpdateDate = cardType.getLastUpdateDate() != null 
            ? cardType.getLastUpdateDate().format(formatter) 
            : "";
        
        return new CardTypeDTO(
            cardType.getId(),
            cardType.getName(),
            cardType.getUnitLabel(),
            creationDate,
            lastUpdateDate
        );
    }
} 