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
  <a href="#requisitos">Requisitos</a>&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;
  <a href="#sugestoes">Sugestões de buscas relacionadas</a>&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;
  <a href="#diagrama">Diagrama UML inicial</a>
</p>

## <div id="tecnologias">🚀 Tecnologias</div>

Esse projeto foi desenvolvido com as seguintes tecnologias:

- ☕ Java
- 💾 Sqlite ~~Mysql~~
- 🖥️ JavaFX

## <div id="funcionalidades">Funcionalidades</div>

- Criar e excluir board de tarefas
- Criar cards
- Editar título e descrição dos cards com duplo clique do mouse
- Mover cards entre as colunas não iniciado, em andamento e concluído usando o drag and drop

## <div id="desafio">Desafio</div>

Board para Gerenciamento de Tarefas simples criado a partir de desafio do Bootcamp Bradesco Java DIO. 

O desafio abordou todas as etapas do desenvolvimento, desde o planejamento e estruturação até a implementação de funcionalidades como gerenciamento de dados e integração entre camadas, seguindo boas práticas de programação.

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
