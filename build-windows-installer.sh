#!/bin/bash

# Script para gerar instalador Windows
# Este script cria o instalador Windows (.exe) usando jpackage

echo "=== Simple Task Board Manager - Gerador de Instalador Windows ==="
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

# Verificar se o ícone ICO existe
if [ ! -f "src/main/resources/icon.ico" ]; then
    echo "❌ Ícone ICO não encontrado: src/main/resources/icon.ico"
    echo "Este arquivo é necessário para o instalador Windows"
    exit 1
fi

# Função para verificar se um comando existe
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Verificar dependências
echo "🔧 Verificando dependências..."

# Verificar jpackage
if ! command_exists jpackage; then
    echo "❌ jpackage não encontrado!"
    echo "Instale o JDK 21 com jpackage:"
    echo "sudo apt install openjdk-21-jdk"
    exit 1
fi

echo "✅ Dependências verificadas"

# Preservar instaladores existentes
PRESERVE_EXISTING=false
if [ -d "build/dist" ]; then
    EXISTING_INSTALLERS=$(find build/dist -type f \( -name "*.exe" -o -name "*.AppImage" -o -name "*.deb" -o -name "*.rpm" -o -name "*.snap" \))
    if [ -n "$EXISTING_INSTALLERS" ]; then
        echo "💾 Preservando instaladores existentes..."
        mkdir -p build/dist/backup
        cp build/dist/*.exe build/dist/*.AppImage build/dist/*.deb build/dist/*.rpm build/dist/*.snap build/dist/backup/ 2>/dev/null || true
        PRESERVE_EXISTING=true
        echo "✅ Instaladores preservados em build/dist/backup/"
    fi
fi

# Limpar builds anteriores (mas preservar dist se necessário)
echo "🧹 Limpando builds anteriores..."
if [ "$PRESERVE_EXISTING" = true ]; then
    echo "💾 Preservando diretório dist/ para manter instaladores existentes..."
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

# Criar diretório de destino
mkdir -p build/dist

# Restaurar instaladores preservados
if [ "$PRESERVE_EXISTING" = true ]; then
    echo "🔄 Restaurando instaladores preservados..."
    cp build/dist/backup/* build/dist/ 2>/dev/null || true
    echo "✅ Instaladores restaurados"
fi

echo ""
echo "🪟 Gerando instalador Windows..."

# Gerar instalador Windows
if ./gradlew jpackage --continue; then
    echo "✅ Instalador Windows (.exe) criado com sucesso!"
else
    echo "❌ Erro ao criar instalador Windows"
    exit 1
fi

echo ""
echo "📁 Instalador gerado em: build/dist/"
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
if [ "$PRESERVE_EXISTING" = true ] && [ -d "build/dist/backup" ]; then
    echo "🧹 Limpando arquivos temporários..."
    rm -rf build/dist/backup
fi

echo ""
echo "=== Instruções de Instalação ==="
echo ""
echo "🪟 Windows:"
echo "  Execute o arquivo .exe diretamente"
echo "  O instalador irá:"
echo "    - Criar atalhos no menu Iniciar"
echo "    - Criar atalho na área de trabalho"
echo "    - Instalar no diretório Program Files"
echo "    - Configurar atualizações automáticas"
echo ""
echo "=== Build de Instalador Windows Concluído ===" 