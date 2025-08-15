@echo off
echo === Teste de Deteccao de Aplicacao Instalada ===
echo.

set APP_NAME=SimpleTaskBoardManager

echo Verificando locais possiveis de instalacao...
echo.

if exist "%PROGRAMFILES%\%APP_NAME%\%APP_NAME%.exe" (
    echo [OK] Encontrado em: %PROGRAMFILES%\%APP_NAME%\%APP_NAME%.exe
    set INSTALLED_PATH=%PROGRAMFILES%\%APP_NAME%\%APP_NAME%.exe
) else (
    echo [X] Nao encontrado em: %PROGRAMFILES%\%APP_NAME%\%APP_NAME%.exe
)

if exist "%PROGRAMFILES(X86)%\%APP_NAME%\%APP_NAME%.exe" (
    echo [OK] Encontrado em: %PROGRAMFILES(X86)%\%APP_NAME%\%APP_NAME%.exe
    set INSTALLED_PATH=%PROGRAMFILES(X86)%\%APP_NAME%\%APP_NAME%.exe
) else (
    echo [X] Nao encontrado em: %PROGRAMFILES(X86)%\%APP_NAME%\%APP_NAME%.exe
)

if exist "%USERPROFILE%\AppData\Local\%APP_NAME%\%APP_NAME%.exe" (
    echo [OK] Encontrado em: %USERPROFILE%\AppData\Local\%APP_NAME%\%APP_NAME%.exe
    set INSTALLED_PATH=%USERPROFILE%\AppData\Local\%APP_NAME%\%APP_NAME%.exe
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
) else (
    echo === Nenhuma Aplicacao Instalada Detectada ===
    echo.
    if defined JAVA_HOME (
        echo Java encontrado em: %JAVA_HOME%
    ) else (
        echo Java nao encontrado.
    )
)

echo.
echo Pressione qualquer tecla para sair...
pause >nul
