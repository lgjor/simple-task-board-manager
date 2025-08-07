# Guia de Atualização Segura - Preservação do Banco de Dados H2

## Visão Geral

Este documento descreve como garantir que o banco de dados H2 seja preservado durante atualizações do sistema, permitindo que os dados existentes permaneçam disponíveis na nova versão.

## Estrutura do Banco de Dados

### Localização
- **Linux/Mac**: `~/myboards/board_h2_db.*`
- **Windows**: `%USERPROFILE%\myboards\board_h2_db.*`

### Arquivos do Banco
- `board_h2_db.mv.db` - Arquivo principal do banco
- `board_h2_db.lock.db` - Arquivo de lock (temporário)
- `board_h2_db.trace.db` - Arquivo de log (opcional)

## Estratégia de Preservação

### 1. Preservação Automática de Dados

O sistema agora preserva dados automaticamente através de configurações seguras:

```properties
# Configuração no application.properties
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.defer-datasource-initialization=false
```

**Melhorias na versão 1.0.6:**
- Verificação automática de integridade do banco
- Migração automática quando necessário
- Preservação completa de dados existentes

### 2. Verificação de Integridade Automática

O sistema inclui um verificador automático que:
- Verifica se o banco existe
- Confirma que as tabelas necessárias estão presentes
- Valida a estrutura das tabelas
- Executa migrações automáticas quando necessário
- Registra informações de debug detalhadas

### 3. Migração Automática

O `DatabaseMigrationService` executa automaticamente:
- Verificação de tabelas obrigatórias
- Verificação de colunas obrigatórias
- Execução de scripts de migração seguros
- Rollback automático em caso de erro

### 4. Scripts de Backup e Restauração

#### Backup Automático (Linux/Mac)
```bash
./scripts/backup-database.sh
```

#### Backup Automático (Windows)
```cmd
scripts\backup-database.bat
```

#### Restauração (Linux/Mac)
```bash
./scripts/restore-database.sh
```

#### Restauração (Windows)
```cmd
scripts\restore-database.bat
```

## Processo de Atualização Segura

### Pré-Atualização

1. **Faça backup do banco atual**:
   ```bash
   # Linux/Mac
   ./scripts/backup-database.sh
   
   # Windows
   scripts\backup-database.bat
   ```

2. **Verifique se a aplicação está parada**:
   ```bash
   # Linux/Mac
   pkill -f "SimpleTaskBoardManager"
   
   # Windows
   taskkill /f /im SimpleTaskBoardManager.exe
   ```

### Durante a Atualização

1. **Instale a nova versão**:
   - Execute o instalador da nova versão
   - O instalador NÃO deve sobrescrever o diretório `~/myboards/`

2. **Verificação automática**:
   - O sistema detectará automaticamente o banco existente
   - A validação de esquema será executada automaticamente
   - Migrações serão aplicadas automaticamente se necessário
   - A integridade será verificada

### Pós-Atualização

1. **Inicie a aplicação**:
   ```bash
   # A aplicação deve iniciar normalmente
   # A validação de esquema será executada automaticamente
   # Migrações serão aplicadas automaticamente se necessário
   ```

2. **Verifique os logs**:
   - Procure por mensagens de verificação de integridade
   - Confirme que não há erros de validação
   - Verifique se as migrações foram aplicadas com sucesso

3. **Teste a funcionalidade**:
   - Abra a aplicação
   - Verifique se os dados existentes estão presentes
   - Teste as funcionalidades principais

## Recuperação em Caso de Problemas

### Se a Atualização Falhar

1. **Pare a aplicação**:
   ```bash
   pkill -f "SimpleTaskBoardManager"
   ```

2. **Restaure o backup**:
   ```bash
   ./scripts/restore-database.sh
   ```

3. **Reinicie a aplicação**:
   - A aplicação deve funcionar com a versão anterior
   - Os dados estarão preservados

### Se a Validação Falhar

1. **Verifique os logs**:
   ```bash
   # Procure por erros de validação nos logs da aplicação
   ```

2. **Execute verificação manual** (se necessário):
   ```bash
   # Use o script de verificação
   scripts\check-database.bat
   ```

## Configurações Importantes

### application.properties
```properties
# Preserva dados durante atualizações
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.defer-datasource-initialization=false
```

### DataConfig.java
```java
// Configuração que preserva o banco existente
boolean shouldInitialize = !dbFile.exists() || !isDatabaseValid(dataSource);
initializer.setEnabled(shouldInitialize);
```

### DatabaseMigrationService.java
```java
// Execução automática de migrações
@PostConstruct
public void executeMigrations() {
    if (needsMigration()) {
        executeMigrationScript();
    }
}
```

## Monitoramento e Logs

### Logs Importantes
- `application.log` - Logs da aplicação
- `DatabaseIntegrityChecker` - Verificações de integridade
- `DatabaseMigrationService` - Logs de migração
- Logs de inicialização do Spring Boot

### Comandos de Verificação
```bash
# Verificar se o banco está acessível
java -cp h2.jar org.h2.tools.Server -tcp -tcpPort 9092

# Conectar ao banco via console
java -cp h2.jar org.h2.tools.Console
```

## Novas Funcionalidades na Versão 1.0.6

### 1. Migração Automática
- Verificação automática de estrutura do banco
- Aplicação de migrações quando necessário
- Preservação completa de dados existentes

### 2. Verificação de Integridade Melhorada
- Verificação de todas as tabelas obrigatórias
- Verificação de colunas obrigatórias
- Logs detalhados de verificação

### 3. Scripts de Backup Melhorados
- Backup automático antes de parar a aplicação
- Verificação de integridade do backup
- Metadados detalhados do backup

### 4. Recuperação Robusta
- Rollback automático em caso de erro
- Backup do estado atual antes de restauração
- Verificação de integridade após restauração

## Boas Práticas

### Para Desenvolvedores

1. **Sempre teste migrações**:
   - Teste em ambiente de desenvolvimento
   - Use dados de exemplo realistas
   - Verifique a integridade após migração

2. **Documente mudanças**:
   - Atualize este documento
   - Documente novas migrações
   - Mantenha logs de migração

3. **Versionamento**:
   - Mantenha versões do banco compatíveis
   - Use versionamento semântico
   - Teste compatibilidade entre versões

### Para Usuários

1. **Faça backup antes de atualizar**:
   - Sempre execute o script de backup
   - Mantenha múltiplos backups
   - Verifique a integridade do backup

2. **Teste após atualização**:
   - Verifique se os dados estão presentes
   - Teste funcionalidades críticas
   - Monitore logs de migração

3. **Mantenha logs**:
   - Guarde logs de erro
   - Reporte problemas com logs
   - Documente comportamentos inesperados

## Troubleshooting

### Problemas Comuns

#### Banco não encontrado
```
ERRO: Diretório do banco não encontrado
```
**Solução**: Verifique se o diretório `~/myboards/` existe

#### Validação falhou
```
ERRO: Database validation failed
```
**Solução**: Restaure backup e verifique logs

#### Aplicação não inicia
```
ERRO: Database connection failed
```
**Solução**: Verifique permissões do arquivo do banco

#### Migração falhou
```
ERRO: Migration failed
```
**Solução**: Verifique logs de migração e restaure backup se necessário

### Comandos de Diagnóstico

```bash
# Verificar arquivos do banco
ls -la ~/myboards/

# Verificar logs da aplicação
tail -f application.log

# Testar conexão com banco
java -cp h2.jar org.h2.tools.Server -tcp -tcpPort 9092

# Verificar integridade do banco
scripts\check-database.bat
```

## Suporte

Para problemas relacionados à atualização do banco de dados:

1. **Colete informações**:
   - Logs da aplicação
   - Logs de migração
   - Informações do sistema
   - Backup do banco (se possível)

2. **Documente o problema**:
   - Versão anterior e nova
   - Passos para reproduzir
   - Comportamento esperado vs atual
   - Logs de erro completos

3. **Contate o suporte**:
   - Forneça todas as informações coletadas
   - Inclua backups se necessário
   - Descreva o ambiente de execução 