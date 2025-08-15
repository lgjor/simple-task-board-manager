#!/bin/bash

echo "Criando arquivo de configuração para Simple Task Board Manager..."
echo

# Criar diretório de configuração
CONFIG_DIR="$HOME/myboards/config"
if [ ! -d "$CONFIG_DIR" ]; then
    mkdir -p "$CONFIG_DIR"
    echo "Diretório criado: $CONFIG_DIR"
else
    echo "Diretório já existe: $CONFIG_DIR"
fi

# Criar arquivo de configuração
CONFIG_FILE="$CONFIG_DIR/app-metadata.json"
echo "Criando arquivo de configuração: $CONFIG_FILE"

# Conteúdo do arquivo de configuração
cat > "$CONFIG_FILE" << 'EOF'
{
  "metadataVersion": "1.0",
  "defaultCardTypeId": 1,
  "defaultProgressType": "PERCENTAGE",
  "defaultBoardGroupId": null,
  "installationDirectory": "${user.dir}",
  "userDataDirectory": "${user.home}/myboards",
  "logDirectory": "${user.home}/myboards/logs",
  "defaultLogLevel": "INFO",
  "maxLogFileSizeMB": 10,
  "maxLogFiles": 5,
  "updateCheckIntervalHours": 24,
  "autoCheckUpdates": true,
  "showSystemNotifications": true,
  "databaseTimeoutSeconds": 30,
  "autoBackupDatabase": true,
  "autoBackupIntervalHours": 24,
  "autoBackupDirectory": "${user.home}/myboards/backups",
  "uiConfig": {
    "theme": "system",
    "language": "pt-BR",
    "fontSize": 12,
    "showTooltips": true,
    "confirmDestructiveActions": true,
    "showProgressBars": true
  },
  "performanceConfig": {
    "maxCardsPerPage": 100,
    "enableCaching": true,
    "maxCacheSizeMB": 50,
    "cacheTimeToLiveMinutes": 30
  },
  "securityConfig": {
    "validateInput": true,
    "logSensitiveOperations": false,
    "maxSessionTimeMinutes": 480
  }
}
EOF

if [ -f "$CONFIG_FILE" ]; then
    echo
    echo "Arquivo de configuração criado com sucesso!"
    echo
    echo "Configurações aplicadas:"
    echo "- Tipo de card padrão: Card (ID: 1)"
    echo "- Tipo de progresso padrão: PERCENTAGE"
    echo "- Grupo padrão: Nenhum (usuário deve configurar explicitamente)"
    echo
    echo "Para aplicar as configurações:"
    echo "1. Reinicie a aplicação Simple Task Board Manager"
    echo "2. Ao criar novos cards, os tipos padrão serão sugeridos automaticamente"
    echo "3. Configure o grupo padrão nas preferências se desejar"
    echo
    echo "Pressione Enter para sair..."
else
    echo
    echo "Erro ao criar arquivo de configuração!"
    echo "Verifique se você tem permissão para criar arquivos em $CONFIG_DIR"
fi

read
