package org.desviante.integration.observer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.desviante.calendar.CalendarEventPriority;
import org.desviante.calendar.CalendarEventType;
import org.desviante.calendar.CalendarService;
import org.desviante.calendar.dto.CalendarEventDTO;
import org.desviante.integration.event.EventObserver;
import org.desviante.integration.event.card.CardScheduledEvent;
import org.desviante.integration.event.card.CardUnscheduledEvent;
import org.desviante.integration.event.card.CardUpdatedEvent;
import org.desviante.model.Card;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Observador responsável pela sincronização com o sistema de calendário.
 * 
 * <p>Este observador processa eventos relacionados a cards agendados e
 * executa as operações necessárias para manter a sincronização com
 * o calendário, incluindo criação, atualização e remoção de eventos.</p>
 * 
 * <p><strong>Princípios SOLID Aplicados:</strong></p>
 * <ul>
 *   <li><strong>SRP:</strong> Responsável apenas pela sincronização com calendário</li>
 *   <li><strong>OCP:</strong> Extensível através de novos tipos de eventos</li>
 *   <li><strong>LSP:</strong> Implementa EventObserver corretamente</li>
 *   <li><strong>ISP:</strong> Interface específica para observação de eventos</li>
 *   <li><strong>DIP:</strong> Depende de abstrações (CalendarService)</li>
 * </ul>
 * 
 * <p><strong>Operações Suportadas:</strong></p>
 * <ul>
 *   <li>Criação de evento quando card é agendado</li>
 *   <li>Atualização de evento quando card é modificado</li>
 *   <li>Remoção de evento quando card é desagendado</li>
 *   <li>Sincronização de datas, títulos e prioridades</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see EventObserver
 * @see CardScheduledEvent
 * @see CardUnscheduledEvent
 * @see CardUpdatedEvent
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CalendarSyncObserver implements EventObserver<CardScheduledEvent> {
    
    private final CalendarService calendarService;
    
    @Override
    public void handle(CardScheduledEvent event) throws Exception {
        if (event == null || event.getCard() == null) {
            log.warn("Evento de agendamento inválido recebido");
            return;
        }
        
        Card card = event.getCard();
        
        // Verificar se o card tem data agendada
        if (card.getScheduledDate() == null) {
            log.debug("Card {} não tem data agendada, ignorando sincronização com calendário", card.getId());
            return;
        }
        
        log.info("Processando sincronização com calendário para card agendado: {}", card.getId());
        
        try {
            if (event.isFirstScheduling()) {
                // Primeira vez sendo agendado - criar novo evento
                createCalendarEvent(card);
            } else {
                // Card já estava agendado - atualizar evento existente
                updateCalendarEvent(card);
            }
            
            log.debug("Sincronização com calendário concluída com sucesso para card: {}", card.getId());
            
        } catch (Exception e) {
            log.error("Erro ao sincronizar card {} com calendário: {}", card.getId(), e.getMessage(), e);
            throw e; // Re-throw para que o sistema de eventos possa tratar
        }
    }
    
    @Override
    public boolean canHandle(org.desviante.integration.event.DomainEvent event) {
        return event instanceof CardScheduledEvent || 
               event instanceof CardUnscheduledEvent || 
               event instanceof CardUpdatedEvent;
    }
    
    @Override
    public int getPriority() {
        return 20; // Prioridade alta para calendário (mais alta que Google Tasks)
    }
    
    @Override
    public String getObserverName() {
        return "CalendarSyncObserver";
    }
    
    /**
     * Cria um novo evento no calendário para um card agendado.
     * 
     * @param card card agendado
     * @throws Exception se ocorrer erro durante a criação
     */
    private void createCalendarEvent(Card card) throws Exception {
        log.debug("Criando novo evento no calendário para card: {}", card.getId());
        
        CalendarEventDTO eventDTO = CalendarEventDTO.builder()
                .title(card.getTitle())
                .description(buildEventDescription(card))
                .startDateTime(card.getScheduledDate())
                .endDateTime(calculateEndDateTime(card))
                .allDay(isAllDayEvent(card))
                .type(CalendarEventType.CARD)
                .priority(calculatePriority(card))
                .color(calculateColor(card))
                .relatedEntityId(card.getId())
                .relatedEntityType("Card")
                .active(true)
                .build();
        
        calendarService.createEvent(eventDTO);
        
        log.info("Evento criado no calendário para card: {}", card.getId());
    }
    
    /**
     * Atualiza um evento existente no calendário.
     * 
     * @param card card atualizado
     * @throws Exception se ocorrer erro durante a atualização
     */
    private void updateCalendarEvent(Card card) throws Exception {
        log.debug("Atualizando evento no calendário para card: {}", card.getId());
        
        // Buscar evento existente
        var existingEvents = calendarService.getEventsForDate(card.getScheduledDate().toLocalDate());
        var existingEvent = existingEvents.stream()
                .filter(event -> "Card".equals(event.getRelatedEntityType()) && 
                               card.getId().equals(event.getRelatedEntityId()))
                .findFirst();
        
        if (existingEvent.isEmpty()) {
            log.warn("Evento não encontrado para card {}, criando novo", card.getId());
            createCalendarEvent(card);
            return;
        }
        
        // Atualizar evento existente
        var eventDTO = existingEvent.get();
        eventDTO.setTitle(card.getTitle());
        eventDTO.setDescription(buildEventDescription(card));
        eventDTO.setStartDateTime(card.getScheduledDate());
        eventDTO.setEndDateTime(calculateEndDateTime(card));
        eventDTO.setAllDay(isAllDayEvent(card));
        eventDTO.setPriority(calculatePriority(card));
        eventDTO.setColor(calculateColor(card));
        
        calendarService.updateEvent(eventDTO);
        
        log.info("Evento atualizado no calendário para card: {}", card.getId());
    }
    
    /**
     * Remove um evento do calendário quando um card é desagendado.
     * 
     * @param card card desagendado
     * @throws Exception se ocorrer erro durante a remoção
     */
    public void handleUnscheduled(Card card) throws Exception {
        if (card == null) {
            log.warn("Card inválido recebido para desagendamento");
            return;
        }
        
        log.info("Removendo evento do calendário para card desagendado: {}", card.getId());
        
        // Buscar eventos relacionados ao card
        var events = calendarService.getEventsForDate(LocalDateTime.now().toLocalDate());
        var relatedEvents = events.stream()
                .filter(event -> "Card".equals(event.getRelatedEntityType()) && 
                               card.getId().equals(event.getRelatedEntityId()))
                .toList();
        
        for (var event : relatedEvents) {
            calendarService.deleteEvent(event.getId());
            log.debug("Evento removido do calendário: {}", event.getId());
        }
        
        log.info("Eventos removidos do calendário para card: {}", card.getId());
    }
    
    /**
     * Atualiza um evento quando um card é modificado.
     * 
     * @param card card atualizado
     * @param previousCard versão anterior do card
     * @throws Exception se ocorrer erro durante a atualização
     */
    public void handleUpdated(Card card, Card previousCard) throws Exception {
        if (card == null) {
            log.warn("Card inválido recebido para atualização");
            return;
        }
        
        // Só processar se o card está agendado
        if (card.getScheduledDate() == null) {
            log.debug("Card {} não está agendado, ignorando atualização no calendário", card.getId());
            return;
        }
        
        log.info("Atualizando evento no calendário para card modificado: {}", card.getId());
        
        // Se o card não tinha data de agendamento antes, criar novo evento
        if (previousCard == null || previousCard.getScheduledDate() == null) {
            createCalendarEvent(card);
        } else {
            // Atualizar evento existente
            updateCalendarEvent(card);
        }
    }
    
    /**
     * Calcula a data de fim do evento baseada no card.
     * 
     * @param card card para cálculo
     * @return data de fim do evento
     */
    private LocalDateTime calculateEndDateTime(Card card) {
        if (card.getDueDate() != null) {
            return card.getDueDate();
        }
        
        // Se não há data de vencimento, usar 1 hora após o agendamento
        LocalDateTime scheduled = card.getScheduledDate();
        if (scheduled == null) {
            // Se não há data agendada, usar data atual + 1 hora
            scheduled = LocalDateTime.now();
            log.debug("Card sem data agendada, usando data atual: {}", scheduled);
        }
        
        if (isAllDayEvent(card)) {
            return scheduled.toLocalDate().atTime(23, 59, 59);
        } else {
            return scheduled.plusHours(1);
        }
    }
    
    /**
     * Determina se o evento deve ser de dia inteiro.
     * 
     * @param card card para verificação
     * @return true se for evento de dia inteiro
     */
    private boolean isAllDayEvent(Card card) {
        LocalDateTime scheduled = card.getScheduledDate();
        if (scheduled == null) {
            return false;
        }
        
        // Se não há horário específico (meia-noite), é evento de dia inteiro
        return scheduled.getHour() == 0 && scheduled.getMinute() == 0;
    }
    
    /**
     * Calcula a prioridade do evento baseada na urgência do card.
     * 
     * @param card card para cálculo da prioridade
     * @return prioridade do evento
     */
    private CalendarEventPriority calculatePriority(Card card) {
        int urgencyLevel = card.getUrgencyLevel();
        
        return switch (urgencyLevel) {
            case 4 -> CalendarEventPriority.URGENT;      // Vencido
            case 3 -> CalendarEventPriority.HIGH;        // Vence hoje
            case 2 -> CalendarEventPriority.HIGH;        // Vence em 1 dia
            case 1 -> CalendarEventPriority.STANDARD;    // Vence em 2-3 dias
            default -> CalendarEventPriority.LOW;        // Sem urgência
        };
    }
    
    /**
     * Calcula a cor do evento baseada na urgência do card.
     * 
     * @param card card para cálculo da cor
     * @return cor em formato hexadecimal
     */
    private String calculateColor(Card card) {
        int urgencyLevel = card.getUrgencyLevel();
        
        return switch (urgencyLevel) {
            case 4 -> "#FF0000";  // Vermelho - vencido
            case 3 -> "#FF6600";  // Laranja - vence hoje
            case 2 -> "#FFAA00";  // Amarelo - vence em 1 dia
            case 1 -> "#00AAFF";  // Azul - vence em 2-3 dias
            default -> "#00AA00"; // Verde - sem urgência
        };
    }
    
    /**
     * Constrói a descrição do evento baseada nas informações do card.
     * 
     * @param card card para construção da descrição
     * @return descrição formatada para o evento
     */
    private String buildEventDescription(Card card) {
        StringBuilder description = new StringBuilder();
        
        if (card.getDescription() != null && !card.getDescription().trim().isEmpty()) {
            description.append(card.getDescription());
        }
        
        if (card.getDueDate() != null) {
            if (description.length() > 0) {
                description.append("\n\n");
            }
            description.append("Prazo: ").append(card.getDueDate());
        }
        
        return description.toString();
    }
}
