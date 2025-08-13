# Guia dos Scripts de Build - Simple Task Board Manager

Este guia explica como usar os scripts de build para gerar instaladores Linux e Windows.

## 🚀 **Scripts Disponíveis**

### 1. **`build-all-installers.sh`** (Recomendado)
Script principal com menu interativo para escolher o tipo de build.

### 2. **`build-linux-installers.sh`**
Script para gerar instaladores Linux (com opção para incluir Windows).

### 3. **`build-windows-installer.sh`**
Script específico para gerar apenas o instalador Windows.

## 📋 **Como Usar**

### **Opção 1: Script Principal (Recomendado)**
```bash
./build-all-installers.sh
```
- Apresenta um menu interativo
- Permite escolher o tipo de build
- Mais amigável para usuários

### **Opção 2: Script Linux com Argumentos**
```bash
# Apenas instaladores Linux
./build-linux-installers.sh --linux-only
# ou
./build-linux-installers.sh -l

# Todos os instaladores (Linux + Windows)
./build-linux-installers.sh --all
# ou
./build-linux-installers.sh -a

# Padrão (apenas Linux)
./build-linux-installers.sh
```

### **Opção 3: Script Windows Específico**
```bash
./build-windows-installer.sh
```

## 🔧 **Funcionalidades dos Scripts**

### **Preservação de Instaladores Existentes**
- ✅ **Antes**: `./gradlew clean` removia TUDO, incluindo instaladores Windows
- ✅ **Agora**: Scripts preservam instaladores existentes durante o build
- ✅ **Backup automático**: Cria backup temporário e restaura após build

### **Build Inteligente**
- 🔍 Detecta instaladores existentes automaticamente
- 💾 Preserva arquivos importantes
- 🧹 Limpa apenas o necessário (classes e JARs)
- 🔄 Restaura instaladores preservados

## 📁 **Estrutura de Arquivos Gerados**

```
build/dist/
├── SimpleTaskBoardManager-1.0.6.exe          # Windows
├── SimpleTaskBoardManager-x86_64.AppImage    # Linux AppImage
├── simple-task-board-manager_1.0.3_amd64.deb # Linux DEB
├── simple-task-board-manager-1.0.3-1.x86_64.rpm # Linux RPM
└── simple-task-board-manager_1.0.3_amd64.snap # Linux Snap
```

## 🎯 **Cenários de Uso**

### **Cenário 1: Desenvolvedor Linux**
```bash
# Gerar apenas instaladores Linux
./build-linux-installers.sh --linux-only
```

### **Cenário 2: Desenvolvedor Cross-Platform**
```bash
# Gerar todos os instaladores
./build-all-installers.sh
# Escolher opção 3 no menu
```

### **Cenário 3: Manutenção de Instalador Windows**
```bash
# Atualizar apenas o instalador Windows
./build-windows-installer.sh
```

### **Cenário 4: Build Completo**
```bash
# Gerar todos os instaladores de uma vez
./build-linux-installers.sh --all
```

## ⚠️ **Problemas Resolvidos**

### **Problema Original**
- ❌ `./gradlew clean` removia TUDO
- ❌ Instaladores Windows eram perdidos
- ❌ Não havia opção para preservar arquivos

### **Solução Implementada**
- ✅ **Preservação inteligente**: Detecta e preserva instaladores existentes
- ✅ **Build seletivo**: Limpa apenas o necessário
- ✅ **Backup automático**: Cria e restaura backups temporários
- ✅ **Opções flexíveis**: Linux apenas, Windows apenas, ou ambos

## 🔍 **Detalhes Técnicos**

### **Comandos Gradle Usados**
```bash
# Antes (removia tudo)
./gradlew clean

# Agora (preserva dist/)
./gradlew cleanClasses cleanJar
```

### **Processo de Preservação**
1. 🔍 **Detecção**: Verifica instaladores existentes
2. 💾 **Backup**: Cria backup temporário
3. 🧹 **Limpeza**: Remove apenas classes e JARs
4. 🔄 **Restauração**: Restaura instaladores preservados
5. 🧹 **Limpeza**: Remove backup temporário

### **Arquivos Preservados**
- ✅ `.exe` (Windows)
- ✅ `.AppImage` (Linux)
- ✅ `.deb` (Linux)
- ✅ `.rpm` (Linux)
- ✅ `.snap` (Linux)

## 📚 **Comandos Rápidos**

### **Build Rápido Linux**
```bash
./build-linux-installers.sh
```

### **Build Rápido Windows**
```bash
./build-windows-installer.sh
```

### **Build Rápido Todos**
```bash
./build-linux-installers.sh --all
```

### **Menu Interativo**
```bash
./build-all-installers.sh
```

## 🎉 **Benefícios**

1. **🚀 Eficiência**: Não perde mais instaladores existentes
2. **🔄 Flexibilidade**: Opções para diferentes tipos de build
3. **💾 Segurança**: Backup automático de arquivos importantes
4. **🧹 Inteligência**: Limpeza seletiva e eficiente
5. **📱 Cross-Platform**: Suporte completo para Linux e Windows

## 🔧 **Troubleshooting**

### **Erro: "jpackage não encontrado"**
```bash
sudo apt install openjdk-21-jdk
```

### **Erro: "Ícone não encontrado"**
- Windows: Verificar `src/main/resources/icon.ico`
- Linux: Verificar `src/main/resources/icon.png`

### **Erro: "Gradle wrapper não encontrado"**
```bash
chmod +x ./gradlew
```

## 📖 **Referências**

- [JPackage Documentation](https://docs.oracle.com/en/java/javase/21/docs/specs/man/jpackage.html)
- [Gradle Build System](https://gradle.org/)
- [Cross-Platform Development](CROSS_PLATFORM_DEVELOPMENT.md)

---

**🎯 Resumo**: Os novos scripts resolvem o problema de perda de instaladores Windows e oferecem flexibilidade para diferentes tipos de build, com preservação inteligente de arquivos existentes. 