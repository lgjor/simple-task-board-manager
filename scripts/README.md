# Scripts de Instalação e Desinstalação

Este diretório contém scripts para automatizar o processo de instalação e desinstalação do SimpleTaskBoardManager.

## Scripts Disponíveis

### 🚀 Instalação Automatizada

#### `install-new-version.bat` (Windows)
Script completo que automatiza todo o processo de atualização:
- Backup automático do banco de dados
- Desinstalação da versão anterior
- Instalação da nova versão
- Verificação de integridade

**Uso:**
```cmd
scripts\install-new-version.bat
```

#### `install-new-version.sh` (Linux/Mac)
Versão Linux/Mac do script de instalação automatizada.

**Uso:**
```bash
./scripts/install-new-version.sh
```

### 🗑️ Desinstalação

#### `uninstall-previous-version.bat` (Windows)
Remove versões anteriores do sistema:
- Para a aplicação se estiver rodando
- Remove instalações do Program Files
- Remove atalhos do menu Iniciar
- Remove atalhos da área de trabalho
- Remove entradas do registro
- Remove arquivos temporários

**Uso:**
```cmd
scripts\uninstall-previous-version.bat
```

#### `uninstall-previous-version.sh` (Linux/Mac)
Versão Linux/Mac do script de desinstalação.

**Uso:**
```bash
./scripts/uninstall-previous-version.sh
```

### 💾 Backup e Restauração

#### `backup-database.bat` (Windows)
Faz backup do banco de dados H2:
- Cria backup com timestamp
- Para a aplicação automaticamente
- Salva metadados do backup

**Uso:**
```cmd
scripts\backup-database.bat
```

#### `backup-database.sh` (Linux/Mac)
Versão Linux/Mac do script de backup.

**Uso:**
```bash
./scripts/backup-database.sh
```

#### `restore-database.bat` (Windows)
Restaura backup do banco de dados:
- Lista backups disponíveis
- Permite seleção do backup
- Cria backup do estado atual antes da restauração

**Uso:**
```cmd
scripts\restore-database.bat
```

#### `restore-database.sh` (Linux/Mac)
Versão Linux/Mac do script de restauração.

**Uso:**
```bash
./scripts/restore-database.sh
```

### 🔍 Verificação

#### `check-database.bat` (Windows)
Verifica a integridade do banco de dados:
- Testa conexão com o banco
- Verifica estrutura das tabelas
- Valida dados existentes

**Uso:**
```cmd
scripts\check-database.bat
```

#### `check-database.sh` (Linux/Mac)
Versão Linux/Mac do script de verificação.

**Uso:**
```bash
./scripts/check-database.sh
```

## Processo de Atualização Recomendado

### Para Usuários Finais

1. **Instalação Automatizada (Recomendada):**
   ```cmd
   # Windows
   scripts\install-new-version.bat
   
   # Linux/Mac
   ./scripts/install-new-version.sh
   ```

2. **Processo Manual (Se necessário):**
   ```cmd
   # 1. Backup
   scripts\backup-database.bat
   
   # 2. Desinstalar anterior
   scripts\uninstall-previous-version.bat
   
   # 3. Instalar nova versão manualmente
   # 4. Verificar
   scripts\check-database.bat
   ```

### Para Desenvolvedores

1. **Compilar nova versão:**
   ```cmd
   # Windows
   gradlew jpackage
   
   # Linux
   ./gradlew jpackageLinux
   ```

2. **Testar instalação automatizada:**
   ```cmd
   scripts\install-new-version.bat
   ```

## Configuração do JPackage

### Windows
O instalador Windows agora inclui:
- `--win-per-user-install`: Instalação por usuário
- `--win-upgrade-uuid`: UUID para upgrade automático
- `--win-dir-chooser`: Permite escolher diretório
- `--win-menu`: Cria atalho no menu Iniciar
- `--win-shortcut`: Cria atalho na área de trabalho

### Linux
O instalador Linux inclui:
- AppImage para distribuição fácil
- Desktop files para integração com menu
- Ícones em múltiplos tamanhos
- Instalação em `/opt/` ou `/usr/local/`

## Locais de Instalação

### Windows
- **Program Files (64-bit):** `C:\Program Files\SimpleTaskBoardManager\`
- **Program Files (32-bit):** `C:\Program Files (x86)\SimpleTaskBoardManager\`
- **Menu Iniciar:** `%USERPROFILE%\AppData\Roaming\Microsoft\Windows\Start Menu\Programs\`
- **Área de Trabalho:** `%USERPROFILE%\Desktop\`

### Linux
- **Sistema:** `/opt/SimpleTaskBoardManager/`
- **Usuário:** `$HOME/.local/share/SimpleTaskBoardManager/`
- **Menu:** `/usr/share/applications/` ou `$HOME/.local/share/applications/`

## Banco de Dados

### Localização
- **Windows:** `%USERPROFILE%\myboards\board_h2_db.*`
- **Linux/Mac:** `~/myboards/board_h2_db.*`

### Arquivos
- `board_h2_db.mv.db`: Arquivo principal do banco
- `board_h2_db.lock.db`: Arquivo de lock (temporário)
- `board_h2_db.trace.db`: Arquivo de log (opcional)

## Troubleshooting

### Problemas Comuns

#### "Instalador não encontrado"
**Solução:** Compile o projeto primeiro:
```cmd
gradlew jpackage
```

#### "Permissão negada"
**Solução:** Execute como administrador ou use:
```cmd
sudo ./scripts/install-new-version.sh
```

#### "Aplicação não inicia"
**Solução:** Verifique logs e restaure backup:
```cmd
scripts\restore-database.bat
```

#### "Múltiplas versões instaladas"
**Solução:** Execute desinstalação manual:
```cmd
scripts\uninstall-previous-version.bat
```

### Logs Importantes

#### Windows
- **Logs da aplicação:** `%USERPROFILE%\myboards\logs\`
- **Logs de instalação:** `%TEMP%\SimpleTaskBoardManager*`

#### Linux
- **Logs da aplicação:** `~/.local/share/SimpleTaskBoardManager/logs/`
- **Logs do sistema:** `/var/log/`

## Suporte

Para problemas com instalação:

1. **Colete informações:**
   - Versão do sistema operacional
   - Logs de instalação
   - Logs da aplicação
   - Backup do banco (se disponível)

2. **Documente o problema:**
   - Passos para reproduzir
   - Comportamento esperado vs atual
   - Mensagens de erro completas

3. **Contate o suporte:**
   - Forneça todas as informações coletadas
   - Inclua screenshots se necessário 