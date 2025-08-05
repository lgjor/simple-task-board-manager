#!/bin/bash

# Script para criar ícone PNG para Linux a partir do ícone ICO
# Requer ImageMagick instalado: sudo apt install imagemagick

echo "=== Criando ícone PNG para Linux ==="

ICON_ICO="src/main/resources/icon.ico"
ICON_PNG="src/main/resources/icon.png"

# Verifica se o ImageMagick está instalado
if ! command -v convert &> /dev/null; then
    echo "❌ ImageMagick não encontrado!"
    echo "Instale com: sudo apt install imagemagick"
    exit 1
fi

# Verifica se o ícone ICO existe
if [ ! -f "$ICON_ICO" ]; then
    echo "❌ Ícone ICO não encontrado: $ICON_ICO"
    exit 1
fi

# Converte ICO para PNG
echo "Convertendo ICO para PNG..."
convert "$ICON_ICO" -resize 256x256 "$ICON_PNG"

if [ $? -eq 0 ]; then
    echo "✅ Ícone PNG criado: $ICON_PNG"
    echo "📏 Tamanho: $(identify -format '%wx%h' "$ICON_PNG")"
else
    echo "❌ Erro na conversão do ícone"
    exit 1
fi

# Cria também versões em diferentes tamanhos para Linux
echo "Criando versões em diferentes tamanhos..."

# Criar diretórios para diferentes tamanhos
mkdir -p src/main/resources/icons/linux/16x16/apps
mkdir -p src/main/resources/icons/linux/32x32/apps
mkdir -p src/main/resources/icons/linux/48x48/apps
mkdir -p src/main/resources/icons/linux/64x64/apps
mkdir -p src/main/resources/icons/linux/128x128/apps
mkdir -p src/main/resources/icons/linux/256x256/apps
mkdir -p src/main/resources/icons/linux/512x512/apps

# 16x16
convert "$ICON_ICO" -resize 16x16 "src/main/resources/icons/linux/16x16/apps/simple-task-board-manager.png"
# 32x32
convert "$ICON_ICO" -resize 32x32 "src/main/resources/icons/linux/32x32/apps/simple-task-board-manager.png"
# 48x48
convert "$ICON_ICO" -resize 48x48 "src/main/resources/icons/linux/48x48/apps/simple-task-board-manager.png"
# 64x64
convert "$ICON_ICO" -resize 64x64 "src/main/resources/icons/linux/64x64/apps/simple-task-board-manager.png"
# 128x128
convert "$ICON_ICO" -resize 128x128 "src/main/resources/icons/linux/128x128/apps/simple-task-board-manager.png"
# 256x256
convert "$ICON_ICO" -resize 256x256 "src/main/resources/icons/linux/256x256/apps/simple-task-board-manager.png"
# 512x512
convert "$ICON_ICO" -resize 512x512 "src/main/resources/icons/linux/512x512/apps/simple-task-board-manager.png"

echo "✅ Ícones Linux criados em: src/main/resources/icons/linux/"
echo "📁 Estrutura criada para diferentes resoluções" 