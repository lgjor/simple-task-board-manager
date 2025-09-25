package org.desviante.calendar;

import lombok.RequiredArgsConstructor;
import org.desviante.calendar.dto.CalendarEventDTO;
import org.desviante.calendar.provider.CalendarEventProvider;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Serviço para gerenciar o calendário do sistema.
 * 
 * <p>Esta classe implementa a lógica de negócio principal para o sistema de calendário,
 * coordenando diferentes provedores de eventos e gerenciando a integração entre
 * o calendário e as entidades do sistema (cards, tasks, etc.).</p>
 * 
 * <p><strong>Responsabilidades Principais:</strong></p>
 * <ul>
 *   <li>Coordenação de múltiplos provedores de eventos</li>
 *   <li>Agregação de eventos de diferentes fontes</li>
 *   <li>Validação e transformação de dados</li>
 *   <li>Gerenciamento de cache e performance</li>
 *   <li>Integração com o sistema de calendário</li>
 * </ul>
 * 
 * <p><strong>Princípios SOLID Aplicados:</strong></p>
 * <ul>
 *   <li><strong>SRP:</strong> Responsável apenas pela lógica de negócio do calendário</li>
 *   <li><strong>OCP:</strong> Extensível através de novos provedores</li>
 *   <li><strong>LSP:</strong> Trabalha com qualquer implementação de CalendarEventProvider</li>
 *   <li><strong>ISP:</strong> Interface específica para operações de calendário</li>
 *   <li><strong>DIP:</strong> Depende de abstrações (CalendarEventProvider)</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see CalendarEventProvider
 * @see CalendarEventDTO
 * @see CalendarEventManager
 */
@Service
@RequiredArgsConstructor
public class CalendarService {
    
    /**
     * Lista de provedores de eventos do calendário.
     * 
     * <p>Injetada automaticamente pelo Spring, contém todos os provedores
     * disponíveis no contexto da aplicação.</p>
     */
    private final List<CalendarEventProvider> providers;
    
    /**
     * Gerenciador de eventos do calendário.
     * 
     * <p>Responsável por manter o estado dos eventos e coordenar
     * operações de CRUD.</p>
     */
    private final CalendarEventManager eventManager;

    /**
     * Obtém todos os eventos para um período específico.
     * 
     * <p>Consulta todos os provedores registrados e agrega os eventos
     * encontrados no período especificado.</p>
     * 
     * @param start data de início do período
     * @param end data de fim do período
     * @return lista agregada de eventos no período
     */
    public List<CalendarEventDTO> getEventsForDateRange(LocalDate start, LocalDate end) {
        return providers.stream()
                .flatMap(provider -> provider.getEventsForDateRange(start, end).stream())
                .sorted((e1, e2) -> e1.getStartDateTime().compareTo(e2.getStartDateTime()))
                .toList();
    }

    /**
     * Obtém eventos para uma data específica.
     * 
     * @param date data para consulta
     * @return lista de eventos na data especificada
     */
    public List<CalendarEventDTO> getEventsForDate(LocalDate date) {
        return getEventsForDateRange(date, date);
    }

    /**
     * Cria um novo evento no calendário.
     * 
     * <p>Delega a criação para o provedor apropriado baseado
     * no tipo do evento.</p>
     * 
     * @param event dados do evento a ser criado
     * @return evento criado com ID gerado
     */
    public CalendarEventDTO createEvent(CalendarEventDTO event) {
        CalendarEventProvider provider = findProviderForEvent(event);
        return provider.createEvent(event);
    }

    /**
     * Atualiza um evento existente.
     * 
     * @param event dados atualizados do evento
     */
    public void updateEvent(CalendarEventDTO event) {
        CalendarEventProvider provider = findProviderForEvent(event);
        provider.updateEvent(event);
    }

    /**
     * Remove um evento do calendário.
     * 
     * @param eventId ID do evento a ser removido
     */
    public void deleteEvent(Long eventId) {
        // Para deletar, precisamos encontrar o provedor correto
        // Por simplicidade, tentamos em todos os provedores
        for (CalendarEventProvider provider : providers) {
            try {
                provider.deleteEvent(eventId);
                return; // Se chegou aqui, o evento foi deletado com sucesso
            } catch (Exception e) {
                // Continua tentando outros provedores
            }
        }
        throw new IllegalArgumentException("Evento com ID " + eventId + " não encontrado");
    }

    /**
     * Encontra o provedor apropriado para um evento.
     * 
     * <p>Implementação simplificada que retorna o primeiro provedor.
     * Em uma implementação mais robusta, a escolha seria baseada
     * no tipo do evento ou outras características.</p>
     * 
     * @param event evento para o qual encontrar o provedor
     * @return provedor apropriado
     */
    private CalendarEventProvider findProviderForEvent(CalendarEventDTO event) {
        if (providers.isEmpty()) {
            throw new IllegalStateException("Nenhum provedor de eventos configurado");
        }
        return providers.get(0); // Implementação simplificada
    }

    /**
     * Obtém estatísticas dos eventos para um período.
     * 
     * @param start data de início do período
     * @param end data de fim do período
     * @return estatísticas dos eventos
     */
    public CalendarEventStats getEventStats(LocalDate start, LocalDate end) {
        List<CalendarEventDTO> events = getEventsForDateRange(start, end);
        
        return CalendarEventStats.builder()
                .totalEvents(events.size())
                .allDayEvents((int) events.stream().filter(CalendarEventDTO::isAllDay).count())
                .timedEvents((int) events.stream().filter(e -> !e.isAllDay()).count())
                .urgentEvents((int) events.stream().filter(e -> e.getPriority() == CalendarEventPriority.URGENT).count())
                .highPriorityEvents((int) events.stream().filter(e -> e.getPriority() == CalendarEventPriority.HIGH).count())
                .build();
    }

    /**
     * Verifica se há eventos em uma data específica.
     * 
     * @param date data para verificação
     * @return true se há eventos na data
     */
    public boolean hasEventsOnDate(LocalDate date) {
        return !getEventsForDate(date).isEmpty();
    }

    /**
     * Obtém o número de eventos em uma data específica.
     * 
     * @param date data para contagem
     * @return número de eventos na data
     */
    public int getEventCountOnDate(LocalDate date) {
        return getEventsForDate(date).size();
    }
}