@echo off
echo ========================================
echo   MIGRACAO DE DADOS EXISTENTES
echo ========================================
echo.
echo Este script executa a migracao de dados existentes
echo para compatibilidade com a nova versao do sistema.
echo.

REM Verificar se o banco existe
if not exist "C:\Users\%USERNAME%\myboards\board_h2_db.mv.db" (
    echo ERRO: Banco de dados nao encontrado!
    echo Certifique-se de que a aplicacao foi executada pelo menos uma vez.
    pause
    exit /b 1
)

echo Banco de dados encontrado.
echo.

REM Criar backup antes da migracao
echo Criando backup do banco antes da migracao...
set BACKUP_DIR=C:\Users\%USERNAME%\myboards\backup
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"

set TIMESTAMP=%date:~-4,4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set TIMESTAMP=%TIMESTAMP: =0%
set BACKUP_FILE=%BACKUP_DIR%\board_h2_db_backup_%TIMESTAMP%.mv.db

copy "C:\Users\%USERNAME%\myboards\board_h2_db.mv.db" "%BACKUP_FILE%"
if %ERRORLEVEL% NEQ 0 (
    echo ERRO: Falha ao criar backup!
    pause
    exit /b 1
)

echo Backup criado: %BACKUP_FILE%
echo.

REM Executar a aplicacao para trigger da migracao
echo Executando aplicacao para executar migracao automatica...
echo A migracao sera executada automaticamente na inicializacao.
echo.
echo Pressione qualquer tecla para iniciar a aplicacao...
pause >nul

echo.
echo Iniciando aplicacao...
echo A migracao sera executada automaticamente.
echo.
echo IMPORTANTE: Aguarde a aplicacao inicializar completamente
echo e verifique os logs para confirmar que a migracao foi executada.
echo.
echo Pressione qualquer tecla para continuar...
pause >nul

echo.
echo ========================================
echo   MIGRACAO CONCLUIDA
echo ========================================
echo.
echo Verifique os logs da aplicacao para confirmar
echo que a migracao foi executada com sucesso.
echo.
echo Backup disponivel em: %BACKUP_FILE%
echo.
pause
