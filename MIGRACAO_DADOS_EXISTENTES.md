# Migração de Dados Existentes

## Visão Geral

Este documento explica como garantir que o banco de dados existente funcione corretamente após as atualizações do sistema, especificamente as correções do `order_index` e `ProgressType`.

## Problemas Identificados

### 1. **Race Condition no `order_index`**
- **Antes**: Cards eram criados com `order_index = 0` devido a race condition na transação
- **Depois**: Cards são criados com `order_index` sequencial (1, 2, 3...)

### 2. **ProgressType Incorreto**
- **Antes**: Todos os cards tinham `ProgressType.PERCENTAGE` por padrão
- **Depois**: 
  - Cards do tipo "CARD": `ProgressType.NONE` (sem progresso)
  - Cards dos tipos BOOK/VIDEO/COURSE: `ProgressType.PERCENTAGE` (com progresso)

### 3. **Campos de Progresso Inconsistentes**
- **Antes**: Todos os cards tinham `total_units = 1` e `current_units = 0`
- **Depois**: Apenas cards com progresso habilitado têm esses campos

## Solução Implementada

### **Migração Automática**
O sistema agora inclui um `DataMigrationService` que:
1. **Corrige automaticamente** o `order_index` de todos os cards existentes
2. **Ajusta o `ProgressType`** baseado no tipo do card
3. **Limpa campos de progresso** para cards que não devem tê-los
4. **Executa na inicialização** da aplicação

### **Scripts de Migração Manual**
Scripts disponíveis para execução manual da migração:

#### **Windows**
```batch
scripts\migrate-existing-data.bat
```

#### **Linux/macOS**
```bash
chmod +x scripts/migrate-existing-data.sh
./scripts/migrate-existing-data.sh
```

## Como Executar a Migração

### **Opção 1: Automática (Recomendada)**
1. **Execute a aplicação** normalmente
2. **A migração será executada automaticamente** na inicialização
3. **Verifique os logs** para confirmar o sucesso

### **Opção 2: Manual com Scripts**
1. **Execute o script apropriado** para seu sistema operacional
2. **O script criará um backup** automático do banco
3. **Execute a aplicação** para trigger da migração
4. **Verifique os logs** para confirmar o sucesso

### **Opção 3: SQL Direto**
Execute o script SQL diretamente no banco:
```sql
src/main/resources/db/changelog/migrations/db.changelog-20250111-fix-existing-data.sql
```

## O que a Migração Corrige

### **1. Order Index**
```sql
-- Corrige order_index para ser sequencial por coluna
UPDATE cards 
SET order_index = ROW_NUMBER() OVER (PARTITION BY board_column_id ORDER BY creation_date ASC)
```

### **2. Progress Type**
```sql
-- Cards do tipo CARD devem ter ProgressType.NONE
UPDATE cards 
SET progress_type = 'NONE' 
WHERE card_type_id = 1

-- Cards dos tipos BOOK/VIDEO/COURSE devem ter ProgressType.PERCENTAGE
UPDATE cards 
SET progress_type = 'PERCENTAGE' 
WHERE card_type_id IN (2, 3, 4)
```

### **3. Campos de Progresso**
```sql
-- Limpa campos para cards sem progresso
UPDATE cards 
SET total_units = NULL, current_units = NULL 
WHERE progress_type = 'NONE'

-- Garante valores válidos para cards com progresso
UPDATE cards 
SET total_units = COALESCE(total_units, 1), current_units = COALESCE(current_units, 0)
WHERE progress_type = 'PERCENTAGE'
```

## Verificação da Migração

### **Logs da Aplicação**
A migração gera logs detalhados:
```
INFO - Iniciando migração de dados existentes...
INFO - Corrigindo order_index dos cards existentes...
INFO - Order_index corrigido para X cards
INFO - Corrigindo ProgressType dos cards existentes...
INFO - ProgressType corrigido para X cards do tipo CARD
INFO - ProgressType corrigido para X cards dos tipos BOOK/VIDEO/COURSE
INFO - Limpando campos de progresso...
INFO - Campos de progresso limpos para X cards sem progresso
INFO - Valores de progresso corrigidos para X cards com progresso
INFO - Migração de dados existentes concluída com sucesso!
```

### **Verificação Manual**
Execute esta consulta para verificar a integridade:
```sql
SELECT 
    COUNT(*) as total_cards,
    COUNT(CASE WHEN progress_type = 'NONE' THEN 1 END) as cards_sem_progresso,
    COUNT(CASE WHEN progress_type = 'PERCENTAGE' THEN 1 END) as cards_com_progresso,
    COUNT(CASE WHEN order_index > 0 THEN 1 END) as cards_com_order_index_valido
FROM cards;
```

## Backup e Segurança

### **Backup Automático**
- **Localização**: `~/myboards/backup/` (Linux/macOS) ou `C:\Users\%USERNAME%\myboards\backup\` (Windows)
- **Formato**: `board_h2_db_backup_YYYYMMDD_HHMMSS.mv.db`
- **Criação**: Automática antes da migração

### **Restauração**
Se algo der errado:
1. **Pare a aplicação**
2. **Copie o arquivo de backup** para o local original
3. **Renomeie** para `board_h2_db.mv.db`
4. **Reinicie a aplicação**

## Troubleshooting

### **Erro: "Tabela não encontrada"**
- **Causa**: Banco não foi inicializado
- **Solução**: Execute a aplicação pelo menos uma vez antes da migração

### **Erro: "Permissão negada"**
- **Causa**: Sem permissão para escrever no diretório
- **Solução**: Execute como administrador ou verifique permissões

### **Migração não executou**
- **Causa**: Dados já estão corretos
- **Verificação**: Execute `isMigrationNeeded()` para confirmar

## Resultado Esperado

Após a migração bem-sucedida:

✅ **Order Index**: Todos os cards terão `order_index` sequencial (1, 2, 3...)
✅ **Progress Type**: Cards terão o tipo correto baseado em sua categoria
✅ **Campos de Progresso**: Apenas cards que devem ter progresso terão esses campos
✅ **Movimentação**: Funcionará corretamente sem erros "Card já está na base"

## Suporte

Se encontrar problemas:
1. **Verifique os logs** da aplicação
2. **Confirme o backup** foi criado
3. **Execute a verificação manual** de integridade
4. **Consulte** este documento para troubleshooting
