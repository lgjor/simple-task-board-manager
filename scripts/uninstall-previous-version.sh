#!/bin/bash

# Script para desinstalar versões anteriores do SimpleTaskBoardManager
# Este script deve ser executado antes da instalação de uma nova versão

set -e

echo "=== Desinstalador de Versões Anteriores ==="
echo "Data/Hora: $(date)"

# Configurações
APP_NAME="SimpleTaskBoardManager"
INSTALL_DIRS=(
    "/opt/$APP_NAME"
    "/usr/local/$APP_NAME"
    "$HOME/.local/share/$APP_NAME"
    "$HOME/$APP_NAME"
)

echo "Verificando versões instaladas..."

# Para a aplicação se estiver rodando
echo "Verificando se a aplicação está rodando..."
if pgrep -f "$APP_NAME" > /dev/null; then
    echo "Parando aplicação..."
    pkill -f "$APP_NAME"
    sleep 3
fi

# Remove instalações em diferentes locais
for dir in "${INSTALL_DIRS[@]}"; do
    if [ -d "$dir" ]; then
        echo "Removendo instalação: $dir"
        rm -rf "$dir" 2>/dev/null || {
            echo "AVISO: Não foi possível remover completamente $dir"
            echo "Tente executar com privilégios de administrador:"
            echo "sudo rm -rf $dir"
        }
    fi
done

# Remove atalhos do menu de aplicações
echo "Removendo atalhos do menu..."
DESKTOP_FILES=(
    "/usr/share/applications/$APP_NAME.desktop"
    "$HOME/.local/share/applications/$APP_NAME.desktop"
    "/usr/local/share/applications/$APP_NAME.desktop"
)

for desktop_file in "${DESKTOP_FILES[@]}"; do
    if [ -f "$desktop_file" ]; then
        echo "Removendo atalho: $desktop_file"
        rm -f "$desktop_file" 2>/dev/null || {
            echo "AVISO: Não foi possível remover $desktop_file"
        }
    fi
done

# Remove atalhos da área de trabalho
echo "Removendo atalhos da área de trabalho..."
if [ -f "$HOME/Desktop/$APP_NAME.desktop" ]; then
    rm -f "$HOME/Desktop/$APP_NAME.desktop"
fi

# Remove binários do PATH
echo "Removendo binários do PATH..."
BIN_LOCATIONS=(
    "/usr/local/bin/$APP_NAME"
    "/usr/bin/$APP_NAME"
    "$HOME/.local/bin/$APP_NAME"
)

for bin in "${BIN_LOCATIONS[@]}"; do
    if [ -f "$bin" ]; then
        echo "Removendo binário: $bin"
        rm -f "$bin" 2>/dev/null || {
            echo "AVISO: Não foi possível remover $bin"
        }
    fi
done

# Remove ícones
echo "Removendo ícones..."
ICON_DIRS=(
    "/usr/share/icons/hicolor/*/apps/$APP_NAME.png"
    "/usr/share/icons/hicolor/*/apps/$APP_NAME.svg"
    "/usr/local/share/icons/hicolor/*/apps/$APP_NAME.png"
    "/usr/local/share/icons/hicolor/*/apps/$APP_NAME.svg"
    "$HOME/.local/share/icons/hicolor/*/apps/$APP_NAME.png"
    "$HOME/.local/share/icons/hicolor/*/apps/$APP_NAME.svg"
)

for icon_pattern in "${ICON_DIRS[@]}"; do
    for icon in $icon_pattern; do
        if [ -f "$icon" ]; then
            echo "Removendo ícone: $icon"
            rm -f "$icon" 2>/dev/null || {
                echo "AVISO: Não foi possível remover $icon"
            }
        fi
    done
done

# Atualiza cache de ícones
if command -v gtk-update-icon-cache > /dev/null; then
    echo "Atualizando cache de ícones..."
    gtk-update-icon-cache -f -t /usr/share/icons/hicolor 2>/dev/null || true
fi

# Verifica se ainda existem instalações
echo ""
echo "Verificando se ainda existem instalações..."
found_installations=false

for dir in "${INSTALL_DIRS[@]}"; do
    if [ -d "$dir" ]; then
        echo "AVISO: Ainda existe instalação em: $dir"
        found_installations=true
    fi
done

if [ "$found_installations" = true ]; then
    echo ""
    echo "ATENÇÃO: Algumas instalações não puderam ser removidas automaticamente."
    echo "Você pode precisar remover manualmente ou executar com privilégios de administrador."
    echo ""
    echo "Locais para verificar:"
    for dir in "${INSTALL_DIRS[@]}"; do
        echo "- $dir"
    done
    echo ""
    read -p "Deseja continuar com a instalação mesmo assim? (s/N): " -r
    if [[ ! $REPLY =~ ^[Ss]$ ]]; then
        echo "Instalação cancelada."
        exit 1
    fi
else
    echo "Todas as versões anteriores foram removidas com sucesso!"
fi

echo ""
echo "Desinstalação concluída!"
echo "Você pode agora prosseguir com a instalação da nova versão."
echo ""
read -p "Pressione Enter para continuar..." 