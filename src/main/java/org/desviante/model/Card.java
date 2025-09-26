package org.desviante.model;

import lombok.*;
import org.desviante.model.enums.BoardColumnKindEnum;
import org.desviante.model.enums.ProgressType;

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
     * 
     * @return identificador único do card
     * @param id novo identificador único do card
     */
    private Long id;

    /**
     * Título da tarefa.
     * <p>Representa o nome ou título descritivo da tarefa que será
     * exibido no card na interface do usuário.</p>
     * 
     * @return título da tarefa
     * @param title novo título da tarefa
     */
    private String title;

    /**
     * Descrição detalhada da tarefa.
     * <p>Contém informações adicionais sobre a tarefa, como requisitos,
     * instruções, observações ou qualquer detalhe relevante para o trabalho.</p>
     * 
     * @return descrição detalhada da tarefa
     * @param description nova descrição detalhada da tarefa
     */
    private String description;

    /**
     * Tipo de card que permite criar cards personalizados com seus próprios labels de unidade.
     * 
     * @return tipo de card associado
     * @param cardType novo tipo de card associado
     * @see CardType
     */
    private CardType cardType;

    /**
     * Total de unidades para acompanhamento de progresso.
     * <p>Representa o valor total para calcular o percentual de progresso.
     * Exemplos: total de páginas (BOOK), duração em minutos (VIDEO),
     * número de aulas (COURSE). Null para cards do tipo CARD.</p>
     * 
     * @return total de unidades para acompanhamento de progresso
     * @param totalUnits novo total de unidades para acompanhamento de progresso
     */
    private Integer totalUnits;

    /**
     * Unidades atuais para acompanhamento de progresso.
     * <p>Representa o valor atual para calcular o percentual de progresso.
     * Exemplos: página atual (BOOK), minuto atual (VIDEO), aula atual (COURSE).
     * Null para cards do tipo CARD ou quando não há progresso.</p>
     * 
     * @return unidades atuais para acompanhamento de progresso
     * @param currentUnits novas unidades atuais para acompanhamento de progresso
     */
    private Integer currentUnits;

    /**
     * Índice de ordem do card.
     * <p>Representa a posição do card na coluna do quadro.
     * Útil para ordenação e exibição em interfaces.</p>
     * 
     * @return índice de ordem do card
     * @param orderIndex novo índice de ordem do card
     */
    private Integer orderIndex;

    /**
     * Calcula o percentual de progresso do card de forma unificada.
     * 
     * <p>Para cards que suportam progresso, calcula baseado em currentUnits/totalUnits.
     * Se não houver unidades definidas ou total for inválido, retorna 0%.</p>
     * 
     * @return percentual de progresso (0.0 a 100.0)
     */
    public Double getProgressPercentage() {
        // Verificar se o card suporta progresso
        if (!isProgressable()) {
            return 0.0;
        }
        
        // Progresso baseado nas unidades
        if (totalUnits == null || totalUnits <= 0 || currentUnits == null || currentUnits < 0) {
            return 0.0;
        }
        return Math.min(100.0, (double) currentUnits / totalUnits * 100);
    }



    /**
     * Data e hora de criação do card.
     * <p>Este campo é automaticamente preenchido quando um novo card é criado
     * e não deve ser modificado posteriormente. Útil para auditoria e histórico.</p>
     * 
     * @return data e hora de criação do card
     * @param creationDate nova data e hora de criação do card
     */
    private LocalDateTime creationDate;

    /**
     * Data e hora da última atualização do card.
     * <p>Este campo é atualizado automaticamente sempre que o card é modificado,
     * permitindo acompanhar quando foi a última alteração realizada.</p>
     * 
     * @return data e hora da última atualização do card
     * @param lastUpdateDate nova data e hora da última atualização do card
     */
    private LocalDateTime lastUpdateDate;

    /**
     * Data e hora de conclusão da tarefa.
     * <p>Este campo é preenchido quando a tarefa é marcada como concluída,
     * geralmente quando o card é movido para uma coluna final do quadro.</p>
     * 
     * @return data e hora de conclusão da tarefa
     * @param completionDate nova data e hora de conclusão da tarefa
     */
    private LocalDateTime completionDate;

    /**
     * Data e hora de agendamento da tarefa.
     * <p>Este campo define quando a tarefa deve ser iniciada ou agendada.
     * É usado para sincronização com o calendário e planejamento temporal.
     * Pode ser null se a tarefa não tiver data de agendamento específica.</p>
     * 
     * @return data e hora de agendamento da tarefa
     * @param scheduledDate nova data e hora de agendamento da tarefa
     */
    private LocalDateTime scheduledDate;

    /**
     * Data e hora de vencimento da tarefa.
     * <p>Este campo define o prazo limite para conclusão da tarefa.
     * É usado para cálculo de urgência e priorização de tarefas.
     * Pode ser null se a tarefa não tiver prazo específico.</p>
     * 
     * @return data e hora de vencimento da tarefa
     * @param dueDate nova data e hora de vencimento da tarefa
     */
    private LocalDateTime dueDate;

    /**
     * Identificador da coluna onde o card está localizado.
     * <p>Representa a chave estrangeira para a tabela 'board_columns'.
     * Em uma abordagem com JDBC, os relacionamentos são representados
     * diretamente pelos IDs, em vez de referências a objetos.</p>
     * 
     * @return identificador da coluna onde o card está localizado
     * @param boardColumnId novo identificador da coluna onde o card está localizado
     * @see BoardColumn
     */
    private Long boardColumnId;

    /**
     * Identificador do tipo de card associado ao card.
     * <p>Quando um card usa um tipo de card,
     * este campo armazena o ID do tipo de card.</p>
     * 
     * @return identificador do tipo de card associado ao card
     * @param cardTypeId novo identificador do tipo de card associado ao card
     * @see CardType
     */
    private Long cardTypeId;

    /**
     * Tipo de progresso do card.
     * <p>Define se e como o progresso deve ser exibido e calculado.
     * Cards podem ter progresso percentual, customizado ou nenhum progresso.</p>
     * 
     * @return tipo de progresso do card
     * @param progressType novo tipo de progresso do card
     * @see ProgressType
     */
    private ProgressType progressType;



    /**
     * Verifica se o card suporta acompanhamento de progresso.
     * 
     * <p>O progresso é habilitado baseado no tipo de progresso definido.
     * Cards com progressType NONE não mostram progresso na interface.</p>
     * 
     * @return true se o card suporta progresso, false caso contrário
     */
    public boolean isProgressable() {
        return progressType != null && progressType.isEnabled();
    }

    /**
     * Verifica se o card é do tipo especificado.
     * 
     * @param typeName nome do tipo a ser verificado
     * @return true se o card é do tipo especificado, false caso contrário
     */
    public boolean isType(String typeName) {
        return cardType != null && typeName.equals(cardType.getName());
    }

    /**
     * Obtém o tipo de progresso do card, retornando o padrão se não definido.
     * 
     * <p>Se o progressType for null, retorna NONE como padrão.</p>
     * 
     * @return tipo de progresso do card
     */
    public ProgressType getProgressTypeOrDefault() {
        return progressType != null ? progressType : ProgressType.NONE;
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

    /**
     * Verifica se o card está agendado para uma data específica.
     * 
     * @param date data a ser verificada
     * @return true se o card está agendado para a data especificada
     */
    public boolean isScheduledForDate(java.time.LocalDate date) {
        return scheduledDate != null && scheduledDate.toLocalDate().equals(date);
    }

    /**
     * Verifica se o card está vencido.
     * 
     * @return true se o card tem data de vencimento e já passou do prazo
     */
    public boolean isOverdue() {
        return dueDate != null && 
               completionDate == null && 
               java.time.LocalDateTime.now().isAfter(dueDate);
    }

    /**
     * Verifica se o card está próximo do vencimento.
     * 
     * @param daysThreshold número de dias para considerar "próximo do vencimento"
     * @return true se o card está próximo do vencimento
     */
    public boolean isNearDue(int daysThreshold) {
        if (dueDate == null || completionDate != null) {
            return false;
        }
        
        java.time.LocalDateTime threshold = java.time.LocalDateTime.now().plusDays(daysThreshold);
        return dueDate.isBefore(threshold) && !isOverdue();
    }

    /**
     * Calcula o nível de urgência do card baseado na data de vencimento.
     * 
     * @return nível de urgência (0 = sem urgência, 1 = baixa, 2 = média, 3 = alta, 4 = crítica)
     */
    public int getUrgencyLevel() {
        if (dueDate == null || completionDate != null) {
            return 0; // Sem urgência se não há prazo ou já foi concluído
        }
        
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        long daysUntilDue = java.time.temporal.ChronoUnit.DAYS.between(now.toLocalDate(), dueDate.toLocalDate());
        
        if (daysUntilDue < 0) {
            return 4; // Crítica - vencido
        } else if (daysUntilDue == 0) {
            return 3; // Alta - vence hoje
        } else if (daysUntilDue <= 1) {
            return 2; // Média - vence em 1 dia
        } else if (daysUntilDue <= 3) {
            return 1; // Baixa - vence em 2-3 dias
        }
        
        return 0; // Sem urgência
    }

    /**
     * Verifica se o card pode ser agendado (tem data de agendamento definida).
     * 
     * @return true se o card pode ser agendado
     */
    public boolean canBeScheduled() {
        return scheduledDate != null;
    }

    /**
     * Verifica se o card tem prazo definido.
     * 
     * @return true se o card tem data de vencimento definida
     */
    public boolean hasDueDate() {
        return dueDate != null;
    }
}