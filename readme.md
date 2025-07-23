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
  <a href="#testes">Testes</a>
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
- Persistência de dados em um banco de dados em memória H2.
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

- **Gerenciamento do Banco de Dados via Script**: A partir da documentação do Spring Framework, Data Access, capítulo 3: Data Access with JDBCA, optamos por deixar de usar o hibernate, e passamos a adotar um conjuto de classes utilitárias. A inicialização do banco de dados em memória **H2** é gerenciada pelo Spring Boot. Ao iniciar, o Spring detecta e executa automaticamente o arquivo `schema.sql` presente no classpath. Este script é responsável por criar toda a estrutura de tabelas, garantindo um ambiente limpo e consistente a cada execução da aplicação, o que é ideal para desenvolvimento e demonstração.
## <div id="testes">🧪 Testes Implementados</div>

Para garantir a qualidade e a estabilidade do código, o projeto conta com uma suíte de testes que cobre as principais camadas da aplicação, utilizando **JUnit 5** e **Mockito**.

- **Testes de Unidade (Services)**: Focam em validar a lógica de negócio de cada serviço (`BoardService`, `CardService`) de forma isolada. As dependências externas, como os repositórios, são substituídas por *mocks* criados com o Mockito. Isso permite testar regras de negócio específicas (ex: a lógica de conclusão de um card ao ser movido para a coluna "Concluído") sem a necessidade de interagir com o banco de dados.

- **Testes de Integração (Facade e Camada de Persistência)**: Utilizando a anotação `@SpringBootTest`, estes testes carregam o contexto completo do Spring e validam a integração entre as diferentes camadas, desde a `TaskManagerFacade` até a camada de persistência com o banco de dados H2. Eles garantem que as entidades JPA estão corretamente mapeadas, que as consultas dos repositórios funcionam como esperado e que as transações (`@Transactional`) se comportam corretamente. O perfil `test` é ativado para garantir um ambiente de execução controlado e separado.