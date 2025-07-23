package org.desviante.repository;

import org.desviante.config.DataConfig;
import org.desviante.model.Board;
import org.desviante.model.BoardColumn;
import org.desviante.model.Card;
import org.desviante.model.Task;
import org.desviante.model.enums.BoardColumnKindEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql; // Import necessário
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(classes = DataConfig.class)
@Sql(scripts = "/test-schema.sql") // CORREÇÃO: Garante que o schema seja criado antes dos testes.
public class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private BoardColumnRepository columnRepository;
    @Autowired
    private BoardRepository boardRepository;

    private Card testCard;

    @BeforeEach
    void setup() {
        Board testBoard = boardRepository.save(new Board(null, "Board de Teste para Tasks", LocalDateTime.now()));
        BoardColumn testColumn = columnRepository.save(new BoardColumn(null, "Coluna de Teste para Tasks", 0, BoardColumnKindEnum.INITIAL, testBoard.getId()));
        LocalDateTime now = LocalDateTime.now();
        testCard = cardRepository.save(new Card(null, "Card de Teste para Tasks", "Descrição", now, now, null, testColumn.getId()));
    }

    @Test
    @DisplayName("Deve salvar uma nova task associada a um card")
    void save_shouldInsertNewTask() {
        // ARRANGE
        Task newTask = new Task();
        newTask.setTitle("Minha primeira Google Task");
        newTask.setNotes("Algumas notas importantes.");
        newTask.setCardId(testCard.getId());
        newTask.setCreationDate(LocalDateTime.now());
        newTask.setLastUpdateDate(LocalDateTime.now());

        // ACT
        Task savedTask = taskRepository.save(newTask);

        // ASSERT
        assertNotNull(savedTask);
        assertNotNull(savedTask.getId());
        assertEquals("Minha primeira Google Task", savedTask.getTitle());
        assertEquals(testCard.getId(), savedTask.getCardId());
    }

    @Test
    @DisplayName("Deve encontrar uma task pelo seu ID")
    void findById_shouldReturnTask_whenExists() {
        // ARRANGE
        Task taskToSave = new Task();
        taskToSave.setTitle("Task para busca");
        taskToSave.setCardId(testCard.getId());
        taskToSave.setCreationDate(LocalDateTime.now());
        taskToSave.setLastUpdateDate(LocalDateTime.now());
        Task savedTask = taskRepository.save(taskToSave);

        // ACT
        Optional<Task> foundTaskOpt = taskRepository.findById(savedTask.getId());

        // ASSERT
        assertTrue(foundTaskOpt.isPresent());
        assertEquals(savedTask.getId(), foundTaskOpt.get().getId());
    }

    @Test
    @DisplayName("Deve atualizar uma task existente")
    void save_shouldUpdateExistingTask() {
        // ARRANGE
        Task taskToSave = new Task();
        taskToSave.setTitle("Título Original da Task");
        taskToSave.setCardId(testCard.getId());
        taskToSave.setCreationDate(LocalDateTime.now());
        taskToSave.setLastUpdateDate(LocalDateTime.now());
        Task savedTask = taskRepository.save(taskToSave);

        // ACT
        savedTask.setTitle("Título Atualizado da Task");
        savedTask.setSent(true);
        taskRepository.save(savedTask);

        // ASSERT
        Optional<Task> updatedTaskOpt = taskRepository.findById(savedTask.getId());
        assertTrue(updatedTaskOpt.isPresent());
        Task updatedTask = updatedTaskOpt.get();
        assertEquals("Título Atualizado da Task", updatedTask.getTitle());
        assertTrue(updatedTask.isSent());
    }

    @Test
    @DisplayName("Deve deletar uma task pelo seu ID")
    void deleteById_shouldRemoveTask() {
        // ARRANGE
        Task taskToSave = new Task();
        taskToSave.setTitle("Task a ser deletada");
        taskToSave.setCardId(testCard.getId());
        taskToSave.setCreationDate(LocalDateTime.now());
        taskToSave.setLastUpdateDate(LocalDateTime.now());
        Task savedTask = taskRepository.save(taskToSave);
        Long id = savedTask.getId();

        // ACT
        taskRepository.deleteById(id);

        // ASSERT
        Optional<Task> deletedTask = taskRepository.findById(id);
        assertTrue(deletedTask.isEmpty());
    }
}