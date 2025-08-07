package org.desviante.service.dto;

import java.time.LocalDateTime;

/**
 * DTO para representação de grupos de board na interface de usuário.
 * 
 * <p>Fornece uma estrutura de dados otimizada para exibição em tabelas
 * e componentes de interface, incluindo formatação adequada de datas
 * e informações essenciais para gerenciamento.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
public record BoardGroupDTO(
    Long id,
    String name,
    String description,
    String color,
    String icon,
    String creationDate
) {
    
    /**
     * Construtor para criar DTO a partir de entidade BoardGroup.
     * 
     * @param id identificador único do grupo
     * @param name nome do grupo
     * @param description descrição do grupo
     * @param color cor do grupo em formato hexadecimal
     * @param icon ícone do grupo
     * @param creationDate data de criação formatada
     */
    public BoardGroupDTO {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome não pode ser nulo ou vazio");
        }
    }
}
