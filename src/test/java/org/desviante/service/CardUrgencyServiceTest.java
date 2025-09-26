package org.desviante.service;

import org.desviante.model.Card;
import org.desviante.model.CardType;
import org.desviante.model.enums.ProgressType;
import org.desviante.repository.CardRepository;
import org.desviante.service.CardService;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para funcionalidades de urgência de cards.
 * 
 * <p>Esta classe testa especificamente as funcionalidades relacionadas
 * ao cálculo de urgência e níveis de prioridade dos cards baseados
 * nas datas de vencimento, seguindo os princípios SOLID.</p>
 * 
 * @author Aú Desviante - Lucas Godoy
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CardUrgencyService - Testes de Urgência")
class CardUrgencyServiceTest {

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
    @DisplayName("Deve calcular urgência como NORMAL quando não há data de vencimento")
    void deveCalcularUrgenciaComoNormalQuandoNaoHaDataVencimento() {
        // Arrange
        testCard.setDueDate(null);

        // Act
        int urgencyLevel = testCard.getUrgencyLevel();

        // Assert
        assertThat(urgencyLevel).isEqualTo(0); // NORMAL
    }

    @Test
    @DisplayName("Deve calcular urgência como NORMAL quando vencimento está em mais de 7 dias")
    void deveCalcularUrgenciaComoNormalQuandoVencimentoEmMaisDe7Dias() {
        // Arrange
        testCard.setDueDate(LocalDateTime.now().plusDays(10));

        // Act
        int urgencyLevel = testCard.getUrgencyLevel();

        // Assert
        assertThat(urgencyLevel).isEqualTo(0); // NORMAL
    }

    @Test
    @DisplayName("Deve calcular urgência como BAIXA quando vencimento está entre 2-3 dias")
    void deveCalcularUrgenciaComoBaixaQuandoVencimentoEntre2e3Dias() {
        // Arrange
        testCard.setDueDate(LocalDateTime.now().plusDays(3));

        // Act
        int urgencyLevel = testCard.getUrgencyLevel();

        // Assert
        assertThat(urgencyLevel).isEqualTo(1); // BAIXA
    }

    @Test
    @DisplayName("Deve calcular urgência como MÉDIA quando vencimento está em 1 dia")
    void deveCalcularUrgenciaComoMediaQuandoVencimentoEm1Dia() {
        // Arrange
        testCard.setDueDate(LocalDateTime.now().plusDays(1));

        // Act
        int urgencyLevel = testCard.getUrgencyLevel();

        // Assert
        assertThat(urgencyLevel).isEqualTo(2); // MÉDIA
    }

    @Test
    @DisplayName("Deve calcular urgência como ALTA quando vencimento é hoje")
    void deveCalcularUrgenciaComoAltaQuandoVencimentoEHoje() {
        // Arrange
        testCard.setDueDate(LocalDateTime.now().withHour(23).withMinute(59));

        // Act
        int urgencyLevel = testCard.getUrgencyLevel();

        // Assert
        assertThat(urgencyLevel).isEqualTo(3); // ALTA
    }

    @Test
    @DisplayName("Deve calcular urgência como CRÍTICA quando vencimento já passou")
    void deveCalcularUrgenciaComoCriticaQuandoVencimentoJaPassou() {
        // Arrange
        testCard.setDueDate(LocalDateTime.now().minusDays(1));

        // Act
        int urgencyLevel = testCard.getUrgencyLevel();

        // Assert
        assertThat(urgencyLevel).isEqualTo(4); // CRÍTICA
    }

    @Test
    @DisplayName("Deve identificar card como vencido quando data de vencimento passou")
    void deveIdentificarCardComoVencidoQuandoDataVencimentoPassou() {
        // Arrange
        testCard.setDueDate(LocalDateTime.now().minusDays(1));

        // Act
        boolean isOverdue = testCard.isOverdue();

        // Assert
        assertThat(isOverdue).isTrue();
    }

    @Test
    @DisplayName("Deve identificar card como próximo do vencimento quando está entre 1-3 dias")
    void deveIdentificarCardComoProximoDoVencimento() {
        // Arrange
        testCard.setDueDate(LocalDateTime.now().plusDays(2));

        // Act
        boolean isNearDue = testCard.isNearDue(3);

        // Assert
        assertThat(isNearDue).isTrue();
    }

    @Test
    @DisplayName("Deve retornar cards próximos do vencimento")
    void deveRetornarCardsProximosDoVencimento() {
        // Arrange
        Card card1 = createCardWithDueDate(LocalDateTime.now().plusDays(1), 1L);
        Card card2 = createCardWithDueDate(LocalDateTime.now().plusDays(2), 2L);
        Card card3 = createCardWithDueDate(LocalDateTime.now().plusDays(10), 3L);
        
        when(cardRepository.findNearDue(3)).thenReturn(Arrays.asList(card1, card2));

        // Act
        List<Card> nearDueCards = cardService.getCardsNearDue(3);

        // Assert
        assertThat(nearDueCards).hasSize(2);
        assertThat(nearDueCards).contains(card1, card2);
        assertThat(nearDueCards).doesNotContain(card3);
        
        verify(cardRepository).findNearDue(3);
    }

    @Test
    @DisplayName("Deve retornar cards vencidos")
    void deveRetornarCardsVencidos() {
        // Arrange
        Card card1 = createCardWithDueDate(LocalDateTime.now().minusDays(1), 1L);
        Card card2 = createCardWithDueDate(LocalDateTime.now().minusDays(5), 2L);
        Card card3 = createCardWithDueDate(LocalDateTime.now().plusDays(1), 3L);
        
        when(cardRepository.findOverdue()).thenReturn(Arrays.asList(card1, card2));

        // Act
        List<Card> overdueCards = cardService.getOverdueCards();

        // Assert
        assertThat(overdueCards).hasSize(2);
        assertThat(overdueCards).contains(card1, card2);
        assertThat(overdueCards).doesNotContain(card3);
        
        verify(cardRepository).findOverdue();
    }

    @Test
    @DisplayName("Deve retornar cards por nível de urgência")
    void deveRetornarCardsPorNivelDeUrgencia() {
        // Arrange
        Card card1 = createCardWithDueDate(LocalDateTime.now().withHour(23).withMinute(59), 1L); // ALTA
        Card card2 = createCardWithDueDate(LocalDateTime.now().plusDays(1), 2L); // MÉDIA
        Card card3 = createCardWithDueDate(LocalDateTime.now().plusDays(3), 3L); // BAIXA
        
        when(cardRepository.findByUrgencyLevel(3)).thenReturn(Arrays.asList(card1));
        when(cardRepository.findByUrgencyLevel(2)).thenReturn(Arrays.asList(card2));
        when(cardRepository.findByUrgencyLevel(1)).thenReturn(Arrays.asList(card3));

        // Act
        List<Card> highUrgencyCards = cardService.getCardsByUrgencyLevel(3);
        List<Card> mediumUrgencyCards = cardService.getCardsByUrgencyLevel(2);
        List<Card> lowUrgencyCards = cardService.getCardsByUrgencyLevel(1);

        // Assert
        assertThat(highUrgencyCards).containsExactly(card1);
        assertThat(mediumUrgencyCards).containsExactly(card2);
        assertThat(lowUrgencyCards).containsExactly(card3);
        
        verify(cardRepository).findByUrgencyLevel(3);
        verify(cardRepository).findByUrgencyLevel(2);
        verify(cardRepository).findByUrgencyLevel(1);
    }

    @Test
    @DisplayName("Deve calcular estatísticas de urgência corretamente")
    void deveCalcularEstatisticasDeUrgenciaCorretamente() {
        // Arrange
        Card card1 = createCardWithDueDate(LocalDateTime.now().withHour(23).withMinute(59), 1L); // ALTA
        Card card2 = createCardWithDueDate(LocalDateTime.now().plusDays(1), 2L); // MÉDIA
        Card card3 = createCardWithDueDate(LocalDateTime.now().plusDays(3), 3L); // BAIXA
        Card card4 = createCardWithDueDate(LocalDateTime.now().minusDays(1), 4L); // CRÍTICA
        
        when(cardRepository.findOverdue()).thenReturn(Arrays.asList(card4));
        when(cardRepository.findNearDue(3)).thenReturn(Arrays.asList(card2));
        when(cardRepository.findByUrgencyLevel(1)).thenReturn(Arrays.asList(card3));
        when(cardRepository.findByUrgencyLevel(2)).thenReturn(Arrays.asList(card2));
        when(cardRepository.findByUrgencyLevel(3)).thenReturn(Arrays.asList(card1));

        // Act
        CardService.UrgencyStats stats = cardService.getUrgencyStats();

        // Assert
        assertThat(stats.getOverdueCount()).isEqualTo(1);
        assertThat(stats.getNearDueCount()).isEqualTo(1);
        assertThat(stats.getHighUrgencyCount()).isEqualTo(1);
        assertThat(stats.getMediumUrgencyCount()).isEqualTo(1);
        assertThat(stats.getLowUrgencyCount()).isEqualTo(1);
        assertThat(stats.getTotalUrgentCards()).isEqualTo(3);
        
        verify(cardRepository).findOverdue();
        verify(cardRepository).findNearDue(3);
        verify(cardRepository).findByUrgencyLevel(1);
        verify(cardRepository).findByUrgencyLevel(2);
        verify(cardRepository).findByUrgencyLevel(3);
    }

    private Card createCardWithDueDate(LocalDateTime dueDate, Long id) {
        return Card.builder()
                .id(id)
                .title("Test Card " + id)
                .description("Test Description " + id)
                .cardType(testCardType)
                .totalUnits(1)
                .currentUnits(0)
                .progressType(ProgressType.NONE)
                .creationDate(LocalDateTime.now())
                .lastUpdateDate(LocalDateTime.now())
                .boardColumnId(1L)
                .orderIndex(0)
                .dueDate(dueDate)
                .build();
    }
}
