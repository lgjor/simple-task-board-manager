@echo off
REM Script para desinstalar versões anteriores do SimpleTaskBoardManager
REM Este script deve ser executado antes da instalação de uma nova versão

setlocal enabledelayedexpansion

echo === Desinstalador de Versões Anteriores ===
echo Data/Hora: %date% %time%

REM Configurações
set "APP_NAME=SimpleTaskBoardManager"
set "PROGRAM_FILES=%PROGRAMFILES%"
set "PROGRAM_FILES_X86=%PROGRAMFILES(X86)%"
set "USER_PROFILE=%USERPROFILE%"

echo Verificando versões instaladas...

REM Para a aplicação se estiver rodando
echo Verificando se a aplicação está rodando...
tasklist /FI "IMAGENAME eq SimpleTaskBoardManager.exe" 2>NUL | find /I /N "SimpleTaskBoardManager.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo Parando aplicação...
    taskkill /F /IM SimpleTaskBoardManager.exe 2>NUL
    timeout /t 3 /nobreak >NUL
)

REM Remove do Program Files (64-bit)
if exist "%PROGRAM_FILES%\%APP_NAME%" (
    echo Removendo instalação 64-bit: %PROGRAM_FILES%\%APP_NAME%
    rmdir /S /Q "%PROGRAM_FILES%\%APP_NAME%" 2>NUL
    if !ERRORLEVEL! neq 0 (
        echo AVISO: Não foi possível remover completamente %PROGRAM_FILES%\%APP_NAME%
    )
)

REM Remove do Program Files (32-bit)
if exist "%PROGRAM_FILES_X86%\%APP_NAME%" (
    echo Removendo instalação 32-bit: %PROGRAM_FILES_X86%\%APP_NAME%
    rmdir /S /Q "%PROGRAM_FILES_X86%\%APP_NAME%" 2>NUL
    if !ERRORLEVEL! neq 0 (
        echo AVISO: Não foi possível remover completamente %PROGRAM_FILES_X86%\%APP_NAME%
    )
)

REM Remove atalhos do menu Iniciar
echo Removendo atalhos do menu Iniciar...
if exist "%USERPROFILE%\AppData\Roaming\Microsoft\Windows\Start Menu\Programs\%APP_NAME%" (
    rmdir /S /Q "%USERPROFILE%\AppData\Roaming\Microsoft\Windows\Start Menu\Programs\%APP_NAME%" 2>NUL
)

REM Remove atalhos da área de trabalho
echo Removendo atalhos da área de trabalho...
if exist "%USERPROFILE%\Desktop\%APP_NAME%.lnk" (
    del "%USERPROFILE%\Desktop\%APP_NAME%.lnk" 2>NUL
)

REM Remove entradas do registro (se existirem)
echo Removendo entradas do registro...
reg delete "HKCU\Software\Microsoft\Windows\CurrentVersion\Uninstall\%APP_NAME%" /f 2>NUL
reg delete "HKLM\SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\%APP_NAME%" /f 2>NUL

REM Remove arquivos temporários
echo Removendo arquivos temporários...
if exist "%TEMP%\%APP_NAME%*" (
    del /Q "%TEMP%\%APP_NAME%*" 2>NUL
)

REM Verifica se ainda existem instalações
echo.
echo Verificando se ainda existem instalações...
set "found_installations=false"

if exist "%PROGRAM_FILES%\%APP_NAME%" (
    echo AVISO: Ainda existe instalação em: %PROGRAM_FILES%\%APP_NAME%
    set "found_installations=true"
)

if exist "%PROGRAM_FILES_X86%\%APP_NAME%" (
    echo AVISO: Ainda existe instalação em: %PROGRAM_FILES_X86%\%APP_NAME%
    set "found_installations=true"
)

if "%found_installations%"=="true" (
    echo.
    echo ATENÇÃO: Algumas instalações não puderam ser removidas automaticamente.
    echo Você pode precisar remover manualmente ou executar como administrador.
    echo.
    echo Locais para verificar:
    echo - %PROGRAM_FILES%\%APP_NAME%
    echo - %PROGRAM_FILES_X86%\%APP_NAME%
    echo.
    set /p "continue=Deseja continuar com a instalação mesmo assim? (s/N): "
    if /i not "!continue!"=="s" (
        echo Instalação cancelada.
        pause
        exit /b 1
    )
) else (
    echo Todas as versões anteriores foram removidas com sucesso!
)

echo.
echo Desinstalação concluída!
echo Você pode agora prosseguir com a instalação da nova versão.
echo.
echo Pressione qualquer tecla para continuar...
pause >NUL 