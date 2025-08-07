@echo off
REM Script de restauração do banco de dados H2 para Windows
REM Este script deve ser executado em caso de problemas após atualização

setlocal enabledelayedexpansion

REM Configurações
set "DB_DIR=%USERPROFILE%\myboards"
set "DB_NAME=board_h2_db"
set "BACKUP_DIR=%USERPROFILE%\myboards\backups"

echo === Restauração do Banco de Dados H2 ===
echo Data/Hora: %date% %time%
echo Diretório do banco: %DB_DIR%

REM Verifica se há backups disponíveis
if not exist "%BACKUP_DIR%" (
    echo ERRO: Diretório de backups não encontrado: %BACKUP_DIR%
    pause
    exit /b 1
)

REM Lista backups disponíveis
echo.
echo Backups disponíveis:
set "counter=0"
for %%f in ("%BACKUP_DIR%\board_h2_db_backup_*") do (
    set /a counter+=1
    echo !counter!. %%~nxf
)

if %counter%==0 (
    echo Nenhum backup encontrado.
    pause
    exit /b 1
)

REM Solicita qual backup restaurar
echo.
set /p "backup_number=Digite o número do backup para restaurar (ou 'q' para sair): "

if /i "%backup_number%"=="q" (
    echo Operação cancelada.
    pause
    exit /b 0
)

REM Obtém o nome do backup selecionado
set "counter=0"
for %%f in ("%BACKUP_DIR%\board_h2_db_backup_*") do (
    set /a counter+=1
    if !counter!==%backup_number% (
        set "backup_name=%%~nxf"
        goto :found_backup
    )
)

echo ERRO: Backup selecionado não encontrado.
pause
exit /b 1

:found_backup
echo.
echo Backup selecionado: %backup_name%
echo Localização: %BACKUP_DIR%\%backup_name%

REM Confirma a operação
set /p "confirm=Tem certeza que deseja restaurar este backup? (s/N): "

if /i not "%confirm%"=="s" (
    echo Operação cancelada.
    pause
    exit /b 0
)

REM Para a aplicação se estiver rodando
echo Verificando se a aplicação está rodando...
tasklist /FI "IMAGENAME eq SimpleTaskBoardManager.exe" 2>NUL | find /I /N "SimpleTaskBoardManager.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo Parando aplicação...
    taskkill /F /IM SimpleTaskBoardManager.exe 2>NUL
    timeout /t 3 /nobreak >NUL
)

REM Cria backup do estado atual antes da restauração
if exist "%DB_DIR%" (
    echo Criando backup do estado atual...
    set "current_backup=current_before_restore_%date:~-4,4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%%time:~6,2%"
    set "current_backup=%current_backup: =0%"
    mkdir "%BACKUP_DIR%\%current_backup%" 2>NUL
    
    for %%f in ("%DB_DIR%\%DB_NAME%.*") do (
        if exist "%%f" (
            copy "%%f" "%BACKUP_DIR%\%current_backup%\" >NUL
        )
    )
)

REM Remove arquivos atuais do banco
echo Removendo arquivos atuais do banco...
for %%f in ("%DB_DIR%\%DB_NAME%.*") do (
    if exist "%%f" (
        del "%%f" 2>NUL
    )
)

REM Restaura o backup
echo Restaurando backup...
for %%f in ("%BACKUP_DIR%\%backup_name%\*") do (
    if exist "%%f" (
        echo Copiando: %%~nxf
        copy "%%f" "%DB_DIR%\" >NUL
        if !ERRORLEVEL! neq 0 (
            echo ERRO: Falha ao restaurar arquivo: %%~nxf
            goto :error
        )
    )
)

echo.
echo Restauração concluída com sucesso!
echo Você pode agora iniciar a aplicação novamente.
goto :end

:error
echo ERRO: Falha durante a restauração
pause
exit /b 1

:end
echo.
echo Pressione qualquer tecla para continuar...
pause >NUL 