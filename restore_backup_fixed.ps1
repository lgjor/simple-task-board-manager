# Script de Restauração de Backup - VERSÃO CORRIGIDA
# Cria banco sem senha e restaura dados manualmente

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  RESTAURAÇÃO DE BACKUP (SEM SENHA)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Garantir que não há processos rodando
Write-Host "1. Parando processos Java..." -ForegroundColor Yellow
Stop-Process -Name "java" -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 2
Write-Host "   ✓ Processos parados" -ForegroundColor Green

# 2. Remover banco atual
Write-Host "2. Removendo banco de dados atual..." -ForegroundColor Yellow
Remove-Item "C:\Users\Lucas\myboards\board_h2_db.*" -Force -ErrorAction SilentlyContinue
Write-Host "   ✓ Banco removido" -ForegroundColor Green

# 3. Iniciar o sistema para criar o banco automaticamente
Write-Host "3. Iniciando sistema para criar banco novo..." -ForegroundColor Yellow
Write-Host "   (Aguarde o sistema abrir e feche-o manualmente)" -ForegroundColor Yellow
Write-Host ""
Write-Host "   IMPORTANTE: Quando o sistema abrir, FECHE-O imediatamente!" -ForegroundColor Red
Write-Host "   Pressione qualquer tecla para iniciar..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

Write-Host ""
Write-Host "   Iniciando sistema..." -ForegroundColor Yellow
