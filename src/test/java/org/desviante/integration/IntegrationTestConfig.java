package org.desviante.integration;

import org.desviante.integration.retry.RetryConfig;
import org.desviante.integration.retry.RetryExecutor;
import org.desviante.integration.retry.ExponentialBackoffRetryStrategy;
import org.desviante.integration.event.SimpleEventPublisher;
import org.desviante.integration.coordinator.DefaultIntegrationCoordinator;
import org.desviante.integration.observer.GoogleTasksSyncObserver;
import org.desviante.integration.observer.CalendarSyncObserver;
import org.desviante.integration.sync.IntegrationSyncService;
import org.desviante.service.TaskService;
import org.desviante.calendar.CalendarService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.mockito.Mockito;

import java.time.Duration;

/**
 * Configuração para testes de integração.
 * 
 * <p>Esta classe configura o contexto de teste com mocks apropriados
 * para simular o comportamento dos serviços externos durante os testes
 * de integração, permitindo validar o fluxo completo do sistema.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
@TestConfiguration
public class IntegrationTestConfig {
    
    /**
     * Configuração de retry para testes.
     * Usa configuração rápida para acelerar os testes.
     */
    @Bean
    @Primary
    public RetryConfig testRetryConfig() {
        return RetryConfig.builder()
                .maxAttempts(2)
                .initialDelay(Duration.ofMillis(100))
                .maxDelay(Duration.ofSeconds(1))
                .backoffMultiplier(1.5)
                .enableJitter(false) // Desabilitar jitter para testes determinísticos
                .maxRetryDuration(Duration.ofSeconds(5))
                .build();
    }
    
    /**
     * Estratégia de retry para testes.
     */
    @Bean
    @Primary
    public ExponentialBackoffRetryStrategy testRetryStrategy(RetryConfig testRetryConfig) {
        return new ExponentialBackoffRetryStrategy(testRetryConfig);
    }
    
    /**
     * Executor de retry para testes.
     */
    @Bean
    @Primary
    public RetryExecutor testRetryExecutor(ExponentialBackoffRetryStrategy testRetryStrategy) {
        return new RetryExecutor(testRetryStrategy);
    }
    
    /**
     * Mock do TaskService para testes.
     */
    @Bean
    @Primary
    public TaskService mockTaskService() {
        return Mockito.mock(TaskService.class);
    }
    
    /**
     * Mock do CalendarService para testes.
     */
    @Bean
    @Primary
    public CalendarService mockCalendarService() {
        return Mockito.mock(CalendarService.class);
    }
    
    /**
     * Mock do IntegrationSyncService para testes.
     */
    @Bean
    @Primary
    public IntegrationSyncService mockIntegrationSyncService() {
        return Mockito.mock(IntegrationSyncService.class);
    }
    
    /**
     * EventPublisher para testes.
     * Usa implementação real para testar o fluxo completo.
     */
    @Bean
    @Primary
    public SimpleEventPublisher testEventPublisher() {
        return new SimpleEventPublisher();
    }
    
    /**
     * IntegrationCoordinator para testes.
     * Usa implementação real para testar a coordenação.
     */
    @Bean
    @Primary
    public DefaultIntegrationCoordinator testIntegrationCoordinator(SimpleEventPublisher testEventPublisher) {
        return new DefaultIntegrationCoordinator(testEventPublisher);
    }
    
    /**
     * GoogleTasksSyncObserver para testes.
     * Usa implementação real com TaskService mockado.
     */
    @Bean
    @Primary
    public GoogleTasksSyncObserver testGoogleTasksSyncObserver(TaskService mockTaskService) {
        return new GoogleTasksSyncObserver(mockTaskService);
    }
    
    /**
     * CalendarSyncObserver para testes.
     * Usa implementação real com CalendarService mockado.
     */
    @Bean
    @Primary
    public CalendarSyncObserver testCalendarSyncObserver(CalendarService mockCalendarService) {
        return new CalendarSyncObserver(mockCalendarService);
    }
}
