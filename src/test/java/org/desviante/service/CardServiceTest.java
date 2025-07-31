package org.desviante.service;

import org.desviante.exception.ResourceNotFoundException;
import org.desviante.model.BoardColumn;
import org.desviante.model.Card;
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
        long columnId = 1L;
        String title = "Novo Card";
        String description = "Descrição do card";

        // Mocking behavior
        // 1. When the column repository is checked, pretend the column exists.
        when(columnRepository.findById(columnId)).thenReturn(Optional.of(new BoardColumn()));
        // 2. When the card repository saves any card, return it.
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        // CORRECTION: Arguments are now in the correct order (title, description, columnId)
        Card result = cardService.createCard(title, description, columnId);

        // Assert
        assertNotNull(result);
        assertEquals(title, result.getTitle());
        assertEquals(description, result.getDescription());
        assertEquals(columnId, result.getBoardColumnId());

        // Verify that the save method was called
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException se a coluna pai não for encontrada")
    void shouldThrowExceptionWhenColumnNotFound() {
        // Arrange
        long nonExistentColumnId = 99L;
        String title = "Título";
        String description = "Descrição";

        // Mocking behavior: pretend the column does not exist.
        when(columnRepository.findById(nonExistentColumnId)).thenReturn(Optional.empty());

        // Act & Assert
        // CORRECTION: Arguments are in the correct order
        assertThrows(ResourceNotFoundException.class,
                () -> cardService.createCard(title, description, nonExistentColumnId));

        // Verify that the save method was never called, preventing orphaned data
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    @DisplayName("Deve deletar um card com sucesso quando o card existe")
    void shouldDeleteCardSuccessfully() {
        // Arrange
        Long cardId = 1L;
        Card cardToDelete = new Card();
        cardToDelete.setId(cardId);
        cardToDelete.setTitle("Card para Deletar");
        cardToDelete.setDescription("Descrição do card");
        cardToDelete.setBoardColumnId(1L);
        cardToDelete.setCreationDate(LocalDateTime.now());
        cardToDelete.setLastUpdateDate(LocalDateTime.now());

        // Mocking behavior: pretend the card exists
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(cardToDelete));
        // Mocking behavior: pretend the delete operation succeeds
        doNothing().when(cardRepository).deleteById(cardId);

        // Act
        cardService.deleteCard(cardId);

        // Assert
        // Verify that findById was called to check if card exists
        verify(cardRepository).findById(cardId);
        // Verify that deleteById was called
        verify(cardRepository).deleteById(cardId);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao tentar deletar um card inexistente")
    void shouldThrowExceptionWhenDeletingNonExistentCard() {
        // Arrange
        Long nonExistentCardId = 99L;

        // Mocking behavior: pretend the card does not exist
        when(cardRepository.findById(nonExistentCardId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> cardService.deleteCard(nonExistentCardId));

        // Verify that findById was called but deleteById was never called
        verify(cardRepository).findById(nonExistentCardId);
        verify(cardRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando cardId é null")
    void shouldThrowExceptionWhenCardIdIsNull() {
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class, 
                () -> cardService.deleteCard(null),
                "Deve lançar ResourceNotFoundException quando cardId é null"
        );

        // Verify that findById was called (the method checks for existence even with null)
        verify(cardRepository).findById(null);
        verify(cardRepository, never()).deleteById(any());

        // Verify the exception message contains appropriate information
        assertTrue(exception.getMessage().contains("não encontrado"), 
                "A mensagem de erro deve indicar que o card não foi encontrado.");
        assertTrue(exception.getMessage().contains("null"), 
                "A mensagem de erro deve mencionar que o ID é null.");
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando cardId é null - cenário de borda")
    void shouldThrowExceptionWhenCardIdIsNull_EdgeCase() {
        // Arrange - Mock behavior for null ID
        when(cardRepository.findById(null)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class, 
                () -> cardService.deleteCard(null),
                "Deve lançar ResourceNotFoundException mesmo com mock configurado para null"
        );

        // Verify the exact behavior
        verify(cardRepository).findById(null);
        verify(cardRepository, never()).deleteById(any());

        // Verify exception details
        assertNotNull(exception.getMessage(), "A exceção deve ter uma mensagem.");
        assertTrue(exception.getMessage().contains("não encontrado"), 
                "A mensagem deve indicar que o card não foi encontrado.");
    }

    @Test
    @DisplayName("Deve atualizar um card com sucesso")
    void shouldUpdateCardDetailsSuccessfully() {
        // Arrange
        Long cardId = 1L;
        String newTitle = "Título Atualizado";
        String newDescription = "Descrição Atualizada";

        Card existingCard = new Card();
        existingCard.setId(cardId);
        existingCard.setTitle("Título Antigo");
        existingCard.setDescription("Descrição Antiga");
        existingCard.setBoardColumnId(1L);
        existingCard.setCreationDate(LocalDateTime.now().minusDays(1));
        existingCard.setLastUpdateDate(LocalDateTime.now().minusDays(1));

        // Mocking behavior: pretend the card exists
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Card result = cardService.updateCardDetails(cardId, newTitle, newDescription);

        // Assert
        assertNotNull(result);
        assertEquals(cardId, result.getId());
        assertEquals(newTitle, result.getTitle());
        assertEquals(newDescription, result.getDescription());
        assertEquals(existingCard.getBoardColumnId(), result.getBoardColumnId());
        assertEquals(existingCard.getCreationDate(), result.getCreationDate());
        assertNotNull(result.getLastUpdateDate());

        // Verify that findById and save were called
        verify(cardRepository).findById(cardId);
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao tentar atualizar um card inexistente")
    void shouldThrowExceptionWhenUpdatingNonExistentCard() {
        // Arrange
        Long nonExistentCardId = 99L;
        String newTitle = "Novo Título";
        String newDescription = "Nova Descrição";

        // Mocking behavior: pretend the card does not exist
        when(cardRepository.findById(nonExistentCardId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
                () -> cardService.updateCardDetails(nonExistentCardId, newTitle, newDescription));

        // Verify that findById was called but save was never called
        verify(cardRepository).findById(nonExistentCardId);
        verify(cardRepository, never()).save(any());
    }
}