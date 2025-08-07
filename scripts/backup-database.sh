#!/bin/bash

# Script de backup do banco de dados H2
# Este script deve ser executado antes de qualquer atualização

set -e

# Configurações
DB_DIR="$HOME/myboards"
DB_NAME="board_h2_db"
BACKUP_DIR="$HOME/myboards/backups"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_NAME="board_h2_db_backup_${TIMESTAMP}"

echo "=== Backup do Banco de Dados H2 ==="
echo "Data/Hora: $(date)"
echo "Diretório do banco: $DB_DIR"
echo "Nome do backup: $BACKUP_NAME"

# Verifica se o diretório do banco existe
if [ ! -d "$DB_DIR" ]; then
    echo "ERRO: Diretório do banco não encontrado: $DB_DIR"
    exit 1
fi

# Cria diretório de backup se não existir
mkdir -p "$BACKUP_DIR"

# Verifica se o banco está em uso
if [ -f "$DB_DIR/${DB_NAME}.lock.db" ]; then
    echo "AVISO: Banco de dados pode estar em uso. Tentando backup mesmo assim..."
fi

# Realiza o backup
echo "Iniciando backup..."
cp -r "$DB_DIR/${DB_NAME}."* "$BACKUP_DIR/${BACKUP_NAME}/" 2>/dev/null || {
    echo "ERRO: Falha ao copiar arquivos do banco"
    exit 1
}

# Cria um arquivo de metadados do backup
cat > "$BACKUP_DIR/${BACKUP_NAME}/backup-info.txt" << EOF
Backup realizado em: $(date)
Versão da aplicação: $(grep 'val appVersion' build.gradle.kts | cut -d'"' -f2 || echo "Desconhecida")
Diretório original: $DB_DIR
Arquivos incluídos:
$(ls -la "$BACKUP_DIR/${BACKUP_NAME}/" | grep -E "\.(db|mv\.db|lock\.db)$")
EOF

echo "Backup concluído com sucesso!"
echo "Localização: $BACKUP_DIR/${BACKUP_NAME}/"
echo ""

# Lista backups recentes
echo "Backups disponíveis:"
ls -la "$BACKUP_DIR" | grep "board_h2_db_backup" | tail -5 