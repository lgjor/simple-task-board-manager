@echo off
echo ========================================
echo Build do Instalador Windows
echo Simple Task Board Manager v1.2.4
echo ========================================
echo.

echo [1/3] Limpando builds anteriores...
if exist build\dist rmdir /s /q build\dist
if exist build\libs\*-all.jar del build\libs\*-all.jar

echo.
echo [2/3] Gerando JAR com todas as dependencias...
call gradlew.bat clean shadowJar --no-daemon
if %ERRORLEVEL% neq 0 (
    echo ERRO: Falha ao gerar o JAR
    pause
    exit /b 1
)

echo.
echo [3/3] Gerando instalador Windows...
call gradlew.bat jpackage --no-daemon
if %ERRORLEVEL% neq 0 (
    echo ERRO: Falha ao gerar o instalador
    pause
    exit /b 1
)

echo.
echo ========================================
echo Build concluido com sucesso!
echo ========================================
echo.
echo Instalador gerado em: build\dist\SimpleTaskBoardManager-1.2.4.exe
echo.
echo O instalador agora:
echo - Sera instalado em Program Files (requer permissoes de admin)
echo - Inclui todas as opcoes JVM necessarias
echo - Suporta JavaFX e Spring Boot corretamente
echo.
pause
