package org.desviante.service.dto;

/**
 * DTO para requisição de criação de tipos de card.
 * 
 * <p>Usado para receber dados da interface para criação de novos
 * tipos de cards.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
public record CreateCardTypeRequestDTO(
    String name,        // Nome do tipo de card
    String unitLabel    // Label da unidade de progresso
) {
    
    /**
     * Valida se os dados da requisição são válidos.
     *
     * @return true se os dados são válidos, false caso contrário
     */
    public boolean isValid() {
        return name != null && !name.trim().isEmpty();
    }
    
    /**
     * Retorna o nome normalizado (sem espaços extras).
     *
     * @return nome normalizado
     */
    public String getNormalizedName() {
        return name != null ? name.trim() : "";
    }
    
    /**
     * Retorna o label da unidade normalizado (sem espaços extras).
     * Se estiver vazio, retorna "unidade" como valor padrão.
     *
     * @return label da unidade normalizado ou "unidade" se vazio
     */
    public String getNormalizedUnitLabel() {
        String normalized = unitLabel != null ? unitLabel.trim() : "";
        return normalized.isEmpty() ? "unidade" : normalized;
    }
} 