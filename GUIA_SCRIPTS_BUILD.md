# Guia dos Scripts de Build - Simple Task Board Manager

Este guia explica como usar os scripts de build para gerar instaladores Linux e Windows.

## 🚀 **Scripts Disponíveis**

### 1. **`build-all-installers.sh`** (Recomendado)
Script principal com menu interativo para escolher o tipo de build.

### 2. **`build-linux-installers.sh`**
Script para gerar instaladores Linux (com opção para incluir Windows).

### 3. **`build-windows-installer.sh`**
Script específico para gerar apenas o instalador Windows.

## ⚠️ **IMPORTANTE: Limitação Cross-Platform**

### **🔍 Problema Identificado:**
O `jpackage` é **plataforma-específico** e **NÃO consegue** gerar instaladores cross-platform:

- ❌ **Linux → Windows**: Não funciona
- ❌ **Windows → Linux**: Não funciona  
- ✅ **Linux → Linux**: Funciona
- ✅ **Windows → Windows**: Funciona

### **🚨 Erro Comum:**
```
Error: Option [--win-dir-chooser] is not valid on this platform
```

## 📋 **Como Usar**

### **Opção 1: Script Principal (Recomendado)**
```bash
./build-all-installers.sh
```
- **Detecta automaticamente** a plataforma
- **Mostra opções apropriadas** para cada sistema
- **Previne tentativas inválidas** de cross-platform

### **Opção 2: Script Linux com Argumentos**
```bash
# Apenas instaladores Linux (funciona em Linux)
./build-linux-installers.sh --linux-only
# ou
./build-linux-installers.sh -l

# Todos os instaladores (só funciona em Windows)
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
- **Só funciona em Windows**
- **Detecta plataforma** e mostra erro informativo em Linux

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

### **Detecção de Plataforma**
- 🐧 **Linux**: Detecta e limita opções apropriadas
- 🪟 **Windows**: Detecta e limita opções apropriadas
- ⚠️ **Cross-platform**: Previne tentativas inválidas

## 📁 **Estrutura de Arquivos Gerados**

```
build/dist/
├── SimpleTaskBoardManager-1.0.6.exe          # Windows (só em Windows)
├── SimpleTaskBoardManager-x86_64.AppImage    # Linux AppImage
├── simple-task-board-manager_1.0.3_amd64.deb # Linux DEB
├── simple-task-board-manager_1.0.3-1.x86_64.rpm # Linux RPM
└── simple-task-board-manager_1.0.3_amd64.snap # Linux Snap
```

## 🎯 **Cenários de Uso**

### **Cenário 1: Desenvolvedor Linux**
```bash
# Gerar apenas instaladores Linux
./build-linux-installers.sh --linux-only
```

### **Cenário 2: Desenvolvedor Windows**
```bash
# Gerar apenas instalador Windows
./build-windows-installer.sh
```

### **Cenário 3: Desenvolvedor Cross-Platform**
```bash
# Use GitHub Actions para builds automáticos
# Ou execute manualmente em cada plataforma
```

### **Cenário 4: Build Completo**
```bash
# Só funciona em ambiente que suporte ambas as plataformas
# Use GitHub Actions ou Docker multi-platform
```

## 🚀 **Soluções para Cross-Platform**

### **1. GitHub Actions (Recomendado)**
```yaml
# .github/workflows/build.yml
- job: build-windows
  runs-on: windows-latest
- job: build-linux  
  runs-on: ubuntu-latest
- job: build-macos
  runs-on: macos-latest
```

**Vantagens:**
- ✅ **Builds automáticos** para todas as plataformas
- ✅ **Runners nativos** de cada sistema operacional
- ✅ **Integração** com releases do GitHub
- ✅ **Sem necessidade** de máquinas locais

### **2. Docker Multi-Platform**
```bash
# Container Windows
docker run --rm -v $(pwd):/app mcr.microsoft.com/windows/servercore:ltsc2019

# Container Linux
docker run --rm -v $(pwd):/app ubuntu:20.04
```

### **3. Build Manual em Cada Plataforma**
- **Windows**: Execute scripts em máquina Windows
- **Linux**: Execute scripts em máquina Linux
- **macOS**: Execute scripts em máquina macOS

### **4. WSL2 (Windows Subsystem for Linux)**
- Execute scripts Linux no Windows
- **Mas Windows ainda precisa ser executado nativamente**

## ⚠️ **Problemas Resolvidos**

### **Problema Original**
- ❌ `./gradlew clean` removia TUDO
- ❌ Instaladores Windows eram perdidos
- ❌ Não havia opção para preservar arquivos
- ❌ Tentativas de cross-platform falhavam silenciosamente

### **Solução Implementada**
- ✅ **Preservação inteligente**: Detecta e preserva instaladores existentes
- ✅ **Build seletivo**: Limpa apenas o necessário
- ✅ **Backup automático**: Cria e restaura backups temporários
- ✅ **Detecção de plataforma**: Previne tentativas inválidas
- ✅ **Mensagens informativas**: Explica limitações e alternativas

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

### **Menu Interativo**
```bash
./build-all-installers.sh
```

### **GitHub Actions (Cross-Platform)**
```bash
# Push para main ou tag v*.*.*
git push origin main
git tag v1.0.0
git push origin v1.0.0
```

## 🎉 **Benefícios**

1. **🚀 Eficiência**: Não perde mais instaladores existentes
2. **🔄 Flexibilidade**: Opções para diferentes tipos de build
3. **💾 Segurança**: Backup automático de arquivos importantes
4. **🧹 Inteligência**: Limpeza seletiva e eficiente
5. **🐧🪟 Cross-Platform**: Suporte inteligente para Linux e Windows
6. **⚠️ Prevenção**: Detecta e previne tentativas inválidas
7. **📚 Informação**: Explica limitações e fornece alternativas

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

### **Erro: "Option not valid on this platform"**
- ✅ **Normal**: jpackage é plataforma-específico
- 🔧 **Solução**: Use GitHub Actions ou execute em plataforma nativa

## 📖 **Referências**

- [JPackage Documentation](https://docs.oracle.com/en/java/javase/21/docs/specs/man/jpackage.html)
- [Gradle Build System](https://gradle.org/)
- [GitHub Actions](https://docs.github.com/en/actions)
- [Cross-Platform Development](CROSS_PLATFORM_DEVELOPMENT.md)

---

**🎯 Resumo**: Os novos scripts resolvem o problema de perda de instaladores Windows, oferecem flexibilidade para diferentes tipos de build, **detectam automaticamente a plataforma** e **previnem tentativas inválidas de cross-platform**. Para builds completos de todas as plataformas, use **GitHub Actions**. 