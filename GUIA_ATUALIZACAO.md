# Guia de Atualização - Preservação de Dados

## Problema Resolvido

**Problema**: Ao instalar a versão 1.0.6 do sistema no ambiente de produção a partir do executável, o banco de dados antigo sumiu e o programa abriu sem nenhum dado cadastrado.

**Solução**: Implementamos um sistema robusto de preservação de dados que garante que seus dados sejam mantidos durante atualizações.

## Como Funciona a Preservação de Dados

### 1. Localização Segura do Banco
O banco de dados está localizado fora do diretório da aplicação:
- **Windows**: `%USERPROFILE%\myboards\board_h2_db.*`
- **Linux/Mac**: `~/myboards/board_h2_db.*`

Isso garante que o instalador não sobrescreva seus dados.

### 2. Verificação Automática de Integridade
O sistema verifica automaticamente:
- Se o banco de dados existe
- Se todas as tabelas necessárias estão presentes
- Se a estrutura está correta
- Se precisa de migrações

### 3. Migração Automática
Se o banco precisar de atualizações, o sistema:
- Detecta automaticamente as mudanças necessárias
- Aplica migrações de forma segura
- Preserva todos os dados existentes
- Faz rollback automático em caso de erro

## Processo de Atualização Segura

### Opção 1: Instalação Direta pelo .exe (Recomendada para Usuários Finais)

**Para usuários finais:**
1. Baixe o arquivo `.exe` da versão mais recente
2. Execute o instalador como administrador
3. Siga as instruções na tela

**O instalador faz automaticamente:**
- ✅ Detecção de versões anteriores
- ✅ Backup automático do banco de dados
- ✅ Remoção da versão anterior
- ✅ Instalação da nova versão
- ✅ Configuração de atalhos

### Opção 2: Instalação Automatizada (Para Administradores)

**Windows:**
```cmd
scripts\install-new-version.bat
```

**Linux/Mac:**
```bash
./scripts/install-new-version.sh
```

Este script automatiza todo o processo:
1. ✅ Backup automático do banco de dados
2. ✅ Desinstalação da versão anterior
3. ✅ Instalação da nova versão
4. ✅ Verificação de integridade

### Opção 3: Processo Manual

#### Passo 1: Backup (Recomendado)
Antes de atualizar, faça um backup:

**Windows:**
```cmd
scripts\backup-database.bat
```

**Linux/Mac:**
```bash
./scripts/backup-database.sh
```

#### Passo 2: Desinstalar Versão Anterior
**Windows:**
```cmd
scripts\uninstall-previous-version.bat
```

**Linux/Mac:**
```bash
./scripts/uninstall-previous-version.sh
```

#### Passo 3: Instalação
1. Pare a aplicação atual (se estiver rodando)
2. Execute o instalador da nova versão
3. O sistema detectará automaticamente seu banco existente

#### Passo 4: Verificação
1. Inicie a nova versão
2. O sistema verificará automaticamente a integridade
3. Migrações serão aplicadas automaticamente se necessário
4. Seus dados estarão preservados

## Recuperação em Caso de Problemas

### Se os Dados Sumirem
1. **NÃO entre em pânico** - seus dados provavelmente estão seguros
2. **Verifique a localização**: `%USERPROFILE%\myboards\` (Windows) ou `~/myboards/` (Linux/Mac)
3. **Restaure o backup**:
   ```cmd
   scripts\restore-database.bat
   ```

### Se a Aplicação Não Iniciar
1. **Verifique os logs** da aplicação
2. **Execute verificação manual**:
   ```cmd
   scripts\check-database.bat
   ```
3. **Restaure backup se necessário**

## Logs Importantes

### Onde Encontrar Logs
- **Logs da aplicação**: `application.log`
- **Logs de migração**: Console da aplicação
- **Logs de verificação**: Console da aplicação

### O Que Procurar
```
INFO: Banco de dados existe: true
INFO: Banco de dados válido: true
INFO: Inicialização necessária: false
INFO: Migração concluída com sucesso!
```

## Verificação Manual

### Verificar se o Banco Existe
**Windows:**
```cmd
dir "%USERPROFILE%\myboards\board_h2_db.*"
```

**Linux/Mac:**
```bash
ls -la ~/myboards/board_h2_db.*
```

### Verificar Integridade
**Windows:**
```cmd
scripts\check-database.bat
```

**Linux/Mac:**
```bash
./scripts/check-database.sh
```

## Troubleshooting

### Problema: "Banco não encontrado"
**Solução**: Verifique se o diretório `%USERPROFILE%\myboards\` existe

### Problema: "Validação falhou"
**Solução**: Restaure backup e verifique logs

### Problema: "Migração falhou"
**Solução**: Verifique logs de migração e restaure backup se necessário

### Problema: "Aplicação não inicia"
**Solução**: Verifique permissões do arquivo do banco

## Comandos Úteis

### Backup Manual
```cmd
# Windows
scripts\backup-database.bat

# Linux/Mac
./scripts/backup-database.sh
```

### Restauração Manual
```cmd
# Windows
scripts\restore-database.bat

# Linux/Mac
./scripts/restore-database.sh
```

### Verificação de Integridade
```cmd
# Windows
scripts\check-database.bat

# Linux/Mac
./scripts/check-database.sh
```

## Prevenção de Problemas

### Antes de Atualizar
1. **Sempre faça backup**
2. **Pare a aplicação completamente**
3. **Verifique se não há processos pendentes**

### Durante a Atualização
1. **Não interrompa o processo**
2. **Aguarde a conclusão completa**
3. **Monitore os logs se possível**

### Após a Atualização
1. **Verifique se os dados estão presentes**
2. **Teste funcionalidades críticas**
3. **Monitore logs por alguns minutos**

## Suporte

Se você ainda tiver problemas:

1. **Colete informações**:
   - Logs da aplicação
   - Logs de migração
   - Informações do sistema
   - Backup do banco (se possível)

2. **Documente o problema**:
   - Versão anterior e nova
   - Passos para reproduzir
   - Comportamento esperado vs atual

3. **Contate o suporte**:
   - Forneça todas as informações coletadas
   - Inclua backups se necessário

## Melhorias na Versão 1.0.6

### Novas Funcionalidades
- ✅ Verificação automática de integridade do banco
- ✅ Migração automática quando necessário
- ✅ Preservação completa de dados existentes
- ✅ Scripts de backup melhorados
- ✅ Recuperação robusta em caso de erro
- ✅ Logs detalhados de verificação e migração
- ✅ **Desinstalação automática de versões anteriores**
- ✅ **Instalação automatizada completa**
- ✅ **Configuração JPackage com upgrade UUID**

### Correções
- ✅ Problema de perda de dados durante atualização
- ✅ Verificação inadequada de integridade
- ✅ Falta de migração automática
- ✅ Scripts de backup incompletos
- ✅ **Múltiplas versões instaladas simultaneamente**
- ✅ **Falta de desinstalação automática**

## Conclusão

Com essas melhorias, seu sistema agora:
- **Preserva automaticamente** todos os dados durante atualizações
- **Verifica a integridade** do banco antes e depois das atualizações
- **Aplica migrações** automaticamente quando necessário
- **Fornece ferramentas** de backup e restauração robustas
- **Registra logs detalhados** para diagnóstico de problemas

**Seus dados estão seguros!** 🛡️ 