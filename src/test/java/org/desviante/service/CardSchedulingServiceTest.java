package org.desviante.service;

import org.desviante.model.Card;
import org.desviante.model.CardType;
import org.desviante.model.enums.ProgressType;
import org.desviante.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para funcionalidades de agendamento de cards.
 * 
 * <p>Esta classe testa especificamente as funcionalidades relacionadas
 * ao agendamento e vencimento de cards, seguindo os princípios SOLID
 * e boas práticas de programação orientada a objetos.</p>
 * 
 * @author Aú Desviante - Lucas Godoy
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CardSchedulingService - Testes de Agendamento")
class CardSchedulingServiceTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardService cardService;

    private Card testCard;
    private CardType testCardType;

    @BeforeEach
    void setUp() {
        // Configurar dados de teste
        testCardType = new CardType();
        testCardType.setId(1L);
        testCardType.setName("TASK");
        testCardType.setUnitLabel("tarefa");

        testCard = Card.builder()
                .id(1L)
                .title("Tarefa de Teste")
                .description("Descrição da tarefa de teste")
                .cardType(testCardType)
                .totalUnits(1)
                .currentUnits(0)
                .progressType(ProgressType.NONE)
                .creationDate(LocalDateTime.now())
                .lastUpdateDate(LocalDateTime.now())
                .boardColumnId(1L)
                .orderIndex(0)
                .build();
    }

    @Test
    @DisplayName("Deve definir data de agendamento com sucesso")
    void deveDefinirDataDeAgendamentoComSucesso() {
        // Arrange
        LocalDateTime scheduledDate = LocalDateTime.now().plusDays(1);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            card.setLastUpdateDate(LocalDateTime.now());
            return card;
        });

        // Act
        Card result = cardService.setScheduledDate(1L, scheduledDate);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getScheduledDate()).isEqualTo(scheduledDate);
        assertThat(result.getLastUpdateDate()).isNotNull();
        
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(testCard);
    }

    @Test
    @DisplayName("Deve definir data de vencimento com sucesso")
    void deveDefinirDataDeVencimentoComSucesso() {
        // Arrange
        LocalDateTime dueDate = LocalDateTime.now().plusDays(3);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            card.setLastUpdateDate(LocalDateTime.now());
            return card;
        });

        // Act
        Card result = cardService.setDueDate(1L, dueDate);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getDueDate()).isEqualTo(dueDate);
        assertThat(result.getLastUpdateDate()).isNotNull();
        
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(testCard);
    }

    @Test
    @DisplayName("Deve definir ambas as datas de agendamento e vencimento com sucesso")
    void deveDefinirAmbasAsDatasComSucesso() {
        // Arrange
        LocalDateTime scheduledDate = LocalDateTime.now().plusDays(1);
        LocalDateTime dueDate = LocalDateTime.now().plusDays(3);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            card.setLastUpdateDate(LocalDateTime.now());
            return card;
        });

        // Act
        Card result = cardService.setSchedulingDates(1L, scheduledDate, dueDate);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getScheduledDate()).isEqualTo(scheduledDate);
        assertThat(result.getDueDate()).isEqualTo(dueDate);
        assertThat(result.getLastUpdateDate()).isNotNull();
        
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(testCard);
    }

    @Test
    @DisplayName("Deve lançar exceção quando card não for encontrado")
    void deveLancarExcecaoQuandoCardNaoForEncontrado() {
        // Arrange
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> cardService.setScheduledDate(1L, LocalDateTime.now()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Card com ID 1 não encontrado");
        
        verify(cardRepository).findById(1L);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando data de vencimento for anterior à data de agendamento")
    void deveLancarExcecaoQuandoDataVencimentoForAnteriorAoAgendamento() {
        // Arrange
        LocalDateTime scheduledDate = LocalDateTime.now().plusDays(2);
        LocalDateTime dueDate = LocalDateTime.now().plusDays(1); // Anterior ao agendamento
        testCard.setScheduledDate(scheduledDate);
        
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // Act & Assert
        assertThatThrownBy(() -> cardService.setDueDate(1L, dueDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Data de vencimento não pode ser anterior à data de agendamento");
        
        verify(cardRepository).findById(1L);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando data de vencimento for anterior à data de agendamento no setSchedulingDates")
    void deveLancarExcecaoQuandoDataVencimentoForAnteriorAoAgendamentoNoSetSchedulingDates() {
        // Arrange
        LocalDateTime scheduledDate = LocalDateTime.now().plusDays(2);
        LocalDateTime dueDate = LocalDateTime.now().plusDays(1); // Anterior ao agendamento
        
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // Act & Assert
        assertThatThrownBy(() -> cardService.setSchedulingDates(1L, scheduledDate, dueDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Data de vencimento não pode ser anterior à data de agendamento");
        
        verify(cardRepository).findById(1L);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    @DisplayName("Deve permitir definir apenas data de agendamento sem data de vencimento")
    void devePermitirDefinirApenasDataDeAgendamento() {
        // Arrange
        LocalDateTime scheduledDate = LocalDateTime.now().plusDays(1);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        // Act
        Card result = cardService.setSchedulingDates(1L, scheduledDate, null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getScheduledDate()).isEqualTo(scheduledDate);
        assertThat(result.getDueDate()).isNull();
        
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(testCard);
    }

    @Test
    @DisplayName("Deve permitir definir apenas data de vencimento sem data de agendamento")
    void devePermitirDefinirApenasDataDeVencimento() {
        // Arrange
        LocalDateTime dueDate = LocalDateTime.now().plusDays(3);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        // Act
        Card result = cardService.setSchedulingDates(1L, null, dueDate);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getScheduledDate()).isNull();
        assertThat(result.getDueDate()).isEqualTo(dueDate);
        
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(testCard);
    }

    @Test
    @DisplayName("Deve permitir limpar data de agendamento definindo como null")
    void devePermitirLimparDataDeAgendamento() {
        // Arrange
        testCard.setScheduledDate(LocalDateTime.now().plusDays(1));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        // Act
        Card result = cardService.setScheduledDate(1L, null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getScheduledDate()).isNull();
        
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(testCard);
    }

    @Test
    @DisplayName("Deve permitir limpar data de vencimento definindo como null")
    void devePermitirLimparDataDeVencimento() {
        // Arrange
        testCard.setDueDate(LocalDateTime.now().plusDays(3));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        // Act
        Card result = cardService.setDueDate(1L, null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getDueDate()).isNull();
        
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(testCard);
    }
}
