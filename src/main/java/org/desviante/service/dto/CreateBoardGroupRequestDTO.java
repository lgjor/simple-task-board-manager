package org.desviante.service.dto;

/**
 * DTO para requisições de criação e edição de grupos de board.
 * 
 * <p>Encapsula os dados necessários para criar ou atualizar um grupo
 * de board, incluindo validações básicas e normalização de dados.</p>
 * 
 * @param name nome do grupo
 * @param description descrição do grupo
 * @param icon ícone do grupo
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
public record CreateBoardGroupRequestDTO(
    String name,
    String description,
    String icon
) {
    
    /**
     * Construtor com validação e normalização de dados.
     * 
     * @param name nome do grupo
     * @param description descrição do grupo
     * @param icon ícone do grupo
     */
    public CreateBoardGroupRequestDTO {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do grupo é obrigatório");
        }
        
        // Normalizar dados
        name = name.trim();
        description = description != null ? description.trim() : "";
        icon = icon != null ? icon.trim() : "📁";
    }
    
    /**
     * Retorna o nome normalizado do grupo.
     * 
     * @return nome do grupo sem espaços extras
     */
    public String getNormalizedName() {
        return name.trim();
    }
    
    /**
     * Retorna a descrição normalizada do grupo.
     * 
     * @return descrição do grupo ou string vazia se nula
     */
    public String getNormalizedDescription() {
        return description != null ? description.trim() : "";
    }
    
    /**
     * Retorna o ícone normalizado do grupo.
     * 
     * @return ícone do grupo ou ícone padrão se nulo
     */
    public String getNormalizedIcon() {
        return icon != null ? icon.trim() : "📁";
    }
}
