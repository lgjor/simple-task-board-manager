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
  <a href="#arquitetura">Evolução da Arquitetura</a>&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;
  <a href="#diagrama">Diagrama UML</a>
</p>

## <div id="tecnologias">🚀 Tecnologias</div>

Esse projeto foi desenvolvido com as seguintes tecnologias:

- ☕ Java 21
- 🍃 Spring Framework
- 🐘 JPA / Hibernate
- 💾 Banco de Dados H2
- 🖥️ JavaFX

## <div id="funcionalidades">Funcionalidades</div>

- Criar, editar e excluir boards de tarefas.
- Criar cards dentro das colunas do board.
- Editar título e descrição dos cards com duplo clique (edição in-place).
- Mover cards entre as colunas ("A Fazer", "Em Andamento", "Concluído") com drag and drop.
- Visualizar o progresso do board com percentuais de conclusão.
- Persistência de dados em um banco de dados local H2.
- (Em desenvolvimento) Integração com a API do Google Tasks.

## <div id="arquitetura">Evolução da Arquitetura: De JDBC a Spring + JPA</div>

O projeto passou por grandes refatorações que modernizaram sua arquitetura, aumentando a robustez, a manutenibilidade e o desacoplamento entre as camadas.

### Fase 1: Migração para JPA/Hibernate

Inicialmente, a persistência era feita com JDBC puro. A primeira grande evolução foi a migração para o **JPA (Jakarta Persistence API)** com a implementação do **Hibernate**.

- **Mapeamento Objeto-Relacional (ORM)**: As classes do modelo foram transformadas em entidades JPA com anotações (`@Entity`, `@Id`, `@OneToMany`), eliminando a necessidade de escrever SQL manualmente para operações CRUD.
- **Serviços Transacionais**: As classes de serviço (`BoardService`, `CardService`) foram reescritas para utilizar o `EntityManager` do JPA, que passou a gerenciar as transações e operações de persistência (`persist`, `merge`, `remove`).
- **Benefícios**: Redução drástica de código boilerplate (try-catch-finally, manipulação de `ResultSet`), aumento da legibilidade e facilidade na troca do banco de dados.

### Fase 2: Integração com Spring e UI Moderna

A segunda refatoração introduziu o **Spring Framework** para gerenciamento de dependências e reestruturou a interface gráfica (UI) com JavaFX, seguindo padrões modernos.

- **Injeção de Dependência com Spring**: O Spring agora gerencia o ciclo de vida dos componentes da aplicação (`@Service`, `@Component`). A `TaskManagerFacade`, que orquestra a lógica de negócio, é injetada automaticamente nos controllers da UI com `@Autowired`, eliminando o acoplamento manual.

- **Arquitetura de UI Baseada em Componentes**: A interface foi dividida em componentes FXML reutilizáveis (`card-view.fxml`, `column-view.fxml`), cada um com seu próprio controller. Isso torna a UI mais organizada e fácil de manter.

- **Comunicação Desacoplada na UI**: A comunicação entre os controllers filhos e pais (ex: um card notificando o board sobre uma atualização) é feita através de *callbacks* (usando `BiConsumer`), um padrão que evita dependências diretas e promove o encapsulamento.

- **Melhoria de Experiência do Usuário (UX)**:
    - A edição de cards foi transformada de um dialog pop-up para uma **edição in-place**, permitindo que o usuário altere o título e a descrição diretamente no card com um duplo clique.
    - Um botão "Salvar" explícito foi adicionado para tornar a ação de edição mais intuitiva.
    - A identidade visual dos cards foi aprimorada com CSS para criar uma hierarquia clara entre título, descrição e metadados (datas).