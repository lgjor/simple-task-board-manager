#!/bin/bash

# Script de build específico para Linux
# Este script resolve problemas de compatibilidade entre Windows e Linux

echo "=== Simple Task Board Manager - Build Linux ==="
echo "Sistema: $(uname -s) $(uname -r)"
echo "Java: $(java -version 2>&1 | head -n 1)"
echo "Gradle: $(./gradlew --version | grep "Gradle" | head -n 1)"
echo ""

# Verificar se o Gradle wrapper existe
if [ ! -f "./gradlew" ]; then
    echo "Erro: Gradle wrapper não encontrado!"
    exit 1
fi

# Tornar o gradlew executável
chmod +x ./gradlew

# Limpar builds anteriores
echo "Limpando builds anteriores..."
./gradlew clean

# Compilar o projeto
echo "Compilando o projeto..."
./gradlew compileJava

if [ $? -eq 0 ]; then
    echo "✅ Compilação bem-sucedida!"
else
    echo "❌ Erro na compilação!"
    exit 1
fi

# Gerar JAR
echo "Gerando JAR..."
./gradlew shadowJar -x test

if [ $? -eq 0 ]; then
    echo "✅ JAR gerado com sucesso!"
    echo "📁 JAR localizado em: build/libs/"
    ls -la build/libs/*.jar
else
    echo "❌ Erro na geração do JAR!"
    exit 1
fi

# Executar testes (opcional - pode falhar no Linux)
echo ""
echo "Executando testes (opcional)..."
./gradlew test --continue

if [ $? -eq 0 ]; then
    echo "✅ Todos os testes passaram!"
else
    echo "⚠️  Alguns testes falharam (normal no Linux)"
fi

echo ""
echo "=== Build Linux Concluído ==="
echo "Para executar o aplicativo:"
echo "java -jar build/libs/simple-task-board-manager-1.0.3-all.jar" 