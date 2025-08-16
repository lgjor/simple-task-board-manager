# Documentação JavaDoc - Simple Task Board Manager

Este documento descreve como gerar e visualizar a documentação JavaDoc do projeto Simple Task Board Manager.

## Visão Geral

O projeto possui documentação JavaDoc completa para todas as classes principais, incluindo:

- **Classes de Modelo**: `Board`, `Card`, `Task`, `CheckListItem`, etc.
- **Classes de Serviço**: `TaskManagerFacade`, `CardService`, `BoardService`, etc.
- **Classes de Repositório**: `CardRepository`, `BoardRepository`, etc.
- **Classes de Configuração**: `AppConfig`, `AppMetadataConfig`, etc.
- **Classes de Interface**: `MainApp`, `PreferencesController`, etc.
- **Classes de Utilitário**: `WindowManager`, `IconManager`, etc.
- **Classes de Exceção**: `ResourceNotFoundException`, etc.

## Como Gerar a Documentação

### 1. Usando Gradle (Recomendado)

```bash
# Gerar documentação JavaDoc
./gradlew javadoc

# A documentação será gerada em: build/docs/javadoc/
```

### 2. Usando Maven (Alternativo)

```bash
# Gerar documentação JavaDoc
mvn javadoc:javadoc

# A documentação será gerada em: target/site/apidocs/
```

### 3. Usando IDE

#### IntelliJ IDEA
1. Vá para `Tools` → `Generate JavaDoc`
2. Configure as opções desejadas
3. Clique em `OK`

#### Eclipse
1. Clique com botão direito no projeto
2. Selecione `Export` → `Java` → `Javadoc`
3. Configure as opções e clique em `Finish`

## Estrutura da Documentação

### Classes de Modelo (Package: `org.desviante.model`)

- **`Board`**: Representa um quadro de tarefas
- **`Card`**: Representa uma tarefa individual
- **`Task`**: Representa uma tarefa sincronizada com Google Tasks
- **`CheckListItem`**: Representa um item de checklist
- **`BoardGroup`**: Representa um grupo de quadros
- **`BoardColumn`**: Representa uma coluna de um quadro

### Classes de Serviço (Package: `org.desviante.service`)

- **`TaskManagerFacade`**: Fachada principal para gerenciamento de tarefas
- **`CardService`**: Serviço para operações com cards
- **`BoardService`**: Serviço para operações com quadros
- **`BoardGroupService`**: Serviço para operações com grupos
- **`CardTypeService`**: Serviço para tipos de cards

### Classes de Repositório (Package: `org.desviante.repository`)

- **`CardRepository`**: Persistência de cards usando JDBC
- **`BoardRepository`**: Persistência de quadros usando JDBC
- **`CheckListItemRepository`**: Persistência de itens de checklist

### Classes de Configuração (Package: `org.desviante.config`)

- **`AppConfig`**: Configuração principal da aplicação Spring
- **`AppMetadataConfig`**: Configuração de metadados da aplicação
- **`DataConfig`**: Configuração de banco de dados
- **`GoogleApiConfig`**: Configuração da API do Google

### Classes de Interface (Package: `org.desviante.view`)

- **`MainApp`**: Classe principal da interface JavaFX
- **`PreferencesController`**: Controller para tela de preferências
- **`BoardViewController`**: Controller principal do quadro

## Padrões de Documentação

### 1. Documentação de Classe

Cada classe possui:

```java
/**
 * Descrição da classe.
 * 
 * <p>Explicação detalhada das responsabilidades e funcionalidades.</p>
 * 
 * <p><strong>Características Principais:</strong></p>
 * <ul>
 *   <li>Característica 1</li>
 *   <li>Característica 2</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see ClasseRelacionada
 */
```

### 2. Documentação de Métodos

Cada método público possui:

```java
/**
 * Descrição do método.
 * 
 * <p>Explicação detalhada do comportamento e parâmetros.</p>
 * 
 * @param parametro descrição do parâmetro
 * @return descrição do valor retornado
 * @throws Excecao descrição da exceção
 * @see ClasseRelacionada
 */
```

### 3. Documentação de Campos

Campos importantes possuem:

```java
/**
 * Descrição do campo.
 * <p>Explicação adicional se necessário.</p>
 */
private TipoCampo nomeCampo;
```

## Visualizando a Documentação

### 1. Navegador Web

Após gerar a documentação, abra o arquivo `index.html` em um navegador web:

```
build/docs/javadoc/index.html
```

### 2. Estrutura da Documentação

- **Overview**: Visão geral de todos os pacotes
- **Package**: Lista de classes por pacote
- **Class**: Documentação detalhada de cada classe
- **Index**: Índice alfabético de todas as classes e métodos

## Manutenção da Documentação

### 1. Atualizações

- Sempre atualize a documentação ao modificar classes
- Mantenha os exemplos e descrições atualizados
- Verifique se os links `@see` ainda são válidos

### 2. Padrões

- Use HTML básico para formatação (`<p>`, `<ul>`, `<li>`, `<strong>`)
- Mantenha a consistência na estrutura da documentação
- Inclua exemplos de uso quando apropriado

### 3. Validação

- Execute `./gradlew javadoc` regularmente para verificar erros
- Verifique se todas as classes públicas estão documentadas
- Teste a geração da documentação em diferentes ambientes

## Exemplo de Documentação Completa

```java
/**
 * Fachada principal para o gerenciamento de tarefas no sistema.
 * 
 * <p>Esta classe implementa o padrão Facade, fornecendo uma interface simplificada
 * para todas as operações relacionadas ao gerenciamento de quadros, colunas, cards
 * e tarefas. Ela coordena a interação entre os diversos serviços especializados,
 * garantindo consistência transacional e otimizações de performance.</p>
 * 
 * <p><strong>Responsabilidades Principais:</strong></p>
 * <ul>
 *   <li>Gerenciamento de quadros (boards) e suas operações CRUD</li>
 *   <li>Coordenação de operações entre diferentes entidades do sistema</li>
 *   <li>Implementação de regras de negócio para status e progresso</li>
 *   <li>Otimização de consultas através de agrupamento de dados</li>
 *   <li>Gerenciamento de transações para operações complexas</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see BoardService
 * @see CardService
 * @see TaskService
 */
```

## Conclusão

A documentação JavaDoc está completa e atualizada para todas as classes principais do projeto. Ela segue padrões consistentes e fornece informações detalhadas sobre a arquitetura, responsabilidades e uso de cada componente do sistema.

Para manter a qualidade da documentação, sempre atualize-a ao fazer modificações no código e execute regularmente a geração para verificar erros.
