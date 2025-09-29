package org.desviante.integration.event;

import org.desviante.integration.event.card.CardScheduledEvent;
import org.desviante.model.Card;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para SimpleEventPublisher.
 * 
 * <p>Estes testes verificam o funcionamento básico do sistema de eventos,
 * incluindo publicação síncrona, assíncrona e gerenciamento de observadores.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
class SimpleEventPublisherTest {
    
    private SimpleEventPublisher eventPublisher;
    
    @BeforeEach
    void setUp() {
        eventPublisher = new SimpleEventPublisher();
    }
    
    @Test
    void shouldPublishEventToCompatibleObservers() throws InterruptedException {
        // Arrange
        AtomicInteger handleCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(2);
        
        EventObserver<CardScheduledEvent> observer1 = new EventObserver<CardScheduledEvent>() {
            @Override
            public void handle(CardScheduledEvent event) throws Exception {
                handleCount.incrementAndGet();
                latch.countDown();
            }
            
            @Override
            public boolean canHandle(DomainEvent event) {
                return event instanceof CardScheduledEvent;
            }
        };
        
        EventObserver<CardScheduledEvent> observer2 = new EventObserver<CardScheduledEvent>() {
            @Override
            public void handle(CardScheduledEvent event) throws Exception {
                handleCount.incrementAndGet();
                latch.countDown();
            }
            
            @Override
            public boolean canHandle(DomainEvent event) {
                return event instanceof CardScheduledEvent;
            }
        };
        
        eventPublisher.subscribe(observer1);
        eventPublisher.subscribe(observer2);
        
        Card card = Card.builder()
                .id(1L)
                .title("Test Card")
                .description("Test Description")
                .scheduledDate(LocalDateTime.now())
                .build();
        
        CardScheduledEvent event = CardScheduledEvent.builder()
                .card(card)
                .scheduledDate(LocalDateTime.now())
                .build();
        
        // Act
        eventPublisher.publish(event);
        
        // Assert
        assertTrue(latch.await(1, TimeUnit.SECONDS), "Observadores deveriam ter sido notificados");
        assertEquals(2, handleCount.get(), "Ambos os observadores deveriam ter processado o evento");
    }
    
    @Test
    void shouldNotPublishToIncompatibleObservers() {
        // Arrange
        AtomicInteger handleCount = new AtomicInteger(0);
        
        EventObserver<CardScheduledEvent> incompatibleObserver = new EventObserver<CardScheduledEvent>() {
            @Override
            public void handle(CardScheduledEvent event) throws Exception {
                handleCount.incrementAndGet();
            }
            
            @Override
            public boolean canHandle(DomainEvent event) {
                return false; // Sempre retorna false
            }
        };
        
        eventPublisher.subscribe(incompatibleObserver);
        
        Card card = Card.builder()
                .id(1L)
                .title("Test Card")
                .scheduledDate(LocalDateTime.now())
                .build();
        
        CardScheduledEvent event = CardScheduledEvent.builder()
                .card(card)
                .scheduledDate(LocalDateTime.now())
                .build();
        
        // Act
        eventPublisher.publish(event);
        
        // Assert
        assertEquals(0, handleCount.get(), "Observador incompatível não deveria ter sido notificado");
    }
    
    @Test
    void shouldPublishAsyncWithoutBlocking() throws InterruptedException {
        // Arrange
        AtomicInteger handleCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);
        
        EventObserver<CardScheduledEvent> observer = new EventObserver<CardScheduledEvent>() {
            @Override
            public void handle(CardScheduledEvent event) throws Exception {
                Thread.sleep(50); // Simula processamento demorado
                handleCount.incrementAndGet();
                latch.countDown();
            }
            
            @Override
            public boolean canHandle(DomainEvent event) {
                return event instanceof CardScheduledEvent;
            }
        };
        
        eventPublisher.subscribe(observer);
        
        Card card = Card.builder()
                .id(1L)
                .title("Test Card")
                .scheduledDate(LocalDateTime.now())
                .build();
        
        CardScheduledEvent event = CardScheduledEvent.builder()
                .card(card)
                .scheduledDate(LocalDateTime.now())
                .build();
        
        // Act
        long startTime = System.currentTimeMillis();
        CompletableFuture<Void> future = eventPublisher.publishAsync(event);
        long publishTime = System.currentTimeMillis();
        
        // Assert
        assertTrue(publishTime - startTime < 50, "Publicação assíncrona deveria retornar rapidamente");
        assertTrue(latch.await(1, TimeUnit.SECONDS), "Observador deveria ter sido notificado assincronamente");
        assertEquals(1, handleCount.get(), "Observador deveria ter processado o evento");
        
        // Aguardar o future completar
        try {
            future.get(2, TimeUnit.SECONDS);
        } catch (Exception e) {
            fail("Future deveria completar sem erro: " + e.getMessage());
        }
        assertTrue(future.isDone(), "Future deveria estar completo");
    }
    
    @Test
    void shouldHandleObserverErrorsGracefully() {
        // Arrange
        AtomicInteger successCount = new AtomicInteger(0);
        
        EventObserver<CardScheduledEvent> errorObserver = new EventObserver<CardScheduledEvent>() {
            @Override
            public void handle(CardScheduledEvent event) throws Exception {
                throw new RuntimeException("Erro simulado");
            }
            
            @Override
            public boolean canHandle(DomainEvent event) {
                return event instanceof CardScheduledEvent;
            }
        };
        
        EventObserver<CardScheduledEvent> successObserver = new EventObserver<CardScheduledEvent>() {
            @Override
            public void handle(CardScheduledEvent event) throws Exception {
                successCount.incrementAndGet();
            }
            
            @Override
            public boolean canHandle(DomainEvent event) {
                return event instanceof CardScheduledEvent;
            }
        };
        
        eventPublisher.subscribe(errorObserver);
        eventPublisher.subscribe(successObserver);
        
        Card card = Card.builder()
                .id(1L)
                .title("Test Card")
                .scheduledDate(LocalDateTime.now())
                .build();
        
        CardScheduledEvent event = CardScheduledEvent.builder()
                .card(card)
                .scheduledDate(LocalDateTime.now())
                .build();
        
        // Act & Assert
        assertThrows(EventPublishingException.class, () -> eventPublisher.publish(event));
        assertEquals(1, successCount.get(), "Observador que não falhou deveria ter processado o evento");
    }
    
    @Test
    void shouldManageObserversCorrectly() {
        // Arrange
        EventObserver<CardScheduledEvent> observer = new EventObserver<CardScheduledEvent>() {
            @Override
            public void handle(CardScheduledEvent event) throws Exception {
                // Mock implementation
            }
            
            @Override
            public boolean canHandle(DomainEvent event) {
                return event instanceof CardScheduledEvent;
            }
        };
        
        // Act & Assert
        assertEquals(0, eventPublisher.getObserverCount());
        assertFalse(eventPublisher.isSubscribed(observer));
        
        eventPublisher.subscribe(observer);
        assertEquals(1, eventPublisher.getObserverCount());
        assertTrue(eventPublisher.isSubscribed(observer));
        
        eventPublisher.unsubscribe(observer);
        assertEquals(0, eventPublisher.getObserverCount());
        assertFalse(eventPublisher.isSubscribed(observer));
    }
    
    @Test
    void shouldValidateInputs() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> eventPublisher.publish(null));
        assertThrows(IllegalArgumentException.class, () -> eventPublisher.subscribe(null));
    }
}
