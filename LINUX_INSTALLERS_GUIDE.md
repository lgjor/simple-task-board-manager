# Guia de Instaladores Linux - Simple Task Board Manager

Este guia explica as diferentes opções de instaladores disponíveis para Linux, similar ao `jpackage` do Windows.

## 🎯 Opções de Instaladores Linux

### 1. **AppImage** (Recomendado)
- **Vantagens**: Portável, não requer instalação, funciona em qualquer distribuição
- **Desvantagens**: Arquivo maior, não integra com o sistema
- **Uso**: Ideal para distribuição simples e portabilidade

### 2. **DEB Package** (Ubuntu/Debian)
- **Vantagens**: Integração nativa, gerenciamento de dependências
- **Desvantagens**: Apenas para distribuições baseadas em Debian
- **Uso**: Ideal para Ubuntu, Debian, Linux Mint

### 3. **RPM Package** (Fedora/RHEL)
- **Vantagens**: Integração nativa, gerenciamento de dependências
- **Desvantagens**: Apenas para distribuições baseadas em Red Hat
- **Uso**: Ideal para Fedora, RHEL, CentOS, openSUSE

### 4. **Snap Package** (Universal)
- **Vantagens**: Funciona em qualquer distribuição, sandboxing
- **Desvantagens**: Arquivo maior, permissões restritivas
- **Uso**: Ideal para distribuição universal

## 🛠️ Ferramentas Utilizadas

### jpackage (Java 21+)
- **Função**: Ferramenta oficial do Java para criar instaladores
- **Suporte**: AppImage, DEB, RPM
- **Vantagem**: Integrado ao JDK, configuração simples

### appimagetool (Opcional)
- **Função**: Criar AppImages mais personalizados
- **Suporte**: AppImage
- **Vantagem**: Mais controle sobre o AppImage

### snapcraft (Opcional)
- **Função**: Criar Snap packages
- **Suporte**: Snap
- **Vantagem**: Distribuição universal

## 📋 Pré-requisitos

### Dependências Obrigatórias
```bash
# JDK 21 com jpackage
sudo apt install openjdk-21-jdk

# Verificar se jpackage está disponível
jpackage --version
```

### Dependências Opcionais
```bash
# ImageMagick (para converter ícones)
sudo apt install imagemagick

# appimagetool (para AppImage personalizado)
# Download: https://github.com/AppImage/AppImageKit

# snapcraft (para Snap packages)
sudo snap install snapcraft --classic
```

## 🚀 Como Gerar Instaladores

### Método 1: Script Automatizado (Recomendado)
```bash
# Gerar todos os tipos de instaladores
./build-linux-installers.sh
```

### Método 2: Comandos Individuais
```bash
# AppImage via jpackage
./gradlew jpackageLinux

# Pacote DEB
./gradlew jpackageLinuxDeb

# Pacote RPM
./gradlew jpackageLinuxRpm

# AppImage via appimagetool
./gradlew createAppImage

# Snap package
./gradlew createSnap
```

## 📁 Estrutura de Arquivos Gerados

```
build/dist/
├── SimpleTaskBoardManager-x86_64.AppImage    # AppImage via jpackage
├── simple-task-board-manager_1.0.3_amd64.deb # Pacote DEB
├── simple-task-board-manager-1.0.3-1.x86_64.rpm # Pacote RPM
├── SimpleTaskBoardManager-x86_64.AppImage    # AppImage via appimagetool
└── simple-task-board-manager_1.0.3_amd64.snap # Snap package
```

## 📦 Instruções de Instalação

### AppImage
```bash
# Tornar executável
chmod +x SimpleTaskBoardManager-x86_64.AppImage

# Executar
./SimpleTaskBoardManager-x86_64.AppImage

# Ou instalar no sistema
./SimpleTaskBoardManager-x86_64.AppImage --install
```

### DEB Package (Ubuntu/Debian)
```bash
# Instalar
sudo dpkg -i simple-task-board-manager_1.0.3_amd64.deb

# Corrigir dependências se necessário
sudo apt-get install -f

# Desinstalar
sudo apt remove simple-task-board-manager
```

### RPM Package (Fedora/RHEL)
```bash
# Instalar
sudo dnf install simple-task-board-manager-1.0.3-1.x86_64.rpm

# Ou
sudo rpm -i simple-task-board-manager-1.0.3-1.x86_64.rpm

# Desinstalar
sudo dnf remove simple-task-board-manager
```

### Snap Package
```bash
# Instalar (desenvolvimento)
sudo snap install simple-task-board-manager_1.0.3_amd64.snap --dangerous

# Ou publicar na Snap Store
snapcraft upload simple-task-board-manager_1.0.3_amd64.snap

# Desinstalar
sudo snap remove simple-task-board-manager
```

## 🎨 Configuração de Ícones

### Criar Ícone PNG
```bash
# Converter ICO para PNG
./create-linux-icon.sh

# Ou manualmente
convert src/main/resources/icon.ico -resize 256x256 src/main/resources/icon.png
```

### Estrutura de Ícones Linux
```
src/main/resources/icons/linux/
├── 16x16/apps/simple-task-board-manager.png
├── 32x32/apps/simple-task-board-manager.png
├── 48x48/apps/simple-task-board-manager.png
├── 64x64/apps/simple-task-board-manager.png
├── 128x128/apps/simple-task-board-manager.png
├── 256x256/apps/simple-task-board-manager.png
└── 512x512/apps/simple-task-board-manager.png
```

## ⚙️ Configurações Avançadas

### Personalizar AppImage
```bash
# Editar build.gradle.kts - task createAppImage
# Modificar estrutura do AppDir
# Adicionar dependências específicas
```

### Personalizar DEB/RPM
```bash
# Editar build.gradle.kts - tasks jpackageLinuxDeb/jpackageLinuxRpm
# Adicionar dependências
# Configurar metadados do pacote
```

### Personalizar Snap
```bash
# Editar snapcraft.yaml gerado
# Configurar permissões
# Adicionar interfaces
```

## 🔧 Troubleshooting

### Problemas Comuns

#### 1. jpackage não encontrado
```bash
# Instalar JDK 21
sudo apt install openjdk-21-jdk

# Verificar PATH
echo $PATH
which jpackage
```

#### 2. Erro de ícone
```bash
# Criar ícone PNG
./create-linux-icon.sh

# Ou usar ícone padrão
# Remover referência ao ícone no build.gradle.kts
```

#### 3. Erro de permissão
```bash
# Tornar scripts executáveis
chmod +x *.sh
chmod +x gradlew
```

#### 4. Erro de dependência
```bash
# Instalar dependências do sistema
sudo apt install openjfx
sudo apt install libgtk-3-0
```

## 📊 Comparação de Formatos

| Formato | Tamanho | Instalação | Portabilidade | Segurança |
|---------|---------|------------|---------------|-----------|
| AppImage | Média | Não requer | Excelente | Média |
| DEB | Pequena | Nativa | Ubuntu/Debian | Alta |
| RPM | Pequena | Nativa | Fedora/RHEL | Alta |
| Snap | Grande | Nativa | Universal | Muito Alta |

## 🎯 Recomendações

### Para Desenvolvimento
- **AppImage**: Melhor para testes e distribuição rápida
- **DEB**: Melhor para Ubuntu/Debian
- **RPM**: Melhor para Fedora/RHEL

### Para Produção
- **AppImage**: Distribuição universal
- **DEB/RPM**: Integração nativa
- **Snap**: Segurança e sandboxing

### Para Usuários Finais
- **AppImage**: Simples, não requer instalação
- **DEB/RPM**: Integração com gerenciador de pacotes
- **Snap**: Atualizações automáticas

## 📝 Notas Importantes

1. **JavaFX**: Requer dependências específicas no sistema
2. **Ícones**: Use PNG para Linux, ICO para Windows
3. **Permissões**: AppImages precisam ser executáveis
4. **Dependências**: Verifique dependências do sistema
5. **Testes**: Teste em diferentes distribuições

## 🔗 Links Úteis

- [jpackage Documentation](https://docs.oracle.com/en/java/javase/21/docs/specs/man/jpackage.html)
- [AppImage Documentation](https://docs.appimage.org/)
- [Snapcraft Documentation](https://snapcraft.io/docs)
- [DEB Package Guidelines](https://www.debian.org/doc/debian-policy/)
- [RPM Package Guidelines](https://rpm-packaging-guide.github.io/) 