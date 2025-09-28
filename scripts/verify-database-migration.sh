#!/bin/bash

echo "Verificando migração do banco de dados..."
echo

# Verificar se as colunas scheduled_date e due_date existem na tabela cards
echo "Verificando colunas missing na tabela cards..."
if ! sqlite3 board.db "PRAGMA table_info(cards);" | grep -q "scheduled_date\|due_date"; then
    echo "ERRO: Colunas scheduled_date e/ou due_date não encontradas na tabela cards"
    echo "Aplicando migração..."
    if ! sqlite3 board.db < src/main/resources/db/changelog/migrations/db.changelog-202509291200.sql; then
        echo "ERRO: Falha ao aplicar migração"
        exit 1
    fi
    echo "Migração aplicada com sucesso"
else
    echo "Colunas scheduled_date e due_date já existem"
fi

# Verificar se as constraints foram aplicadas
echo
echo "Verificando constraints de integridade referencial..."
if ! sqlite3 board.db "PRAGMA foreign_key_list(calendar_events);" | grep -q "fk_calendar_events_cards"; then
    echo "AVISO: Constraint fk_calendar_events_cards não encontrada"
    echo "Aplicando constraints..."
    if ! sqlite3 board.db < src/main/resources/db/changelog/migrations/db.changelog-202509291200.sql; then
        echo "ERRO: Falha ao aplicar constraints"
        exit 1
    fi
    echo "Constraints aplicadas com sucesso"
else
    echo "Constraints de integridade referencial já existem"
fi

# Verificar se os índices foram criados
echo
echo "Verificando índices..."
if ! sqlite3 board.db "PRAGMA index_list(cards);" | grep -q "idx_cards_scheduled_date\|idx_cards_due_date\|idx_cards_urgency"; then
    echo "AVISO: Alguns índices não foram encontrados"
    echo "Aplicando índices..."
    if ! sqlite3 board.db < src/main/resources/db/changelog/migrations/db.changelog-202509291200.sql; then
        echo "ERRO: Falha ao aplicar índices"
        exit 1
    fi
    echo "Índices aplicados com sucesso"
else
    echo "Índices já existem"
fi

echo
echo "Verificação concluída com sucesso!"
echo "O banco de dados está atualizado e consistente."
