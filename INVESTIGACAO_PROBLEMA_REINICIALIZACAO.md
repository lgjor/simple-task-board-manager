# 🔍 Investigação do Problema Persistente - Reinicialização

## 🚨 **Status Atual**

### **Problema Persistente**
- **✅ Correção 1**: Removido recarregamento de metadados durante reinicialização
- **✅ Correção 2**: Diretório de configuração correto durante reinicialização
- **❌ PROBLEMA 3**: Sistema **AINDA** sugere "Sem Grupo" após reinicialização via "Reiniciar Agora"

### **Análise do Problema**

#### **1. Arquivo de Configuração**
- **✅ Status**: Arquivo `app-metadata.json` está correto
- **✅ Valor**: `"defaultBoardGroupId" : 2` (grupo "Livros")
- **✅ Localização**: `C:\Users\Lucas\myboards\config\app-metadata.json`

#### **2. Comportamento Observado**
- **✅ Reinicialização manual** (`./gradlew run`) → funciona perfeitamente
- **❌ Reinicialização automática** ("Reiniciar Agora") → ainda sugere "Sem Grupo"

#### **3. Hipótese do Problema**
O problema pode estar na **sequência de inicialização** durante a reinicialização automática:

1. **Aplicação é fechada** ✅
2. **Aplicação instalada é executada** ✅
3. **❌ PROBLEMA**: Durante a inicialização, algum erro ocorre
4. **❌ RESULTADO**: Sistema chama `useDefaultMetadata()` que define `defaultBoardGroupId = null`

## 🔧 **Logs de Debug Implementados**

### **1. Logs no Método `loadMetadata()`**
```java
log.info("🔄 Iniciando carregamento de metadados...");
log.info("📁 Caminho do arquivo: {}", metadataFilePath);
log.info("📁 Arquivo existe: {}", Files.exists(metadataFilePath));
log.info("📏 Tamanho do arquivo: {} bytes", fileSize);
log.info("📖 Tentando ler arquivo de metadados...");
log.info("✅ Metadados carregados com sucesso de: {}", metadataFilePath);
log.info("📊 Dados carregados:");
log.info("   - defaultBoardGroupId: {}", this.currentMetadata.getDefaultBoardGroupId());
```

### **2. Logs no Método `useDefaultMetadata()`**
```java
log.warn("🔄 MÉTODO useDefaultMetadata() CHAMADO!");
log.warn("🔄 Stack trace da chamada:");
log.info("✅ Metadados padrão criados e aplicados");
log.info("📊 Dados padrão aplicados:");
log.info("   - defaultBoardGroupId: {}", this.currentMetadata.getDefaultBoardGroupId());
```

## 🧪 **Como Testar a Investigação**

### **1. Teste de Reinicialização Automática com Logs**
1. **Abra a aplicação** Simple Task Board Manager
2. **Clique em "⚙️ Preferências"**
3. **Selecione "Livros"** no campo "Grupo de Board Padrão"
4. **Clique em "Salvar Preferências"**
5. **Clique em "Reiniciar Agora"**
6. **Aguarde a aplicação reiniciar automaticamente**
7. **Verifique os logs** para identificar onde está falhando

### **2. Logs Esperados (Sucesso)**
```
INFO - 🔄 Iniciando carregamento de metadados...
INFO - 📁 Caminho do arquivo: C:\Users\Lucas\myboards\config\app-metadata.json
INFO - 📁 Arquivo existe: true
INFO - 📏 Tamanho do arquivo: XXXX bytes
INFO - 📖 Tentando ler arquivo de metadados...
INFO - ✅ Metadados carregados com sucesso de: C:\Users\Lucas\myboards\config\app-metadata.json
INFO - 📊 Dados carregados:
INFO -    - defaultBoardGroupId: 2
INFO - ✅ Validação de metadados concluída com sucesso
```

### **3. Logs Esperados (Falha)**
```
INFO - 🔄 Iniciando carregamento de metadados...
INFO - 📁 Caminho do arquivo: C:\Users\Lucas\myboards\config\app-metadata.json
INFO - 📁 Arquivo existe: true
INFO - 📏 Tamanho do arquivo: XXXX bytes
INFO - 📖 Tentando ler arquivo de metadados...
ERROR - ❌ Erro ao ler arquivo de metadados: [MENSAGEM DE ERRO]
WARN - 🔄 MÉTODO useDefaultMetadata() CHAMADO!
INFO - ✅ Metadados padrão criados e aplicados
INFO - 📊 Dados padrão aplicados:
INFO -    - defaultBoardGroupId: null
```

## 🎯 **Possíveis Causas do Problema**

### **1. Problema de Permissões**
- **Cenário**: Aplicação instalada não tem permissão para ler o arquivo
- **Sintoma**: `Files.exists(metadataFilePath)` retorna `false`
- **Log**: `❌ Arquivo de metadados não encontrado, usando configurações padrão`

### **2. Problema de Codificação de Caracteres**
- **Cenário**: Arquivo tem caracteres especiais que não são lidos corretamente
- **Sintoma**: `objectMapper.readValue()` falha com erro de parsing
- **Log**: `❌ Erro ao ler arquivo de metadados: [MENSAGEM DE ERRO]`

### **3. Problema de Concorrência**
- **Cenário**: Arquivo está sendo modificado durante a leitura
- **Sintoma**: `Files.size(metadataFilePath)` retorna `0` ou falha
- **Log**: `❌ Arquivo de metadados está vazio, usando configurações padrão`

### **4. Problema de Variáveis de Ambiente**
- **Cenário**: `user.home` ou `user.dir` são diferentes na aplicação instalada
- **Sintoma**: Caminho do arquivo está incorreto
- **Log**: `❌ Arquivo de metadados não encontrado, usando configurações padrão`

## 🔍 **Como Analisar os Logs**

### **1. Identificar o Ponto de Falha**
- **Se o log para em "📁 Arquivo existe: true"**: Problema na leitura do arquivo
- **Se o log para em "📏 Tamanho do arquivo"**: Problema de permissões ou concorrência
- **Se o log para em "📖 Tentando ler arquivo"**: Problema de parsing JSON
- **Se o log mostra "🔄 MÉTODO useDefaultMetadata() CHAMADO!"**: Confirmado que está usando padrão

### **2. Verificar Stack Trace**
- **Logs de stack trace** mostrarão exatamente onde `useDefaultMetadata()` foi chamado
- **Identificar a linha** que está causando a falha

### **3. Verificar Dados Carregados**
- **Se dados são carregados mas `defaultBoardGroupId` é `null`**: Problema no arquivo
- **Se dados não são carregados**: Problema na leitura

## 🔄 **Próximos Passos**

### **1. Executar Teste com Logs**
- **Seguir instruções** de teste acima
- **Coletar logs completos** da reinicialização automática
- **Identificar exatamente** onde está falhando

### **2. Analisar Logs**
- **Verificar se arquivo é encontrado**
- **Verificar se arquivo é lido com sucesso**
- **Verificar se dados são válidos**
- **Identificar ponto de falha**

### **3. Implementar Correção**
- **Baseado nos logs**, implementar correção específica
- **Testar novamente** para confirmar resolução

## 📝 **Comandos para Coletar Logs**

### **1. Executar Aplicação com Logs Detalhados**
```bash
./gradlew run
```

### **2. Verificar Logs em Tempo Real**
```bash
Get-Content $env:USERPROFILE\myboards\logs\*.log -Wait
```

### **3. Verificar Arquivo de Configuração**
```bash
Get-Content $env:USERPROFILE\myboards\config\app-metadata.json
```

## 🏆 **Objetivo da Investigação**

**Identificar exatamente onde e por que** o sistema está falhando durante a reinicialização automática, para implementar uma **correção definitiva** que resolva o problema na raiz.

**Com os logs detalhados, poderemos ver exatamente o que está acontecendo e corrigir o problema de uma vez por todas!** 🎯
