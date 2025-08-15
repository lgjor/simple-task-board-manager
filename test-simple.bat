@echo off
echo === Teste Simples ===
echo.

echo PROGRAMFILES: %PROGRAMFILES%
echo PROGRAMFILES(X86): %PROGRAMFILES(X86)%
echo USERPROFILE: %USERPROFILE%

echo.
echo Testando se existe o diretorio...
if exist "%PROGRAMFILES%" (
    echo [OK] Program Files existe
) else (
    echo [X] Program Files nao existe
)

if exist "%PROGRAMFILES(X86)%" (
    echo [OK] Program Files (x86) existe
) else (
    echo [X] Program Files (x86) nao existe
)

echo.
echo Pressione qualquer tecla para sair...
pause >nul
