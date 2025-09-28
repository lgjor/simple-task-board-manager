# Script para remover todas as anotações @Transactional do CardService
$filePath = "src\main\java\org\desviante\service\CardService.java"
$content = Get-Content $filePath -Raw

# Remover @Transactional (com ou sem parâmetros)
$content = $content -replace '\s*@Transactional(?:\([^)]*\))?\s*\n', "`n"

# Remover linhas vazias duplicadas
$content = $content -replace '\n\s*\n\s*\n', "`n`n"

# Salvar o arquivo
Set-Content -Path $filePath -Value $content -Encoding UTF8

Write-Host "✅ Anotações @Transactional removidas do CardService" -ForegroundColor Green
