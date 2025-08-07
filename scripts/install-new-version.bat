@echo off
REM Script de instalação automatizada para nova versão
REM Este script faz backup, desinstala versão anterior e instala a nova versão

setlocal enabledelayedexpansion

echo ========================================
echo    Instalador Automatizado v1.0.6
echo ========================================
echo.
echo Este script irá:
echo 1. Fazer backup do banco de dados
echo 2. Desinstalar versão anterior
echo 3. Instalar nova versão
echo 4. Verificar integridade
echo.

REM Verifica se o instalador existe
set "INSTALLER_PATH=build\dist\SimpleTaskBoardManager-1.0.6.exe"
if not exist "%INSTALLER_PATH%" (
    echo ERRO: Instalador não encontrado em: %INSTALLER_PATH%
    echo.
    echo Certifique-se de que:
    echo 1. O projeto foi compilado com: gradlew jpackage
    echo 2. O instalador está em: build\dist\
    echo.
    pause
    exit /b 1
)

echo Instalador encontrado: %INSTALLER_PATH%
echo.

REM Passo 1: Backup do banco de dados
echo ========================================
echo PASSO 1: Backup do Banco de Dados
echo ========================================
echo.
call scripts\backup-database.bat
if !ERRORLEVEL! neq 0 (
    echo.
    echo AVISO: Backup falhou, mas continuando...
    echo.
)

REM Passo 2: Desinstalar versão anterior
echo ========================================
echo PASSO 2: Desinstalando Versão Anterior
echo ========================================
echo.
call scripts\uninstall-previous-version.bat
if !ERRORLEVEL! neq 0 (
    echo.
    echo AVISO: Desinstalação falhou, mas continuando...
    echo.
)

REM Passo 3: Instalar nova versão
echo ========================================
echo PASSO 3: Instalando Nova Versão
echo ========================================
echo.
echo Iniciando instalação da versão 1.0.6...
echo.

REM Executa o instalador
echo Executando instalador...
start /wait "" "%INSTALLER_PATH%"

REM Verifica se a instalação foi bem-sucedida
if !ERRORLEVEL! neq 0 (
    echo.
    echo ERRO: Falha na instalação!
    echo.
    echo Possíveis soluções:
    echo 1. Execute o instalador manualmente
    echo 2. Verifique se tem permissões de administrador
    echo 3. Verifique se não há antivírus bloqueando
    echo.
    pause
    exit /b 1
)

echo.
echo Instalação concluída com sucesso!
echo.

REM Passo 4: Verificar integridade
echo ========================================
echo PASSO 4: Verificação de Integridade
echo ========================================
echo.

REM Aguarda um pouco para garantir que a instalação foi finalizada
timeout /t 5 /nobreak >NUL

echo Verificando se a aplicação foi instalada corretamente...

REM Verifica se o executável foi instalado
set "INSTALLED_PATH=%PROGRAMFILES%\SimpleTaskBoardManager\SimpleTaskBoardManager.exe"
if exist "!INSTALLED_PATH!" (
    echo ✓ Aplicação instalada em: !INSTALLED_PATH!
) else (
    echo ✗ Aplicação não encontrada em: !INSTALLED_PATH!
    echo Verificando locais alternativos...
    
    set "ALT_PATH=%PROGRAMFILES(X86)%\SimpleTaskBoardManager\SimpleTaskBoardManager.exe"
    if exist "!ALT_PATH!" (
        echo ✓ Aplicação encontrada em: !ALT_PATH!
    ) else (
        echo ✗ Aplicação não encontrada em locais padrão
    )
)

REM Verifica se o banco de dados está preservado
echo.
echo Verificando integridade do banco de dados...
if exist "%USERPROFILE%\myboards\board_h2_db.mv.db" (
    echo ✓ Banco de dados preservado
) else (
    echo ⚠ Banco de dados não encontrado (pode ser normal para primeira instalação)
)

echo.
echo ========================================
echo    Instalação Concluída!
echo ========================================
echo.
echo Próximos passos:
echo 1. Inicie a aplicação pelo menu Iniciar
echo 2. Verifique se seus dados estão presentes
echo 3. Teste as funcionalidades principais
echo.
echo Se houver problemas:
echo - Execute: scripts\restore-database.bat
echo - Verifique os logs da aplicação
echo - Consulte: GUIA_ATUALIZACAO.md
echo.
echo Pressione qualquer tecla para finalizar...
pause >NUL 