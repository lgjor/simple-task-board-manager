# Script para verificar consistência do schema entre ambientes
# Verifica se as tabelas e colunas necessárias existem no banco de produção

Write-Host "🔍 Verificando consistência do schema do banco de dados..." -ForegroundColor Green
Write-Host ""

# Caminho do banco de dados H2
$dbPath = "$env:USERPROFILE\myboards\board_h2_db"

# Verificar se o banco existe
if (-not (Test-Path $dbPath)) {
    Write-Host "❌ Banco de dados não encontrado em: $dbPath" -ForegroundColor Red
    Write-Host "Execute a aplicação pelo menos uma vez para criar o banco." -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ Banco de dados encontrado: $dbPath" -ForegroundColor Green
Write-Host ""

# Função para executar consultas SQL
function Invoke-SqlQuery {
    param([string]$query)
    try {
        $result = java -cp "build\libs\*" org.h2.tools.Shell -url "jdbc:h2:file:$dbPath" -user "MYBOARDUSER" -password "MYBOARDPASS" -sql $query 2>$null
        return $result
    } catch {
        Write-Host "❌ Erro ao executar consulta: $query" -ForegroundColor Red
        return $null
    }
}

# Verificar tabelas existentes
Write-Host "📋 Verificando tabelas existentes..." -ForegroundColor Yellow
$tablesQuery = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC' ORDER BY TABLE_NAME;"
$tables = Invoke-SqlQuery $tablesQuery

if ($tables) {
    Write-Host "Tabelas encontradas:" -ForegroundColor Cyan
    $tables | ForEach-Object { Write-Host "  - $_" -ForegroundColor White }
} else {
    Write-Host "❌ Não foi possível listar as tabelas" -ForegroundColor Red
}

Write-Host ""

# Verificar se a tabela cards tem as colunas necessárias
Write-Host "🔍 Verificando colunas da tabela CARDS..." -ForegroundColor Yellow
$cardsColumnsQuery = "SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'CARDS' ORDER BY ORDINAL_POSITION;"
$cardsColumns = Invoke-SqlQuery $cardsColumnsQuery

if ($cardsColumns) {
    Write-Host "Colunas da tabela CARDS:" -ForegroundColor Cyan
    $cardsColumns | ForEach-Object { Write-Host "  - $_" -ForegroundColor White }
    
    # Verificar colunas específicas
    $requiredColumns = @("ID", "TITLE", "DESCRIPTION", "SCHEDULED_DATE", "DUE_DATE", "BOARD_COLUMN_ID", "ORDER_INDEX")
    $missingColumns = @()
    
    foreach ($column in $requiredColumns) {
        if ($cardsColumns -notmatch $column) {
            $missingColumns += $column
        }
    }
    
    if ($missingColumns.Count -gt 0) {
        Write-Host "❌ Colunas ausentes na tabela CARDS:" -ForegroundColor Red
        $missingColumns | ForEach-Object { Write-Host "  - $_" -ForegroundColor Red }
    } else {
        Write-Host "✅ Todas as colunas necessárias estão presentes na tabela CARDS" -ForegroundColor Green
    }
} else {
    Write-Host "❌ Não foi possível verificar as colunas da tabela CARDS" -ForegroundColor Red
}

Write-Host ""

# Verificar se a tabela integration_sync_status existe
Write-Host "🔍 Verificando tabela INTEGRATION_SYNC_STATUS..." -ForegroundColor Yellow
$integrationTableQuery = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'INTEGRATION_SYNC_STATUS';"
$integrationTable = Invoke-SqlQuery $integrationTableQuery

if ($integrationTable) {
    Write-Host "✅ Tabela INTEGRATION_SYNC_STATUS existe" -ForegroundColor Green
    
    # Verificar colunas da tabela integration_sync_status
    $integrationColumnsQuery = "SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'INTEGRATION_SYNC_STATUS' ORDER BY ORDINAL_POSITION;"
    $integrationColumns = Invoke-SqlQuery $integrationColumnsQuery
    
    if ($integrationColumns) {
        Write-Host "Colunas da tabela INTEGRATION_SYNC_STATUS:" -ForegroundColor Cyan
        $integrationColumns | ForEach-Object { Write-Host "  - $_" -ForegroundColor White }
    }
} else {
    Write-Host "❌ Tabela INTEGRATION_SYNC_STATUS não existe" -ForegroundColor Red
    Write-Host "Esta tabela é necessária para o funcionamento das integrações." -ForegroundColor Yellow
}

Write-Host ""

# Verificar se a tabela calendar_events existe
Write-Host "🔍 Verificando tabela CALENDAR_EVENTS..." -ForegroundColor Yellow
$calendarTableQuery = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'CALENDAR_EVENTS';"
$calendarTable = Invoke-SqlQuery $calendarTableQuery

if ($calendarTable) {
    Write-Host "✅ Tabela CALENDAR_EVENTS existe" -ForegroundColor Green
} else {
    Write-Host "❌ Tabela CALENDAR_EVENTS não existe" -ForegroundColor Red
}

Write-Host ""

# Verificar índices importantes
Write-Host "🔍 Verificando índices importantes..." -ForegroundColor Yellow
$indexesQuery = "SELECT INDEX_NAME, TABLE_NAME FROM INFORMATION_SCHEMA.INDEXES WHERE TABLE_NAME IN ('CARDS', 'CALENDAR_EVENTS', 'INTEGRATION_SYNC_STATUS') ORDER BY TABLE_NAME, INDEX_NAME;"
$indexes = Invoke-SqlQuery $indexesQuery

if ($indexes) {
    Write-Host "Índices encontrados:" -ForegroundColor Cyan
    $indexes | ForEach-Object { Write-Host "  - $_" -ForegroundColor White }
} else {
    Write-Host "❌ Não foi possível listar os índices" -ForegroundColor Red
}

Write-Host ""

# Verificar constraints de foreign key
Write-Host "🔍 Verificando constraints de foreign key..." -ForegroundColor Yellow
$constraintsQuery = "SELECT CONSTRAINT_NAME, TABLE_NAME, COLUMN_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE REFERENCED_TABLE_NAME IS NOT NULL ORDER BY TABLE_NAME, CONSTRAINT_NAME;"
$constraints = Invoke-SqlQuery $constraintsQuery

if ($constraints) {
    Write-Host "Constraints de foreign key encontradas:" -ForegroundColor Cyan
    $constraints | ForEach-Object { Write-Host "  - $_" -ForegroundColor White }
} else {
    Write-Host "❌ Não foi possível listar as constraints" -ForegroundColor Red
}

Write-Host ""
Write-Host "🎯 Verificação concluída!" -ForegroundColor Green
Write-Host "Se houver problemas, execute as migrações necessárias." -ForegroundColor Yellow
