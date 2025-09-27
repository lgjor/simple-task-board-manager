# API de Integração - Simple Task Board Manager

## Visão Geral

Este documento descreve a API completa do sistema de integração, incluindo todas as interfaces, métodos, eventos e exemplos de uso prático.

## Interfaces Principais

### 1. EventPublisher

Interface para publicação de eventos de domínio.

```java
public interface EventPublisher {
    
    /**
     * Publica um evento de forma síncrona.
     * Todos os observadores são notificados imediatamente.
     * 
     * @param event Evento a ser publicado
     * @throws EventPublishingException se houver erro na publicação
     */
    void publish(DomainEvent event);
    
    /**
     * Publica um evento de forma assíncrona.
     * Os observadores são notificados em threads separadas.
     * 
     * @param event Evento a ser publicado
     */
    void publishAsync(DomainEvent event);
    
    /**
     * Inscreve um observador para receber eventos.
     * 
     * @param observer Observador a ser inscrito
     */
    void subscribe(EventObserver<?> observer);
    
    /**
     * Remove a inscrição de um observador.
     * 
     * @param observer Observador a ser removido
     */
    void unsubscribe(EventObserver<?> observer);
    
    /**
     * Retorna a lista de observadores inscritos.
     * 
     * @return Lista de observadores
     */
    List<EventObserver<?>> getObservers();
}
```

#### Exemplo de Uso

```java
@Service
public class CardService {
    
    @Autowired
    private EventPublisher eventPublisher;
    
    public Card createCard(String title, String description, Long boardId, Long columnId) {
        // Criar card
        Card card = new Card();
        card.setTitle(title);
        card.setDescription(description);
        card.setBoardId(boardId);
        card.setColumnId(columnId);
        
        // Salvar no banco
        card = cardRepository.save(card);
        
        // Publicar evento se card tem data agendada
        if (card.getScheduledDate() != null) {
            CardScheduledEvent event = CardScheduledEvent.builder()
                    .card(card)
                    .scheduledDate(card.getScheduledDate())
                    .build();
            
            eventPublisher.publishAsync(event);
        }
        
        return card;
    }
}
```

### 2. IntegrationCoordinator

Interface para coordenação central de integrações.

```java
public interface IntegrationCoordinator {
    
    /**
     * Processa um card que foi agendado.
     * Dispara eventos para todas as integrações configuradas.
     * 
     * @param card Card que foi agendado
     */
    void onCardScheduled(Card card);
    
    /**
     * Processa um card que foi desagendado.
     * Remove integrações existentes.
     * 
     * @param card Card que foi desagendado
     */
    void onCardUnscheduled(Card card);
    
    /**
     * Processa um card que foi atualizado.
     * Atualiza integrações existentes.
     * 
     * @param card Card que foi atualizado
     */
    void onCardUpdated(Card card);
    
    /**
     * Processa um card que foi movido entre colunas.
     * Mantém integrações existentes.
     * 
     * @param card Card que foi movido
     * @param fromColumnId ID da coluna origem
     * @param toColumnId ID da coluna destino
     */
    void onCardMoved(Card card, Long fromColumnId, Long toColumnId);
    
    /**
     * Processa um card que foi excluído.
     * Remove todas as integrações.
     * 
     * @param cardId ID do card excluído
     */
    void onCardDeleted(Long cardId);
    
    /**
     * Retorna estatísticas de integração.
     * 
     * @return Estatísticas atuais
     */
    IntegrationStats getStats();
    
    /**
     * Verifica se o coordenador está disponível.
     * 
     * @return true se disponível, false caso contrário
     */
    boolean isAvailable();
    
    /**
     * Reinicia as estatísticas.
     */
    void resetStats();
}
```

#### Exemplo de Uso

```java
@Service
public class EnhancedCardService extends CardService {
    
    @Autowired
    private IntegrationCoordinator integrationCoordinator;
    
    @Override
    public Card updateCard(Card card) {
        // Atualizar card
        Card updatedCard = super.updateCard(card);
        
        // Coordenar integração baseada no estado do card
        if (updatedCard.getScheduledDate() != null) {
            // Card foi agendado ou atualizado
            integrationCoordinator.onCardScheduled(updatedCard);
        } else {
            // Card foi desagendado
            integrationCoordinator.onCardUnscheduled(updatedCard);
        }
        
        return updatedCard;
    }
    
    public void moveCard(Long cardId, Long toColumnId) {
        Card card = getCardById(cardId);
        Long fromColumnId = card.getColumnId();
        
        // Mover card
        card.setColumnId(toColumnId);
        updateCard(card);
        
        // Coordenar integração
        integrationCoordinator.onCardMoved(card, fromColumnId, toColumnId);
    }
}
```

### 3. EventObserver

Interface para observadores de eventos.

```java
public interface EventObserver<T extends DomainEvent> {
    
    /**
     * Verifica se o observador pode processar o evento.
     * 
     * @param event Evento a ser verificado
     * @return true se pode processar, false caso contrário
     */
    boolean canHandle(DomainEvent event);
    
    /**
     * Processa o evento.
     * 
     * @param event Evento a ser processado
     */
    void handle(T event);
    
    /**
     * Retorna o nome do observador.
     * 
     * @return Nome do observador
     */
    String getObserverName();
    
    /**
     * Retorna a prioridade do observador.
     * Observadores com prioridade menor são executados primeiro.
     * 
     * @return Prioridade (0 = mais alta)
     */
    int getPriority();
}
```

#### Exemplo de Implementação

```java
@Component
public class SlackNotificationObserver implements EventObserver<CardScheduledEvent> {
    
    @Autowired
    private SlackService slackService;
    
    @Override
    public boolean canHandle(DomainEvent event) {
        return event instanceof CardScheduledEvent;
    }
    
    @Override
    public void handle(CardScheduledEvent event) {
        try {
            Card card = event.getCard();
            
            String message = String.format(
                "📅 Novo card agendado: *%s*\n" +
                "📅 Data: %s\n" +
                "⏰ Prazo: %s\n" +
                "📋 Descrição: %s",
                card.getTitle(),
                card.getScheduledDate(),
                card.getDueDate(),
                card.getDescription()
            );
            
            slackService.sendMessage("#taskboard", message);
            
        } catch (Exception e) {
            log.error("Erro ao enviar notificação no Slack", e);
        }
    }
    
    @Override
    public String getObserverName() {
        return "SlackNotificationObserver";
    }
    
    @Override
    public int getPriority() {
        return 30; // Prioridade baixa
    }
}
```

## Eventos de Domínio

### 1. CardScheduledEvent

Evento disparado quando um card é agendado.

```java
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class CardScheduledEvent extends AbstractDomainEvent {
    
    private final Card card;
    private final LocalDateTime scheduledDate;
    private final LocalDateTime dueDate;
    
    @Override
    public String getEventType() {
        return "CardScheduled";
    }
    
    @Override
    public String getDescription() {
        return String.format("Card '%s' agendado para %s", 
                card.getTitle(), scheduledDate);
    }
}
```

#### Exemplo de Uso

```java
// Criar evento
CardScheduledEvent event = CardScheduledEvent.builder()
        .card(card)
        .scheduledDate(card.getScheduledDate())
        .dueDate(card.getDueDate())
        .build();

// Publicar evento
eventPublisher.publish(event);
```

### 2. CardUnscheduledEvent

Evento disparado quando um card é desagendado.

```java
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class CardUnscheduledEvent extends AbstractDomainEvent {
    
    private final Card card;
    private final LocalDateTime previousScheduledDate;
    
    @Override
    public String getEventType() {
        return "CardUnscheduled";
    }
    
    @Override
    public String getDescription() {
        return String.format("Card '%s' desagendado", card.getTitle());
    }
}
```

### 3. CardUpdatedEvent

Evento disparado quando um card é atualizado.

```java
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class CardUpdatedEvent extends AbstractDomainEvent {
    
    private final Card card;
    private final Card previousCard;
    private final Set<String> changedFields;
    
    @Override
    public String getEventType() {
        return "CardUpdated";
    }
    
    @Override
    public String getDescription() {
        return String.format("Card '%s' atualizado - campos: %s", 
                card.getTitle(), String.join(", ", changedFields));
    }
}
```

## Serviços de Integração

### 1. GoogleTasksSyncObserver

Observador responsável pela sincronização com Google Tasks.

```java
@Component
public class GoogleTasksSyncObserver implements EventObserver<CardScheduledEvent> {
    
    private final TaskService taskService;
    private final IntegrationSyncService syncService;
    
    public GoogleTasksSyncObserver(TaskService taskService) {
        this.taskService = taskService;
    }
    
    @Override
    public boolean canHandle(DomainEvent event) {
        return event instanceof CardScheduledEvent;
    }
    
    @Override
    public void handle(CardScheduledEvent event) {
        try {
            Card card = event.getCard();
            
            // Verificar se card está agendado
            if (card.getScheduledDate() == null) {
                return;
            }
            
            // Criar task no Google Tasks
            taskService.createTask(
                    "Simple Task Board Manager", // Lista padrão
                    card.getTitle(),
                    card.getDescription(),
                    card.getDueDate(),
                    card.getId()
            );
            
            // Registrar sincronização
            if (syncService != null) {
                syncService.createSyncStatus(
                        card.getId(),
                        IntegrationType.GOOGLE_TASKS,
                        card.getId().toString(),
                        SyncStatus.SYNCED
                );
            }
            
            log.info("Task criada no Google Tasks para card ID: {}", card.getId());
            
        } catch (Exception e) {
            log.error("Erro ao sincronizar com Google Tasks", e);
            
            // Registrar erro
            if (syncService != null) {
                syncService.createSyncStatus(
                        event.getCard().getId(),
                        IntegrationType.GOOGLE_TASKS,
                        null,
                        SyncStatus.ERROR
                );
            }
        }
    }
    
    @Override
    public String getObserverName() {
        return "GoogleTasksSyncObserver";
    }
    
    @Override
    public int getPriority() {
        return 10; // Prioridade alta
    }
}
```

### 2. CalendarSyncObserver

Observador responsável pela sincronização com calendário local.

```java
@Component
public class CalendarSyncObserver implements EventObserver<CardScheduledEvent> {
    
    private final CalendarService calendarService;
    private final IntegrationSyncService syncService;
    
    public CalendarSyncObserver(CalendarService calendarService) {
        this.calendarService = calendarService;
    }
    
    @Override
    public boolean canHandle(DomainEvent event) {
        return event instanceof CardScheduledEvent;
    }
    
    @Override
    public void handle(CardScheduledEvent event) {
        try {
            Card card = event.getCard();
            
            // Verificar se card está agendado
            if (card.getScheduledDate() == null) {
                return;
            }
            
            // Criar evento no calendário
            CalendarEventDTO eventDTO = CalendarEventDTO.builder()
                    .title(card.getTitle())
                    .description(card.getDescription())
                    .startDate(card.getScheduledDate())
                    .endDate(card.getDueDate())
                    .relatedEntityId(card.getId())
                    .relatedEntityType("Card")
                    .build();
            
            calendarService.createEvent(eventDTO);
            
            // Registrar sincronização
            if (syncService != null) {
                syncService.createSyncStatus(
                        card.getId(),
                        IntegrationType.CALENDAR,
                        card.getId().toString(),
                        SyncStatus.SYNCED
                );
            }
            
            log.info("Evento criado no calendário para card ID: {}", card.getId());
            
        } catch (Exception e) {
            log.error("Erro ao sincronizar com calendário", e);
            
            // Registrar erro
            if (syncService != null) {
                syncService.createSyncStatus(
                        event.getCard().getId(),
                        IntegrationType.CALENDAR,
                        null,
                        SyncStatus.ERROR
                );
            }
        }
    }
    
    @Override
    public String getObserverName() {
        return "CalendarSyncObserver";
    }
    
    @Override
    public int getPriority() {
        return 20; // Prioridade média
    }
}
```

## Sistema de Retry

### 1. RetryExecutor

Executor responsável por executar operações com retry automático.

```java
@Component
public class RetryExecutor {
    
    private final RetryStrategy strategy;
    
    public RetryExecutor(RetryStrategy strategy) {
        this.strategy = strategy;
    }
    
    /**
     * Executa uma operação com retry automático.
     * 
     * @param operation Operação a ser executada
     * @param operationName Nome da operação (para logs)
     * @return Resultado da operação
     * @throws Exception se a operação falhar após todas as tentativas
     */
    public <T> T execute(Supplier<T> operation, String operationName) throws Exception {
        RetryContext context = new RetryContext();
        
        while (context.getCurrentAttempt() <= strategy.getMaxAttempts()) {
            try {
                return executeOperation(operation, context);
                
            } catch (Exception e) {
                if (!strategy.shouldRetry(context)) {
                    throw e;
                }
                
                Duration delay = strategy.calculateDelay(context.getCurrentAttempt());
                log.warn("Tentativa {} falhou para {}, tentando novamente em {}ms", 
                        context.getCurrentAttempt(), operationName, delay.toMillis());
                
                Thread.sleep(delay.toMillis());
            }
        }
        
        throw new MaxRetriesExceededException("Máximo de tentativas excedido para: " + operationName);
    }
    
    private <T> T executeOperation(Supplier<T> operation, RetryContext context) throws Exception {
        context.incrementAttempt();
        
        try {
            T result = operation.get();
            context.markAsSuccessful();
            return result;
            
        } catch (Exception e) {
            context.markAsFailed(e);
            throw e;
        }
    }
}
```

#### Exemplo de Uso

```java
@Service
public class GoogleTasksService {
    
    @Autowired
    private RetryExecutor retryExecutor;
    
    public void createTask(String listId, String title, String notes, LocalDateTime due, Long cardId) {
        retryExecutor.execute(() -> {
            // Chamada para Google Tasks API
            return googleTasksApi.createTask(listId, title, notes, due);
        }, "createTask");
    }
}
```

### 2. RetryConfig

Configuração para comportamento de retry.

```java
@Data
@Builder
public class RetryConfig {
    
    @Builder.Default
    private int maxAttempts = 3;
    
    @Builder.Default
    private Duration initialDelay = Duration.ofSeconds(1);
    
    @Builder.Default
    private Duration maxDelay = Duration.ofSeconds(30);
    
    @Builder.Default
    private double backoffMultiplier = 2.0;
    
    @Builder.Default
    private boolean enableJitter = true;
    
    @Builder.Default
    private Duration maxRetryDuration = Duration.ofMinutes(5);
    
    /**
     * Calcula o delay para uma tentativa específica.
     * 
     * @param attemptNumber Número da tentativa (1-based)
     * @return Delay calculado
     */
    public Duration calculateDelay(int attemptNumber) {
        if (attemptNumber <= 1) {
            return initialDelay;
        }
        
        // Calcular delay exponencial
        double delaySeconds = initialDelay.toMillis() * Math.pow(backoffMultiplier, attemptNumber - 1) / 1000.0;
        Duration delay = Duration.ofMillis((long) delaySeconds * 1000);
        
        // Aplicar limite máximo
        if (delay.compareTo(maxDelay) > 0) {
            delay = maxDelay;
        }
        
        // Aplicar jitter se habilitado
        if (enableJitter) {
            double jitterFactor = 0.5 + (Math.random() * 0.5); // 0.5 a 1.0
            delay = Duration.ofMillis((long) (delay.toMillis() * jitterFactor));
        }
        
        return delay;
    }
}
```

## Sistema de Rastreamento

### 1. IntegrationSyncService

Serviço para gerenciamento do status de sincronização.

```java
@Service
public class IntegrationSyncService {
    
    @Autowired
    private IntegrationSyncRepository repository;
    
    /**
     * Cria um novo status de sincronização.
     * 
     * @param cardId ID do card
     * @param integrationType Tipo de integração
     * @param externalId ID no sistema externo
     * @param syncStatus Status de sincronização
     * @return Status criado
     */
    public IntegrationSyncStatus createSyncStatus(Long cardId, IntegrationType integrationType, 
                                                  String externalId, SyncStatus syncStatus) {
        IntegrationSyncStatus status = IntegrationSyncStatus.builder()
                .cardId(cardId)
                .integrationType(integrationType)
                .externalId(externalId)
                .syncStatus(syncStatus)
                .lastSyncTime(LocalDateTime.now())
                .build();
        
        return repository.save(status);
    }
    
    /**
     * Atualiza o status de sincronização.
     * 
     * @param id ID do status
     * @param syncStatus Novo status
     * @param externalId ID externo (opcional)
     * @param errorMessage Mensagem de erro (opcional)
     * @return Status atualizado
     */
    public IntegrationSyncStatus updateSyncStatus(Long id, SyncStatus syncStatus, 
                                                  String externalId, String errorMessage) {
        IntegrationSyncStatus status = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Status not found: " + id));
        
        status.setSyncStatus(syncStatus);
        status.setLastSyncTime(LocalDateTime.now());
        
        if (externalId != null) {
            status.setExternalId(externalId);
        }
        
        if (errorMessage != null) {
            status.setErrorMessage(errorMessage);
        }
        
        return repository.save(status);
    }
    
    /**
     * Busca status por card e tipo de integração.
     * 
     * @param cardId ID do card
     * @param integrationType Tipo de integração
     * @return Status encontrado ou null
     */
    public IntegrationSyncStatus findByCardAndType(Long cardId, IntegrationType integrationType) {
        return repository.findByCardIdAndIntegrationType(cardId, integrationType).orElse(null);
    }
    
    /**
     * Busca todos os status com erro.
     * 
     * @return Lista de status com erro
     */
    public List<IntegrationSyncStatus> findErrorStatuses() {
        return repository.findBySyncStatus(SyncStatus.ERROR);
    }
    
    /**
     * Retorna estatísticas de sincronização.
     * 
     * @return Estatísticas
     */
    public SyncStatistics getStatistics() {
        long total = repository.count();
        long synced = repository.countBySyncStatus(SyncStatus.SYNCED);
        long pending = repository.countBySyncStatus(SyncStatus.PENDING);
        long error = repository.countBySyncStatus(SyncStatus.ERROR);
        
        return SyncStatistics.builder()
                .total(total)
                .synced(synced)
                .pending(pending)
                .error(error)
                .build();
    }
}
```

### 2. IntegrationSyncStatus

Entidade para rastreamento de status de sincronização.

```java
@Entity
@Table(name = "integration_sync_status")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationSyncStatus {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "card_id", nullable = false)
    private Long cardId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "integration_type", nullable = false)
    private IntegrationType integrationType;
    
    @Column(name = "external_id")
    private String externalId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", nullable = false)
    private SyncStatus syncStatus;
    
    @Column(name = "last_sync_time")
    private LocalDateTime lastSyncTime;
    
    @Column(name = "error_message", length = 1000)
    private String errorMessage;
    
    @Builder.Default
    @Column(name = "retry_count")
    private int retryCount = 0;
    
    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Builder.Default
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
```

## Exemplos Práticos

### 1. Criação de Card com Integração

```java
@RestController
@RequestMapping("/api/cards")
public class CardController {
    
    @Autowired
    private EnhancedCardService cardService;
    
    @PostMapping
    public ResponseEntity<Card> createCard(@RequestBody CreateCardRequest request) {
        try {
            Card card = cardService.createCard(
                    request.getTitle(),
                    request.getDescription(),
                    request.getBoardId(),
                    request.getColumnId()
            );
            
            // Se card tem data agendada, integração será automática
            if (request.getScheduledDate() != null) {
                card.setScheduledDate(request.getScheduledDate());
                card.setDueDate(request.getDueDate());
                card = cardService.updateCard(card);
            }
            
            return ResponseEntity.ok(card);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
```

### 2. Monitoramento de Integrações

```java
@RestController
@RequestMapping("/api/integration")
public class IntegrationController {
    
    @Autowired
    private IntegrationCoordinator integrationCoordinator;
    
    @Autowired
    private IntegrationSyncService syncService;
    
    @GetMapping("/stats")
    public ResponseEntity<IntegrationStats> getStats() {
        IntegrationStats stats = integrationCoordinator.getStats();
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/sync-status")
    public ResponseEntity<List<IntegrationSyncStatus>> getSyncStatus() {
        List<IntegrationSyncStatus> statuses = syncService.findErrorStatuses();
        return ResponseEntity.ok(statuses);
    }
    
    @PostMapping("/retry/{cardId}")
    public ResponseEntity<Void> retryIntegration(@PathVariable Long cardId) {
        // Implementar lógica de retry manual
        return ResponseEntity.ok().build();
    }
}
```

### 3. Webhook para Google Tasks

```java
@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {
    
    @Autowired
    private IntegrationCoordinator integrationCoordinator;
    
    @PostMapping("/google-tasks")
    public ResponseEntity<Void> handleGoogleTasksWebhook(@RequestBody GoogleTasksWebhookPayload payload) {
        try {
            // Processar webhook do Google Tasks
            // Atualizar card local se necessário
            
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            log.error("Erro ao processar webhook do Google Tasks", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
```

## Conclusão

Esta API fornece uma interface completa e robusta para integração com sistemas externos, seguindo as melhores práticas de desenvolvimento e oferecendo flexibilidade para extensões futuras.
