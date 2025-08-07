@echo off
REM Script de backup do banco de dados H2 para Windows
REM Este script deve ser executado antes de qualquer atualização

setlocal enabledelayedexpansion

REM Configurações
set "DB_DIR=%USERPROFILE%\myboards"
set "DB_NAME=board_h2_db"
set "BACKUP_DIR=%USERPROFILE%\myboards\backups"
set "TIMESTAMP=%date:~-4,4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%%time:~6,2%"
set "TIMESTAMP=%TIMESTAMP: =0%"
set "BACKUP_NAME=board_h2_db_backup_%TIMESTAMP%"

echo === Backup do Banco de Dados H2 ===
echo Data/Hora: %date% %time%
echo Diretório do banco: %DB_DIR%
echo Nome do backup: %BACKUP_NAME%

REM Verifica se o diretório do banco existe
if not exist "%DB_DIR%" (
    echo ERRO: Diretório do banco não encontrado: %DB_DIR%
    echo Criando diretório...
    mkdir "%DB_DIR%"
)

REM Cria diretório de backup se não existir
if not exist "%BACKUP_DIR%" (
    echo Criando diretório de backup...
    mkdir "%BACKUP_DIR%"
)

REM Verifica se o banco está em uso
if exist "%DB_DIR%\%DB_NAME%.lock.db" (
    echo AVISO: Banco de dados pode estar em uso. Tentando backup mesmo assim...
)

REM Para a aplicação se estiver rodando (opcional)
echo Verificando se a aplicação está rodando...
tasklist /FI "IMAGENAME eq SimpleTaskBoardManager.exe" 2>NUL | find /I /N "SimpleTaskBoardManager.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo Parando aplicação...
    taskkill /F /IM SimpleTaskBoardManager.exe 2>NUL
    timeout /t 3 /nobreak >NUL
)

REM Realiza o backup
echo Iniciando backup...
set "BACKUP_PATH=%BACKUP_DIR%\%BACKUP_NAME%"
mkdir "%BACKUP_PATH%" 2>NUL

REM Copia todos os arquivos do banco
for %%f in ("%DB_DIR%\%DB_NAME%.*") do (
    if exist "%%f" (
        echo Copiando: %%~nxf
        copy "%%f" "%BACKUP_PATH%\" >NUL
        if !ERRORLEVEL! neq 0 (
            echo ERRO: Falha ao copiar arquivo: %%~nxf
            goto :error
        )
    )
)

REM Cria um arquivo de metadados do backup
echo Backup realizado em: %date% %time% > "%BACKUP_PATH%\backup-info.txt"
echo Diretório original: %DB_DIR% >> "%BACKUP_PATH%\backup-info.txt"
echo Arquivos incluídos: >> "%BACKUP_PATH%\backup-info.txt"
dir "%BACKUP_PATH%" /B | findstr /R "\.db$" >> "%BACKUP_PATH%\backup-info.txt"

echo Backup concluído com sucesso!
echo Localização: %BACKUP_PATH%
echo.

REM Lista backups recentes
echo Backups disponíveis:
dir "%BACKUP_DIR%" /B | findstr "board_h2_db_backup" | findstr /V "backup-info.txt"
goto :end

:error
echo ERRO: Falha durante o backup
exit /b 1

:end
echo.
echo Pressione qualquer tecla para continuar...
pause >NUL 