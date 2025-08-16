package org.desviante.model;

import lombok.*;
import org.desviante.model.enums.BoardColumnKindEnum;

/**
 * Representa uma coluna de um quadro no sistema de gerenciamento de tarefas.
 * 
 * <p>Uma coluna define uma etapa ou categoria dentro de um quadro, permitindo
 * organizar as tarefas em diferentes estados ou fases do processo. Cada coluna
 * tem um tipo específico (kind) que define seu comportamento e uma ordem
 * de exibição no quadro.</p>
 * 
 * <p>Exemplos de colunas típicas: "A Fazer", "Em Andamento", "Revisão", "Concluído".</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see Board
 * @see BoardColumnKindEnum
 */
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
public class BoardColumn {
    
    /**
     * Identificador único da coluna.
     * <p>Usado como chave primária na persistência e para operações
     * de igualdade e hash code.</p>
     * 
     * @return identificador único da coluna
     * @param id novo identificador único da coluna
     */
    private Long id;

    /**
     * Nome da coluna.
     * <p>Representa o título da coluna que será exibido no quadro,
     * como "A Fazer", "Em Andamento", etc.</p>
     * 
     * @return nome da coluna
     * @param name novo nome da coluna
     */
    private String name;

    /**
     * Índice de ordem da coluna no quadro.
     * <p>Define a posição da coluna em relação às outras colunas do mesmo quadro.
     * Valores menores aparecem primeiro (da esquerda para a direita).</p>
     * 
     * @return índice de ordem da coluna no quadro
     * @param orderIndex novo índice de ordem da coluna no quadro
     */
    private int orderIndex;

    /**
     * Tipo da coluna.
     * <p>Define o comportamento e características específicas da coluna,
     * como se permite adicionar tarefas, se é uma coluna de entrada/saída, etc.</p>
     * 
     * @return tipo da coluna
     * @param kind novo tipo da coluna
     * @see BoardColumnKindEnum
     */
    private BoardColumnKindEnum kind;

    /**
     * Identificador do quadro ao qual esta coluna pertence.
     * <p>Representa a chave estrangeira para a tabela 'boards'.
     * Em uma abordagem com JDBC, os relacionamentos são representados
     * diretamente pelos IDs, em vez de referências a objetos.</p>
     * 
     * @return identificador do quadro ao qual esta coluna pertence
     * @param boardId novo identificador do quadro ao qual esta coluna pertence
     * @see Board
     */
    private Long boardId;
}
