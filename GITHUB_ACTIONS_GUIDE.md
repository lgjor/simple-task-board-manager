# Guia GitHub Actions - Builds Cross-Platform

Este guia explica como configurar e usar o GitHub Actions para gerar instaladores de todas as plataformas automaticamente.

## 🚀 **Visão Geral**

### **Problema Resolvido:**
- ❌ **Localmente**: jpackage só gera instaladores para a plataforma atual
- ✅ **GitHub Actions**: Gera instaladores para todas as plataformas automaticamente

### **Como Funciona:**
1. **Push para main** → Trigger automático
2. **Runners nativos** executam em cada plataforma
3. **Artefatos** são gerados e armazenados
4. **Release automático** quando há tags

## 📁 **Estrutura do Workflow**

### **Arquivo: `.github/workflows/build.yml`**
```yaml
name: Build Cross-Platform Installers

on:
  push:
    branches: [ main, develop ]
    tags: [ 'v*' ]
  pull_request:
    branches: [ main ]
  workflow_dispatch:
```

### **Jobs Configurados:**
- 🐧 **build-linux**: Ubuntu runner
- 🪟 **build-windows**: Windows runner  
- 🍎 **build-macos**: macOS runner
- 📦 **create-release**: Cria release com todos os instaladores

## 🔧 **Configuração**

### **1. Estrutura de Diretórios**
```
.github/
└── workflows/
    └── build.yml
```

### **2. Permissões Necessárias**
- ✅ **Repository**: `Actions: Write` (para upload de artefatos)
- ✅ **Contents**: `Read` (para checkout do código)
- ✅ **Metadata**: `Read` (para informações do repositório)

### **3. Secrets (se necessário)**
```yaml
env:
  GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}  # Automático
```

## 📋 **Como Usar**

### **Opção 1: Push Automático**
```bash
# Qualquer push para main ou develop
git push origin main
```

### **Opção 2: Tag para Release**
```bash
# Criar tag de versão
git tag v1.0.0
git push origin v1.0.0
```

### **Opção 3: Manual (workflow_dispatch)**
1. Vá para **Actions** no GitHub
2. Selecione **Build Cross-Platform Installers**
3. Clique em **Run workflow**
4. Escolha branch e clique em **Run workflow**

## 🔍 **Monitoramento**

### **1. Status dos Jobs**
- 🟢 **Verde**: Sucesso
- 🔴 **Vermelho**: Falha
- 🟡 **Amarelo**: Em execução

### **2. Logs Detalhados**
- Clique no job para ver logs
- Expanda steps para debug
- Download de artefatos em caso de falha

### **3. Artefatos Gerados**
- **linux-installers**: AppImage, DEB, RPM, Snap
- **windows-installer**: .exe
- **macos-installers**: .pkg, .dmg

## 📦 **Artefatos e Downloads**

### **Retenção:**
- **30 dias** por padrão
- **Download manual** disponível
- **Release automático** para tags

### **Estrutura dos Artefatos:**
```
linux-installers/
├── SimpleTaskBoardManager-x86_64.AppImage
├── simple-task-board-manager_1.0.3_amd64.deb
├── simple-task-board-manager_1.0.3-1.x86_64.rpm
└── simple-task-board-manager_1.0.3_amd64.snap

windows-installer/
└── SimpleTaskBoardManager-1.0.6.exe

macos-installers/
├── SimpleTaskBoardManager-1.0.6.pkg
└── SimpleTaskBoardManager-1.0.6.dmg
```

## 🚨 **Troubleshooting**

### **Erro: "Permission denied"**
```yaml
# Adicione permissões explícitas
permissions:
  actions: write
  contents: read
  metadata: read
```

### **Erro: "Java not found"**
```yaml
# Use setup-java action
- name: Set up JDK
  uses: actions/setup-java@v4
  with:
    java-version: '21'
    distribution: 'temurin'
```

### **Erro: "Gradle failed"**
- Verifique logs detalhados
- Teste localmente primeiro
- Verifique dependências no `build.gradle.kts`

### **Erro: "Artifact upload failed"**
- Verifique permissões do repositório
- Verifique espaço disponível
- Verifique tamanho dos arquivos

## 🔄 **Personalização**

### **1. Adicionar Novas Plataformas**
```yaml
build-android:
  runs-on: ubuntu-latest
  steps:
    - uses: actions/checkout@v4
    - name: Setup Android SDK
      # ... configuração Android
```

### **2. Modificar Triggers**
```yaml
on:
  push:
    branches: [ main, develop, feature/* ]
  pull_request:
    branches: [ main, develop ]
  schedule:
    - cron: '0 2 * * 1'  # Toda segunda às 2h
```

### **3. Adicionar Testes**
```yaml
test:
  runs-on: ubuntu-latest
  steps:
    - uses: actions/checkout@v4
    - name: Run Tests
      run: ./gradlew test
```

## 📊 **Métricas e Analytics**

### **1. Tempo de Build**
- **Linux**: ~5-10 minutos
- **Windows**: ~8-15 minutos
- **macOS**: ~6-12 minutos

### **2. Custos**
- **GitHub-hosted runners**: Gratuito para repositórios públicos
- **Self-hosted runners**: Sem custo adicional
- **Limites**: 2000 minutos/mês para repositórios privados

### **3. Otimizações**
- **Cache Gradle**: Reduz tempo de build
- **Matrix builds**: Paraleliza jobs
- **Dependencies**: Reutiliza dependências entre builds

## 🎯 **Casos de Uso**

### **1. Desenvolvimento Contínuo**
```bash
# Cada push gera builds de teste
git push origin feature/nova-funcionalidade
```

### **2. Release de Versão**
```bash
# Tag cria release automático
git tag v1.1.0
git push origin v1.1.0
```

### **3. Pull Request**
```bash
# Build automático para PRs
git push origin feature/atualizacao
# Criar PR no GitHub
```

### **4. Build Manual**
```bash
# Para builds sob demanda
# Usar workflow_dispatch no GitHub
```

## 🔐 **Segurança**

### **1. Tokens**
- **GITHUB_TOKEN**: Automático e seguro
- **Personal Access Token**: Só se necessário
- **Secrets**: Para credenciais sensíveis

### **2. Permissões**
- **Mínimo necessário**: Só o essencial
- **Escopo limitado**: Apenas para o repositório
- **Auditoria**: Logs de todas as ações

### **3. Validação**
- **Code review**: Para mudanças no workflow
- **Branch protection**: Para branches principais
- **Status checks**: Para garantir qualidade

## 📚 **Exemplos Práticos**

### **1. Workflow Básico**
```yaml
name: Simple Build
on: [push]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - run: echo "Hello World"
```

### **2. Workflow com Cache**
```yaml
- name: Cache Gradle packages
  uses: actions/cache@v3
  with:
    path: |
      ~/.gradle/caches
      ~/.gradle/wrapper
    key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
```

### **3. Workflow com Matrix**
```yaml
strategy:
  matrix:
    java-version: [8, 11, 17, 21]
    os: [ubuntu-latest, windows-latest, macos-latest]
```

## 🎉 **Benefícios**

1. **🚀 Automatização**: Sem intervenção manual
2. **🐧🪟 Cross-Platform**: Todas as plataformas automaticamente
3. **📦 Releases**: Automáticos com tags
4. **🔍 Visibilidade**: Logs e status em tempo real
5. **💾 Histórico**: Artefatos preservados
6. **🔄 Consistência**: Mesmo processo sempre
7. **📊 Métricas**: Tempo e sucesso dos builds

## 🔗 **Links Úteis**

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Workflow Syntax](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions)
- [Actions Marketplace](https://github.com/marketplace?type=actions)
- [Self-hosted Runners](https://docs.github.com/en/actions/hosting-your-own-runners)

---

**🎯 Resumo**: O GitHub Actions resolve completamente o problema de cross-platform, gerando instaladores para todas as plataformas automaticamente, sem necessidade de máquinas locais ou intervenção manual. 