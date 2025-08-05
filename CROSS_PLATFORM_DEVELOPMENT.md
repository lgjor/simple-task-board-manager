# Desenvolvimento Cross-Platform - Simple Task Board Manager

Este guia explica como desenvolver e fazer build do projeto em diferentes sistemas operacionais.

## Problemas Identificados

### Windows → Linux
- **Javadoc**: Propriedades `charSet`, `docEncoding` e `links` não são reconhecidas no Linux
- **Testes**: Problemas de execução de processos filhos no Linux
- **JavaFX**: Configurações específicas necessárias para headless mode

### Linux → Windows
- **Caminhos**: Diferenças entre `/` e `\`
- **Permissões**: Problemas de execução de scripts
- **Encoding**: Diferenças de encoding de arquivos

## Soluções Implementadas

### 1. Build Scripts Específicos

#### Para Linux:
```bash
./build-linux.sh
```

#### Para Windows:
```cmd
gradlew.bat build
```

### 2. Configurações Gradle Cross-Platform

#### `build.gradle.kts`
- Detecção automática do sistema operacional
- Configurações específicas para cada OS
- Tasks específicas para cada plataforma

#### `gradle.properties`
- Configurações otimizadas para Linux
- Propriedades de sistema para testes headless

### 3. Tasks Gradle Disponíveis

```bash
# Build completo (Windows)
./gradlew buildWindows

# Build Linux (sem testes problemáticos)
./gradlew buildLinux

# Apenas compilação
./gradlew compileJava

# Gerar JAR sem testes
./gradlew shadowJar -x test
```

## Configurações Específicas por OS

### Linux
- **Testes**: Configurados para modo headless
- **JavaFX**: Usa software rendering
- **Processos**: Limitação de forks paralelos
- **Encoding**: UTF-8 forçado

### Windows
- **Testes**: Execução normal
- **JavaFX**: Hardware acceleration
- **Processos**: Execução paralela completa
- **Encoding**: Configuração padrão

## Troubleshooting

### Problemas Comuns no Linux

#### 1. Erro de Javadoc
```
Unresolved reference: charSet
```
**Solução**: Usar `./gradlew buildLinux` ou `./build-linux.sh`

#### 2. Erro de Processos
```
Could not start '/usr/lib/jvm/java-21-openjdk-amd64/bin/java'
```
**Solução**: Configurações de teste específicas para Linux já implementadas

#### 3. Problemas de Permissão
```
Permission denied: ./gradlew
```
**Solução**: `chmod +x ./gradlew`

### Problemas Comuns no Windows

#### 1. Caminhos Longos
```
Path too long
```
**Solução**: Usar caminhos relativos e configurações de build otimizadas

#### 2. Encoding
```
Invalid character encoding
```
**Solução**: Configurações UTF-8 já implementadas

## Melhores Práticas

### 1. Desenvolvimento
- Sempre testar em ambos os sistemas operacionais
- Usar scripts específicos para cada OS
- Manter configurações cross-platform no `build.gradle.kts`

### 2. Build
- **Linux**: Usar `./build-linux.sh`
- **Windows**: Usar `gradlew.bat build`
- **CI/CD**: Usar `./gradlew shadowJar -x test`

### 3. Testes
- **Linux**: Testes podem falhar devido a limitações de processo
- **Windows**: Testes completos
- **CI/CD**: Executar testes em ambiente controlado

## Estrutura de Arquivos

```
simple-task-board-manager/
├── build.gradle.kts          # Configuração cross-platform
├── gradle.properties         # Configurações específicas Linux
├── build-linux.sh           # Script de build Linux
├── gradlew                  # Gradle wrapper (Linux/Mac)
├── gradlew.bat             # Gradle wrapper (Windows)
└── CROSS_PLATFORM_DEVELOPMENT.md  # Este arquivo
```

## Comandos Úteis

### Verificar Sistema
```bash
# Linux
uname -a
java -version
./gradlew --version

# Windows
systeminfo
java -version
gradlew.bat --version
```

### Build Rápido
```bash
# Linux
./build-linux.sh

# Windows
gradlew.bat shadowJar
```

### Limpeza
```bash
# Linux
./gradlew clean

# Windows
gradlew.bat clean
```

## Notas Importantes

1. **JavaFX**: Requer configurações específicas para cada OS
2. **Testes**: Podem falhar no Linux devido a limitações de processo
3. **Javadoc**: Configuração simplificada para compatibilidade
4. **Encoding**: UTF-8 forçado em todas as configurações

## Suporte

Para problemas específicos:
1. Verificar logs do Gradle
2. Usar `--stacktrace` para detalhes
3. Consultar este documento
4. Verificar configurações específicas do OS 