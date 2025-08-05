package org.desviante.service.dto;

import java.util.List;

/**
 * DTO para transferência de dados detalhados de um Board.
 * 
 * <p>Representa a visão completa de um board incluindo todas as suas colunas
 * e cards associados. Este DTO é utilizado principalmente para exibir o
 * conteúdo completo de um board na interface do usuário, fornecendo todos
 * os dados necessários para renderizar o Kanban board.</p>
 * 
 * <p>Contém informações essenciais do board (ID e nome) junto com a
 * hierarquia completa de colunas e seus respectivos cards. Esta estrutura
 * permite que a UI construa a visualização completa do board sem necessidade
 * de múltiplas chamadas ao backend.</p>
 * 
 * <p>Utilizado principalmente pelo TaskManagerFacade para retornar dados
 * completos do board quando solicitado pela interface do usuário.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see BoardColumnDetailDTO
 * @see TaskManagerFacade
 */
public record BoardDetailDTO(Long id, String name, List<BoardColumnDetailDTO> columns) {
}