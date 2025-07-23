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
}