package org.desviante.model;

import lombok.*;
import org.desviante.model.enums.CardType;
import org.desviante.model.enums.BoardColumnKindEnum;

import java.time.LocalDateTime;

/**
 * Representa uma tarefa (card) no sistema de gerenciamento de tarefas.
 * 
 * <p>Um card é a unidade fundamental de trabalho no sistema, representando
 * uma tarefa específica que pode ser movida entre colunas de um quadro.
 * Cada card possui informações como título, descrição, datas de criação,
 * atualização e conclusão, além de estar associado a uma coluna específica.</p>
 * 
      * <p>Os cards podem ser de diferentes tipos (CARD, BOOK, VIDEO, COURSE) e
     * alguns tipos suportam acompanhamento de progresso através de unidades
     * genéricas (páginas, minutos, aulas, etc.).</p>
 * 
 * <p>Os cards são os elementos que os usuários movem, editam e acompanham
 * durante o processo de trabalho, permitindo visualizar o progresso das
 * atividades no quadro.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see BoardColumn
 * @see CardType
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
     * Tipo do card que define seu comportamento e campos específicos.
     * <p>Determina se o card suporta acompanhamento de progresso e quais
     * campos específicos estão disponíveis. Cards do tipo CARD não utilizam
     * campos de progresso, enquanto outros tipos (BOOK, VIDEO, COURSE) podem
     * acompanhar progresso através de unidades genéricas.</p>
     * 
     * @see CardType
     */
    private CardType type;

    /**
     * Total de unidades para acompanhamento de progresso.
     * <p>Representa o valor total para calcular o percentual de progresso.
     * Exemplos: total de páginas (BOOK), duração em minutos (VIDEO),
     * número de aulas (COURSE). Null para cards do tipo CARD.</p>
     */
    private Integer totalUnits;

    /**
     * Unidades atuais para acompanhamento de progresso.
     * <p>Representa o valor atual para calcular o percentual de progresso.
     * Exemplos: página atual (BOOK), minuto atual (VIDEO), aula atual (COURSE).
     * Null para cards do tipo CARD ou quando não há progresso.</p>
     */
    private Integer currentUnits;

    /**
     * Progresso manual para cards do tipo CARD.
     * <p>Representa o percentual de progresso manual definido pelo usuário
     * para cards do tipo CARD. Valores de 0 a 100. Null para outros tipos.</p>
     */
    private Integer manualProgress;

    /**
     * Calcula o percentual de progresso do card de forma unificada.
     * 
     * <p>Para todos os tipos de card, calcula baseado em currentUnits/totalUnits.
     * Se não houver unidades definidas, retorna 0%.</p>
     * 
     * @return percentual de progresso (0.0 a 100.0)
     */
    public Double getProgressPercentage() {
        // Para todos os tipos, progresso baseado nas unidades
        if (totalUnits == null || totalUnits == 0 || currentUnits == null) {
            return 0.0;
        }
        return Math.min(100.0, (double) currentUnits / totalUnits * 100);
    }



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



    /**
     * Verifica se o card suporta acompanhamento de progresso.
     * 
     * <p>Todos os tipos de card suportam progresso: CARD baseado na conclusão,
     * outros tipos (BOOK, VIDEO, COURSE) através de unidades.</p>
     * 
     * @return true se o card suporta progresso, false caso contrário
     */
    public boolean isProgressable() {
        return type != null;
    }

    /**
     * Verifica se o card é do tipo especificado.
     * 
     * @param cardType tipo a ser verificado
     * @return true se o card é do tipo especificado, false caso contrário
     */
    public boolean isType(CardType cardType) {
        return type == cardType;
    }

    /**
     * Determina o tipo de coluna apropriado baseado no progresso do card.
     * 
     * <p>Para todos os tipos de card com progresso baseado em unidades:
     * - 0%: INITIAL (não iniciado)
     * - 1-99%: PENDING (em andamento)
     * - 100%: FINAL (concluído)</p>
     * 
     * @return tipo de coluna apropriado ou null se não aplicável
     */
    public BoardColumnKindEnum getAppropriateColumnKind() {
        Double progress = getProgressPercentage();
        if (progress == null) {
            return null;
        }
        
        if (progress == 0) {
            return BoardColumnKindEnum.INITIAL;
        } else if (progress >= 100) {
            return BoardColumnKindEnum.FINAL;
        } else {
            return BoardColumnKindEnum.PENDING;
        }
    }
}