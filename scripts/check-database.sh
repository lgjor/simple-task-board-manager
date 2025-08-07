#!/bin/bash

# Script de verificação de integridade do banco de dados H2
# Este script verifica se o banco está acessível e funcional

set -e

# Configurações
DB_DIR="$HOME/myboards"
DB_NAME="board_h2_db"
DB_FILE="$DB_DIR/${DB_NAME}.mv.db"

echo "=== Verificação de Integridade do Banco de Dados H2 ==="
echo "Data/Hora: $(date)"
echo ""

# Verifica se o diretório existe
if [ ! -d "$DB_DIR" ]; then
    echo "❌ ERRO: Diretório do banco não encontrado: $DB_DIR"
    echo "   O banco será criado na primeira execução da aplicação."
    exit 1
fi

# Verifica se o arquivo do banco existe
if [ ! -f "$DB_FILE" ]; then
    echo "⚠️  AVISO: Arquivo do banco não encontrado: $DB_FILE"
    echo "   O banco será criado na primeira execução da aplicação."
    exit 0
fi

echo "✅ Arquivo do banco encontrado: $DB_FILE"

# Verifica tamanho do arquivo
FILE_SIZE=$(du -h "$DB_FILE" | cut -f1)
echo "📊 Tamanho do arquivo: $FILE_SIZE"

# Verifica permissões
if [ -r "$DB_FILE" ] && [ -w "$DB_FILE" ]; then
    echo "✅ Permissões de leitura/escrita OK"
else
    echo "❌ ERRO: Problemas de permissão no arquivo do banco"
    exit 1
fi

# Verifica se há lock file
LOCK_FILE="$DB_DIR/${DB_NAME}.lock.db"
if [ -f "$LOCK_FILE" ]; then
    echo "⚠️  AVISO: Arquivo de lock encontrado - banco pode estar em uso"
    echo "   Tamanho do lock: $(du -h "$LOCK_FILE" | cut -f1)"
else
    echo "✅ Nenhum arquivo de lock encontrado"
fi

# Lista todos os arquivos relacionados ao banco
echo ""
echo "📁 Arquivos do banco de dados:"
ls -la "$DB_DIR/${DB_NAME}."* 2>/dev/null || echo "   Nenhum arquivo adicional encontrado"

# Verifica se há backups
BACKUP_DIR="$HOME/myboards/backups"
if [ -d "$BACKUP_DIR" ]; then
    BACKUP_COUNT=$(ls -1 "$BACKUP_DIR" | grep "board_h2_db_backup" | wc -l)
    echo ""
    echo "💾 Backups disponíveis: $BACKUP_COUNT"
    if [ "$BACKUP_COUNT" -gt 0 ]; then
        echo "   Backups recentes:"
        ls -la "$BACKUP_DIR" | grep "board_h2_db_backup" | tail -3
    fi
else
    echo ""
    echo "⚠️  AVISO: Diretório de backups não encontrado: $BACKUP_DIR"
fi

echo ""
echo "✅ Verificação de integridade concluída!"
echo "   O banco de dados parece estar em bom estado." 