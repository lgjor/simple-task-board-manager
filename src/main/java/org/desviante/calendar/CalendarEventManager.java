package org.desviante.calendar;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Gerenciador de eventos do calendário.
 * 
 * <p>Esta classe é responsável por manter o estado dos eventos do calendário
 * e coordenar operações de CRUD. Implementa um repositório em memória para
 * demonstração, mas em produção seria substituído por persistência em banco.</p>
 * 
 * <p><strong>Responsabilidades:</strong></p>
 * <ul>
 *   <li>Armazenamento temporário de eventos</li>
 *   <li>Geração de IDs únicos</li>
 *   <li>Operações CRUD básicas</li>
 *   <li>Validação de integridade</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see CalendarEvent
 */
@Component
@Slf4j
public class CalendarEventManager {

    private final ConcurrentHashMap<Long, CalendarEvent> events = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    /**
     * Salva um evento no gerenciador.
     * 
     * @param event evento a ser salvo
     * @return evento salvo com ID gerado
     */
    public CalendarEvent save(CalendarEvent event) {
        System.out.println("🔧 CALENDAR EVENT MANAGER - save chamado para: " + event.getTitle());
        System.out.println("🔧 CALENDAR EVENT MANAGER - Data: " + event.getStartDateTime());
        
        if (event.getId() == null) {
            event.setId(idGenerator.getAndIncrement());
            event.setCreatedAt(LocalDateTime.now());
            System.out.println("🔧 CALENDAR EVENT MANAGER - Novo evento criado com ID: " + event.getId());
        } else {
            System.out.println("🔧 CALENDAR EVENT MANAGER - Atualizando evento existente com ID: " + event.getId());
        }
        event.setUpdatedAt(LocalDateTime.now());
        
        events.put(event.getId(), event);
        System.out.println("🔧 CALENDAR EVENT MANAGER - Evento salvo no mapa. Total de eventos: " + events.size());
        log.debug("Evento salvo: {}", event);
        
        return event;
    }

    /**
     * Busca um evento por ID.
     * 
     * @param id ID do evento
     * @return evento encontrado ou null
     */
    public CalendarEvent findById(Long id) {
        return events.get(id);
    }

    /**
     * Lista todos os eventos.
     * 
     * @return lista de todos os eventos
     */
    public List<CalendarEvent> findAll() {
        return new ArrayList<>(events.values());
    }

    /**
     * Remove um evento por ID.
     * 
     * @param id ID do evento a ser removido
     * @return true se o evento foi removido
     */
    public boolean deleteById(Long id) {
        CalendarEvent removed = events.remove(id);
        if (removed != null) {
            log.debug("Evento removido: {}", removed);
            return true;
        }
        return false;
    }

    /**
     * Verifica se existe um evento com o ID especificado.
     * 
     * @param id ID do evento
     * @return true se o evento existe
     */
    public boolean existsById(Long id) {
        return events.containsKey(id);
    }

    /**
     * Conta o número total de eventos.
     * 
     * @return número total de eventos
     */
    public long count() {
        return events.size();
    }

    /**
     * Limpa todos os eventos (método para testes).
     */
    public void clear() {
        events.clear();
        idGenerator.set(1);
        log.debug("Todos os eventos foram removidos");
    }
}