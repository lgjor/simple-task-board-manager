#!/bin/bash

echo "=== Teste de Detecção de Aplicação Instalada ==="
echo

# Simular a detecção de aplicação instalada
APP_NAME="SimpleTaskBoardManager"
JAVA_HOME="${JAVA_HOME:-$(dirname $(dirname $(which java)))}"

echo "Verificando locais possíveis de instalação..."
echo

# Verificar /usr/bin
if [ -f "/usr/bin/$APP_NAME" ]; then
    echo "✓ Encontrado em: /usr/bin/$APP_NAME"
    INSTALLED_PATH="/usr/bin/$APP_NAME"
elif [ -f "/usr/local/bin/$APP_NAME" ]; then
    echo "✓ Encontrado em: /usr/local/bin/$APP_NAME"
    INSTALLED_PATH="/usr/local/bin/$APP_NAME"
elif [ -f "$HOME/.local/bin/$APP_NAME" ]; then
    echo "✓ Encontrado em: $HOME/.local/bin/$APP_NAME"
    INSTALLED_PATH="$HOME/.local/bin/$APP_NAME"
elif [ -f "/opt/$APP_NAME/bin/$APP_NAME" ]; then
    echo "✓ Encontrado em: /opt/$APP_NAME/bin/$APP_NAME"
    INSTALLED_PATH="/opt/$APP_NAME/bin/$APP_NAME"
else
    echo "✗ Não encontrado em locais padrão"
fi

# Verificar se é executável
if [ -n "$INSTALLED_PATH" ] && [ -x "$INSTALLED_PATH" ]; then
    echo "✓ Executável encontrado e com permissões de execução"
else
    echo "✗ Executável não encontrado ou sem permissões de execução"
    INSTALLED_PATH=""
fi

echo
if [ -n "$INSTALLED_PATH" ]; then
    echo "=== Aplicação Instalada Detectada ==="
    echo "Caminho: $INSTALLED_PATH"
    echo
    echo "Testando comando de reinicialização..."
    echo "Comando: $INSTALLED_PATH"
    echo
    echo "NOTA: Este é apenas um teste de detecção."
    echo "Para testar a reinicialização real, execute a aplicação."
else
    echo "=== Nenhuma Aplicação Instalada Detectada ==="
    echo
    if [ -n "$JAVA_HOME" ]; then
        echo "Java encontrado em: $JAVA_HOME"
        echo "A aplicação tentará reiniciar via Java diretamente."
    else
        echo "Java não encontrado. Configure JAVA_HOME."
    fi
fi

echo
echo "Pressione Enter para sair..."
read
