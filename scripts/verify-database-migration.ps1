Write-Host "Verificando migração do banco de dados..." -ForegroundColor Green
Write-Host ""

# Verificar se as colunas scheduled_date e due_date existem na tabela cards
Write-Host "Verificando colunas missing na tabela cards..." -ForegroundColor Yellow
$dbPath = "$env:USERPROFILE\myboards\board_h2_db"
$columns = java -cp "build\libs\*" org.h2.tools.Shell -url "jdbc:h2:file:$dbPath" -user "MYBOARDUSER" -password "MYBOARDPASS" -sql "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'CARDS' AND COLUMN_NAME IN ('SCHEDULED_DATE', 'DUE_DATE');" | Select-String "SCHEDULED_DATE|DUE_DATE"
if (-not $columns) {
    Write-Host "ERRO: Colunas scheduled_date e/ou due_date não encontradas na tabela cards" -ForegroundColor Red
    Write-Host "Aplicando migração..." -ForegroundColor Yellow
    Get-Content src\main\resources\db\changelog\migrations\db.changelog-202509291200-h2.sql | java -cp "build\libs\*" org.h2.tools.Shell -url "jdbc:h2:file:$dbPath" -user "MYBOARDUSER" -password "MYBOARDPASS"
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERRO: Falha ao aplicar migração" -ForegroundColor Red
        exit 1
    }
    Write-Host "Migração aplicada com sucesso" -ForegroundColor Green
} else {
    Write-Host "Colunas scheduled_date e due_date já existem" -ForegroundColor Green
}

# Verificar se as constraints foram aplicadas
Write-Host ""
Write-Host "Verificando constraints de integridade referencial..." -ForegroundColor Yellow
$constraints = java -cp "build\libs\*" org.h2.tools.Shell -url "jdbc:h2:file:$dbPath" -user "MYBOARDUSER" -password "MYBOARDPASS" -sql "SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE TABLE_NAME = 'CALENDAR_EVENTS' AND CONSTRAINT_NAME = 'FK_CALENDAR_EVENTS_CARDS';" | Select-String "FK_CALENDAR_EVENTS_CARDS"
if (-not $constraints) {
    Write-Host "AVISO: Constraint fk_calendar_events_cards não encontrada" -ForegroundColor Yellow
    Write-Host "Aplicando constraints..." -ForegroundColor Yellow
    Get-Content src\main\resources\db\changelog\migrations\db.changelog-202509291200-h2.sql | java -cp "build\libs\*" org.h2.tools.Shell -url "jdbc:h2:file:$dbPath" -user "MYBOARDUSER" -password "MYBOARDPASS"
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERRO: Falha ao aplicar constraints" -ForegroundColor Red
        exit 1
    }
    Write-Host "Constraints aplicadas com sucesso" -ForegroundColor Green
} else {
    Write-Host "Constraints de integridade referencial já existem" -ForegroundColor Green
}

# Verificar se os índices foram criados
Write-Host ""
Write-Host "Verificando índices..." -ForegroundColor Yellow
$indexes = java -cp "build\libs\*" org.h2.tools.Shell -url "jdbc:h2:file:$dbPath" -user "MYBOARDUSER" -password "MYBOARDPASS" -sql "SELECT INDEX_NAME FROM INFORMATION_SCHEMA.INDEXES WHERE TABLE_NAME = 'CARDS' AND INDEX_NAME IN ('IDX_CARDS_SCHEDULED_DATE', 'IDX_CARDS_DUE_DATE', 'IDX_CARDS_URGENCY');" | Select-String "IDX_CARDS_SCHEDULED_DATE|IDX_CARDS_DUE_DATE|IDX_CARDS_URGENCY"
if (-not $indexes) {
    Write-Host "AVISO: Alguns índices não foram encontrados" -ForegroundColor Yellow
    Write-Host "Aplicando índices..." -ForegroundColor Yellow
    Get-Content src\main\resources\db\changelog\migrations\db.changelog-202509291200-h2.sql | java -cp "build\libs\*" org.h2.tools.Shell -url "jdbc:h2:file:$dbPath" -user "MYBOARDUSER" -password "MYBOARDPASS"
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERRO: Falha ao aplicar índices" -ForegroundColor Red
        exit 1
    }
    Write-Host "Índices aplicados com sucesso" -ForegroundColor Green
} else {
    Write-Host "Índices já existem" -ForegroundColor Green
}

Write-Host ""
Write-Host "Verificação concluída com sucesso!" -ForegroundColor Green
Write-Host "O banco de dados está atualizado e consistente." -ForegroundColor Green
