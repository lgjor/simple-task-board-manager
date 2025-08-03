package org.desviante.model;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Representa uma tarefa (card) no sistema de gerenciamento de tarefas.
 * 
 * <p>Um card é a unidade fundamental de trabalho no sistema, representando
 * uma tarefa específica que pode ser movida entre colunas de um quadro.
 * Cada card possui informações como título, descrição, datas de criação,
 * atualização e conclusão, além de estar associado a uma coluna específica.</p>
 * 
 * <p>Os cards são os elementos que os usuários movem, editam e acompanham
 * durante o processo de trabalho, permitindo visualizar o progresso das
 * atividades no quadro.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see BoardColumn
 * @see java.time.LocalDateTime
 */
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {
    
    /**
     * Identificador único do card.
     * <p>Usado como chave primária na persistência e para operações
     * de igualdade e hash code.</p>
     */
    private Long id;

    /**
     * Título da tarefa.
     * <p>Representa o nome ou título descritivo da tarefa que será
     * exibido no card na interface do usuário.</p>
     */
    private String title;

    /**
     * Descrição detalhada da tarefa.
     * <p>Contém informações adicionais sobre a tarefa, como requisitos,
     * instruções, observações ou qualquer detalhe relevante para o trabalho.</p>
     */
    private String description;

    /**
     * Data e hora de criação do card.
     * <p>Este campo é automaticamente preenchido quando um novo card é criado
     * e não deve ser modificado posteriormente. Útil para auditoria e histórico.</p>
     */
    private LocalDateTime creationDate;

    /**
     * Data e hora da última atualização do card.
     * <p>Este campo é atualizado automaticamente sempre que o card é modificado,
     * permitindo acompanhar quando foi a última alteração realizada.</p>
     */
    private LocalDateTime lastUpdateDate;

    /**
     * Data e hora de conclusão da tarefa.
     * <p>Este campo é preenchido quando a tarefa é marcada como concluída,
     * geralmente quando o card é movido para uma coluna final do quadro.</p>
     */
    private LocalDateTime completionDate;

    /**
     * Identificador da coluna onde o card está localizado.
     * <p>Representa a chave estrangeira para a tabela 'board_columns'.
     * Em uma abordagem com JDBC, os relacionamentos são representados
     * diretamente pelos IDs, em vez de referências a objetos.</p>
     * 
     * @see BoardColumn
     */
    private Long boardColumnId;
}