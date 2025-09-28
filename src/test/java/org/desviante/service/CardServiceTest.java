package org.desviante.service;

import org.desviante.calendar.CalendarEventManager;
import org.desviante.exception.ResourceNotFoundException;
import org.desviante.model.BoardColumn;
import org.desviante.model.Card;
import org.desviante.model.CardType;
import org.desviante.model.enums.ProgressType;
import org.desviante.repository.BoardColumnRepository;
import org.desviante.repository.CardRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

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
@MockitoSettings(strictness = Strictness.LENIENT)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private BoardColumnRepository columnRepository;

    @Mock
    private CardTypeService cardTypeService;

    @Mock
    private CalendarEventManager calendarEventManager;

    @Mock
    private CardTypeService customCardTypeService;

    @InjectMocks
    private CardService cardService;

    @Test
    @DisplayName("Deve criar um card com sucesso quando a coluna pai existe")
    void shouldCreateCardSuccessfully() {
        // Arrange
        String title = "Test Card";
        String description = "Test Description";
        Long parentColumnId = 1L;
        Long customTypeId = 1L;

        BoardColumn parentColumn = new BoardColumn();
        parentColumn.setId(parentColumnId);

        CardType cardType = CardType.builder()
                .id(customTypeId)
                .name("CARD")
                .unitLabel("card")
                .build();

        when(columnRepository.findById(parentColumnId)).thenReturn(Optional.of(parentColumn));
        when(customCardTypeService.getCardTypeById(customTypeId)).thenReturn(cardType);
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            card.setId(1L);
            return card;
        });
        when(calendarEventManager.findByRelatedEntity(anyLong(), anyString())).thenReturn(java.util.Collections.emptyList());

        // Act
        Card result = cardService.createCard(title, description, parentColumnId, customTypeId);

        // Assert
        assertNotNull(result);
        assertEquals(title, result.getTitle());
        assertEquals(description, result.getDescription());
        assertEquals(parentColumnId, result.getBoardColumnId());
        assertEquals(customTypeId, result.getCardTypeId());
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
        Long customTypeId = 1L;

        when(columnRepository.findById(parentColumnId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> cardService.createCard(title, description, parentColumnId, customTypeId));

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
        Long customTypeId = 2L;

        BoardColumn parentColumn = new BoardColumn();
        parentColumn.setId(parentColumnId);

        CardType cardType = CardType.builder()
                .id(customTypeId)
                .name("BOOK")
                .unitLabel("páginas")
                .build();

        when(columnRepository.findById(parentColumnId)).thenReturn(Optional.of(parentColumn));
        when(customCardTypeService.getCardTypeById(customTypeId)).thenReturn(cardType);
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            card.setId(1L);
            return card;
        });
        when(calendarEventManager.findByRelatedEntity(anyLong(), anyString())).thenReturn(java.util.Collections.emptyList());

        // Act
        Card result = cardService.createCard(title, description, parentColumnId, customTypeId, ProgressType.PERCENTAGE);

        // Assert
        assertNotNull(result);
        assertEquals(title, result.getTitle());
        assertEquals(description, result.getDescription());
        assertEquals(customTypeId, result.getCardTypeId());
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
        Long customTypeId = 3L;

        BoardColumn parentColumn = new BoardColumn();
        parentColumn.setId(parentColumnId);

        CardType cardType = CardType.builder()
                .id(customTypeId)
                .name("VIDEO")
                .unitLabel("minutos")
                .build();

        when(columnRepository.findById(parentColumnId)).thenReturn(Optional.of(parentColumn));
        when(customCardTypeService.getCardTypeById(customTypeId)).thenReturn(cardType);
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            card.setId(1L);
            return card;
        });
        when(calendarEventManager.findByRelatedEntity(anyLong(), anyString())).thenReturn(java.util.Collections.emptyList());

        // Act
        Card result = cardService.createCard(title, description, parentColumnId, customTypeId, ProgressType.PERCENTAGE);

        // Assert
        assertNotNull(result);
        assertEquals(title, result.getTitle());
        assertEquals(description, result.getDescription());
        assertEquals(customTypeId, result.getCardTypeId());
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
        Long customTypeId = 4L;

        BoardColumn parentColumn = new BoardColumn();
        parentColumn.setId(parentColumnId);

        CardType cardType = CardType.builder()
                .id(customTypeId)
                .name("COURSE")
                .unitLabel("aulas")
                .build();

        when(columnRepository.findById(parentColumnId)).thenReturn(Optional.of(parentColumn));
        when(customCardTypeService.getCardTypeById(customTypeId)).thenReturn(cardType);
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            card.setId(1L);
            return card;
        });
        when(calendarEventManager.findByRelatedEntity(anyLong(), anyString())).thenReturn(java.util.Collections.emptyList());

        // Act
        Card result = cardService.createCard(title, description, parentColumnId, customTypeId, ProgressType.PERCENTAGE);

        // Assert
        assertNotNull(result);
        assertEquals(title, result.getTitle());
        assertEquals(description, result.getDescription());
        assertEquals(customTypeId, result.getCardTypeId());
        assertEquals(1, result.getTotalUnits());
        assertEquals(0, result.getCurrentUnits());
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    @DisplayName("Deve validar que cards do tipo CARD não suportam progresso")
    void shouldValidateCardTypeDoesNotSupportProgress() {
        // Arrange
        String title = "Test Card";
        String description = "Test Description";
        Long parentColumnId = 1L;
        Long customTypeId = 1L;

        BoardColumn parentColumn = new BoardColumn();
        parentColumn.setId(parentColumnId);

        CardType cardType = CardType.builder()
                .id(customTypeId)
                .name("CARD")
                .unitLabel("card")
                .build();

        when(columnRepository.findById(parentColumnId)).thenReturn(Optional.of(parentColumn));
        when(customCardTypeService.getCardTypeById(customTypeId)).thenReturn(cardType);
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            card.setId(1L);
            return card;
        });
        when(calendarEventManager.findByRelatedEntity(anyLong(), anyString())).thenReturn(java.util.Collections.emptyList());

        // Act
        Card result = cardService.createCard(title, description, parentColumnId, customTypeId);

        // Assert
        assertNotNull(result);
        // Cards do tipo CARD não suportam progresso (ProgressType.NONE por padrão)
        assertFalse(result.isProgressable(), "Cards do tipo CARD não devem suportar progresso");
        assertEquals(ProgressType.NONE, result.getProgressTypeOrDefault());
        assertNull(result.getTotalUnits());
        assertNull(result.getCurrentUnits());
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    @DisplayName("Deve calcular progresso corretamente para cards com unidades")
    void shouldCalculateProgressCorrectly() {
        // Arrange
        Card card = new Card();
        CardType cardType = CardType.builder()
                .id(2L)
                .name("BOOK")
                .unitLabel("páginas")
                .build();
        card.setCardType(cardType);
        card.setProgressType(ProgressType.PERCENTAGE);
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
        CardType cardType = CardType.builder()
                .id(2L)
                .name("BOOK")
                .unitLabel("páginas")
                .build();
        card.setCardType(cardType);
        card.setProgressType(ProgressType.PERCENTAGE);
        card.setTotalUnits(100);
        card.setCurrentUnits(150);

        // Act
        Double progress = card.getProgressPercentage();

        // Assert
        assertEquals(100.0, progress, 0.01);
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar card com título vazio")
    void shouldThrowExceptionWhenCreatingCardWithEmptyTitle() {
        // Arrange
        String emptyTitle = "";
        String description = "Test Description";
        Long parentColumnId = 1L;
        Long customTypeId = 1L;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> cardService.createCard(emptyTitle, description, parentColumnId, customTypeId));
        
        assertEquals("Título do card não pode ser vazio", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar card com título nulo")
    void shouldThrowExceptionWhenCreatingCardWithNullTitle() {
        // Arrange
        String nullTitle = null;
        String description = "Test Description";
        Long parentColumnId = 1L;
        Long customTypeId = 1L;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> cardService.createCard(nullTitle, description, parentColumnId, customTypeId));
        
        assertEquals("Título do card não pode ser vazio", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar card com título contendo apenas espaços")
    void shouldThrowExceptionWhenCreatingCardWithWhitespaceOnlyTitle() {
        // Arrange
        String whitespaceTitle = "   ";
        String description = "Test Description";
        Long parentColumnId = 1L;
        Long customTypeId = 1L;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> cardService.createCard(whitespaceTitle, description, parentColumnId, customTypeId));
        
        assertEquals("Título do card não pode ser vazio", exception.getMessage());
    }

    @Test
    @DisplayName("Deve remover evento de calendário quando data de agendamento é definida como null")
    void shouldRemoveCalendarEventWhenScheduledDateIsSetToNull() {
        // Arrange
        Long cardId = 1L;
        LocalDateTime originalScheduledDate = LocalDateTime.now().plusDays(1);
        
        Card card = new Card();
        card.setId(cardId);
        card.setTitle("Test Card");
        card.setScheduledDate(originalScheduledDate); // Card tem data de agendamento
        
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(calendarEventManager.findByRelatedEntity(cardId, "CARD")).thenReturn(java.util.Collections.emptyList());
        
        // Act - Definir data de agendamento como null (remover)
        Card result = cardService.setScheduledDate(cardId, null);
        
        // Assert
        assertNotNull(result);
        assertNull(result.getScheduledDate()); // Data deve ser null
        
        // Verificar se o método de busca de eventos foi chamado
        verify(calendarEventManager, times(1)).findByRelatedEntity(cardId, "CARD");
        verify(cardRepository, times(1)).findById(cardId);
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    @DisplayName("Deve manter evento de calendário quando data de agendamento é alterada para nova data")
    void shouldKeepCalendarEventWhenScheduledDateIsChangedToNewDate() {
        // Arrange
        Long cardId = 1L;
        LocalDateTime originalScheduledDate = LocalDateTime.now().plusDays(1);
        LocalDateTime newScheduledDate = LocalDateTime.now().plusDays(2);
        
        Card card = new Card();
        card.setId(cardId);
        card.setTitle("Test Card");
        card.setScheduledDate(originalScheduledDate);
        
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act - Alterar data de agendamento para nova data
        Card result = cardService.setScheduledDate(cardId, newScheduledDate);
        
        // Assert
        assertNotNull(result);
        assertEquals(newScheduledDate, result.getScheduledDate());
        
        verify(cardRepository, times(1)).findById(cardId);
        verify(cardRepository, times(1)).save(any(Card.class));
    }
}
