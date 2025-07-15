# Gerenciador de boards de tarefas

<p align="center">
  <a href="https://github.com/lgjor/desafio-board-dio" target="_blank">
    <img src=".github/preview.jpg" width="100%" alt="Gerenciador de boards de tarefas">
  </a>
</p>

<p align="center">
Projeto desenvolvido para finalização do Bootcamp Bradesco Java <a href="https://www.dio.me" target="_blank">DIO</a><br/>
</p>

<p align="center">
  <a href="#tecnologias">Tecnologias</a>&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;
  <a href="#funcionalidades">Funcionalidades</a>&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;  
  <a href="#desafio">Desafio</a>&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;
  <a href="#migracao">Migração para JPA/Hibernate</a>&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;
  <a href="#diagrama">Diagrama UML inicial</a>
</p>

## <div id="tecnologias">🚀 Tecnologias</div>

Esse projeto foi desenvolvido com as seguintes tecnologias:

- ☕ Java
- 💾 Sqlite
- 🐘 JPA/Hibernate
- 🖥️ JavaFX

## <div id="funcionalidades">Funcionalidades</div>

- Criar e excluir board de tarefas
- Criar cards
- Editar título e descrição dos cards com duplo clique do mouse
- Mover cards entre as colunas não iniciado, em andamento e concluído usando o drag and drop
- Método de cálculo do % não inciado, em andamento e concluído dos boards
- Persistência de dados com banco de dados locais SQLite
- Integração com a **API do Google Tasks** para criar tarefas a partir dos cards

## <div id="desafio">Desafio</div>

Board para Gerenciamento de Tarefas simples criado a partir de desafio do Bootcamp Bradesco Java DIO. 

O desafio abordou todas as etapas do desenvolvimento, desde o planejamento e estruturação até a implementação de funcionalidades como gerenciamento de dados e integração entre camadas, seguindo boas práticas de programação.

A partir dessa provocação eu expandi e desenvolvi mais funcionalidades para o projeto.

## <div id="migracao">Migração para JPA/Hibernate</div>

O projeto passou por uma refatoração significativa, migrando da persistência manual com JDBC para o uso do **JPA (Jakarta Persistence API)** com a implementação do **Hibernate**. Essa mudança modernizou a camada de dados, trazendo mais robustez, manutenibilidade e produtividade.

### Principais Alterações

- **Mapeamento Objeto-Relacional (ORM)**: As classes de modelo (`BoardEntity`, `CardEntity`, etc.) foram transformadas em entidades JPA com anotações como `@Entity`, `@Id`, `@OneToMany` e `@ManyToOne`. Isso eliminou a necessidade de escrever SQL manualmente para operações CRUD.

- **Camada de Serviço Refatorada**: As classes `BoardService` e `CardService` foram completamente reescritas para utilizar o `EntityManager` do JPA. Toda a lógica de transação (iniciar, comitar, reverter) e operações de persistência (`persist`, `merge`, `remove`, `find`) agora são gerenciadas pelo Hibernate.

- **Configuração Centralizada**: A configuração do banco de dados foi centralizada no arquivo `src/main/resources/META-INF/persistence.xml`, definindo o dialeto do SQLite, o driver e outras propriedades do Hibernate.

- **Gerenciamento de Conexão com `JPAUtil`**: Foi criada a classe `JPAUtil` para gerenciar o ciclo de vida do `EntityManagerFactory` (que é custoso e criado apenas uma vez) e fornecer instâncias do `EntityManager` para cada transação.

- **Localização Dinâmica do Banco de Dados**: A aplicação agora salva o arquivo do banco de dados (`myboard.db`) de forma dinâmica na pasta `MyBoards` dentro do diretório do usuário (ex: `C:\Users\username\MyBoards`), garantindo que os dados não sejam perdidos e que a aplicação seja mais portável.

### Desafios Superados Durante a Migração

A migração para um framework ORM robusto como o Hibernate trouxe desafios de aprendizado que foram superados:

- **`LazyInitializationException`**: Resolvido através do uso de `JOIN FETCH` em consultas JPQL para garantir que coleções "preguiçosas" fossem carregadas junto com suas entidades pai antes de a sessão ser fechada.

- **`MultipleBagFetchException`**: Contornado ao implementar uma estratégia de busca em duas etapas, carregando primeiro a coleção principal e, em uma segunda consulta, as coleções aninhadas, evitando o "produto cartesiano" indesejado.

- **`orphanRemoval`**: O comportamento de exclusão inesperada de cards foi corrigido ajustando a lógica de negócio para modificar apenas o lado "dono" (`@ManyToOne`) da relação, permitindo que o Hibernate gerencie a sincronização das coleções corretamente.

### Benefícios Obtidos

- **Redução de Código Boilerplate**: Eliminação de blocos `try-catch-finally` para gerenciamento de `Connection`, `Statement` e `ResultSet`.
- **Código Mais Legível e Declarativo**: A lógica de persistência se tornou mais clara e focada no modelo de domínio.
- **Segurança e Integridade**: Gerenciamento de transações mais seguro e explícito.
- **Independência de Banco de Dados**: Embora o projeto use SQLite, a arquitetura agora facilita a troca para outro banco de dados com alterações mínimas.

## <div id="diagrama">Diagrama UML inicial</div>

```mermaid
classDiagram
class Board {
+Long id
+String name
}
class BoardColumn {
    +Long id
    +String name
    +Integer order
    +String kind
    +Long boardId
}
class Card {
    +Long id
    +String title
    +String description
    +Long boardColumnId
}
class Block {
    +Long id
    +DateTime blockedAt
    +String blockReason
    +DateTime unblockedAt
    +String unblockReason
    +Long cardId
}
class BoardRepository {
    <<interface>>
    +Board findById(Long id)
    +List<Board> findAll()
    +void save(Board board)
    +void delete(Board board)
}
class BoardColumnRepository {
    <<interface>>
    +BoardColumn findById(Long id)
    +List<BoardColumn> findByBoardIdOrderByOrder(Long boardId)
    +void save(BoardColumn boardColumn)
    +void delete(BoardColumn boardColumn)
}
class CardRepository {
    <<interface>>
    +Card findById(Long id)
    +List<Card> findByBoardColumnId(Long boardColumnId)
    +void save(Card card)
    +void delete(Card card)
}
class BlockRepository {
    <<interface>>
    +Block findById(Long id)
    +List<Block> findByCardId(Long cardId)
    +void save(Block block)
}
Board "1" -- "*" BoardColumn : has
BoardColumn "1" -- "*" Card : has
Card "1" -- "*" Block : has
Board --|> BoardRepository : uses
BoardColumn --|> BoardColumnRepository : uses
Card --|> CardRepository : uses
Block --|> BlockRepository : uses
```
