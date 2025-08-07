# Configuração do Console - Simple Task Board Manager

Este documento explica como o console (prompt de comando) foi configurado para proporcionar uma experiência limpa para o usuário final.

## 🎯 Solução Implementada

### **Sem Console (Recomendado para Usuários Finais)**
```bash
./gradlew jpackage
```
- **Comportamento**: A aplicação executa sem mostrar nenhum prompt de comando
- **Uso**: Ideal para distribuição final aos usuários
- **Vantagens**: Interface limpa, sem distrações para o usuário final

## 🔧 Como Funciona

A configuração do JPackage foi ajustada removendo a opção `--win-console`, o que faz com que:

1. **A aplicação execute em segundo plano** sem mostrar o prompt de comando
2. **A interface gráfica apareça normalmente** sem interferências
3. **Os logs continuem sendo gerados** mas não sejam exibidos ao usuário

## 📁 Arquivos Relacionados

- `build.gradle.kts`: Configuração do JPackage sem console
- `CONFIGURACAO_CONSOLE.md`: Este arquivo de documentação

## 🚀 Como Usar

### Para Gerar o Instalador Final
```bash
./gradlew jpackage
```

O instalador será gerado em `build/dist/` e não mostrará o console durante a execução.

## 📝 Logs e Debug

Mesmo sem o console visível, os logs ainda são gerados e podem ser encontrados em:
- Logs do Spring: `%USERPROFILE%\myboards\logs\`
- Logs do sistema: Event Viewer do Windows

## ⚠️ Notas Importantes

1. **Performance**: Ocultar o console não afeta a performance da aplicação
2. **Logs**: Os logs continuam sendo gerados mesmo sem o console visível
3. **Debug**: Para debug, execute a aplicação diretamente via IDE ou linha de comando
4. **Compatibilidade**: Compatível com Windows 10/11

## 🔄 Como Alterar o Comportamento

### Para Mostrar o Console (Desenvolvimento)
Se precisar ver o console durante o desenvolvimento, execute a aplicação diretamente:

```bash
./gradlew bootRun
```

Ou via IDE executando a classe `SimpleTaskBoardManagerApplication`.

### Para Reativar o Console no Instalador
Edite `build.gradle.kts` e adicione `"--win-console",` na task `jpackage`:

```kotlin
commandLine(
    "jpackage",
    // ... outras opções ...
    "--win-console",  // Adicione esta linha
    // ... outras opções ...
)
```
