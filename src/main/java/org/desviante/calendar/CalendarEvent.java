package org.desviante.calendar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Representa um evento no calendário do sistema.
 * 
 * <p>Esta classe encapsula todas as informações necessárias para exibir e gerenciar
 * eventos no calendário, incluindo dados básicos, datas, prioridades e metadados
 * para integração com o sistema de cards e tasks existente.</p>
 * 
 * <p><strong>Características Principais:</strong></p>
 * <ul>
 *   <li>Suporte a eventos de dia inteiro e com horário específico</li>
 *   <li>Sistema de prioridades com cores diferenciadas</li>
 *   <li>Integração com entidades existentes (cards, tasks)</li>
 *   <li>Suporte a eventos recorrentes</li>
 *   <li>Metadados para rastreamento e sincronização</li>
 * </ul>
 * 
 * <p><strong>Princípios SOLID Aplicados:</strong></p>
 * <ul>
 *   <li><strong>SRP:</strong> Responsável apenas pela representação de eventos</li>
 *   <li><strong>OCP:</strong> Extensível através de tipos de evento</li>
 *   <li><strong>LSP:</strong> Implementa contratos consistentes</li>
 *   <li><strong>ISP:</strong> Interface específica para eventos</li>
 *   <li><strong>DIP:</strong> Depende de abstrações (LocalDateTime, etc.)</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see CalendarEventType
 * @see CalendarEventPriority
 * @see LocalDateTime
 * @see LocalDate
 * @see LocalTime
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEvent {

    /**
     * Identificador único do evento no calendário.
     * 
     * <p>Este ID é gerado automaticamente e é único dentro do contexto
     * do calendário. Pode corresponder ao ID de uma entidade existente
     * (card, task) ou ser específico do calendário.</p>
     */
    private Long id;

    /**
     * Título do evento exibido no calendário.
     * 
     * <p>Deve ser descritivo e conciso para facilitar a identificação
     * rápida do evento na visualização do calendário.</p>
     */
    private String title;

    /**
     * Descrição detalhada do evento.
     * 
     * <p>Informações adicionais que podem ser exibidas em tooltips
     * ou na visualização detalhada do evento.</p>
     */
    private String description;

    /**
     * Data de início do evento.
     * 
     * <p>Para eventos de dia inteiro, apenas a data é considerada.
     * Para eventos com horário específico, inclui data e hora.</p>
     */
    private LocalDateTime startDateTime;

    /**
     * Data de fim do evento.
     * 
     * <p>Para eventos de dia inteiro, pode ser null ou a mesma data de início.
     * Para eventos com duração específica, define quando o evento termina.</p>
     */
    private LocalDateTime endDateTime;

    /**
     * Indica se o evento é de dia inteiro.
     * 
     * <p>Quando true, o evento ocupa todo o dia independentemente
     * do horário especificado em startDateTime e endDateTime.</p>
     */
    @Builder.Default
    private boolean allDay = false;

    /**
     * Tipo do evento que determina sua origem e comportamento.
     * 
     * <p>Diferentes tipos podem ter regras específicas de exibição,
     * edição e sincronização com o sistema.</p>
     * 
     * @see CalendarEventType
     */
    private CalendarEventType type;

    /**
     * Prioridade do evento que influencia sua exibição visual.
     * 
     * <p>Eventos com prioridade mais alta podem ser exibidos com
     * cores mais vibrantes ou ícones especiais.</p>
     * 
     * @see CalendarEventPriority
     */
    @Builder.Default
    private CalendarEventPriority priority = CalendarEventPriority.STANDARD;

    /**
     * Cor personalizada do evento em formato hexadecimal.
     * 
     * <p>Se especificada, sobrescreve a cor padrão definida pela prioridade.
     * Deve estar no formato #RRGGBB ou #RGB.</p>
     */
    private String color;

    /**
     * ID da entidade relacionada (card, task, etc.).
     * 
     * <p>Permite rastrear a origem do evento e manter sincronização
     * com as entidades do sistema principal.</p>
     */
    private Long relatedEntityId;

    /**
     * Tipo da entidade relacionada.
     * 
     * <p>Especifica se o evento está relacionado a um card, task,
     * ou outra entidade do sistema.</p>
     */
    private String relatedEntityType;

    /**
     * Indica se o evento é recorrente.
     * 
     * <p>Eventos recorrentes são gerados automaticamente baseados
     * em regras de recorrência definidas.</p>
     */
    @Builder.Default
    private boolean recurring = false;

    /**
     * Regra de recorrência para eventos recorrentes.
     * 
     * <p>Define como o evento se repete (diariamente, semanalmente,
     * mensalmente, etc.).</p>
     */
    private String recurrenceRule;

    /**
     * Data de criação do evento no calendário.
     * 
     * <p>Usado para auditoria e ordenação cronológica.</p>
     */
    private LocalDateTime createdAt;

    /**
     * Data da última atualização do evento.
     * 
     * <p>Usado para controle de versão e sincronização.</p>
     */
    private LocalDateTime updatedAt;

    /**
     * Indica se o evento está ativo/visível no calendário.
     * 
     * <p>Eventos inativos não são exibidos mas são mantidos
     * para histórico e possíveis reativações.</p>
     */
    @Builder.Default
    private boolean active = true;

    /**
     * Constrói um evento de dia inteiro com data específica.
     * 
     * <p>Método de conveniência para criar eventos que ocupam
     * todo o dia, como feriados ou marcos importantes.</p>
     * 
     * @param title título do evento
     * @param description descrição do evento
     * @param date data do evento
     * @param type tipo do evento
     * @return evento configurado para dia inteiro
     */
    public static CalendarEvent createAllDayEvent(String title, String description, 
                                                 LocalDate date, CalendarEventType type) {
        return CalendarEvent.builder()
                .title(title)
                .description(description)
                .startDateTime(date.atStartOfDay())
                .endDateTime(date.atTime(23, 59, 59))
                .allDay(true)
                .type(type)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Constrói um evento com horário específico.
     * 
     * <p>Método de conveniência para criar eventos com início
     * e fim definidos, como reuniões ou compromissos.</p>
     * 
     * @param title título do evento
     * @param description descrição do evento
     * @param startDateTime data e hora de início
     * @param endDateTime data e hora de fim
     * @param type tipo do evento
     * @return evento configurado com horário específico
     */
    public static CalendarEvent createTimedEvent(String title, String description,
                                                LocalDateTime startDateTime, LocalDateTime endDateTime,
                                                CalendarEventType type) {
        return CalendarEvent.builder()
                .title(title)
                .description(description)
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .allDay(false)
                .type(type)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Verifica se o evento está ativo em uma data específica.
     * 
     * <p>Considera eventos de dia inteiro e eventos com horário específico,
     * incluindo eventos recorrentes.</p>
     * 
     * @param date data a ser verificada
     * @return true se o evento está ativo na data especificada
     */
    public boolean isActiveOnDate(LocalDate date) {
        if (!active) {
            return false;
        }

        if (allDay) {
            return startDateTime.toLocalDate().equals(date);
        }

        LocalDate startDate = startDateTime.toLocalDate();
        LocalDate endDate = endDateTime != null ? endDateTime.toLocalDate() : startDate;
        
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Verifica se o evento está ativo em um período específico.
     * 
     * @param startDate data de início do período
     * @param endDate data de fim do período
     * @return true se o evento está ativo no período especificado
     */
    public boolean isActiveInPeriod(LocalDate startDate, LocalDate endDate) {
        if (!active) {
            return false;
        }

        LocalDate eventStartDate = startDateTime.toLocalDate();
        LocalDate eventEndDate = endDateTime != null ? endDateTime.toLocalDate() : eventStartDate;
        
        return !eventEndDate.isBefore(startDate) && !eventStartDate.isAfter(endDate);
    }

    /**
     * Atualiza a data de última modificação do evento.
     * 
     * <p>Deve ser chamado sempre que o evento for modificado
     * para manter o controle de versão atualizado.</p>
     */
    public void markAsUpdated() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Verifica se o evento está relacionado a uma entidade específica.
     * 
     * @param entityId ID da entidade
     * @param entityType tipo da entidade
     * @return true se o evento está relacionado à entidade especificada
     */
    public boolean isRelatedTo(Long entityId, String entityType) {
        // Se ambos os parâmetros são null, retorna false
        if (entityId == null && entityType == null) {
            return false;
        }
        
        return Objects.equals(this.relatedEntityId, entityId) && 
               Objects.equals(this.relatedEntityType, entityType);
    }

    /**
     * Obtém a duração do evento em minutos.
     * 
     * @return duração em minutos, ou 0 se for evento de dia inteiro
     */
    public long getDurationInMinutes() {
        if (allDay || endDateTime == null) {
            return 0;
        }
        
        return java.time.Duration.between(startDateTime, endDateTime).toMinutes();
    }

    /**
     * Verifica se o evento é válido.
     * 
     * <p>Um evento é considerado válido se tem título não vazio,
     * data de início definida e data de fim posterior ou igual à de início.</p>
     * 
     * @return true se o evento é válido
     */
    public boolean isValid() {
        if (title == null || title.trim().isEmpty()) {
            return false;
        }
        
        if (startDateTime == null) {
            return false;
        }
        
        if (endDateTime != null && endDateTime.isBefore(startDateTime)) {
            return false;
        }
        
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CalendarEvent that = (CalendarEvent) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("CalendarEvent{id=%s, title='%s', startDateTime=%s, type=%s, priority=%s}", 
                           id, title, startDateTime, type, priority);
    }
}
