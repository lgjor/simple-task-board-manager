#!/bin/bash

# Script principal para gerar instaladores
# Este script permite escolher qual tipo de instalador gerar

echo "=== Simple Task Board Manager - Gerador de Instaladores ==="
echo "Sistema: $(uname -s) $(uname -r)"
echo "Java: $(java -version 2>&1 | head -n 1)"
echo ""

# Função para mostrar menu
show_menu() {
    echo "📋 Escolha o tipo de build:"
    echo ""
    echo "1) 🐧 Apenas instaladores Linux"
    echo "2) 🪟 Apenas instalador Windows"
    echo "3) 🚀 Todos os instaladores (Linux + Windows)"
    echo "4) ❌ Sair"
    echo ""
    echo -n "Digite sua escolha (1-4): "
}

# Função para executar build Linux
build_linux() {
    echo ""
    echo "🐧 Executando build de instaladores Linux..."
    if [ -f "./build-linux-installers.sh" ]; then
        chmod +x ./build-linux-installers.sh
        ./build-linux-installers.sh --linux-only
    else
        echo "❌ Script build-linux-installers.sh não encontrado!"
        exit 1
    fi
}

# Função para executar build Windows
build_windows() {
    echo ""
    echo "🪟 Executando build de instalador Windows..."
    if [ -f "./build-windows-installer.sh" ]; then
        chmod +x ./build-windows-installer.sh
        ./build-windows-installer.sh
    else
        echo "❌ Script build-windows-installer.sh não encontrado!"
        exit 1
    fi
}

# Função para executar build completo
build_all() {
    echo ""
    echo "🚀 Executando build de todos os instaladores..."
    if [ -f "./build-linux-installers.sh" ]; then
        chmod +x ./build-linux-installers.sh
        ./build-linux-installers.sh --all
    else
        echo "❌ Script build-linux-installers.sh não encontrado!"
        exit 1
    fi
}

# Verificar se o Gradle wrapper existe
if [ ! -f "./gradlew" ]; then
    echo "❌ Gradle wrapper não encontrado!"
    exit 1
fi

# Tornar o gradlew executável
chmod +x ./gradlew

# Loop principal do menu
while true; do
    show_menu
    read -r choice
    
    case $choice in
        1)
            build_linux
            break
            ;;
        2)
            build_windows
            break
            ;;
        3)
            build_all
            break
            ;;
        4)
            echo "👋 Saindo..."
            exit 0
            ;;
        *)
            echo "❌ Opção inválida! Digite 1, 2, 3 ou 4."
            echo ""
            ;;
    esac
done

echo ""
echo "🎉 Build concluído com sucesso!"
echo "📁 Verifique os instaladores gerados em: build/dist/" 