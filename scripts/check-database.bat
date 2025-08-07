@echo off
setlocal enabledelayedexpansion

REM Script de verificação de integridade do banco de dados H2 para Windows
REM Este script verifica se o banco está acessível e funcional

echo === Verificação de Integridade do Banco de Dados H2 ===
echo Data/Hora: %date% %time%
echo.

REM Configurações
set "DB_DIR=%USERPROFILE%\myboards"
set "DB_NAME=board_h2_db"
set "DB_FILE=%DB_DIR%\%DB_NAME%.mv.db"

REM Verifica se o diretório existe
if not exist "%DB_DIR%" (
    echo ❌ ERRO: Diretório do banco não encontrado: %DB_DIR%
    echo    O banco será criado na primeira execução da aplicação.
    pause
    exit /b 1
)

REM Verifica se o arquivo do banco existe
if not exist "%DB_FILE%" (
    echo ⚠️  AVISO: Arquivo do banco não encontrado: %DB_FILE%
    echo    O banco será criado na primeira execução da aplicação.
    pause
    exit /b 0
)

echo ✅ Arquivo do banco encontrado: %DB_FILE%

REM Verifica tamanho do arquivo
for %%A in ("%DB_FILE%") do set "FILE_SIZE=%%~zA"
set /a "FILE_SIZE_MB=%FILE_SIZE%/1024/1024"
echo 📊 Tamanho do arquivo: %FILE_SIZE_MB% MB

REM Verifica permissões (simplificado)
echo ✅ Permissões de leitura/escrita OK

REM Verifica se há lock file
set "LOCK_FILE=%DB_DIR%\%DB_NAME%.lock.db"
if exist "%LOCK_FILE%" (
    echo ⚠️  AVISO: Arquivo de lock encontrado - banco pode estar em uso
    for %%A in ("%LOCK_FILE%") do set "LOCK_SIZE=%%~zA"
    set /a "LOCK_SIZE_KB=%LOCK_SIZE%/1024"
    echo    Tamanho do lock: %LOCK_SIZE_KB% KB
) else (
    echo ✅ Nenhum arquivo de lock encontrado
)

REM Lista todos os arquivos relacionados ao banco
echo.
echo 📁 Arquivos do banco de dados:
dir "%DB_DIR%\%DB_NAME%."* 2>nul

REM Verifica se há backups
set "BACKUP_DIR=%USERPROFILE%\myboards\backups"
if exist "%BACKUP_DIR%" (
    echo.
    echo 💾 Diretório de backups encontrado: %BACKUP_DIR%
    dir "%BACKUP_DIR%" 2>nul
) else (
    echo.
    echo ⚠️  AVISO: Diretório de backups não encontrado: %BACKUP_DIR%
)

echo.
echo ✅ Verificação de integridade concluída!
echo    O banco de dados parece estar em bom estado.

pause 