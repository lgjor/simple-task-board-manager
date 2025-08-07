#!/bin/bash

# Script de restauração do banco de dados H2
# Este script deve ser executado em caso de problemas após atualização

set -e

# Configurações
DB_DIR="$HOME/myboards"
DB_NAME="board_h2_db"
BACKUP_DIR="$HOME/myboards/backups"

echo "=== Restauração do Banco de Dados H2 ==="
echo "Data/Hora: $(date)"
echo "Diretório do banco: $DB_DIR"

# Verifica se há backups disponíveis
if [ ! -d "$BACKUP_DIR" ]; then
    echo "ERRO: Diretório de backups não encontrado: $BACKUP_DIR"
    exit 1
fi

# Lista backups disponíveis
echo "Backups disponíveis:"
ls -la "$BACKUP_DIR" | grep "board_h2_db_backup" | nl

# Solicita qual backup restaurar
echo ""
read -p "Digite o número do backup para restaurar (ou 'q' para sair): " backup_number

if [ "$backup_number" = "q" ]; then
    echo "Operação cancelada."
    exit 0
fi

# Obtém o nome do backup selecionado
backup_name=$(ls -1 "$BACKUP_DIR" | grep "board_h2_db_backup" | sed -n "${backup_number}p")

if [ -z "$backup_name" ]; then
    echo "ERRO: Backup selecionado não encontrado."
    exit 1
fi

echo "Backup selecionado: $backup_name"
echo "Localização: $BACKUP_DIR/$backup_name"

# Confirma a operação
read -p "Tem certeza que deseja restaurar este backup? (s/N): " confirm

if [ "$confirm" != "s" ] && [ "$confirm" != "S" ]; then
    echo "Operação cancelada."
    exit 0
fi

# Para a aplicação se estiver rodando (opcional)
echo "Verificando se a aplicação está rodando..."
pkill -f "SimpleTaskBoardManager" || echo "Aplicação não estava rodando ou não foi possível parar."

# Aguarda um pouco para garantir que o banco foi liberado
sleep 2

# Cria backup do estado atual antes da restauração
if [ -d "$DB_DIR" ] && [ "$(ls -A "$DB_DIR" 2>/dev/null)" ]; then
    echo "Criando backup do estado atual..."
    current_backup="$BACKUP_DIR/current_before_restore_$(date +"%Y%m%d_%H%M%S")"
    mkdir -p "$current_backup"
    cp -r "$DB_DIR/${DB_NAME}."* "$current_backup/" 2>/dev/null || echo "AVISO: Não foi possível fazer backup do estado atual"
fi

# Remove arquivos atuais do banco
echo "Removendo arquivos atuais do banco..."
rm -f "$DB_DIR/${DB_NAME}."* 2>/dev/null || echo "AVISO: Não foi possível remover alguns arquivos"

# Restaura o backup
echo "Restaurando backup..."
cp -r "$BACKUP_DIR/$backup_name/"* "$DB_DIR/" 2>/dev/null || {
    echo "ERRO: Falha ao restaurar backup"
    exit 1
}

echo "Restauração concluída com sucesso!"
echo "Você pode agora iniciar a aplicação novamente." 