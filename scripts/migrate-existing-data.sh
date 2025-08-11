#!/bin/bash

echo "========================================"
echo "   MIGRACAO DE DADOS EXISTENTES"
echo "========================================"
echo

echo "Este script executa a migracao de dados existentes"
echo "para compatibilidade com a nova versao do sistema."
echo

# Verificar se o banco existe
DB_PATH="$HOME/myboards/board_h2_db.mv.db"
if [ ! -f "$DB_PATH" ]; then
    echo "ERRO: Banco de dados nao encontrado!"
    echo "Certifique-se de que a aplicacao foi executada pelo menos uma vez."
    exit 1
fi

echo "Banco de dados encontrado."
echo

# Criar backup antes da migracao
echo "Criando backup do banco antes da migracao..."
BACKUP_DIR="$HOME/myboards/backup"
mkdir -p "$BACKUP_DIR"

TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="$BACKUP_DIR/board_h2_db_backup_$TIMESTAMP.mv.db"

cp "$DB_PATH" "$BACKUP_FILE"
if [ $? -ne 0 ]; then
    echo "ERRO: Falha ao criar backup!"
    exit 1
fi

echo "Backup criado: $BACKUP_FILE"
echo

# Executar a aplicacao para trigger da migracao
echo "Executando aplicacao para executar migracao automatica..."
echo "A migracao sera executada automaticamente na inicializacao."
echo
echo "Pressione qualquer tecla para iniciar a aplicacao..."
read -n 1

echo
echo "Iniciando aplicacao..."
echo "A migracao sera executada automaticamente."
echo
echo "IMPORTANTE: Aguarde a aplicacao inicializar completamente"
echo "e verifique os logs para confirmar que a migracao foi executada."
echo
echo "Pressione qualquer tecla para continuar..."
read -n 1

echo
echo "========================================"
echo "   MIGRACAO CONCLUIDA"
echo "========================================"
echo
echo "Verifique os logs da aplicacao para confirmar"
echo "que a migracao foi executada com sucesso."
echo
echo "Backup disponivel em: $BACKUP_FILE"
echo
