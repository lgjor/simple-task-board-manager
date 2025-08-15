@echo off
echo === Teste de Deteccao de Aplicacao Instalada ===
echo.

REM Simular a deteccao de aplicacao instalada
set "APP_NAME=SimpleTaskBoardManager"
set "JAVA_HOME=%JAVA_HOME%"

echo Verificando locais possiveis de instalacao...
echo.

REM Verificar Program Files
if exist "%PROGRAMFILES%\%APP_NAME%\%APP_NAME%.exe" (
    echo [OK] Encontrado em: %PROGRAMFILES%\%APP_NAME%\%APP_NAME%.exe
    set "INSTALLED_PATH=%PROGRAMFILES%\%APP_NAME%\%APP_NAME%.exe"
) else (
    echo [X] Nao encontrado em: %PROGRAMFILES%\%APP_NAME%\%APP_NAME%.exe
)

REM Verificar Program Files (x86)
if exist "%PROGRAMFILES(X86)%\%APP_NAME%\%APP_NAME%.exe" (
    echo [OK] Encontrado em: %PROGRAMFILES(X86)%\%APP_NAME%\%APP_NAME%.exe
    set "INSTALLED_PATH=%PROGRAMFILES(X86)%\%APP_NAME%\%APP_NAME%.exe"
) else (
    echo [X] Nao encontrado em: %PROGRAMFILES(X86)%\%APP_NAME%\%APP_NAME%.exe
)

REM Verificar AppData Local
if exist "%USERPROFILE%\AppData\Local\%APP_NAME%\%APP_NAME%.exe" (
    echo [OK] Encontrado em: %USERPROFILE%\AppData\Local\%APP_NAME%\%APP_NAME%.exe
    set "INSTALLED_PATH=%USERPROFILE%\AppData\Local\%APP_NAME%\%APP_NAME%.exe"
) else (
    echo [X] Nao encontrado em: %USERPROFILE%\AppData\Local\%APP_NAME%\%APP_NAME%.exe
)

echo.
if defined INSTALLED_PATH (
    echo === Aplicacao Instalada Detectada ===
    echo Caminho: %INSTALLED_PATH%
    echo.
    echo Testando comando de reinicializacao...
    echo Comando: cmd /c start "%APP_NAME%" "%INSTALLED_PATH%"
    echo.
    echo NOTA: Este e apenas um teste de deteccao.
    echo Para testar a reinicializacao real, execute a aplicacao.
) else (
    echo === Nenhuma Aplicacao Instalada Detectada ===
    echo.
    if defined JAVA_HOME (
        echo Java encontrado em: %JAVA_HOME%
        echo A aplicacao tentara reiniciar via Java diretamente.
    ) else (
        echo Java nao encontrado. Configure JAVA_HOME.
    )
)

echo.
echo Pressione qualquer tecla para sair...
pause >nul
