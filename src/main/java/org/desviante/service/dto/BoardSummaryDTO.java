package org.desviante.service.dto;

import org.desviante.model.BoardGroup;

/**
 * DTO para transferência de dados resumidos de um Board para a tabela principal.
 * 
 * <p>Representa um resumo de board com informações essenciais para exibição
 * na tabela principal da interface do usuário. Este DTO inclui dados básicos
 * do board (ID e nome), percentuais de progresso por status, status geral
 * e informações do grupo associado.</p>
 * 
 * <p>Contém percentuais calculados para cada tipo de coluna (INITIAL, PENDING,
 * FINAL) que permitem visualizar o progresso do board de forma rápida e
 * intuitiva. O status geral fornece uma visão consolidada do estado atual
 * do board, enquanto as informações do grupo permitem organização e filtros.</p>
 * 
 * <p>Utilizado principalmente na tabela principal de boards para fornecer
 * uma visão geral de todos os boards disponíveis, permitindo ao usuário
 * identificar rapidamente o progresso e status de cada board sem necessidade
 * de abrir detalhes completos.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see BoardGroup
 * @see BoardDetailDTO
 */
public record BoardSummaryDTO(
        Long id,                    // Identificador único do board
        String name,                // Nome do board para exibição
        Integer percentInitial,     // Percentual de cards em colunas INITIAL
        Integer percentPending,     // Percentual de cards em colunas PENDING
        Integer percentFinal,       // Percentual de cards em colunas FINAL
        String status,              // Status geral consolidado do board
        BoardGroup group            // Grupo associado ao board (pode ser null)
) {
}