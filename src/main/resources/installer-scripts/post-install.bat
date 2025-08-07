@echo off
REM Script de pós-instalação executado automaticamente pelo instalador
REM Este script é executado após a instalação para verificar integridade

setlocal enabledelayedexpansion

echo === Pós-Instalação SimpleTaskBoardManager ===
echo Data/Hora: %date% %time%

REM Configurações
set "APP_NAME=SimpleTaskBoardManager"
set "DB_DIR=%USERPROFILE%\myboards"
set "INSTALLED_PATH=%PROGRAMFILES%\%APP_NAME%\%APP_NAME%.exe"

REM Verifica se a aplicação foi instalada corretamente
echo Verificando instalação...

if exist "%INSTALLED_PATH%" (
    echo ✓ Aplicação instalada em: %INSTALLED_PATH%
) else (
    set "ALT_PATH=%PROGRAMFILES(X86)%\%APP_NAME%\%APP_NAME%.exe"
    if exist "%ALT_PATH%" (
        echo ✓ Aplicação instalada em: %ALT_PATH%
    ) else (
        echo ✗ Aplicação não encontrada em locais padrão
        echo Verificando outros locais...
        
        REM Verifica se está no PATH
        where %APP_NAME% >NUL 2>&1
        if !ERRORLEVEL! equ 0 (
            echo ✓ Aplicação encontrada no PATH
        ) else (
            echo ✗ Aplicação não encontrada
        )
    )
)

REM Verifica integridade do banco de dados
echo.
echo Verificando integridade do banco de dados...

if exist "%DB_DIR%\board_h2_db.mv.db" (
    echo ✓ Banco de dados encontrado
    echo   Localização: %DB_DIR%
    
    REM Verifica se o banco está acessível
    echo   Verificando acessibilidade...
    
    REM Tenta conectar ao banco (simulação)
    if exist "%DB_DIR%\board_h2_db.lock.db" (
        echo   ⚠ Banco pode estar em uso
    ) else (
        echo   ✓ Banco parece estar acessível
    )
) else (
    echo ⚠ Banco de dados não encontrado
    echo   Isso é normal para primeira instalação
    echo   O banco será criado na primeira execução
)

REM Verifica atalhos
echo.
echo Verificando atalhos...

set "start_menu=%USERPROFILE%\AppData\Roaming\Microsoft\Windows\Start Menu\Programs\%APP_NAME%"
if exist "%start_menu%" (
    echo ✓ Atalho do menu Iniciar criado
) else (
    echo ⚠ Atalho do menu Iniciar não encontrado
)

set "desktop_shortcut=%USERPROFILE%\Desktop\%APP_NAME%.lnk"
if exist "%desktop_shortcut%" (
    echo ✓ Atalho da área de trabalho criado
) else (
    echo ⚠ Atalho da área de trabalho não encontrado
)

REM Cria arquivo de informações da instalação
echo.
echo Criando informações da instalação...

set "install_info=%DB_DIR%\install-info.txt"
echo Instalação realizada em: %date% %time% > "%install_info%"
echo Versão instalada: 1.0.6 >> "%install_info%"
echo Local da aplicação: %INSTALLED_PATH% >> "%install_info%"
echo Local do banco: %DB_DIR% >> "%install_info%"
echo Usuário: %USERNAME% >> "%install_info%"

echo ✓ Informações da instalação salvas em: %install_info%

REM Verifica se é uma atualização
if exist "%DB_DIR%\backups\auto_backup_*" (
    echo.
    echo ========================================
    echo ATUALIZAÇÃO DETECTADA
    echo ========================================
    echo.
    echo Esta foi uma atualização de uma versão anterior.
    echo Seus dados foram preservados automaticamente.
    echo.
    echo Para verificar se tudo está funcionando:
    echo 1. Inicie a aplicação pelo menu Iniciar
    echo 2. Verifique se seus dados estão presentes
    echo 3. Teste as funcionalidades principais
    echo.
    echo Se houver problemas, você pode restaurar o backup:
    echo - Local: %DB_DIR%\backups\
    echo - Execute: scripts\restore-database.bat
    echo.
) else (
    echo.
    echo ========================================
    echo NOVA INSTALAÇÃO
    echo ========================================
    echo.
    echo Esta foi uma nova instalação.
    echo.
    echo Para começar a usar:
    echo 1. Inicie a aplicação pelo menu Iniciar
    echo 2. Crie seus primeiros quadros e tarefas
    echo 3. Explore as funcionalidades
    echo.
)

echo.
echo ========================================
echo    Instalação Concluída!
echo ========================================
echo.
echo Próximos passos:
echo 1. Inicie a aplicação pelo menu Iniciar
echo 2. Verifique se tudo está funcionando
echo 3. Consulte a documentação se necessário
echo.
echo Para suporte:
echo - GitHub: https://github.com/desviante/simple-task-board-manager
echo - Documentação: GUIA_ATUALIZACAO.md
echo.
echo Pressione qualquer tecla para finalizar...
pause >NUL 