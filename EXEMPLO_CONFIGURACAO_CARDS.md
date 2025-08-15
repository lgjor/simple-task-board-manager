# Configuração de Tipos Padrão para Cards

## Como Configurar

### 1. Criar Arquivo de Configuração

Crie o arquivo `%USERPROFILE%\myboards\config\app-metadata.json` com o seguinte conteúdo:

```json
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
```

### 2. Configurações Disponíveis

#### defaultCardTypeId
- **1**: Card (padrão)
- **2**: Book (livro)
- **3**: Video (vídeo)
- **4**: Course (curso)
- **null**: Sem sugestão (usa primeiro disponível)

#### defaultProgressType
- **"PERCENTAGE"**: Progresso percentual
- **"CHECKLIST"**: Lista de verificação
- **"NONE"**: Sem progresso
- **null**: Sem sugestão (usa NONE)

### 3. Como Funciona

1. **Ao criar um novo card**, o sistema verifica o arquivo de configuração
2. **Se `defaultCardTypeId` estiver configurado**, esse tipo será selecionado automaticamente
3. **Se não estiver configurado**, o sistema usa o primeiro tipo disponível
4. **O mesmo vale para o tipo de progresso**

### 4. Exemplo de Configuração

```json
{
  "defaultCardTypeId": 2,
  "defaultProgressType": "CHECKLIST"
}
```

**Resultado:**
- Novos cards serão criados com tipo "Book" e progresso "Checklist"

### 5. Reinicialização

Após alterar o arquivo de configuração:
1. **Feche a aplicação**
2. **Reinicie a aplicação**
3. **As novas configurações serão aplicadas**

## Troubleshooting

### Problema: Tipos padrão não estão sendo aplicados
**Solução:**
1. Verificar se o arquivo `app-metadata.json` existe em `%USERPROFILE%\myboards\config\`
2. Confirmar que os IDs configurados existem no banco
3. Verificar os logs da aplicação para debug
4. Reiniciar a aplicação após alterações

### Problema: IDs de tipos não são reconhecidos
**Solução:**
1. Verificar quais tipos existem no banco de dados
2. Usar IDs válidos na configuração
3. Se um tipo foi removido, atualizar a configuração
