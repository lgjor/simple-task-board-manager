# Solução - Google API no Linux

## ✅ Problema Resolvido

O aplicativo **Simple Task Board Manager** agora funciona perfeitamente no Linux Ubuntu 24.04.2 LTS, mesmo sem as credenciais do Google API configuradas.

## 🔍 Problema Original

```
Error creating bean with name 'googleTasksApiService' defined in file [...]: 
Unsatisfied dependency expressed through constructor parameter 0: 
Error creating bean with name 'tasksService' defined in class path resource [...]: 
Failed to instantiate [com.google.api.services.tasks.Tasks]: 
Factory method 'tasksService' threw exception with message: 
User credentials not found. Please run the application in interactive mode once to authorize.
```

## 🛠️ Solução Implementada

### 1. **Configuração Condicional do Google API**

#### `GoogleApiConfig.java`
- Adicionada anotação `@ConditionalOnProperty(name = "google.api.enabled", havingValue = "true", matchIfMissing = false)`
- Beans só são criados quando `google.api.enabled=true` no `application.properties`
- Tratamento gracioso de erros com logs de warning em vez de exceções

#### `GoogleTasksApiService.java`
- Adicionada anotação `@ConditionalOnProperty` para tornar o serviço opcional
- Verificações de null para `tasksService`
- Método `isGoogleTasksAvailable()` para verificar disponibilidade

#### `TaskService.java`
- Modificado construtor para aceitar `GoogleTasksApiService` como opcional
- Uso de `@Autowired(required = false)`
- Lógica condicional para criar tarefas apenas localmente quando Google API não está disponível

### 2. **Comportamento Implementado**

#### Com Google API Configurado:
- Tarefas são criadas no Google Tasks
- Sincronização completa entre sistema local e Google
- Flag `sent=true` nas tarefas locais

#### Sem Google API Configurado:
- Tarefas são criadas apenas localmente
- Funcionalidade completa do sistema mantida
- Flag `sent=false` nas tarefas locais
- Logs informativos sobre a indisponibilidade

## 📁 Arquivos Modificados

### `src/main/java/org/desviante/config/GoogleApiConfig.java`
```java
@ConditionalOnProperty(name = "google.api.enabled", havingValue = "true", matchIfMissing = false)
public GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow() {
    // Tratamento gracioso de erros
    if (in == null) {
        log.warning("Google API credentials not found. Google Tasks integration will be disabled.");
        return null;
    }
    // ...
}
```

### `src/main/java/org/desviante/service/GoogleTasksApiService.java`
```java
@ConditionalOnProperty(name = "google.api.enabled", havingValue = "true", matchIfMissing = false)
public class GoogleTasksApiService {
    // Verificações de null
    if (tasksService == null) {
        log.warning("Google Tasks API não está disponível.");
        throw new GoogleApiServiceException("Google Tasks API não está configurada.", null);
    }
}
```

### `src/main/java/org/desviante/service/TaskService.java`
```java
public TaskService(TaskRepository taskRepository, CardRepository cardRepository, 
                  @Autowired(required = false) GoogleTasksApiService googleApiService) {
    // Construtor modificado para aceitar serviço opcional
}

// Lógica condicional
if (googleApiService == null) {
    // Cria apenas a entidade local sem sincronização
    localTask.setGoogleTaskId(null);
    localTask.setSent(false);
} else {
    // Sincronização completa com Google Tasks
}
```

## 🚀 Como Usar

### Para Desabilitar Google API:
```properties
# application.properties
google.api.enabled=false
```

### Para Habilitar Google API:
```properties
# application.properties
google.api.enabled=true
```

### Para Configurar Credenciais Google:
1. Coloque o arquivo `credentials.json` em `src/main/resources/auth/`
2. Execute o aplicativo uma vez para autorizar
3. As credenciais serão salvas em `~/.credentials/simple-task-board-manager/`

## 📊 Resultados

### ✅ Funcionando no Linux:
- **Aplicativo inicia sem erros**
- **Interface gráfica funciona perfeitamente**
- **Banco de dados local funciona**
- **Criação de tarefas funciona (localmente)**
- **Todas as funcionalidades principais mantidas**

### ⚠️ Limitações (quando Google API não configurado):
- Tarefas não são sincronizadas com Google Tasks
- Funcionalidade de integração Google desabilitada
- Logs informativos sobre a indisponibilidade

## 🎯 Benefícios da Solução

1. **Robustez**: Aplicativo funciona independente da configuração do Google API
2. **Flexibilidade**: Pode ser usado com ou sem integração Google
3. **Compatibilidade**: Funciona em diferentes ambientes (desenvolvimento, produção)
4. **Manutenibilidade**: Código limpo com separação clara de responsabilidades
5. **Experiência do Usuário**: Interface funciona mesmo sem configuração completa

## 🔧 Configuração Futura

Para habilitar a integração completa com Google Tasks:

1. **Obter credenciais Google**:
   - Acesse Google Cloud Console
   - Crie um projeto
   - Habilite Google Tasks API
   - Crie credenciais OAuth 2.0

2. **Configurar credenciais**:
   ```bash
   # Copie o arquivo credentials.json
   cp ~/Downloads/credentials.json src/main/resources/auth/
   ```

3. **Habilitar integração**:
   ```properties
   # application.properties
   google.api.enabled=true
   ```

4. **Autorizar aplicação**:
   - Execute o aplicativo
   - Siga o fluxo de autorização OAuth
   - As credenciais serão salvas automaticamente

## 📝 Conclusão

A solução implementada resolve completamente o problema de compatibilidade do Google API no Linux, permitindo que o aplicativo funcione de forma robusta e flexível, mantendo todas as funcionalidades principais mesmo sem a integração com Google Tasks configurada. 