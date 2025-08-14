package org.desviante.service;

import org.desviante.exception.ResourceNotFoundException;
import org.desviante.model.Board;
import org.desviante.model.BoardColumn;
import org.desviante.model.enums.BoardColumnKindEnum;
import org.desviante.repository.BoardColumnRepository;
import org.desviante.repository.BoardRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime; // Import necessário
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para o BoardColumnService.
 * 
 * <p>Testa as operações de negócio relacionadas às colunas de quadro,
 * incluindo criação de colunas e validação de relacionamentos com boards.</p>
 * 
 * <p>Foca na validação de regras de negócio, tratamento de exceções e
 * verificação de integridade referencial entre colunas e boards.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see BoardColumnService
 * @see BoardColumn
 * @see Board
 */
@ExtendWith(MockitoExtension.class)
class BoardColumnServiceTest {

    @Mock
    private BoardColumnRepository columnRepository;

    @Mock
    private BoardRepository boardRepository;

    @InjectMocks
    private BoardColumnService columnService;

    @Test
    @DisplayName("Deve criar uma coluna com sucesso quando o board pai existe")
    void shouldCreateColumnSuccessfully() {
        // Arrange
        long boardId = 1L;
        // CORREÇÃO: Usamos o construtor correto que inclui a data de criação.
        Board parentBoard = new Board(boardId, "Board Pai", LocalDateTime.now(), null, null);

        // Mocking: Fingir que o board pai foi encontrado.
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(parentBoard));
        // Mocking: Fingir que o salvamento da coluna funciona.
        when(columnRepository.save(any(BoardColumn.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        BoardColumn result = columnService.createColumn("To Do", 0, BoardColumnKindEnum.INITIAL, boardId);

        // Assert
        assertNotNull(result);
        assertEquals("To Do", result.getName());
        assertEquals(boardId, result.getBoardId());

        // Verify
        verify(boardRepository).findById(boardId); // Verifica se a validação foi chamada.
        verify(columnRepository).save(any(BoardColumn.class)); // Verifica se o salvamento foi chamado.
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao tentar criar coluna para um board inexistente")
    void shouldThrowResourceNotFoundExceptionWhenBoardDoesNotExist() {
        // Arrange
        long nonExistentBoardId = 999L;
        when(boardRepository.findById(nonExistentBoardId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            columnService.createColumn("To Do", 0, BoardColumnKindEnum.INITIAL, nonExistentBoardId);
        });

        // Verify
        verify(boardRepository).findById(nonExistentBoardId); // Verifica se a validação foi chamada.
        verify(columnRepository, never()).save(any()); // Verifica se o salvamento NÃO foi chamado.
    }
}
