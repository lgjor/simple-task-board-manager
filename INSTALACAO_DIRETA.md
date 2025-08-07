# Guia de Instalação Direta pelo .exe

## Cenário: Usuário Final Instala Diretamente pelo .exe

Este guia explica como funciona a instalação quando o usuário final executa diretamente o arquivo `.exe` sem usar os scripts de instalação automatizada.

## Como Funciona a Instalação Direta

### ✅ **Funcionalidades Automáticas**

Quando você executa o instalador `.exe` diretamente, o sistema:

1. **Detecta Versões Anteriores Automaticamente**
   - Verifica se há instalações anteriores no Program Files
   - Identifica se é uma nova instalação ou atualização

2. **Preserva Dados Automaticamente**
   - O banco de dados está localizado em `%USERPROFILE%\myboards\`
   - O instalador NÃO sobrescreve este diretório
   - Seus dados são preservados automaticamente

3. **Aplica Migrações Automaticamente**
   - O sistema detecta se precisa de migrações
   - Aplica atualizações do banco automaticamente
   - Preserva todos os dados existentes

4. **Configura Atalhos Automaticamente**
   - Cria atalho no menu Iniciar
   - Cria atalho na área de trabalho (se selecionado)
   - Remove atalhos de versões anteriores

## Processo de Instalação

### Passo 1: Download e Execução
1. Baixe o arquivo `.exe` da versão mais recente
2. Execute o instalador como administrador (recomendado)
3. Siga as instruções na tela

### Passo 2: Configuração
1. **Escolha o diretório de instalação** (opcional)
   - Por padrão: `C:\Program Files\SimpleTaskBoardManager\`
   - Você pode escolher outro local

2. **Selecione componentes** (opcional)
   - ✅ Aplicação principal
   - ✅ Atalho no menu Iniciar
   - ✅ Atalho na área de trabalho (opcional)

### Passo 3: Instalação
1. O instalador detecta automaticamente versões anteriores
2. Remove a versão anterior (se existir)
3. Instala a nova versão
4. Configura atalhos e integração

### Passo 4: Verificação
1. Inicie a aplicação pelo menu Iniciar
2. Verifique se seus dados estão presentes
3. Teste as funcionalidades principais

## Cenários de Instalação

### 📦 **Nova Instalação**
- Primeira vez instalando o sistema
- Nenhuma versão anterior encontrada
- Banco de dados será criado na primeira execução

**Comportamento:**
```
✓ Instalação limpa
✓ Criação de atalhos
✓ Configuração inicial
```

### 🔄 **Atualização**
- Versão anterior encontrada
- Dados existentes detectados
- Atualização automática

**Comportamento:**
```
✓ Backup automático do banco
✓ Remoção da versão anterior
✓ Preservação de dados
✓ Migração automática
```

## Localização dos Dados

### 📁 **Banco de Dados**
- **Localização**: `%USERPROFILE%\myboards\board_h2_db.*`
- **Preservação**: Sempre preservado durante atualizações
- **Backup**: Criado automaticamente em `%USERPROFILE%\myboards\backups\`

### 📁 **Aplicação**
- **Localização**: `C:\Program Files\SimpleTaskBoardManager\`
- **Atalhos**: Menu Iniciar e área de trabalho
- **Configuração**: Integração com sistema

## Verificação Pós-Instalação

### ✅ **O que verificar:**

1. **Aplicação inicia corretamente**
   - Clique no atalho do menu Iniciar
   - Verifique se abre sem erros

2. **Dados estão preservados**
   - Seus quadros e tarefas devem estar presentes
   - Configurações devem ser mantidas

3. **Funcionalidades funcionam**
   - Crie uma nova tarefa para testar
   - Verifique se as funcionalidades principais funcionam

### ⚠️ **Se algo não funcionar:**

1. **Verifique os logs**
   - Logs da aplicação: `%USERPROFILE%\myboards\logs\`
   - Logs de instalação: `%TEMP%\SimpleTaskBoardManager*`

2. **Restaure backup se necessário**
   ```cmd
   scripts\restore-database.bat
   ```

3. **Reinstale se necessário**
   - Desinstale pelo Painel de Controle
   - Execute o instalador novamente

## Troubleshooting

### Problema: "Aplicação não inicia"
**Soluções:**
1. Execute como administrador
2. Verifique se o Java está instalado
3. Verifique logs de erro

### Problema: "Dados sumiram"
**Soluções:**
1. Verifique `%USERPROFILE%\myboards\`
2. Restaure backup automático
3. Execute `scripts\restore-database.bat`

### Problema: "Múltiplas versões instaladas"
**Soluções:**
1. Desinstale pelo Painel de Controle
2. Execute `scripts\uninstall-previous-version.bat`
3. Reinstale a nova versão

### Problema: "Erro de permissão"
**Soluções:**
1. Execute como administrador
2. Verifique permissões do diretório
3. Desative temporariamente antivírus

## Vantagens da Instalação Direta

### ✅ **Simplicidade**
- Um clique para instalar
- Interface gráfica intuitiva
- Processo automatizado

### ✅ **Segurança**
- Backup automático
- Preservação de dados
- Rollback em caso de erro

### ✅ **Compatibilidade**
- Funciona em todas as versões do Windows
- Não requer conhecimentos técnicos
- Integração nativa com o sistema

### ✅ **Confiabilidade**
- Processo testado e validado
- Logs detalhados para diagnóstico
- Recuperação automática

## Comparação: Script vs .exe

| Aspecto | Script de Instalação | Instalação Direta (.exe) |
|---------|---------------------|-------------------------|
| **Facilidade** | Requer execução manual | Interface gráfica |
| **Automação** | Completa | Parcial |
| **Backup** | Manual | Automático |
| **Verificação** | Detalhada | Básica |
| **Recuperação** | Robusta | Limitada |
| **Usuário Final** | Técnico | Qualquer usuário |

## Recomendações

### 🎯 **Para Usuários Finais**
- **Use a instalação direta pelo .exe**
- É mais simples e intuitiva
- Funciona para a maioria dos casos

### 🛠️ **Para Administradores**
- **Use os scripts para controle total**
- Melhor para ambientes corporativos
- Logs mais detalhados

### 🔧 **Para Desenvolvedores**
- **Teste ambos os métodos**
- Use scripts para desenvolvimento
- Use .exe para distribuição

## Conclusão

A instalação direta pelo `.exe` **funciona perfeitamente** para a maioria dos usuários finais. O sistema foi projetado para:

- ✅ **Detectar automaticamente** versões anteriores
- ✅ **Preservar dados** durante atualizações
- ✅ **Aplicar migrações** automaticamente
- ✅ **Configurar integração** com o sistema
- ✅ **Fornecer backup** automático

**Para usuários finais, a instalação direta pelo .exe é a opção recomendada!** 🚀 