# Deploy do Sistema de Integração

## Visão Geral

Este documento descreve o processo completo de deploy do sistema de integração do Simple Task Board Manager, incluindo configuração, instalação, monitoramento e manutenção.

## Pré-requisitos

### 1. Requisitos de Sistema

- **Java**: 17 ou superior
- **Memória**: Mínimo 2GB RAM, recomendado 4GB+
- **Armazenamento**: Mínimo 1GB livre
- **Rede**: Conexão estável com internet (para Google Tasks API)

### 2. Dependências Externas

- **Google Cloud Console**: Projeto configurado com Tasks API
- **OAuth 2.0**: Credenciais configuradas
- **Banco de Dados**: H2 (padrão) ou PostgreSQL/MySQL

### 3. Configurações de Rede

```bash
# Portas necessárias
8080 - Aplicação principal
5432 - PostgreSQL (se usado)
3306 - MySQL (se usado)

# URLs externas necessárias
https://accounts.google.com/o/oauth2/auth
https://oauth2.googleapis.com/token
https://www.googleapis.com/tasks/v1
```

## Processo de Deploy

### 1. Preparação do Ambiente

#### 1.1 Configuração do Google Cloud Console

```bash
# 1. Criar projeto no Google Cloud Console
# 2. Ativar Google Tasks API
# 3. Criar credenciais OAuth 2.0
# 4. Configurar URIs de redirecionamento
```

#### 1.2 Configuração de Variáveis de Ambiente

```bash
# Google Tasks API
export GOOGLE_TASKS_CLIENT_ID="seu-client-id.apps.googleusercontent.com"
export GOOGLE_TASKS_CLIENT_SECRET="seu-client-secret"
export GOOGLE_TASKS_REDIRECT_URI="http://localhost:8080/oauth/callback"

# Banco de Dados
export SPRING_DATASOURCE_URL="jdbc:h2:file:./data/taskboard"
export SPRING_DATASOURCE_USERNAME="sa"
export SPRING_DATASOURCE_PASSWORD=""

# Configurações de Integração
export INTEGRATION_ASYNC_ENABLED="true"
export INTEGRATION_RETRY_MAX_ATTEMPTS="3"
export INTEGRATION_RETRY_INITIAL_DELAY="1s"
export INTEGRATION_RETRY_MAX_DELAY="30s"
```

### 2. Deploy Local

#### 2.1 Build da Aplicação

```bash
# Compilar e testar
./gradlew clean build

# Executar testes
./gradlew test

# Gerar JAR executável
./gradlew shadowJar
```

#### 2.2 Execução Local

```bash
# Executar aplicação
java -jar build/libs/board-1.2.6-all.jar

# Ou com configurações específicas
java -jar build/libs/board-1.2.6-all.jar \
  --spring.profiles.active=prod \
  --integration.async.enabled=true \
  --integration.retry.max-attempts=5
```

### 3. Deploy em Produção

#### 3.1 Deploy com Docker

```dockerfile
# Dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copiar JAR
COPY build/libs/board-*.jar app.jar

# Configurar usuário não-root
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# Configurações de integração
ENV INTEGRATION_ASYNC_ENABLED=true
ENV INTEGRATION_RETRY_MAX_ATTEMPTS=5
ENV INTEGRATION_RETRY_INITIAL_DELAY=1s
ENV INTEGRATION_RETRY_MAX_DELAY=60s

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
```

```bash
# Build da imagem Docker
docker build -t taskboard-integration:latest .

# Executar container
docker run -d \
  --name taskboard-integration \
  -p 8080:8080 \
  -e GOOGLE_TASKS_CLIENT_ID="seu-client-id" \
  -e GOOGLE_TASKS_CLIENT_SECRET="seu-client-secret" \
  -v ./data:/app/data \
  taskboard-integration:latest
```

#### 3.2 Deploy com Kubernetes

```yaml
# k8s-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: taskboard-integration
spec:
  replicas: 3
  selector:
    matchLabels:
      app: taskboard-integration
  template:
    metadata:
      labels:
        app: taskboard-integration
    spec:
      containers:
      - name: taskboard-integration
        image: taskboard-integration:latest
        ports:
        - containerPort: 8080
        env:
        - name: GOOGLE_TASKS_CLIENT_ID
          valueFrom:
            secretKeyRef:
              name: integration-secrets
              key: google-tasks-client-id
        - name: GOOGLE_TASKS_CLIENT_SECRET
          valueFrom:
            secretKeyRef:
              name: integration-secrets
              key: google-tasks-client-secret
        - name: INTEGRATION_ASYNC_ENABLED
          value: "true"
        - name: INTEGRATION_RETRY_MAX_ATTEMPTS
          value: "5"
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10

---
apiVersion: v1
kind: Service
metadata:
  name: taskboard-integration-service
spec:
  selector:
    app: taskboard-integration
  ports:
  - port: 80
    targetPort: 8080
  type: LoadBalancer
```

### 4. Configuração de Banco de Dados

#### 4.1 H2 (Desenvolvimento/Teste)

```properties
# application.properties
spring.datasource.url=jdbc:h2:file:./data/taskboard
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true
```

#### 4.2 PostgreSQL (Produção)

```properties
# application-prod.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/taskboard
spring.datasource.username=taskboard_user
spring.datasource.password=taskboard_password
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

```sql
-- Script de criação do banco PostgreSQL
CREATE DATABASE taskboard;
CREATE USER taskboard_user WITH PASSWORD 'taskboard_password';
GRANT ALL PRIVILEGES ON DATABASE taskboard TO taskboard_user;
```

## Monitoramento e Logs

### 1. Health Checks

```bash
# Health check geral
curl http://localhost:8080/actuator/health

# Health check específico de integração
curl http://localhost:8080/actuator/health/integration

# Health check de readiness
curl http://localhost:8080/actuator/health/readiness

# Health check de liveness
curl http://localhost:8080/actuator/health/liveness
```

### 2. Métricas

```bash
# Estatísticas de integração
curl http://localhost:8080/actuator/integration/stats

# Status de sincronização
curl http://localhost:8080/actuator/integration/sync-status

# Métricas de performance
curl http://localhost:8080/actuator/metrics

# Thread dump
curl http://localhost:8080/actuator/threaddump
```

### 3. Logs

```bash
# Logs de integração
tail -f logs/integration.log

# Logs de aplicação
tail -f logs/application.log

# Logs de erro
tail -f logs/error.log

# Logs com filtro
grep "Integration" logs/application.log
```

### 4. Configuração de Logs

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

## Backup e Recuperação

### 1. Backup Automático

```bash
#!/bin/bash
# backup-integration.sh

BACKUP_DIR="/backup/integration/$(date +%Y%m%d_%H%M%S)"
mkdir -p "$BACKUP_DIR"

# Backup do banco de dados
if [ "$SPRING_PROFILES_ACTIVE" = "prod" ]; then
    pg_dump -h localhost -U taskboard_user -d taskboard > "$BACKUP_DIR/database.sql"
else
    cp ./data/taskboard.mv.db "$BACKUP_DIR/"
fi

# Backup das configurações
cp application*.properties "$BACKUP_DIR/"

# Backup dos logs
cp -r logs/ "$BACKUP_DIR/"

# Backup das credenciais (criptografadas)
cp -r config/ "$BACKUP_DIR/"

echo "Backup completed: $BACKUP_DIR"
```

### 2. Restauração

```bash
#!/bin/bash
# restore-integration.sh

BACKUP_DIR="$1"

if [ -z "$BACKUP_DIR" ]; then
    echo "Usage: $0 <backup_directory>"
    exit 1
fi

# Parar aplicação
systemctl stop taskboard-integration

# Restaurar banco de dados
if [ "$SPRING_PROFILES_ACTIVE" = "prod" ]; then
    psql -h localhost -U taskboard_user -d taskboard < "$BACKUP_DIR/database.sql"
else
    cp "$BACKUP_DIR/taskboard.mv.db" ./data/
fi

# Restaurar configurações
cp "$BACKUP_DIR/application*.properties" ./

# Restaurar credenciais
cp -r "$BACKUP_DIR/config/" ./

# Iniciar aplicação
systemctl start taskboard-integration

echo "Restore completed from: $BACKUP_DIR"
```

## Manutenção

### 1. Atualizações

```bash
# Parar aplicação
systemctl stop taskboard-integration

# Backup
./backup-integration.sh

# Atualizar aplicação
cp new-version.jar /opt/taskboard/

# Iniciar aplicação
systemctl start taskboard-integration

# Verificar status
curl http://localhost:8080/actuator/health
```

### 2. Limpeza de Dados

```sql
-- Limpar registros antigos de sincronização
DELETE FROM integration_sync_status 
WHERE created_at < NOW() - INTERVAL '30 days' 
AND sync_status = 'SYNCED';

-- Limpar logs antigos
DELETE FROM integration_logs 
WHERE created_at < NOW() - INTERVAL '7 days';
```

### 3. Monitoramento de Performance

```bash
# Verificar uso de memória
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Verificar threads
curl http://localhost:8080/actuator/metrics/jvm.threads.live

# Verificar GC
curl http://localhost:8080/actuator/metrics/jvm.gc.pause
```

## Troubleshooting

### 1. Problemas Comuns

#### Aplicação não inicia
```bash
# Verificar logs
tail -f logs/application.log

# Verificar configurações
java -jar app.jar --debug

# Verificar dependências
./gradlew dependencies
```

#### Integração não funciona
```bash
# Verificar credenciais
curl -H "Authorization: Bearer $ACCESS_TOKEN" \
     https://www.googleapis.com/tasks/v1/users/@me/lists

# Verificar logs de integração
tail -f logs/integration.log

# Verificar status de sincronização
curl http://localhost:8080/actuator/integration/sync-status
```

#### Performance lenta
```bash
# Verificar thread dump
curl http://localhost:8080/actuator/threaddump

# Verificar métricas
curl http://localhost:8080/actuator/metrics

# Verificar logs de performance
tail -f logs/performance.log
```

### 2. Comandos de Diagnóstico

```bash
# Status geral
./gradlew status

# Testes de integração
./gradlew test --tests "*Integration*Test"

# Verificar configuração
./gradlew check

# Análise de dependências
./gradlew dependencyInsight --dependency org.springframework
```

### 3. Recuperação de Emergência

```bash
# Resetar configurações
cp application.properties.default application.properties

# Limpar cache
rm -rf ./cache/

# Reiniciar aplicação
systemctl restart taskboard-integration

# Verificar integridade
curl http://localhost:8080/actuator/health
```

## Segurança

### 1. Configurações de Segurança

```properties
# Configurações de segurança
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized
management.security.enabled=true

# Configurações de SSL
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=password
server.ssl.key-store-type=PKCS12
```

### 2. Criptografia de Credenciais

```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public CredentialEncryptionService credentialEncryptionService() {
        return new CredentialEncryptionService(
            System.getenv("CREDENTIAL_ENCRYPTION_KEY")
        );
    }
}
```

### 3. Validação de Input

```java
@Component
public class IntegrationValidationService {
    
    public void validateCard(Card card) {
        if (card == null) {
            throw new IllegalArgumentException("Card cannot be null");
        }
        
        if (card.getTitle() == null || card.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Card title cannot be empty");
        }
        
        if (card.getTitle().length() > 255) {
            throw new IllegalArgumentException("Card title too long");
        }
    }
}
```

## Conclusão

Este documento fornece um guia completo para deploy, monitoramento e manutenção do sistema de integração. Para suporte adicional, consulte a documentação de API ou entre em contato com a equipe de desenvolvimento.

### Checklist de Deploy

- [ ] Google Cloud Console configurado
- [ ] Credenciais OAuth 2.0 configuradas
- [ ] Banco de dados configurado
- [ ] Variáveis de ambiente definidas
- [ ] Aplicação compilada e testada
- [ ] Health checks funcionando
- [ ] Logs configurados
- [ ] Backup configurado
- [ ] Monitoramento ativo
- [ ] Segurança configurada
