Write-Host "=== Teste de Deteccao de Aplicacao Instalada ===" -ForegroundColor Green
Write-Host ""

$APP_NAME = "SimpleTaskBoardManager"
$JAVA_HOME = $env:JAVA_HOME

Write-Host "Verificando locais possiveis de instalacao..." -ForegroundColor Yellow
Write-Host ""

# Verificar Program Files
$programFilesPath = "$env:PROGRAMFILES\$APP_NAME\$APP_NAME.exe"
if (Test-Path $programFilesPath) {
    Write-Host "[OK] Encontrado em: $programFilesPath" -ForegroundColor Green
    $INSTALLED_PATH = $programFilesPath
} else {
    Write-Host "[X] Nao encontrado em: $programFilesPath" -ForegroundColor Red
}

# Verificar Program Files (x86)
$programFilesX86Path = "$env:PROGRAMFILES(X86)\$APP_NAME\$APP_NAME.exe"
if (Test-Path $programFilesX86Path) {
    Write-Host "[OK] Encontrado em: $programFilesX86Path" -ForegroundColor Green
    $INSTALLED_PATH = $programFilesX86Path
} else {
    Write-Host "[X] Nao encontrado em: $programFilesX86Path" -ForegroundColor Red
}

# Verificar AppData Local
$appDataPath = "$env:USERPROFILE\AppData\Local\$APP_NAME\$APP_NAME.exe"
if (Test-Path $appDataPath) {
    Write-Host "[OK] Encontrado em: $appDataPath" -ForegroundColor Green
    $INSTALLED_PATH = $appDataPath
} else {
    Write-Host "[X] Nao encontrado em: $appDataPath" -ForegroundColor Red
}

Write-Host ""
if ($INSTALLED_PATH) {
    Write-Host "=== Aplicacao Instalada Detectada ===" -ForegroundColor Green
    Write-Host "Caminho: $INSTALLED_PATH"
    Write-Host ""
    Write-Host "Testando comando de reinicializacao..." -ForegroundColor Yellow
    Write-Host "Comando: Start-Process '$INSTALLED_PATH'"
    Write-Host ""
    Write-Host "NOTA: Este e apenas um teste de deteccao." -ForegroundColor Cyan
    Write-Host "Para testar a reinicializacao real, execute a aplicacao."
} else {
    Write-Host "=== Nenhuma Aplicacao Instalada Detectada ===" -ForegroundColor Yellow
    Write-Host ""
    if ($JAVA_HOME) {
        Write-Host "Java encontrado em: $JAVA_HOME" -ForegroundColor Green
        Write-Host "A aplicacao tentara reiniciar via Java diretamente."
    } else {
        Write-Host "Java nao encontrado. Configure JAVA_HOME." -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "Pressione Enter para sair..." -ForegroundColor Cyan
Read-Host
