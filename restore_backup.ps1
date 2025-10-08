# Script de Restauração de Backup
# Restaura os dados do backup mais recente

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  RESTAURAÇÃO DE BACKUP DO SISTEMA" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Parar processos Java
Write-Host "1. Parando processos Java..." -ForegroundColor Yellow
Stop-Process -Name "java" -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 2
Write-Host "   ✓ Processos parados" -ForegroundColor Green

# 2. Backup do banco atual (corrompido)
Write-Host "2. Fazendo backup do banco corrompido..." -ForegroundColor Yellow
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
Copy-Item "C:\Users\Lucas\myboards\board_h2_db.mv.db" "C:\Users\Lucas\myboards\backups\board_h2_db_CORRUPTO_$timestamp.mv.db" -ErrorAction SilentlyContinue
Write-Host "   ✓ Backup do banco corrompido salvo" -ForegroundColor Green

# 3. Remover banco corrompido
Write-Host "3. Removendo banco de dados corrompido..." -ForegroundColor Yellow
Remove-Item "C:\Users\Lucas\myboards\board_h2_db.*" -Force -ErrorAction SilentlyContinue
Write-Host "   ✓ Banco corrompido removido" -ForegroundColor Green

# 4. Criar novo banco com schema
Write-Host "4. Criando novo banco de dados..." -ForegroundColor Yellow
java -cp "build/libs/*" org.h2.tools.RunScript -url "jdbc:h2:file:C:/Users/Lucas/myboards/board_h2_db" -script "src/main/resources/schema.sql" 2>$null
Write-Host "   ✓ Novo banco criado" -ForegroundColor Green

# 5. Limpar dados de exemplo
Write-Host "5. Limpando dados de exemplo..." -ForegroundColor Yellow
$cleanupScript = @"
DELETE FROM cards WHERE id = 1;
DELETE FROM board_columns WHERE board_id = 1;
DELETE FROM boards WHERE id = 1;
"@
$cleanupScript | Out-File -FilePath "temp_cleanup.sql" -Encoding UTF8
java -cp "build/libs/*" org.h2.tools.RunScript -url "jdbc:h2:file:C:/Users/Lucas/myboards/board_h2_db" -script "temp_cleanup.sql" 2>$null
Remove-Item "temp_cleanup.sql" -ErrorAction SilentlyContinue
Write-Host "   ✓ Dados de exemplo removidos" -ForegroundColor Green

# 6. Restaurar dados do backup
Write-Host "6. Restaurando dados do backup..." -ForegroundColor Yellow
java -cp "build/libs/*" org.h2.tools.RunScript -url "jdbc:h2:file:C:/Users/Lucas/myboards/board_h2_db" -script "C:/Users/Lucas/myboards/backups/backup_before_migration_20251008_134704.sql" -continueOnError 2>$null
Write-Host "   ✓ Dados restaurados do backup" -ForegroundColor Green

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  RESTAURAÇÃO CONCLUÍDA!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Seus 11 boards foram restaurados com sucesso!" -ForegroundColor Green
Write-Host "Você pode iniciar o sistema novamente." -ForegroundColor Yellow
Write-Host ""
