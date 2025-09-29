package org.desviante.repository;

import org.desviante.config.TestDataConfig;
import org.desviante.model.Board;
import org.desviante.model.BoardColumn;
import org.desviante.model.Card;
import org.desviante.model.Task;
import org.desviante.model.enums.BoardColumnKindEnum;
import org.desviante.model.CardType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql; // Import necessário
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração para o TaskRepository.
 * 
 * <p>Estes testes verificam as operações CRUD básicas do TaskRepository,
 * incluindo inserção, busca, atualização e exclusão de tarefas. Os testes
 * utilizam um banco de dados em memória configurado especificamente para
 * testes, garantindo isolamento e limpeza automática dos dados.</p>
 * 
 * <p>Características dos testes:</p>
 * <ul>
 *   <li>Utilizam transações que são revertidas automaticamente</li>
 *   <li>Configuram dados de teste antes de cada teste</li>
 *   <li>Limpam dados de teste após cada teste</li>
 *   <li>Verificam tanto casos de sucesso quanto casos de erro</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see TaskRepository
 * @see Task
 * @see Card
 */
@SpringJUnitConfig(classes = TestDataConfig.class)
@Sql(scripts = "/test-schema.sql") // Garante que o schema seja criado antes dos testes.
@Transactional // Garante que cada teste rode em uma transação isolada e seja revertido
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private BoardColumnRepository columnRepository;

    @Autowired
    private CardTypeRepository cardTypeRepository;

    private Task testTask;
    private Card testCard;
    private BoardColumn testColumn;
    private Board testBoard;
    private CardType testCardType;

    @BeforeEach
    void setUp() {
        // Criar estrutura de teste: Board -> Column -> Card -> Task
        testBoard = boardRepository.save(new Board(null, "Board de Teste", LocalDateTime.now(), null, null));
        testColumn = columnRepository.save(new BoardColumn(null, "Coluna de Teste", 0, BoardColumnKindEnum.INITIAL, testBoard.getId()));
        testCardType = cardTypeRepository.save(new CardType(null, "Tipo de Teste", "unidades", LocalDateTime.now(), LocalDateTime.now()));
        
        testCard = new Card();
        testCard.setTitle("Card de Teste");
        testCard.setDescription("Descrição do card de teste");
        testCard.setBoardColumnId(testColumn.getId());
        testCard.setCardTypeId(testCardType.getId());
        testCard.setCreationDate(LocalDateTime.now());
        testCard.setLastUpdateDate(LocalDateTime.now());
        testCard = cardRepository.save(testCard);

        testTask = new Task();
        testTask.setListTitle("Lista de Teste");
        testTask.setTitle("Tarefa de Teste");
        testTask.setNotes("Notas da tarefa de teste");
        testTask.setCardId(testCard.getId());
        testTask.setCreationDate(LocalDateTime.now());
        testTask.setLastUpdateDate(LocalDateTime.now());
    }

    @AfterEach
    void tearDown() {
        // Limpeza automática pelo @Transactional
    }

    @Test
    @DisplayName("Deve salvar uma nova tarefa com sucesso")
    void shouldSaveNewTaskSuccessfully() {
        // Act
        Task savedTask = taskRepository.save(testTask);

        // Assert
        assertNotNull(savedTask);
        assertNotNull(savedTask.getId());
        assertEquals("Tarefa de Teste", savedTask.getTitle());
        assertEquals("Lista de Teste", savedTask.getListTitle());
        assertEquals(testCard.getId(), savedTask.getCardId());
    }

    @Test
    @DisplayName("Deve encontrar uma tarefa pelo ID")
    void shouldFindTaskById() {
        // Arrange
        Task savedTask = taskRepository.save(testTask);

        // Act
        Optional<Task> foundTask = taskRepository.findById(savedTask.getId());

        // Assert
        assertTrue(foundTask.isPresent());
        assertEquals(savedTask.getId(), foundTask.get().getId());
        assertEquals("Tarefa de Teste", foundTask.get().getTitle());
    }

    @Test
    @DisplayName("Deve encontrar todas as tarefas")
    void shouldFindAllTasks() {
        // Arrange
        Task savedTask = taskRepository.save(testTask);

        // Act
        var foundTasks = taskRepository.findAll();

        // Assert
        assertFalse(foundTasks.isEmpty());
        assertTrue(foundTasks.stream().anyMatch(task -> task.getId().equals(savedTask.getId())));
    }

    @Test
    @DisplayName("Deve atualizar uma tarefa existente")
    void shouldUpdateExistingTask() {
        // Arrange
        Task savedTask = taskRepository.save(testTask);
        String newTitle = "Tarefa Atualizada";

        // Act
        savedTask.setTitle(newTitle);
        Task updatedTask = taskRepository.save(savedTask);

        // Assert
        assertEquals(newTitle, updatedTask.getTitle());
        assertEquals(savedTask.getId(), updatedTask.getId());
    }

    @Test
    @DisplayName("Deve deletar uma tarefa com sucesso")
    void shouldDeleteTaskSuccessfully() {
        // Arrange
        Task savedTask = taskRepository.save(testTask);

        // Act
        taskRepository.deleteById(savedTask.getId());

        // Assert
        Optional<Task> foundTask = taskRepository.findById(savedTask.getId());
        assertFalse(foundTask.isPresent());
    }
}
