@echo off
REM Script de pré-instalação executado automaticamente pelo instalador
REM Este script é executado antes da instalação para garantir compatibilidade

setlocal enabledelayedexpansion

echo === Pré-Instalação SimpleTaskBoardManager ===
echo Data/Hora: %date% %time%

REM Configurações
set "APP_NAME=SimpleTaskBoardManager"
set "DB_DIR=%USERPROFILE%\myboards"
set "BACKUP_DIR=%USERPROFILE%\myboards\backups"

REM Verifica se há uma versão anterior instalada
echo Verificando versões anteriores...

set "found_previous=false"
set "previous_version="

REM Verifica no Program Files (64-bit)
if exist "%PROGRAMFILES%\%APP_NAME%" (
    echo Versão anterior encontrada em: %PROGRAMFILES%\%APP_NAME%
    set "found_previous=true"
    set "previous_location=%PROGRAMFILES%\%APP_NAME%"
)

REM Verifica no Program Files (32-bit)
if exist "%PROGRAMFILES(X86)%\%APP_NAME%" (
    echo Versão anterior encontrada em: %PROGRAMFILES(X86)%\%APP_NAME%
    set "found_previous=true"
    set "previous_location=%PROGRAMFILES(X86)%\%APP_NAME%"
)

if "%found_previous%"=="true" (
    echo.
    echo ========================================
    echo ATUALIZAÇÃO DETECTADA
    echo ========================================
    echo.
    echo Uma versão anterior foi encontrada e será atualizada.
    echo Seus dados serão preservados automaticamente.
    echo.
    
    REM Cria backup automático se o banco existir
    if exist "%DB_DIR%\board_h2_db.mv.db" (
        echo Criando backup automático do banco de dados...
        
        REM Cria diretório de backup se não existir
        if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"
        
        REM Cria backup com timestamp
        set "timestamp=%date:~-4,4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%%time:~6,2%"
        set "timestamp=%timestamp: =0%"
        set "backup_name=auto_backup_%timestamp%"
        
        echo Backup: %BACKUP_DIR%\%backup_name%
        
        REM Copia arquivos do banco
        if not exist "%BACKUP_DIR%\%backup_name%" mkdir "%BACKUP_DIR%\%backup_name%"
        copy "%DB_DIR%\board_h2_db.*" "%BACKUP_DIR%\%backup_name%\" >NUL 2>&1
        
        if !ERRORLEVEL! equ 0 (
            echo ✓ Backup criado com sucesso
        ) else (
            echo ⚠ Backup falhou, mas continuando...
        )
    ) else (
        echo Banco de dados não encontrado (primeira instalação)
    )
    
    REM Para a aplicação se estiver rodando
    echo Verificando se a aplicação está rodando...
    tasklist /FI "IMAGENAME eq %APP_NAME%.exe" 2>NUL | find /I /N "%APP_NAME%.exe">NUL
    if "%ERRORLEVEL%"=="0" (
        echo Parando aplicação anterior...
        taskkill /F /IM %APP_NAME%.exe 2>NUL
        timeout /t 3 /nobreak >NUL
    )
    
    echo.
    echo Pronto para atualização!
    echo A versão anterior será substituída pela nova versão.
    echo.
) else (
    echo.
    echo ========================================
    echo NOVA INSTALAÇÃO
    echo ========================================
    echo.
    echo Esta é uma nova instalação.
    echo Nenhuma versão anterior foi encontrada.
    echo.
)

REM Cria diretório do banco se não existir
if not exist "%DB_DIR%" (
    echo Criando diretório do banco de dados...
    mkdir "%DB_DIR%"
)

echo.
echo Pré-instalação concluída!
echo.
echo Pressione qualquer tecla para continuar com a instalação...
pause >NUL 