@echo off
echo Criando arquivo de configuração para Simple Task Board Manager...
echo.

REM Criar diretório de configuração
set CONFIG_DIR=%USERPROFILE%\myboards\config
if not exist "%CONFIG_DIR%" (
    mkdir "%CONFIG_DIR%"
    echo Diretório criado: %CONFIG_DIR%
) else (
    echo Diretório já existe: %CONFIG_DIR%
)

REM Criar arquivo de configuração
set CONFIG_FILE=%CONFIG_DIR%\app-metadata.json
echo Criando arquivo de configuração: %CONFIG_FILE%

REM Conteúdo do arquivo de configuração
(
echo {
echo   "metadataVersion": "1.0",
echo   "defaultCardTypeId": 1,
echo   "defaultProgressType": "PERCENTAGE",
echo   "defaultBoardGroupId": null,
echo   "installationDirectory": "${user.dir}",
echo   "userDataDirectory": "${user.home}/myboards",
echo   "logDirectory": "${user.home}/myboards/logs",
echo   "defaultLogLevel": "INFO",
echo   "maxLogFileSizeMB": 10,
echo   "maxLogFiles": 5,
echo   "updateCheckIntervalHours": 24,
echo   "autoCheckUpdates": true,
echo   "showSystemNotifications": true,
echo   "databaseTimeoutSeconds": 30,
echo   "autoBackupDatabase": true,
echo   "autoBackupIntervalHours": 24,
echo   "autoBackupDirectory": "${user.home}/myboards/backups",
echo   "uiConfig": {
echo     "theme": "system",
echo     "language": "pt-BR",
echo     "fontSize": 12,
echo     "showTooltips": true,
echo     "confirmDestructiveActions": true,
echo     "showProgressBars": true
echo   },
echo   "performanceConfig": {
echo     "maxCardsPerPage": 100,
echo     "enableCaching": true,
echo     "maxCacheSizeMB": 50,
echo     "cacheTimeToLiveMinutes": 30
echo   },
echo   "securityConfig": {
echo     "validateInput": true,
echo     "logSensitiveOperations": false,
echo     "maxSessionTimeMinutes": 480
echo   }
echo }
) > "%CONFIG_FILE%"

if exist "%CONFIG_FILE%" (
    echo.
    echo Arquivo de configuração criado com sucesso!
    echo.
    echo Configurações aplicadas:
    echo - Tipo de card padrão: Card (ID: 1)
    echo - Tipo de progresso padrão: PERCENTAGE
    echo - Grupo padrão: Nenhum (usuário deve configurar explicitamente)
    echo.
    echo Para aplicar as configurações:
    echo 1. Reinicie a aplicação Simple Task Board Manager
    echo 2. Ao criar novos cards, os tipos padrão serão sugeridos automaticamente
    echo 3. Configure o grupo padrão nas preferências se desejar
    echo.
    echo Pressione qualquer tecla para sair...
) else (
    echo.
    echo Erro ao criar arquivo de configuração!
    echo Verifique se você tem permissão para criar arquivos em %CONFIG_DIR%
)

pause > nul
