package org.desviante.service;

import org.desviante.exception.ResourceNotFoundException;
import org.desviante.model.BoardColumn;
import org.desviante.model.Card;
import org.desviante.model.enums.CardType;
import org.desviante.repository.BoardColumnRepository;
import org.desviante.repository.CardRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para o CardService.
 * 
 * <p>Testa as operações de negócio relacionadas aos cards, incluindo criação,
 * atualização, movimentação e remoção. Foca na validação de regras de negócio
 * e tratamento de exceções.</p>
 * 
 * <p>Progresso e status estão desacoplados: os testes validam que o progresso
 * é independente da coluna onde o card está localizado.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see CardService
 * @see Card
 * @see BoardColumn
 */
@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private BoardColumnRepository columnRepository;

    @InjectMocks
    private CardService cardService;

    @Test
    @DisplayName("Deve criar um card com sucesso quando a coluna pai existe")
    void shouldCreateCardSuccessfully() {
        // Arrange
        String title = "Test Card";
        String description = "Test Description";
        Long parentColumnId = 1L;
        CardType type = CardType.CARD;

        BoardColumn parentColumn = new BoardColumn();
        parentColumn.setId(parentColumnId);

        when(columnRepository.findById(parentColumnId)).thenReturn(Optional.of(parentColumn));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            card.setId(1L);
            return card;
        });

        // Act
        Card result = cardService.createCard(title, description, parentColumnId, type);

        // Assert
        assertNotNull(result);
        assertEquals(title, result.getTitle());
        assertEquals(description, result.getDescription());
        assertEquals(parentColumnId, result.getBoardColumnId());
        assertEquals(type, result.getType());
        assertNotNull(result.getCreationDate());
        assertNotNull(result.getLastUpdateDate());
        assertNull(result.getCompletionDate());
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException se a coluna pai não for encontrada")
    void shouldThrowExceptionWhenColumnNotFound() {
        // Arrange
        String title = "Test Card";
        String description = "Test Description";
        Long parentColumnId = 999L;
        CardType type = CardType.CARD;

        when(columnRepository.findById(parentColumnId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> cardService.createCard(title, description, parentColumnId, type));

        assertEquals("Coluna com ID 999 não encontrada.", exception.getMessage());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    @DisplayName("Deve deletar um card com sucesso quando o card existe")
    void shouldDeleteCardSuccessfully() {
        // Arrange
        Long cardId = 1L;
        Card existingCard = new Card();
        existingCard.setId(cardId);
        existingCard.setTitle("Test Card");

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
        doNothing().when(cardRepository).deleteById(cardId);

        // Act
        cardService.deleteCard(cardId);

        // Assert
        verify(cardRepository).findById(cardId);
        verify(cardRepository).deleteById(cardId);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao tentar deletar um card inexistente")
    void shouldThrowExceptionWhenDeletingNonExistentCard() {
        // Arrange
        Long cardId = 999L;

        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> cardService.deleteCard(cardId));

        assertEquals("Card com ID 999 não encontrado para deleção.", exception.getMessage());
        verify(cardRepository).findById(cardId);
        verify(cardRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando cardId é null")
    void shouldThrowExceptionWhenCardIdIsNull() {
        // Arrange
        Long cardId = null;

        when(cardRepository.findById(null)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> cardService.deleteCard(cardId));

        assertEquals("Card com ID null não encontrado para deleção.", exception.getMessage());
        verify(cardRepository).findById(null);
        verify(cardRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando cardId é null - cenário de borda")
    void shouldThrowExceptionWhenCardIdIsNull_EdgeCase() {
        // Arrange
        Long cardId = null;

        when(cardRepository.findById(null)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> cardService.deleteCard(cardId));

        assertEquals("Card com ID null não encontrado para deleção.", exception.getMessage());
        verify(cardRepository).findById(null);
        verify(cardRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve atualizar um card com sucesso")
    void shouldUpdateCardDetailsSuccessfully() {
        // Arrange
        Long cardId = 1L;
        String newTitle = "Updated Title";
        String newDescription = "Updated Description";

        Card existingCard = new Card();
        existingCard.setId(cardId);
        existingCard.setTitle("Old Title");
        existingCard.setDescription("Old Description");
        existingCard.setLastUpdateDate(LocalDateTime.now().minusDays(1));

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Card result = cardService.updateCardDetails(cardId, newTitle, newDescription);

        // Assert
        assertNotNull(result);
        assertEquals(newTitle, result.getTitle());
        assertEquals(newDescription, result.getDescription());
        assertNotNull(result.getLastUpdateDate());
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao tentar atualizar um card inexistente")
    void shouldThrowExceptionWhenUpdatingNonExistentCard() {
        // Arrange
        Long cardId = 999L;
        String newTitle = "Updated Title";
        String newDescription = "Updated Description";

        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> cardService.updateCardDetails(cardId, newTitle, newDescription));

        assertEquals("Card com ID 999 não encontrado para atualização.", exception.getMessage());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    @DisplayName("Deve criar um card do tipo BOOK com sucesso")
    void shouldCreateBookCardSuccessfully() {
        // Arrange
        String title = "Test Book";
        String description = "Test Book Description";
        Long parentColumnId = 1L;
        CardType type = CardType.BOOK;

        BoardColumn parentColumn = new BoardColumn();
        parentColumn.setId(parentColumnId);

        when(columnRepository.findById(parentColumnId)).thenReturn(Optional.of(parentColumn));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            card.setId(1L);
            return card;
        });

        // Act
        Card result = cardService.createCard(title, description, parentColumnId, type);

        // Assert
        assertNotNull(result);
        assertEquals(title, result.getTitle());
        assertEquals(description, result.getDescription());
        assertEquals(type, result.getType());
        assertEquals(1, result.getTotalUnits());
        assertEquals(0, result.getCurrentUnits());
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    @DisplayName("Deve criar um card do tipo VIDEO com sucesso")
    void shouldCreateVideoCardSuccessfully() {
        // Arrange
        String title = "Test Video";
        String description = "Test Video Description";
        Long parentColumnId = 1L;
        CardType type = CardType.VIDEO;

        BoardColumn parentColumn = new BoardColumn();
        parentColumn.setId(parentColumnId);

        when(columnRepository.findById(parentColumnId)).thenReturn(Optional.of(parentColumn));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            card.setId(1L);
            return card;
        });

        // Act
        Card result = cardService.createCard(title, description, parentColumnId, type);

        // Assert
        assertNotNull(result);
        assertEquals(title, result.getTitle());
        assertEquals(description, result.getDescription());
        assertEquals(type, result.getType());
        assertEquals(1, result.getTotalUnits());
        assertEquals(0, result.getCurrentUnits());
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    @DisplayName("Deve criar um card do tipo COURSE com sucesso")
    void shouldCreateCourseCardSuccessfully() {
        // Arrange
        String title = "Test Course";
        String description = "Test Course Description";
        Long parentColumnId = 1L;
        CardType type = CardType.COURSE;

        BoardColumn parentColumn = new BoardColumn();
        parentColumn.setId(parentColumnId);

        when(columnRepository.findById(parentColumnId)).thenReturn(Optional.of(parentColumn));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            card.setId(1L);
            return card;
        });

        // Act
        Card result = cardService.createCard(title, description, parentColumnId, type);

        // Assert
        assertNotNull(result);
        assertEquals(title, result.getTitle());
        assertEquals(description, result.getDescription());
        assertEquals(type, result.getType());
        assertEquals(1, result.getTotalUnits());
        assertEquals(0, result.getCurrentUnits());
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    @DisplayName("Deve validar que cards do tipo CARD suportam progresso baseado em unidades")
    void shouldValidateCardTypeSupportsProgressBasedOnUnits() {
        // Arrange
        String title = "Test Card";
        String description = "Test Description";
        Long parentColumnId = 1L;
        CardType type = CardType.CARD;

        BoardColumn parentColumn = new BoardColumn();
        parentColumn.setId(parentColumnId);

        when(columnRepository.findById(parentColumnId)).thenReturn(Optional.of(parentColumn));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            card.setId(1L);
            return card;
        });

        // Act
        Card result = cardService.createCard(title, description, parentColumnId, type);

        // Assert
        assertNotNull(result);
        assertTrue(result.isProgressable(), "Cards do tipo CARD devem suportar progresso");
        assertNull(result.getTotalUnits());
        assertNull(result.getCurrentUnits());
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    @DisplayName("Deve calcular progresso corretamente para cards com unidades")
    void shouldCalculateProgressCorrectly() {
        // Arrange
        Card card = new Card();
        card.setTotalUnits(100);
        card.setCurrentUnits(50);

        // Act
        Double progress = card.getProgressPercentage();

        // Assert
        assertEquals(50.0, progress, 0.01);
    }

    @Test
    @DisplayName("Deve retornar 0.0 de progresso quando totalUnits é null")
    void shouldReturnZeroProgressWhenTotalUnitsIsNull() {
        // Arrange
        Card card = new Card();
        card.setTotalUnits(null);
        card.setCurrentUnits(50);

        // Act
        Double progress = card.getProgressPercentage();

        // Assert
        assertEquals(0.0, progress, 0.01);
    }

    @Test
    @DisplayName("Deve retornar 0.0 de progresso quando currentUnits é null")
    void shouldReturnZeroProgressWhenCurrentUnitsIsNull() {
        // Arrange
        Card card = new Card();
        card.setTotalUnits(100);
        card.setCurrentUnits(null);

        // Act
        Double progress = card.getProgressPercentage();

        // Assert
        assertEquals(0.0, progress, 0.01);
    }

    @Test
    @DisplayName("Deve retornar 100.0 de progresso quando currentUnits é maior que totalUnits")
    void shouldReturnMaxProgressWhenCurrentUnitsExceedsTotalUnits() {
        // Arrange
        Card card = new Card();
        card.setTotalUnits(100);
        card.setCurrentUnits(150);

        // Act
        Double progress = card.getProgressPercentage();

        // Assert
        assertEquals(100.0, progress, 0.01);
    }
}
