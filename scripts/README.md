# Scripts de Configuração

Este diretório contém scripts para facilitar a configuração do Simple Task Board Manager.

## Scripts Disponíveis

### Windows
- **`create-config.bat`** - Script para Windows que cria automaticamente o arquivo de configuração

### Linux/Mac
- **`create-config.sh`** - Script para Linux/Mac que cria automaticamente o arquivo de configuração

## Como Usar

### Windows
1. **Execute o script**: Clique duas vezes em `create-config.bat`
2. **Siga as instruções** na tela
3. **Reinicie a aplicação** para aplicar as configurações

### Linux/Mac
1. **Torne o script executável**: `chmod +x create-config.sh`
2. **Execute o script**: `./create-config.sh`
3. **Siga as instruções** na tela
4. **Reinicie a aplicação** para aplicar as configurações

## O que os Scripts Fazem

1. **Criam o diretório** `~/myboards/config` se não existir
2. **Criam o arquivo** `app-metadata.json` com configurações padrão
3. **Configuram automaticamente**:
   - Tipo de card padrão: Card (ID: 1)
   - Tipo de progresso padrão: PERCENTAGE
   - Sem grupo padrão

## Configurações Aplicadas

Após executar os scripts, ao criar novos cards:
- **Tipo de card**: Será automaticamente selecionado como "Card"
- **Tipo de progresso**: Será automaticamente selecionado como "PERCENTAGE"
- **Grupo**: Sem grupo (pode ser alterado manualmente)

## Solução de Problemas

### Script não executa (Windows)
- Verifique se o arquivo tem extensão `.bat`
- Execute como administrador se necessário

### Script não executa (Linux/Mac)
- Torne executável: `chmod +x create-config.sh`
- Execute: `./create-config.sh`

### Arquivo não é criado
- Verifique permissões do diretório home
- Execute como usuário correto
- Verifique se há espaço em disco

## Configuração Manual

Se preferir configurar manualmente:

1. **Crie o diretório**: `~/myboards/config`
2. **Crie o arquivo**: `app-metadata.json`
3. **Adicione o conteúdo** conforme exemplo em `EXEMPLO_CONFIGURACAO_CARDS.md`
4. **Reinicie a aplicação**

## Reinicialização

**IMPORTANTE**: Após criar ou modificar o arquivo de configuração:
1. **Feche completamente** a aplicação Simple Task Board Manager
2. **Reinicie** a aplicação
3. **As novas configurações** serão aplicadas automaticamente 