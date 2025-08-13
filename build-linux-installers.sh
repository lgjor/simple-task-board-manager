#!/bin/bash

# Script para gerar instaladores Linux e Windows
# Este script cria diferentes tipos de instaladores para Linux e Windows

echo "=== Simple Task Board Manager - Gerador de Instaladores ==="
echo "Sistema: $(uname -s) $(uname -r)"
echo "Java: $(java -version 2>&1 | head -n 1)"
echo ""

# Verificar argumentos
GENERATE_WINDOWS=false
if [ "$1" = "--all" ] || [ "$1" = "-a" ]; then
    GENERATE_WINDOWS=true
    echo "🚀 Modo: Gerando TODOS os instaladores (Linux + Windows)"
elif [ "$1" = "--linux-only" ] || [ "$1" = "-l" ]; then
    GENERATE_WINDOWS=false
    echo "🐧 Modo: Gerando apenas instaladores Linux"
else
    echo "📋 Uso: $0 [--all|-a|--linux-only|-l]"
    echo "  --all, -a        : Gerar todos os instaladores (Linux + Windows)"
    echo "  --linux-only, -l : Gerar apenas instaladores Linux (padrão)"
    echo "  sem argumentos   : Gerar apenas instaladores Linux"
    echo ""
    GENERATE_WINDOWS=false
fi

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

# Preservar instaladores Windows existentes
PRESERVE_WINDOWS=false
if [ "$GENERATE_WINDOWS" = true ] && [ -d "build/dist" ]; then
    WINDOWS_INSTALLERS=$(find build/dist -name "*.exe" -type f)
    if [ -n "$WINDOWS_INSTALLERS" ]; then
        echo "💾 Preservando instaladores Windows existentes..."
        mkdir -p build/dist/backup
        cp build/dist/*.exe build/dist/backup/ 2>/dev/null || true
        PRESERVE_WINDOWS=true
        echo "✅ Instaladores Windows preservados em build/dist/backup/"
    fi
fi

# Limpar builds anteriores (mas preservar dist se necessário)
echo "🧹 Limpando builds anteriores..."
if [ "$PRESERVE_WINDOWS" = true ]; then
    echo "💾 Preservando diretório dist/ para manter instaladores Windows..."
    ./gradlew cleanClasses cleanJar
else
    ./gradlew clean
fi

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

# Restaurar instaladores Windows preservados
if [ "$PRESERVE_WINDOWS" = true ]; then
    echo "🔄 Restaurando instaladores Windows preservados..."
    cp build/dist/backup/*.exe build/dist/ 2>/dev/null || true
    echo "✅ Instaladores Windows restaurados"
fi

echo ""
echo "🚀 Gerando instaladores..."

# Gerar instalador Windows se solicitado
if [ "$GENERATE_WINDOWS" = true ]; then
    echo "🪟 Gerando instalador Windows..."
    if ./gradlew jpackage --continue; then
        echo "✅ Instalador Windows (.exe) criado com sucesso!"
    else
        echo "⚠️  Erro ao criar instalador Windows"
    fi
    echo ""
fi

# Gerar instaladores Linux
echo "🐧 Gerando instaladores Linux..."

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
    echo "Windows (.exe): $(ls build/dist/*.exe 2>/dev/null | wc -l)"
    echo "AppImage: $(ls build/dist/*.AppImage 2>/dev/null | wc -l)"
    echo "DEB: $(ls build/dist/*.deb 2>/dev/null | wc -l)"
    echo "RPM: $(ls build/dist/*.rpm 2>/dev/null | wc -l)"
    echo "Snap: $(ls build/dist/*.snap 2>/dev/null | wc -l)"
else
    echo "❌ Nenhum instalador foi gerado"
fi

# Limpar backup temporário
if [ "$PRESERVE_WINDOWS" = true ] && [ -d "build/dist/backup" ]; then
    echo "🧹 Limpando arquivos temporários..."
    rm -rf build/dist/backup
fi

echo ""
echo "=== Instruções de Instalação ==="
echo ""

if [ "$GENERATE_WINDOWS" = true ]; then
    echo "🪟 Windows:"
    echo "  Execute o arquivo .exe diretamente"
    echo ""
fi

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

if [ "$GENERATE_WINDOWS" = true ]; then
    echo "=== Build de TODOS os Instaladores Concluído ==="
else
    echo "=== Build de Instaladores Linux Concluído ==="
fi 