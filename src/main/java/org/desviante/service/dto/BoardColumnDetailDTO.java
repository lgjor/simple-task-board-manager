package org.desviante.service.dto;

import java.util.List;

/**
 * DTO para transferência de dados detalhados de uma Coluna do Board.
 * 
 * <p>Representa uma coluna do Kanban board incluindo todos os seus cards
 * associados. Este DTO é utilizado para estruturar a hierarquia de dados
 * que compõe um board completo, fornecendo informações essenciais da
 * coluna junto com a lista de cards que ela contém.</p>
 * 
 * <p>Contém o identificador único da coluna, seu nome de exibição e a
 * lista completa de cards que pertencem a esta coluna. Esta estrutura
 * permite que a UI renderize cada coluna do Kanban com seus respectivos
 * cards de forma organizada e hierárquica.</p>
 * 
 * <p>Utilizado como componente do BoardDetailDTO para compor a estrutura
 * completa de um board, sendo parte da cadeia de transferência de dados
 * entre o backend e a interface do usuário.</p>
 * 
 * @param id identificador único da coluna do board
 * @param name nome de exibição da coluna
 * @param cards lista de cards que pertencem a esta coluna
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see BoardDetailDTO
 * @see CardDetailDTO
 */
public record BoardColumnDetailDTO(Long id, String name, List<CardDetailDTO> cards) {
}