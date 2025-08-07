#!/bin/bash

# Script de instalação automatizada para nova versão
# Este script faz backup, desinstala versão anterior e instala a nova versão

set -e

echo "========================================"
echo "    Instalador Automatizado v1.0.6"
echo "========================================"
echo ""
echo "Este script irá:"
echo "1. Fazer backup do banco de dados"
echo "2. Desinstalar versão anterior"
echo "3. Instalar nova versão"
echo "4. Verificar integridade"
echo ""

# Verifica se o instalador existe
INSTALLER_PATH="build/dist/SimpleTaskBoardManager-1.0.6.AppImage"
if [ ! -f "$INSTALLER_PATH" ]; then
    echo "ERRO: Instalador não encontrado em: $INSTALLER_PATH"
    echo ""
    echo "Certifique-se de que:"
    echo "1. O projeto foi compilado com: ./gradlew jpackageLinux"
    echo "2. O instalador está em: build/dist/"
    echo ""
    exit 1
fi

echo "Instalador encontrado: $INSTALLER_PATH"
echo ""

# Passo 1: Backup do banco de dados
echo "========================================"
echo "PASSO 1: Backup do Banco de Dados"
echo "========================================"
echo ""
./scripts/backup-database.sh || {
    echo ""
    echo "AVISO: Backup falhou, mas continuando..."
    echo ""
}

# Passo 2: Desinstalar versão anterior
echo "========================================"
echo "PASSO 2: Desinstalando Versão Anterior"
echo "========================================"
echo ""
./scripts/uninstall-previous-version.sh || {
    echo ""
    echo "AVISO: Desinstalação falhou, mas continuando..."
    echo ""
}

# Passo 3: Instalar nova versão
echo "========================================"
echo "PASSO 3: Instalando Nova Versão"
echo "========================================"
echo ""
echo "Iniciando instalação da versão 1.0.6..."
echo ""

# Torna o instalador executável
chmod +x "$INSTALLER_PATH"

# Executa o instalador
echo "Executando instalador..."
if [ -f "$INSTALLER_PATH" ]; then
    # Para AppImage
    if [[ "$INSTALLER_PATH" == *.AppImage ]]; then
        echo "Instalando AppImage..."
        ./"$INSTALLER_PATH" --install
    else
        # Para outros tipos de instalador
        ./"$INSTALLER_PATH"
    fi
else
    echo "ERRO: Instalador não encontrado!"
    exit 1
fi

# Verifica se a instalação foi bem-sucedida
if [ $? -ne 0 ]; then
    echo ""
    echo "ERRO: Falha na instalação!"
    echo ""
    echo "Possíveis soluções:"
    echo "1. Execute o instalador manualmente"
    echo "2. Verifique se tem permissões de administrador"
    echo "3. Verifique se não há problemas de dependências"
    echo ""
    exit 1
fi

echo ""
echo "Instalação concluída com sucesso!"
echo ""

# Passo 4: Verificar integridade
echo "========================================"
echo "PASSO 4: Verificação de Integridade"
echo "========================================"
echo ""

# Aguarda um pouco para garantir que a instalação foi finalizada
sleep 5

echo "Verificando se a aplicação foi instalada corretamente..."

# Verifica se o executável foi instalado
INSTALLED_PATHS=(
    "/opt/SimpleTaskBoardManager/SimpleTaskBoardManager"
    "/usr/local/SimpleTaskBoardManager/SimpleTaskBoardManager"
    "$HOME/.local/share/SimpleTaskBoardManager/SimpleTaskBoardManager"
    "$HOME/SimpleTaskBoardManager/SimpleTaskBoardManager"
)

found_installation=false
for path in "${INSTALLED_PATHS[@]}"; do
    if [ -f "$path" ]; then
        echo "✓ Aplicação instalada em: $path"
        found_installation=true
        break
    fi
done

if [ "$found_installation" = false ]; then
    echo "✗ Aplicação não encontrada em locais padrão"
    echo "Verificando se está no PATH..."
    if command -v SimpleTaskBoardManager > /dev/null; then
        echo "✓ Aplicação encontrada no PATH"
    else
        echo "✗ Aplicação não encontrada no PATH"
    fi
fi

# Verifica se o banco de dados está preservado
echo ""
echo "Verificando integridade do banco de dados..."
if [ -f "$HOME/myboards/board_h2_db.mv.db" ]; then
    echo "✓ Banco de dados preservado"
else
    echo "⚠ Banco de dados não encontrado (pode ser normal para primeira instalação)"
fi

# Verifica se o desktop file foi criado
echo ""
echo "Verificando atalhos do menu..."
DESKTOP_FILES=(
    "/usr/share/applications/SimpleTaskBoardManager.desktop"
    "$HOME/.local/share/applications/SimpleTaskBoardManager.desktop"
    "/usr/local/share/applications/SimpleTaskBoardManager.desktop"
)

found_desktop=false
for desktop_file in "${DESKTOP_FILES[@]}"; do
    if [ -f "$desktop_file" ]; then
        echo "✓ Atalho encontrado: $desktop_file"
        found_desktop=true
    fi
done

if [ "$found_desktop" = false ]; then
    echo "⚠ Nenhum atalho do menu encontrado"
fi

echo ""
echo "========================================"
echo "    Instalação Concluída!"
echo "========================================"
echo ""
echo "Próximos passos:"
echo "1. Inicie a aplicação pelo menu de aplicações"
echo "2. Verifique se seus dados estão presentes"
echo "3. Teste as funcionalidades principais"
echo ""
echo "Se houver problemas:"
echo "- Execute: ./scripts/restore-database.sh"
echo "- Verifique os logs da aplicação"
echo "- Consulte: GUIA_ATUALIZACAO.md"
echo ""
read -p "Pressione Enter para finalizar..." 