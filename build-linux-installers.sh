#!/bin/bash

# Script para gerar instaladores Linux
# Este script cria diferentes tipos de instaladores para Linux

echo "=== Simple Task Board Manager - Gerador de Instaladores Linux ==="
echo "Sistema: $(uname -s) $(uname -r)"
echo "Java: $(java -version 2>&1 | head -n 1)"
echo ""

# Verificar se o Gradle wrapper existe
if [ ! -f "./gradlew" ]; then
    echo "❌ Gradle wrapper não encontrado!"
    exit 1
fi

# Tornar o gradlew executável
chmod +x ./gradlew

# Verificar se o ícone PNG existe, se não, criar
if [ ! -f "src/main/resources/icon.png" ]; then
    echo "📝 Ícone PNG não encontrado, criando a partir do ICO..."
    if [ -f "create-linux-icon.sh" ]; then
        chmod +x create-linux-icon.sh
        ./create-linux-icon.sh
    else
        echo "⚠️  Script de criação de ícone não encontrado"
        echo "Crie manualmente: src/main/resources/icon.png"
    fi
fi

# Função para verificar se um comando existe
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Função para instalar dependências
install_dependencies() {
    echo "🔧 Verificando dependências..."
    
    # Verificar jpackage
    if ! command_exists jpackage; then
        echo "❌ jpackage não encontrado!"
        echo "Instale o JDK 21 com jpackage:"
        echo "sudo apt install openjdk-21-jdk"
        exit 1
    fi
    
    # Verificar appimagetool (opcional)
    if ! command_exists appimagetool; then
        echo "⚠️  appimagetool não encontrado (opcional para AppImage)"
        echo "Para instalar: https://github.com/AppImage/AppImageKit"
    fi
    
    # Verificar snapcraft (opcional)
    if ! command_exists snapcraft; then
        echo "⚠️  snapcraft não encontrado (opcional para Snap)"
        echo "Para instalar: sudo snap install snapcraft --classic"
    fi
    
    echo "✅ Dependências verificadas"
}

# Limpar builds anteriores
echo "🧹 Limpando builds anteriores..."
./gradlew clean

# Compilar o projeto
echo "🔨 Compilando o projeto..."
./gradlew compileJava

if [ $? -eq 0 ]; then
    echo "✅ Compilação bem-sucedida!"
else
    echo "❌ Erro na compilação!"
    exit 1
fi

# Gerar JAR
echo "📦 Gerando JAR..."
./gradlew shadowJar -x test

if [ $? -eq 0 ]; then
    echo "✅ JAR gerado com sucesso!"
else
    echo "❌ Erro na geração do JAR!"
    exit 1
fi

# Verificar dependências
install_dependencies

# Criar diretório de destino
mkdir -p build/dist

echo ""
echo "🚀 Gerando instaladores Linux..."

# 1. AppImage via jpackage
echo "📱 Gerando AppImage (jpackage)..."
if ./gradlew jpackageLinux --continue; then
    echo "✅ AppImage criado com sucesso!"
else
    echo "⚠️  Erro ao criar AppImage via jpackage"
fi

# 2. DEB package
echo "📦 Gerando pacote DEB..."
if ./gradlew jpackageLinuxDeb --continue; then
    echo "✅ Pacote DEB criado com sucesso!"
else
    echo "⚠️  Erro ao criar pacote DEB"
fi

# 3. RPM package
echo "📦 Gerando pacote RPM..."
if ./gradlew jpackageLinuxRpm --continue; then
    echo "✅ Pacote RPM criado com sucesso!"
else
    echo "⚠️  Erro ao criar pacote RPM"
fi

# 4. AppImage via appimagetool (se disponível)
if command_exists appimagetool; then
    echo "📱 Gerando AppImage (appimagetool)..."
    if ./gradlew createAppImage --continue; then
        echo "✅ AppImage (appimagetool) criado com sucesso!"
    else
        echo "⚠️  Erro ao criar AppImage via appimagetool"
    fi
else
    echo "⏭️  Pulando AppImage (appimagetool não disponível)"
fi

# 5. Snap package (se disponível)
if command_exists snapcraft; then
    echo "📦 Gerando Snap package..."
    if ./gradlew createSnap --continue; then
        echo "✅ Snap package criado com sucesso!"
    else
        echo "⚠️  Erro ao criar Snap package"
    fi
else
    echo "⏭️  Pulando Snap package (snapcraft não disponível)"
fi

echo ""
echo "📁 Instaladores gerados em: build/dist/"
echo ""

# Listar arquivos gerados
if [ -d "build/dist" ]; then
    echo "📋 Arquivos gerados:"
    ls -la build/dist/
    echo ""
    
    # Contar arquivos por tipo
    echo "📊 Resumo:"
    echo "AppImage: $(ls build/dist/*.AppImage 2>/dev/null | wc -l)"
    echo "DEB: $(ls build/dist/*.deb 2>/dev/null | wc -l)"
    echo "RPM: $(ls build/dist/*.rpm 2>/dev/null | wc -l)"
    echo "Snap: $(ls build/dist/*.snap 2>/dev/null | wc -l)"
else
    echo "❌ Nenhum instalador foi gerado"
fi

echo ""
echo "=== Instruções de Instalação ==="
echo ""
echo "📱 AppImage:"
echo "  chmod +x SimpleTaskBoardManager-x86_64.AppImage"
echo "  ./SimpleTaskBoardManager-x86_64.AppImage"
echo ""
echo "📦 DEB (Ubuntu/Debian):"
echo "  sudo dpkg -i simple-task-board-manager_1.0.3_amd64.deb"
echo "  sudo apt-get install -f  # se necessário"
echo ""
echo "📦 RPM (Fedora/RHEL):"
echo "  sudo dnf install simple-task-board-manager-1.0.3-1.x86_64.rpm"
echo ""
echo "📦 Snap:"
echo "  sudo snap install simple-task-board-manager_1.0.3_amd64.snap --dangerous"
echo ""
echo "=== Build de Instaladores Linux Concluído ===" 