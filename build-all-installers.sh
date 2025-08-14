#!/bin/bash

# Script principal para gerar instaladores
# Este script permite escolher qual tipo de instalador gerar

echo "=== Simple Task Board Manager - Gerador de Instaladores ==="
echo "Sistema: $(uname -s) $(uname -r)"
echo "Java: $(java -version 2>&1 | head -n 1)"
echo ""

# Detectar plataforma
PLATFORM=$(uname -s)
IS_WINDOWS=false
IS_LINUX=false

if [ "$PLATFORM" = "MINGW64_NT" ] || [ "$PLATFORM" = "MSYS_NT" ] || [ "$PLATFORM" = "CYGWIN_NT" ]; then
    IS_WINDOWS=true
    echo "🪟 Plataforma detectada: Windows"
elif [ "$PLATFORM" = "Linux" ]; then
    IS_LINUX=true
    echo "🐧 Plataforma detectada: Linux"
else
    echo "🖥️  Plataforma detectada: $PLATFORM"
fi

echo ""

# Função para mostrar menu
show_menu() {
    echo "📋 Escolha o tipo de build:"
    echo ""
    
    if [ "$IS_LINUX" = true ]; then
        echo "1) 🐧 Apenas instaladores Linux"
        echo "2) 🪟 Instalador Windows (❌ Não disponível no Linux)"
        echo "3) 🚀 Todos os instaladores (❌ Windows não disponível no Linux)"
        echo "4) 📚 Informações sobre builds cross-platform"
        echo "5) ❌ Sair"
        echo ""
        echo -n "Digite sua escolha (1-5): "
    elif [ "$IS_WINDOWS" = true ]; then
        echo "1) 🐧 Instaladores Linux (❌ Não disponível no Windows)"
        echo "2) 🪟 Apenas instalador Windows"
        echo "3) 🚀 Todos os instaladores (❌ Linux não disponível no Windows)"
        echo "4) 📚 Informações sobre builds cross-platform"
        echo "5) ❌ Sair"
        echo ""
        echo -n "Digite sua escolha (1-5): "
    else
        echo "1) 🐧 Instaladores Linux"
        echo "2) 🪟 Instalador Windows"
        echo "3) 🚀 Todos os instaladores"
        echo "4) 📚 Informações sobre builds cross-platform"
        echo "5) ❌ Sair"
        echo ""
        echo -n "Digite sua escolha (1-5): "
    fi
}

# Função para executar build Linux
build_linux() {
    if [ "$IS_WINDOWS" = true ]; then
        echo ""
        echo "❌ Instaladores Linux não podem ser gerados no Windows"
        echo "   O jpackage é plataforma-específico"
        echo ""
        echo "🔧 Alternativas:"
        echo "   - Use WSL2 para executar em ambiente Linux"
        echo "   - Use GitHub Actions com runner Linux"
        echo "   - Use Docker container Linux"
        return
    fi
    
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
    if [ "$IS_LINUX" = true ]; then
        echo ""
        echo "❌ Instaladores Windows não podem ser gerados no Linux"
        echo "   O jpackage é plataforma-específico"
        echo ""
        echo "🔧 Alternativas:"
        echo "   - Execute em uma máquina Windows"
        echo "   - Use GitHub Actions com runner Windows"
        echo "   - Use Docker container Windows"
        return
    fi
    
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
    if [ "$IS_LINUX" = true ]; then
        echo ""
        echo "❌ Build completo não disponível no Linux"
        echo "   Windows não pode ser gerado em Linux"
        echo ""
        echo "🔧 Alternativa: Use GitHub Actions para builds cross-platform"
        return
    elif [ "$IS_WINDOWS" = true ]; then
        echo ""
        echo "❌ Build completo não disponível no Windows"
        echo "   Linux não pode ser gerado em Windows"
        echo ""
        echo "🔧 Alternativa: Use GitHub Actions para builds cross-platform"
        return
    fi
    
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

# Função para mostrar informações sobre builds cross-platform
show_cross_platform_info() {
    echo ""
    echo "📚 Informações sobre Builds Cross-Platform"
    echo "=========================================="
    echo ""
    echo "🔍 Por que não funciona localmente?"
    echo "   O jpackage é plataforma-específico e só gera instaladores"
    echo "   para a plataforma onde está sendo executado."
    echo ""
    echo "🪟 Windows → Linux: ❌ Não funciona"
    echo "🐧 Linux → Windows: ❌ Não funciona"
    echo "🪟 Windows → Windows: ✅ Funciona"
    echo "🐧 Linux → Linux: ✅ Funciona"
    echo ""
    echo "🚀 Soluções Recomendadas:"
    echo ""
    echo "1) 📦 GitHub Actions (Recomendado)"
    echo "   - Builds automáticos para todas as plataformas"
    echo "   - Executa em runners nativos de cada plataforma"
    echo "   - Configuração via .github/workflows/build.yml"
    echo ""
    echo "2) 🐳 Docker Multi-Platform"
    echo "   - Containers específicos para cada plataforma"
    echo   - Build em ambiente isolado
    echo ""
    echo "3) 🔄 Build Manual em Cada Plataforma"
    echo "   - Windows: Execute em máquina Windows"
    echo "   - Linux: Execute em máquina Linux"
    echo "   - macOS: Execute em máquina macOS"
    echo ""
    echo "4) 💻 WSL2 (Windows Subsystem for Linux)"
    echo "   - Execute scripts Linux no Windows"
    echo "   - Mas Windows ainda precisa ser executado nativamente"
    echo ""
    echo "💡 Para desenvolvimento local:"
    echo "   - Linux: Use ./build-linux-installers.sh"
    echo "   - Windows: Use ./build-windows-installer.sh"
    echo "   - Cross-platform: Configure GitHub Actions"
    echo ""
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
            show_cross_platform_info
            echo ""
            echo "Pressione Enter para continuar..."
            read -r
            ;;
        5)
            echo "👋 Saindo..."
            exit 0
            ;;
        *)
            if [ "$IS_LINUX" = true ]; then
                echo "❌ Opção inválida! Digite 1, 2, 3, 4 ou 5."
            elif [ "$IS_WINDOWS" = true ]; then
                echo "❌ Opção inválida! Digite 1, 2, 3, 4 ou 5."
            else
                echo "❌ Opção inválida! Digite 1, 2, 3, 4 ou 5."
            fi
            echo ""
            ;;
    esac
done

echo ""
echo "🎉 Build concluído com sucesso!"
echo "📁 Verifique os instaladores gerados em: build/dist/" 