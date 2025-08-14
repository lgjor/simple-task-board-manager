package org.desviante.service;

import org.desviante.exception.CardTypeInUseException;
import org.desviante.exception.ResourceNotFoundException;
import org.desviante.model.Card;
import org.desviante.model.CardType;
import org.desviante.repository.CardRepository;
import org.desviante.repository.CardTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para o CardTypeService.
 * 
 * <p>Testa as operações de negócio relacionadas aos tipos de card, incluindo
 * criação, atualização, listagem e remoção com verificação de dependências.</p>
 * 
 * <p>Foca na validação de regras de negócio, tratamento de exceções e
 * verificação de integridade referencial antes da remoção de tipos.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see CardTypeService
 * @see CardType
 * @see CardTypeInUseException
 */
@ExtendWith(MockitoExtension.class)
class CardTypeServiceTest {

    @Mock
    private CardTypeRepository cardTypeRepository;

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardTypeService cardTypeService;

    private CardType testCardType;
    private Card testCard;

    @BeforeEach
    void setUp() {
        testCardType = CardType.builder()
                .id(1L)
                .name("Test Type")
                .unitLabel("test")
                .creationDate(LocalDateTime.now())
                .lastUpdateDate(LocalDateTime.now())
                .build();

        testCard = new Card();
        testCard.setId(1L);
        testCard.setTitle("Test Card");
        testCard.setDescription("Test Description");
        testCard.setCardTypeId(1L);
    }

    @Test
    @DisplayName("Deve verificar se um tipo de card pode ser removido quando não há cards dependentes")
    void shouldAllowDeletionWhenNoCardsDependOnType() {
        // Arrange
        when(cardTypeRepository.findById(1L)).thenReturn(Optional.of(testCardType));
        when(cardRepository.existsByCardTypeId(1L)).thenReturn(false);

        // Act
        CardTypeService.CardTypeRemovalCheck result = cardTypeService.canDeleteCardType(1L);

        // Assert
        assertTrue(result.canDelete());
        assertEquals(0, result.getCardCount());
        assertEquals("Nenhum card está usando este tipo", result.getReason());
        assertEquals(testCardType, result.getCardType());
        assertTrue(result.getAffectedCards().isEmpty());

        verify(cardRepository).existsByCardTypeId(1L);
        verify(cardRepository, never()).countByCardTypeId(any());
        verify(cardRepository, never()).findByCardTypeId(any());
    }

    @Test
    @DisplayName("Deve bloquear remoção quando há cards dependentes")
    void shouldBlockDeletionWhenCardsDependOnType() {
        // Arrange
        when(cardTypeRepository.findById(1L)).thenReturn(Optional.of(testCardType));
        when(cardRepository.existsByCardTypeId(1L)).thenReturn(true);
        when(cardRepository.countByCardTypeId(1L)).thenReturn(2);
        when(cardRepository.findByCardTypeId(1L)).thenReturn(Arrays.asList(testCard, testCard));

        // Act
        CardTypeService.CardTypeRemovalCheck result = cardTypeService.canDeleteCardType(1L);

        // Assert
        assertFalse(result.canDelete());
        assertEquals(2, result.getCardCount());
        assertEquals("Tipo está sendo usado por 2 card(s)", result.getReason());
        assertEquals(testCardType, result.getCardType());
        assertEquals(2, result.getAffectedCards().size());

        verify(cardRepository).existsByCardTypeId(1L);
        verify(cardRepository).countByCardTypeId(1L);
        verify(cardRepository).findByCardTypeId(1L);
    }

    @Test
    @DisplayName("Deve remover tipo de card com sucesso quando não há dependências")
    void shouldDeleteCardTypeSuccessfullyWhenNoDependencies() {
        // Arrange
        when(cardTypeRepository.findById(1L)).thenReturn(Optional.of(testCardType));
        when(cardRepository.existsByCardTypeId(1L)).thenReturn(false);
        when(cardTypeRepository.deleteById(1L)).thenReturn(true);

        // Act
        boolean result = cardTypeService.deleteCardType(1L);

        // Assert
        assertTrue(result);
        verify(cardRepository).existsByCardTypeId(1L);
        verify(cardTypeRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Deve lançar CardTypeInUseException ao tentar remover tipo com cards dependentes")
    void shouldThrowCardTypeInUseExceptionWhenDeletingTypeWithDependencies() {
        // Arrange
        when(cardTypeRepository.findById(1L)).thenReturn(Optional.of(testCardType));
        when(cardRepository.existsByCardTypeId(1L)).thenReturn(true);
        when(cardRepository.countByCardTypeId(1L)).thenReturn(1);
        when(cardRepository.findByCardTypeId(1L)).thenReturn(Arrays.asList(testCard));

        // Act & Assert
        CardTypeInUseException exception = assertThrows(CardTypeInUseException.class, () -> {
            cardTypeService.deleteCardType(1L);
        });

        assertTrue(exception.getMessage().contains("Não é possível remover o tipo de card 'Test Type'"));
        assertTrue(exception.getMessage().contains("porque ele está sendo usado por 1 card(s)"));
        assertTrue(exception.getMessage().contains("'Test Card'"));
        assertTrue(exception.getMessage().contains("Remova ou migre todos os cards para outro tipo antes de remover este tipo"));

        verify(cardRepository).existsByCardTypeId(1L);
        verify(cardRepository).countByCardTypeId(1L);
        verify(cardRepository).findByCardTypeId(1L);
        verify(cardTypeRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando tipo não existe")
    void shouldThrowResourceNotFoundExceptionWhenTypeDoesNotExist() {
        // Arrange
        when(cardTypeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            cardTypeService.canDeleteCardType(999L);
        });

        verify(cardRepository, never()).existsByCardTypeId(any());
    }

    @Test
    @DisplayName("Deve limitar a exibição de cards afetados na mensagem de erro")
    void shouldLimitAffectedCardsDisplayInErrorMessage() {
        // Arrange
        List<Card> manyCards = Arrays.asList(
                createCard("Card 1"),
                createCard("Card 2"),
                createCard("Card 3"),
                createCard("Card 4"),
                createCard("Card 5"),
                createCard("Card 6"),
                createCard("Card 7")
        );

        when(cardTypeRepository.findById(1L)).thenReturn(Optional.of(testCardType));
        when(cardRepository.existsByCardTypeId(1L)).thenReturn(true);
        when(cardRepository.countByCardTypeId(1L)).thenReturn(7);
        when(cardRepository.findByCardTypeId(1L)).thenReturn(manyCards);

        // Act & Assert
        CardTypeInUseException exception = assertThrows(CardTypeInUseException.class, () -> {
            cardTypeService.deleteCardType(1L);
        });

        String message = exception.getMessage();
        assertTrue(message.contains("porque ele está sendo usado por 7 card(s)"));
        assertTrue(message.contains("'Card 1', 'Card 2', 'Card 3', 'Card 4', 'Card 5'"));
        assertTrue(message.contains("e mais 2 card(s)"));
        assertTrue(message.contains("Remova ou migre todos os cards para outro tipo antes de remover este tipo"));
    }

    private Card createCard(String title) {
        Card card = new Card();
        card.setId((long) title.hashCode());
        card.setTitle(title);
        card.setDescription("Description for " + title);
        card.setCardTypeId(1L);
        return card;
    }
}