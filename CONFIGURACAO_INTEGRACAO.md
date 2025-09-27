# Configuração do Sistema de Integração

## Visão Geral

Este documento detalha como configurar e utilizar o sistema de integração do Simple Task Board Manager, incluindo configuração do Google Tasks, calendário local, e todas as opções disponíveis.

## Configuração Inicial

### 1. Configuração do Google Tasks

#### 1.1 Criar Projeto no Google Cloud Console

1. Acesse [Google Cloud Console](https://console.cloud.google.com/)
2. Crie um novo projeto ou selecione um existente
3. Ative a **Google Tasks API**
4. Crie credenciais OAuth 2.0

#### 1.2 Configurar OAuth 2.0

```json
{
  "client_id": "seu-client-id.apps.googleusercontent.com",
  "client_secret": "seu-client-secret",
  "redirect_uris": ["http://localhost:8080/oauth/callback"],
  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
  "token_uri": "https://oauth2.googleapis.com/token",
  "scopes": [
    "https://www.googleapis.com/auth/tasks",
    "https://www.googleapis.com/auth/tasks.readonly"
  ]
}
```

#### 1.3 Configuração no Application

```properties
# application.properties
google.tasks.client.id=seu-client-id.apps.googleusercontent.com
google.tasks.client.secret=seu-client-secret
google.tasks.redirect.uri=http://localhost:8080/oauth/callback
google.tasks.scope=https://www.googleapis.com/auth/tasks
```

### 2. Configuração do Sistema de Eventos

#### 2.1 Configuração Básica

```java
@Configuration
@EnableIntegration
public class IntegrationConfig {
    
    @Bean
    @Primary
    public SimpleEventPublisher eventPublisher() {
        SimpleEventPublisher publisher = new SimpleEventPublisher();
        
        // Configurar thread pool para processamento assíncrono
        publisher.setAsyncExecutor(Executors.newFixedThreadPool(10));
        
        return publisher;
    }
    
    @Bean
    @Primary
    public DefaultIntegrationCoordinator integrationCoordinator(
            SimpleEventPublisher eventPublisher) {
        return new DefaultIntegrationCoordinator(eventPublisher);
    }
}
```

#### 2.2 Configuração de Observadores

```java
@Configuration
public class ObserverConfig {
    
    @Bean
    @Primary
    public GoogleTasksSyncObserver googleTasksSyncObserver(
            TaskService taskService,
            IntegrationSyncService syncService) {
        GoogleTasksSyncObserver observer = new GoogleTasksSyncObserver(taskService);
        observer.setSyncService(syncService);
        observer.setEnabled(true);
        return observer;
    }
    
    @Bean
    @Primary
    public CalendarSyncObserver calendarSyncObserver(
            CalendarService calendarService,
            IntegrationSyncService syncService) {
        CalendarSyncObserver observer = new CalendarSyncObserver(calendarService);
        observer.setSyncService(syncService);
        observer.setEnabled(true);
        return observer;
    }
}
```

### 3. Configuração do Sistema de Retry

#### 3.1 Configuração de Retry

```java
@Configuration
public class RetryConfig {
    
    @Bean
    @Primary
    public RetryConfig retryConfig() {
        return RetryConfig.builder()
                .maxAttempts(3)
                .initialDelay(Duration.ofSeconds(1))
                .maxDelay(Duration.ofSeconds(30))
                .backoffMultiplier(2.0)
                .enableJitter(true)
                .maxRetryDuration(Duration.ofMinutes(5))
                .build();
    }
    
    @Bean
    @Primary
    public ExponentialBackoffRetryStrategy retryStrategy(RetryConfig config) {
        return new ExponentialBackoffRetryStrategy(config);
    }
    
    @Bean
    @Primary
    public RetryExecutor retryExecutor(ExponentialBackoffRetryStrategy strategy) {
        return new RetryExecutor(strategy);
    }
}
```

#### 3.2 Configuração por Ambiente

```properties
# application-dev.properties (Desenvolvimento)
integration.retry.max-attempts=2
integration.retry.initial-delay=500ms
integration.retry.max-delay=5s

# application-prod.properties (Produção)
integration.retry.max-attempts=5
integration.retry.initial-delay=1s
integration.retry.max-delay=60s
integration.retry.max-duration=10m
```

### 4. Configuração do Banco de Dados

#### 4.1 Schema de Integração

```sql
-- Tabela para rastreamento de sincronização
CREATE TABLE integration_sync_status (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    card_id BIGINT NOT NULL,
    integration_type VARCHAR(50) NOT NULL,
    external_id VARCHAR(255),
    sync_status VARCHAR(20) NOT NULL,
    last_sync_time TIMESTAMP,
    error_message TEXT,
    retry_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE,
    INDEX idx_card_id (card_id),
    INDEX idx_sync_status (sync_status),
    INDEX idx_integration_type (integration_type)
);
```

#### 4.2 Configuração de Liquibase

```yaml
# liquibase/integration-sync.yaml
databaseChangeLog:
  - include:
      file: db/changelog/migrations/db.changelog-20250120-add-integration-sync-tracking.sql
```

## Configuração Avançada

### 1. Configuração de Performance

#### 1.1 Thread Pool Configuration

```java
@Configuration
public class PerformanceConfig {
    
    @Bean
    @Primary
    public ExecutorService integrationExecutor() {
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("integration-%d")
                .setDaemon(true)
                .build();
                
        return new ThreadPoolExecutor(
                5,  // core pool size
                20, // maximum pool size
                60L, TimeUnit.SECONDS, // keep alive time
                new LinkedBlockingQueue<>(1000), // queue
                threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy() // rejection policy
        );
    }
    
    @Bean
    @Primary
    public SimpleEventPublisher eventPublisher(ExecutorService executor) {
        SimpleEventPublisher publisher = new SimpleEventPublisher();
        publisher.setAsyncExecutor(executor);
        return publisher;
    }
}
```

#### 1.2 Configuração de Cache

```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    @Primary
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(30))
                .recordStats());
        return cacheManager;
    }
    
    @Cacheable("integration-config")
    public IntegrationConfig getIntegrationConfig() {
        // Retorna configuração de integração
    }
}
```

### 2. Configuração de Monitoramento

#### 2.1 Métricas Customizadas

```java
@Component
public class IntegrationMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Counter successfulIntegrations;
    private final Counter failedIntegrations;
    private final Timer integrationTimer;
    
    public IntegrationMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.successfulIntegrations = Counter.builder("integration.successful")
                .description("Number of successful integrations")
                .register(meterRegistry);
        this.failedIntegrations = Counter.builder("integration.failed")
                .description("Number of failed integrations")
                .register(meterRegistry);
        this.integrationTimer = Timer.builder("integration.duration")
                .description("Integration execution time")
                .register(meterRegistry);
    }
    
    public void recordSuccessfulIntegration() {
        successfulIntegrations.increment();
    }
    
    public void recordFailedIntegration() {
        failedIntegrations.increment();
    }
    
    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }
}
```

#### 2.2 Health Checks

```java
@Component
public class IntegrationHealthIndicator implements HealthIndicator {
    
    @Autowired
    private IntegrationCoordinator integrationCoordinator;
    
    @Override
    public Health health() {
        try {
            boolean isAvailable = integrationCoordinator.isAvailable();
            IntegrationStats stats = integrationCoordinator.getStats();
            
            if (isAvailable) {
                return Health.up()
                        .withDetail("status", "available")
                        .withDetail("successful_integrations", stats.getSuccessfulIntegrations())
                        .withDetail("failed_integrations", stats.getFailedIntegrations())
                        .withDetail("total_integrations", stats.getTotalIntegrations())
                        .build();
            } else {
                return Health.down()
                        .withDetail("status", "unavailable")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
```

### 3. Configuração de Logging

#### 3.1 Logback Configuration

```xml
<!-- logback-spring.xml -->
<configuration>
    <appender name="INTEGRATION_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/integration.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/integration.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxFileSize>10MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>1GB</totalSizeCap>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <logger name="org.desviante.integration" level="INFO" additivity="false">
        <appender-ref ref="INTEGRATION_FILE"/>
        <appender-ref ref="CONSOLE"/>
    </logger>
</configuration>
```

#### 3.2 Configuração de Logs Estruturados

```java
@Component
public class IntegrationLogger {
    
    private static final Logger logger = LoggerFactory.getLogger(IntegrationLogger.class);
    
    public void logCardScheduled(Card card) {
        logger.info("Card scheduled", 
                kv("card_id", card.getId()),
                kv("card_title", card.getTitle()),
                kv("scheduled_date", card.getScheduledDate()),
                kv("due_date", card.getDueDate()));
    }
    
    public void logIntegrationSuccess(String integrationType, Long cardId, String externalId) {
        logger.info("Integration successful",
                kv("integration_type", integrationType),
                kv("card_id", cardId),
                kv("external_id", externalId));
    }
    
    public void logIntegrationFailure(String integrationType, Long cardId, String error) {
        logger.error("Integration failed",
                kv("integration_type", integrationType),
                kv("card_id", cardId),
                kv("error", error));
    }
}
```

## Configuração por Ambiente

### 1. Desenvolvimento

```properties
# application-dev.properties
spring.profiles.active=dev

# Google Tasks (Sandbox)
google.tasks.client.id=dev-client-id.apps.googleusercontent.com
google.tasks.client.secret=dev-client-secret
google.tasks.api.base-url=https://www.googleapis.com/tasks/v1

# Integração
integration.retry.max-attempts=2
integration.retry.initial-delay=500ms
integration.retry.max-delay=5s
integration.async.enabled=true
integration.async.thread-pool-size=5

# Logging
logging.level.org.desviante.integration=DEBUG
logging.level.org.desviante.service.TaskService=DEBUG
```

### 2. Teste

```properties
# application-test.properties
spring.profiles.active=test

# Google Tasks (Test)
google.tasks.client.id=test-client-id.apps.googleusercontent.com
google.tasks.client.secret=test-client-secret

# Integração (Test)
integration.retry.max-attempts=1
integration.retry.initial-delay=100ms
integration.retry.max-delay=1s
integration.async.enabled=false

# Logging
logging.level.org.desviante.integration=INFO
logging.level.org.desviante.service.TaskService=INFO
```

### 3. Produção

```properties
# application-prod.properties
spring.profiles.active=prod

# Google Tasks (Production)
google.tasks.client.id=prod-client-id.apps.googleusercontent.com
google.tasks.client.secret=prod-client-secret
google.tasks.api.base-url=https://www.googleapis.com/tasks/v1

# Integração (Production)
integration.retry.max-attempts=5
integration.retry.initial-delay=1s
integration.retry.max-delay=60s
integration.retry.max-duration=10m
integration.async.enabled=true
integration.async.thread-pool-size=20

# Performance
integration.cache.enabled=true
integration.cache.max-size=1000
integration.cache.ttl=30m

# Logging
logging.level.org.desviante.integration=INFO
logging.level.org.desviante.service.TaskService=WARN
logging.level.org.desviante.integration.retry=INFO
```

## Configuração de Segurança

### 1. Criptografia de Credenciais

```java
@Configuration
public class SecurityConfig {
    
    @Bean
    @Primary
    public CredentialEncryptionService credentialEncryptionService() {
        return new CredentialEncryptionService("sua-chave-secreta-aqui");
    }
    
    @Bean
    @Primary
    public GoogleTasksApiService googleTasksApiService(
            CredentialEncryptionService encryptionService) {
        return new GoogleTasksApiService(encryptionService);
    }
}
```

### 2. Validação de Input

```java
@Component
public class IntegrationValidationService {
    
    public void validateCard(Card card) {
        if (card == null) {
            throw new IllegalArgumentException("Card cannot be null");
        }
        
        if (card.getScheduledDate() != null && card.getDueDate() != null) {
            if (card.getDueDate().isBefore(card.getScheduledDate())) {
                throw new IllegalArgumentException("Due date cannot be before scheduled date");
            }
        }
        
        if (card.getTitle() == null || card.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Card title cannot be empty");
        }
    }
    
    public void validateExternalId(String externalId) {
        if (externalId != null && externalId.length() > 255) {
            throw new IllegalArgumentException("External ID too long");
        }
    }
}
```

### 3. Rate Limiting

```java
@Component
public class RateLimitingService {
    
    private final Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();
    
    public boolean isAllowed(String key, int maxRequests, Duration window) {
        RateLimiter limiter = limiters.computeIfAbsent(key, 
                k -> RateLimiter.create(maxRequests / window.getSeconds()));
        return limiter.tryAcquire();
    }
    
    public void checkGoogleTasksRateLimit() {
        if (!isAllowed("google-tasks", 100, Duration.ofMinutes(1))) {
            throw new RateLimitExceededException("Google Tasks rate limit exceeded");
        }
    }
}
```

## Configuração de Backup e Recuperação

### 1. Backup de Configurações

```bash
#!/bin/bash
# backup-integration-config.sh

BACKUP_DIR="/backup/integration/$(date +%Y%m%d_%H%M%S)"
mkdir -p "$BACKUP_DIR"

# Backup do banco de dados
pg_dump -h localhost -U postgres -d taskboard > "$BACKUP_DIR/integration_sync_status.sql"

# Backup das configurações
cp application.properties "$BACKUP_DIR/"
cp application-prod.properties "$BACKUP_DIR/"

# Backup dos logs
cp -r logs/ "$BACKUP_DIR/"

echo "Backup completed: $BACKUP_DIR"
```

### 2. Restauração de Configurações

```bash
#!/bin/bash
# restore-integration-config.sh

BACKUP_DIR="$1"

if [ -z "$BACKUP_DIR" ]; then
    echo "Usage: $0 <backup_directory>"
    exit 1
fi

# Restaurar banco de dados
psql -h localhost -U postgres -d taskboard < "$BACKUP_DIR/integration_sync_status.sql"

# Restaurar configurações
cp "$BACKUP_DIR/application.properties" ./
cp "$BACKUP_DIR/application-prod.properties" ./

echo "Restore completed from: $BACKUP_DIR"
```

## Configuração de Deployment

### 1. Docker Configuration

```dockerfile
# Dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY build/libs/board-*.jar app.jar

# Configuração de integração
ENV INTEGRATION_ASYNC_ENABLED=true
ENV INTEGRATION_RETRY_MAX_ATTEMPTS=3
ENV INTEGRATION_RETRY_INITIAL_DELAY=1s
ENV INTEGRATION_RETRY_MAX_DELAY=30s

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
```

### 2. Kubernetes Configuration

```yaml
# k8s-integration-config.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: integration-config
data:
  application.properties: |
    integration.retry.max-attempts=3
    integration.retry.initial-delay=1s
    integration.retry.max-delay=30s
    integration.async.enabled=true
    integration.async.thread-pool-size=10

---
apiVersion: v1
kind: Secret
metadata:
  name: integration-secrets
type: Opaque
data:
  google.tasks.client.id: <base64-encoded-client-id>
  google.tasks.client.secret: <base64-encoded-client-secret>
```

## Troubleshooting

### 1. Problemas Comuns

#### Google Tasks API não funciona
```bash
# Verificar credenciais
curl -H "Authorization: Bearer $ACCESS_TOKEN" \
     https://www.googleapis.com/tasks/v1/users/@me/lists

# Verificar logs
tail -f logs/integration.log | grep "GoogleTasks"
```

#### Integrações lentas
```bash
# Verificar estatísticas
curl http://localhost:8080/actuator/integration/stats

# Verificar thread pool
curl http://localhost:8080/actuator/threaddump
```

#### Erros de retry
```bash
# Verificar configuração de retry
curl http://localhost:8080/actuator/configprops | grep retry

# Verificar logs de retry
tail -f logs/integration.log | grep "Retry"
```

### 2. Comandos de Diagnóstico

```bash
# Status geral do sistema
curl http://localhost:8080/actuator/health

# Estatísticas de integração
curl http://localhost:8080/actuator/integration/stats

# Status de sincronização
curl http://localhost:8080/actuator/integration/sync-status

# Configuração atual
curl http://localhost:8080/actuator/configprops
```

### 3. Logs Importantes

```bash
# Logs de integração
tail -f logs/integration.log

# Logs de erro
tail -f logs/error.log

# Logs de performance
tail -f logs/performance.log
```

## Conclusão

Este documento fornece todas as informações necessárias para configurar, monitorar e manter o sistema de integração do Simple Task Board Manager. Para suporte adicional, consulte a documentação de API ou entre em contato com a equipe de desenvolvimento.
