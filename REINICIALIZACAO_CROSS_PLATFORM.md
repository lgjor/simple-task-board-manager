# Funcionalidade de Reinicialização Cross-Platform

## Visão Geral

A funcionalidade de reinicialização foi implementada para funcionar de forma consistente tanto no Windows quanto no Linux, resolvendo o problema onde a aplicação apenas era encerrada ao clicar em "Reiniciar Agora".

## Como Funciona

### 1. Detecção Automática da Plataforma
A aplicação detecta automaticamente o sistema operacional e escolhe a estratégia de reinicialização mais apropriada:

- **Windows**: Usa `cmd /c start` para executar o executável
- **Linux/Mac**: Executa diretamente o binário ou usa `bash -c` com `&` para execução em background

### 2. Detecção de Aplicação Instalada
A funcionalidade tenta primeiro detectar se a aplicação está rodando como um instalador:

#### Windows
- `%PROGRAMFILES%\SimpleTaskBoardManager\SimpleTaskBoardManager.exe`
- `%PROGRAMFILES(X86)%\SimpleTaskBoardManager\SimpleTaskBoardManager.exe`
- `%USERPROFILE%\AppData\Local\SimpleTaskBoardManager\SimpleTaskBoardManager.exe`

#### Linux
- `/usr/bin/SimpleTaskBoardManager`
- `/usr/local/bin/SimpleTaskBoardManager`
- `~/.local/bin/SimpleTaskBoardManager`
- `/opt/SimpleTaskBoardManager/bin/SimpleTaskBoardManager`

#### macOS
- `/Applications/SimpleTaskBoardManager.app/Contents/MacOS/SimpleTaskBoardManager`
- `~/Applications/SimpleTaskBoardManager.app/Contents/MacOS/SimpleTaskBoardManager`

### 3. Fallback para Java Direto
Se a aplicação instalada não for detectada, a funcionalidade tenta reiniciar usando o comando Java diretamente:

```bash
# Windows
cmd /c start "SimpleTaskBoardManager" "C:\Program Files\Java\bin\java.exe" -cp <classpath> org.desviante.SimpleTaskBoardManagerApplication

# Linux/Mac
bash -c "/usr/lib/jvm/java/bin/java -cp <classpath> org.desviante.SimpleTaskBoardManagerApplication &"
```

### 4. Verificação de Sucesso
Após tentar reiniciar, a aplicação verifica se a reinicialização foi bem-sucedida:

- **Windows**: Usa `tasklist` para verificar processos Java
- **Linux/Mac**: Usa `ps aux` para verificar processos Java

## Fluxo de Execução

1. **Usuário clica em "Reiniciar Agora"**
2. **Aplicação fecha todas as janelas** de forma ordenada
3. **Detecta o sistema operacional** e localiza a aplicação instalada
4. **Executa comando de reinicialização** apropriado para a plataforma
5. **Verifica se a reinicialização foi bem-sucedida** usando comandos do sistema
6. **Sai da aplicação atual** apenas após confirmar que a nova instância foi iniciada

## Vantagens da Nova Implementação

### ✅ **Cross-Platform**
- Funciona consistentemente no Windows e Linux
- Detecta automaticamente a plataforma e usa comandos apropriados

### ✅ **Robusta**
- Múltiplas estratégias de fallback
- Verificação de sucesso da reinicialização
- Tratamento de erros abrangente

### ✅ **Inteligente**
- Detecta automaticamente se está rodando como instalador
- Usa o caminho correto para reinicialização
- Preserva variáveis de ambiente e diretório de trabalho

### ✅ **Segura**
- Fecha janelas de forma ordenada
- Aguarda confirmação antes de sair
- Logs detalhados para debugging

## Logs e Debugging

A funcionalidade gera logs detalhados para facilitar o debugging:

```
INFO  - Iniciando processo de reinicialização da aplicação...
INFO  - Fechando aplicação para reinicialização...
INFO  - Fechando janela: Simple Task Board Manager
INFO  - Tentando reiniciar a aplicação...
INFO  - Aplicação instalada detectada em: C:\Program Files\SimpleTaskBoardManager\SimpleTaskBoardManager.exe
INFO  - Comando de reinicialização via aplicação instalada: cmd /c start "SimpleTaskBoardManager" "C:\Program Files\SimpleTaskBoardManager\SimpleTaskBoardManager.exe"
INFO  - Processo de reinicialização iniciado com PID: 12345
INFO  - Comando de reinicialização executado com sucesso
INFO  - Reinicialização confirmada com sucesso
```

## Casos de Uso

### 1. **Aplicação Instalada via jpackage**
- Detecta automaticamente o executável instalado
- Usa o caminho correto para reinicialização
- Funciona com atalhos do menu Iniciar e área de trabalho

### 2. **Desenvolvimento Local**
- Se não encontrar aplicação instalada, usa Java diretamente
- Preserva classpath e variáveis de ambiente
- Funciona em ambiente de desenvolvimento

### 3. **Atualizações de Configuração**
- Permite aplicar mudanças de configuração imediatamente
- Reinicialização limpa sem perda de dados
- Experiência do usuário melhorada

## Compatibilidade

- **Windows 10/11**: ✅ Totalmente compatível
- **Linux (Ubuntu, Debian, CentOS)**: ✅ Totalmente compatível
- **macOS**: ✅ Totalmente compatível
- **Java 21+**: ✅ Totalmente compatível
- **JavaFX 21+**: ✅ Totalmente compatível

## Troubleshooting

### Problema: Reinicialização não funciona
**Solução**: Verificar logs para identificar o erro específico

### Problema: Aplicação não é detectada como instalada
**Solução**: Verificar se o jpackage foi executado corretamente

### Problema: Permissões no Linux
**Solução**: Verificar se o usuário tem permissões para executar comandos bash

## Conclusão

A nova implementação de reinicialização cross-platform resolve o problema original onde a aplicação apenas era encerrada no Windows. Agora, tanto no Windows quanto no Linux, a aplicação é reiniciada de forma consistente e robusta, proporcionando uma melhor experiência do usuário.
