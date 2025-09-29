package org.desviante.integration.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.desviante.integration.event.card.CardScheduledEvent;
import org.desviante.integration.event.card.CardUnscheduledEvent;
import org.desviante.integration.event.card.CardUpdatedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Implementação simples do EventPublisher para o sistema de eventos.
 * 
 * <p>Esta implementação fornece funcionalidades básicas de publicação
 * de eventos, incluindo suporte a publicação síncrona e assíncrona,
 * gerenciamento de observadores e tratamento de erros.</p>
 * 
 * <p><strong>Características:</strong></p>
 * <ul>
 *   <li>Thread-safe usando CopyOnWriteArrayList</li>
 *   <li>Ordenação de observadores por prioridade</li>
 *   <li>Tratamento de erros isolado por observador</li>
 *   <li>Execução assíncrona com thread pool dedicado</li>
 *   <li>Logging detalhado para debugging</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see EventPublisher
 * @see EventObserver
 * @see DomainEvent
 */
@Service
@Slf4j
public class SimpleEventPublisher implements EventPublisher {
    
    /**
     * Lista thread-safe de observadores registrados.
     * 
     * <p>Usa CopyOnWriteArrayList para garantir thread-safety
     * sem necessidade de sincronização explícita.</p>
     */
    private final List<EventObserver<?>> observers = new CopyOnWriteArrayList<>();
    
    /**
     * Executor para processamento assíncrono de eventos.
     */
    private final Executor asyncExecutor = Executors.newFixedThreadPool(5, r -> {
        Thread thread = new Thread(r, "EventPublisher-Async");
        thread.setDaemon(true);
        return thread;
    });
    
    @Override
    public void publish(DomainEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Evento não pode ser null");
        }
        
        log.info("🚀 EVENT PUBLISHER - Publicando evento: {} para {} observadores", 
                 event.getClass().getSimpleName(), observers.size());
        
        List<EventObserver<?>> compatibleObservers = findCompatibleObservers(event);
        
        if (compatibleObservers.isEmpty()) {
            log.warn("⚠️ EVENT PUBLISHER - Nenhum observador compatível encontrado para evento: {}", event.getClass().getSimpleName());
            return;
        }
        
        log.info("✅ EVENT PUBLISHER - Encontrados {} observadores compatíveis para evento: {}", 
                 compatibleObservers.size(), event.getClass().getSimpleName());
        
        // Ordenar observadores por prioridade (maior primeiro)
        compatibleObservers.sort((o1, o2) -> Integer.compare(o2.getPriority(), o1.getPriority()));
        
        List<Exception> errors = new ArrayList<>();
        
        for (EventObserver<?> observer : compatibleObservers) {
            try {
                log.info("📢 EVENT PUBLISHER - Notificando observador: {} para evento: {}", 
                         observer.getObserverName(), event.getClass().getSimpleName());
                handleEvent(observer, event);
                log.info("✅ EVENT PUBLISHER - Observador {} processou evento {} com sucesso", 
                         observer.getObserverName(), event.getClass().getSimpleName());
            } catch (Exception e) {
                log.error("❌ EVENT PUBLISHER - Erro ao processar evento {} no observador {}: {}", 
                         event.getClass().getSimpleName(), observer.getObserverName(), e.getMessage(), e);
                errors.add(e);
            }
        }
        
        if (!errors.isEmpty()) {
            throw new EventPublishingException(
                "Erros ocorreram durante a publicação do evento " + event.getEventType(),
                errors.get(0), event);
        }
        
        log.debug("Evento {} processado com sucesso por {} observadores", 
                 event.getEventType(), compatibleObservers.size());
    }
    
    @Override
    public CompletableFuture<Void> publishAsync(DomainEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Evento não pode ser null");
        }
        
        log.debug("Publicando evento assincronamente: {}", event.getEventType());
        
        return CompletableFuture.runAsync(() -> {
            try {
                publish(event);
            } catch (Exception e) {
                log.error("Erro durante publicação assíncrona do evento {}: {}", 
                         event.getEventType(), e.getMessage(), e);
                throw new RuntimeException("Falha na publicação assíncrona", e);
            }
        }, asyncExecutor);
    }
    
    @Override
    public void subscribe(EventObserver<?> observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Observador não pode ser null");
        }
        
        if (!observers.contains(observer)) {
            observers.add(observer);
            log.debug("Observador {} registrado com sucesso", observer.getObserverName());
        } else {
            log.debug("Observador {} já estava registrado", observer.getObserverName());
        }
    }
    
    @Override
    public void unsubscribe(EventObserver<?> observer) {
        if (observers.remove(observer)) {
            log.debug("Observador {} removido com sucesso", observer.getObserverName());
        } else {
            log.debug("Observador {} não estava registrado", observer.getObserverName());
        }
    }
    
    @Override
    public int getObserverCount() {
        return observers.size();
    }
    
    @Override
    public boolean isSubscribed(EventObserver<?> observer) {
        return observers.contains(observer);
    }
    
    @Override
    public void clearObservers() {
        observers.clear();
        log.debug("Todos os observadores foram removidos");
    }
    
    /**
     * Encontra observadores compatíveis com o evento.
     * 
     * @param event evento para verificação de compatibilidade
     * @return lista de observadores compatíveis
     */
    private List<EventObserver<?>> findCompatibleObservers(DomainEvent event) {
        List<EventObserver<?>> compatible = observers.stream()
                .filter(observer -> {
                    boolean canHandle = observer.canHandle(event);
                    log.debug("Observador {} pode lidar com evento {}: {}", 
                             observer.getObserverName(), event.getClass().getSimpleName(), canHandle);
                    return canHandle;
                })
                .collect(java.util.stream.Collectors.toList());
        
        log.debug("Encontrados {} observadores compatíveis para evento {}", 
                 compatible.size(), event.getClass().getSimpleName());
        return compatible;
    }
    
    /**
     * Processa um evento em um observador específico.
     * 
     * <p>Usa reflection para chamar o método handle() com o tipo correto,
     * garantindo type-safety em tempo de execução.</p>
     * 
     * @param observer observador que deve processar o evento
     * @param event evento a ser processado
     */
    @SuppressWarnings("unchecked")
    private void handleEvent(EventObserver<?> observer, DomainEvent event) throws Exception {
        try {
            // Tratar diferentes tipos de eventos baseado no tipo específico
            if (event instanceof CardScheduledEvent && observer instanceof org.desviante.integration.observer.CalendarSyncObserver) {
                ((org.desviante.integration.observer.CalendarSyncObserver) observer).handle((CardScheduledEvent) event);
            } else if (event instanceof CardUnscheduledEvent && observer instanceof org.desviante.integration.observer.CalendarSyncObserver) {
                ((org.desviante.integration.observer.CalendarSyncObserver) observer).handleUnscheduledEvent((CardUnscheduledEvent) event);
            } else if (event instanceof CardUpdatedEvent && observer instanceof org.desviante.integration.observer.CalendarSyncObserver) {
                ((org.desviante.integration.observer.CalendarSyncObserver) observer).handleUpdatedEvent((CardUpdatedEvent) event);
            } else if (event instanceof CardScheduledEvent && observer instanceof org.desviante.integration.observer.GoogleTasksSyncObserver) {
                ((org.desviante.integration.observer.GoogleTasksSyncObserver) observer).handle((CardScheduledEvent) event);
            } else if (event instanceof CardUnscheduledEvent && observer instanceof org.desviante.integration.observer.GoogleTasksSyncObserver) {
                ((org.desviante.integration.observer.GoogleTasksSyncObserver) observer).handleUnscheduledEvent((CardUnscheduledEvent) event);
            } else if (event instanceof CardUpdatedEvent && observer instanceof org.desviante.integration.observer.GoogleTasksSyncObserver) {
                // GoogleTasksSyncObserver não precisa processar eventos de atualização
                log.debug("GoogleTasksSyncObserver ignorando evento CardUpdatedEvent");
            } else {
                // Fallback para cast genérico
                ((EventObserver<DomainEvent>) observer).handle(event);
            }
        } catch (ClassCastException e) {
            log.error("Erro de tipo ao processar evento {} no observador {}: {}", 
                     event.getEventType(), observer.getObserverName(), e.getMessage());
            throw new EventPublishingException(
                "Erro de tipo ao processar evento no observador " + observer.getObserverName(),
                e, event);
        }
    }
}
