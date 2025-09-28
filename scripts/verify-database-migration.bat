@echo off
echo Verificando migração do banco de dados...
echo.

REM Verificar se as colunas scheduled_date e due_date existem na tabela cards
echo Verificando colunas missing na tabela cards...
sqlite3 board.db "PRAGMA table_info(cards);" | findstr "scheduled_date\|due_date"
if %errorlevel% neq 0 (
    echo ERRO: Colunas scheduled_date e/ou due_date não encontradas na tabela cards
    echo Aplicando migração...
    sqlite3 board.db < src\main\resources\db\changelog\migrations\db.changelog-202509291200.sql
    if %errorlevel% neq 0 (
        echo ERRO: Falha ao aplicar migração
        exit /b 1
    )
    echo Migração aplicada com sucesso
) else (
    echo Colunas scheduled_date e due_date já existem
)

REM Verificar se as constraints foram aplicadas
echo.
echo Verificando constraints de integridade referencial...
sqlite3 board.db "PRAGMA foreign_key_list(calendar_events);" | findstr "fk_calendar_events_cards"
if %errorlevel% neq 0 (
    echo AVISO: Constraint fk_calendar_events_cards não encontrada
    echo Aplicando constraints...
    sqlite3 board.db < src\main\resources\db\changelog\migrations\db.changelog-202509291200.sql
    if %errorlevel% neq 0 (
        echo ERRO: Falha ao aplicar constraints
        exit /b 1
    )
    echo Constraints aplicadas com sucesso
) else (
    echo Constraints de integridade referencial já existem
)

REM Verificar se os índices foram criados
echo.
echo Verificando índices...
sqlite3 board.db "PRAGMA index_list(cards);" | findstr "idx_cards_scheduled_date\|idx_cards_due_date\|idx_cards_urgency"
if %errorlevel% neq 0 (
    echo AVISO: Alguns índices não foram encontrados
    echo Aplicando índices...
    sqlite3 board.db < src\main\resources\db\changelog\migrations\db.changelog-202509291200.sql
    if %errorlevel% neq 0 (
        echo ERRO: Falha ao aplicar índices
        exit /b 1
    )
    echo Índices aplicados com sucesso
) else (
    echo Índices já existem
)

echo.
echo Verificação concluída com sucesso!
echo O banco de dados está atualizado e consistente.
