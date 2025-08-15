# Configuração de Valores Padrão para Cards e Boards

Este documento explica como configurar e usar os valores padrão sugeridos ao criar novos cards e boards no sistema.

## Funcionalidades Implementadas

### 1. Sugestão de Tipo de Card Padrão
- **O que faz**: Ao criar um novo card, o sistema sugere automaticamente um tipo de card baseado nas configurações
- **Configuração**: Definir `defaultCardTypeId` no arquivo de metadados
- **Fallback**: Se não configurado, usa o primeiro tipo disponível

### 2. Sugestão de Tipo de Progresso Padrão
- **O que faz**: Ao criar um novo card, o sistema sugere automaticamente um tipo de progresso
- **Configuração**: Definir `defaultProgressType` no arquivo de metadados
- **Fallback**: Se não configurado, usa `NONE`

### 3. Sugestão de Grupo Padrão para Boards
- **O que faz**: Ao criar um novo board, o sistema sugere automaticamente um grupo
- **Configuração**: Definir `defaultBoardGroupId` no arquivo de metadados
- **Fallback**: Se não configurado, cria o board sem grupo

## Como Configurar

### Arquivo de Configuração
Edite o arquivo `~/myboards/config/app-metadata.json`:

```json
{
  "metadataVersion": "1.0",
  "defaultCardTypeId": 1,
  "defaultProgressType": "PERCENTAGE",
  "defaultBoardGroupId": 2,
  // ... outras configurações
}
```

### Valores Possíveis

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

#### defaultBoardGroupId
- **ID do grupo**: Sugere o grupo específico
- **null**: Sem sugestão (cria sem grupo)

## Exemplo de Configuração

```json
{
  "defaultCardTypeId": 2,
  "defaultProgressType": "CHECKLIST",
  "defaultBoardGroupId": 1
}
```

**Resultado:**
- Novos cards serão criados com tipo "Book" e progresso "Checklist"
- Novos boards serão criados no grupo com ID 1

## Como Funciona

### 1. Criação de Cards
1. Usuário clica em "+ Card" em uma coluna
2. Sistema carrega tipos de card disponíveis
3. **Sugestão**: Seleciona automaticamente o tipo configurado como padrão
4. **Sugestão**: Seleciona automaticamente o tipo de progresso configurado
5. Usuário pode alterar as sugestões se desejar
6. Card é criado com os valores selecionados

### 2. Criação de Boards
1. Usuário clica em "Criar Board"
2. Sistema carrega grupos disponíveis
3. **Sugestão**: Seleciona automaticamente o grupo configurado como padrão
4. Usuário pode alterar a sugestão se desejar
5. Board é criado com o grupo selecionado

## Vantagens

### Para Usuários
- **Produtividade**: Menos cliques para configurar cards/boards
- **Consistência**: Padrões uniformes em todo o sistema
- **Flexibilidade**: Podem alterar as sugestões quando necessário

### Para Administradores
- **Padronização**: Controle sobre os valores padrão do sistema
- **Configurabilidade**: Fácil alteração sem recompilação
- **Manutenção**: Configurações centralizadas em um arquivo

## Fallbacks e Segurança

### Validação de Configurações
- Sistema verifica se os IDs configurados ainda existem
- Se um tipo/grupo configurado foi removido, usa fallback automático
- Logs informam quando fallbacks são utilizados

### Fallbacks Automáticos
1. **Tipo de Card**: Primeiro tipo disponível → Tipo "Card" padrão
2. **Tipo de Progresso**: NONE
3. **Grupo**: Sem grupo (null)

## Monitoramento

### Logs
- Sistema registra quando valores padrão são aplicados
- Fallbacks são logados para auditoria
- Configurações inválidas geram warnings

### Notificações
- Usuário é informado quando reinicialização é necessária após alterações
- Sistema detecta mudanças no arquivo de configuração automaticamente

## Troubleshooting

### Problema: Valores padrão não estão sendo aplicados
**Solução:**
1. Verificar se o arquivo `app-metadata.json` existe
2. Confirmar que os IDs configurados existem no banco
3. Reiniciar a aplicação após alterações

### Problema: Fallbacks sendo usados frequentemente
**Solução:**
1. Verificar se os tipos/grupos configurados foram removidos
2. Atualizar configurações com IDs válidos
3. Verificar logs para identificar problemas

## Conclusão

O sistema de valores padrão oferece uma experiência mais fluida e produtiva para os usuários, mantendo a flexibilidade de personalização. As configurações são centralizadas e fáceis de gerenciar, proporcionando um equilíbrio entre automação e controle manual.
