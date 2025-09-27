package org.desviante.service;

import org.desviante.model.Card;
import org.desviante.model.CardType;
import org.desviante.model.enums.ProgressType;
import org.desviante.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários específicos para funcionalidades de data de vencimento de cards.
 * 
 * <p>Esta classe testa especificamente as funcionalidades relacionadas
 * à data de vencimento dos cards, incluindo validações, cálculos de urgência,
 * consultas e cenários de borda, seguindo os princípios SOLID.</p>
 * 
 * @author Aú Desviante - Lucas Godoy
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CardDueDateService - Testes de Data de Vencimento")
class CardDueDateServiceTest {

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
    @DisplayName("Deve definir data de vencimento com sucesso")
    void deveDefinirDataDeVencimentoComSucesso() {
        // Arrange
        LocalDateTime dueDate = LocalDateTime.now().plusDays(5);
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
    @DisplayName("Deve permitir definir data de vencimento no passado")
    void devePermitirDefinirDataDeVencimentoNoPassado() {
        // Arrange
        LocalDateTime pastDueDate = LocalDateTime.now().minusDays(2);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            card.setLastUpdateDate(LocalDateTime.now());
            return card;
        });

        // Act
        Card result = cardService.setDueDate(1L, pastDueDate);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getDueDate()).isEqualTo(pastDueDate);
        assertThat(result.isOverdue()).isTrue();
        
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(testCard);
    }

    @Test
    @DisplayName("Deve permitir definir data de vencimento exatamente agora")
    void devePermitirDefinirDataDeVencimentoExatamenteAgora() {
        // Arrange
        LocalDateTime nowDueDate = LocalDateTime.now();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            card.setLastUpdateDate(LocalDateTime.now());
            return card;
        });

        // Act
        Card result = cardService.setDueDate(1L, nowDueDate);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getDueDate()).isEqualTo(nowDueDate);
        assertThat(result.getUrgencyLevel()).isEqualTo(3); // ALTA - vence hoje
        
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(testCard);
    }

    @Test
    @DisplayName("Deve permitir limpar data de vencimento")
    void devePermitirLimparDataDeVencimento() {
        // Arrange
        testCard.setDueDate(LocalDateTime.now().plusDays(3));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            card.setLastUpdateDate(LocalDateTime.now());
            return card;
        });

        // Act
        Card result = cardService.setDueDate(1L, null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getDueDate()).isNull();
        assertThat(result.getUrgencyLevel()).isEqualTo(0); // NORMAL - sem data de vencimento
        
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(testCard);
    }

    @Test
    @DisplayName("Deve lançar exceção quando card não for encontrado")
    void deveLancarExcecaoQuandoCardNaoForEncontrado() {
        // Arrange
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> cardService.setDueDate(1L, LocalDateTime.now()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Card com ID 1 não encontrado");
        
        verify(cardRepository).findById(1L);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando data de vencimento for anterior à data de agendamento")
    void deveLancarExcecaoQuandoDataVencimentoForAnteriorAoAgendamento() {
        // Arrange
        LocalDateTime scheduledDate = LocalDateTime.now().plusDays(3);
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
    @DisplayName("Deve permitir data de vencimento igual à data de agendamento")
    void devePermitirDataVencimentoIgualADataAgendamento() {
        // Arrange
        LocalDateTime scheduledDate = LocalDateTime.now().plusDays(2);
        LocalDateTime dueDate = LocalDateTime.now().plusDays(2); // Igual ao agendamento
        testCard.setScheduledDate(scheduledDate);
        
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
        assertThat(result.getScheduledDate()).isEqualTo(scheduledDate);
        
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(testCard);
    }

    @Test
    @DisplayName("Deve permitir data de vencimento posterior à data de agendamento")
    void devePermitirDataVencimentoPosteriorADataAgendamento() {
        // Arrange
        LocalDateTime scheduledDate = LocalDateTime.now().plusDays(1);
        LocalDateTime dueDate = LocalDateTime.now().plusDays(3); // Posterior ao agendamento
        testCard.setScheduledDate(scheduledDate);
        
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
        assertThat(result.getScheduledDate()).isEqualTo(scheduledDate);
        
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(testCard);
    }

    @Test
    @DisplayName("Deve permitir definir data de vencimento quando não há data de agendamento")
    void devePermitirDefinirDataVencimentoQuandoNaoHaDataAgendamento() {
        // Arrange
        LocalDateTime dueDate = LocalDateTime.now().plusDays(5);
        // testCard não tem scheduledDate definido (null)
        
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
        assertThat(result.getScheduledDate()).isNull();
        
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(testCard);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7, 14, 30, 365})
    @DisplayName("Deve calcular urgência corretamente para diferentes dias até vencimento")
    void deveCalcularUrgenciaCorretamenteParaDiferentesDias(int daysUntilDue) {
        // Arrange
        LocalDateTime dueDate = LocalDateTime.now().plusDays(daysUntilDue);
        testCard.setDueDate(dueDate);

        // Act
        int urgencyLevel = testCard.getUrgencyLevel();

        // Assert
        if (daysUntilDue < 0) {
            assertThat(urgencyLevel).isEqualTo(4); // CRÍTICA - vencido
        } else if (daysUntilDue == 0) {
            assertThat(urgencyLevel).isEqualTo(3); // ALTA - vence hoje
        } else if (daysUntilDue == 1) {
            assertThat(urgencyLevel).isEqualTo(2); // MÉDIA - vence em 1 dia
        } else if (daysUntilDue <= 3) {
            assertThat(urgencyLevel).isEqualTo(1); // BAIXA - vence em 2-3 dias
        } else {
            assertThat(urgencyLevel).isEqualTo(0); // NORMAL - vence em mais de 3 dias
        }
    }

    @Test
    @DisplayName("Deve identificar card como vencido corretamente")
    void deveIdentificarCardComoVencidoCorretamente() {
        // Arrange
        testCard.setDueDate(LocalDateTime.now().minusDays(1));
        testCard.setCompletionDate(null); // Não concluído

        // Act
        boolean isOverdue = testCard.isOverdue();

        // Assert
        assertThat(isOverdue).isTrue();
    }

    @Test
    @DisplayName("Deve identificar card como não vencido quando já foi concluído")
    void deveIdentificarCardComoNaoVencidoQuandoJaFoiConcluido() {
        // Arrange
        testCard.setDueDate(LocalDateTime.now().minusDays(1));
        testCard.setCompletionDate(LocalDateTime.now().minusHours(1)); // Já concluído

        // Act
        boolean isOverdue = testCard.isOverdue();

        // Assert
        assertThat(isOverdue).isFalse();
    }

    @Test
    @DisplayName("Deve identificar card como não vencido quando não há data de vencimento")
    void deveIdentificarCardComoNaoVencidoQuandoNaoHaDataVencimento() {
        // Arrange
        testCard.setDueDate(null);

        // Act
        boolean isOverdue = testCard.isOverdue();

        // Assert
        assertThat(isOverdue).isFalse();
    }

    @Test
    @DisplayName("Deve retornar cards vencidos corretamente")
    void deveRetornarCardsVencidosCorretamente() {
        // Arrange
        Card overdueCard1 = createCardWithDueDate(LocalDateTime.now().minusDays(1), 1L);
        Card overdueCard2 = createCardWithDueDate(LocalDateTime.now().minusDays(5), 2L);
        Card notOverdueCard = createCardWithDueDate(LocalDateTime.now().plusDays(1), 3L);
        
        when(cardRepository.findOverdue()).thenReturn(Arrays.asList(overdueCard1, overdueCard2));

        // Act
        List<Card> overdueCards = cardService.getOverdueCards();

        // Assert
        assertThat(overdueCards).hasSize(2);
        assertThat(overdueCards).contains(overdueCard1, overdueCard2);
        assertThat(overdueCards).doesNotContain(notOverdueCard);
        
        verify(cardRepository).findOverdue();
    }

    @Test
    @DisplayName("Deve retornar cards próximos do vencimento corretamente")
    void deveRetornarCardsProximosDoVencimentoCorretamente() {
        // Arrange
        Card nearDueCard1 = createCardWithDueDate(LocalDateTime.now().plusDays(1), 1L);
        Card nearDueCard2 = createCardWithDueDate(LocalDateTime.now().plusDays(2), 2L);
        Card notNearDueCard = createCardWithDueDate(LocalDateTime.now().plusDays(10), 3L);
        
        when(cardRepository.findNearDue(3)).thenReturn(Arrays.asList(nearDueCard1, nearDueCard2));

        // Act
        List<Card> nearDueCards = cardService.getCardsNearDue(3);

        // Assert
        assertThat(nearDueCards).hasSize(2);
        assertThat(nearDueCards).contains(nearDueCard1, nearDueCard2);
        assertThat(nearDueCards).doesNotContain(notNearDueCard);
        
        verify(cardRepository).findNearDue(3);
    }

    @Test
    @DisplayName("Deve retornar cards por nível de urgência corretamente")
    void deveRetornarCardsPorNivelDeUrgenciaCorretamente() {
        // Arrange
        Card highUrgencyCard = createCardWithDueDate(LocalDateTime.now().withHour(23).withMinute(59), 1L);
        Card mediumUrgencyCard = createCardWithDueDate(LocalDateTime.now().plusDays(1), 2L);
        Card lowUrgencyCard = createCardWithDueDate(LocalDateTime.now().plusDays(3), 3L);
        
        when(cardRepository.findByUrgencyLevel(3)).thenReturn(Arrays.asList(highUrgencyCard));
        when(cardRepository.findByUrgencyLevel(2)).thenReturn(Arrays.asList(mediumUrgencyCard));
        when(cardRepository.findByUrgencyLevel(1)).thenReturn(Arrays.asList(lowUrgencyCard));

        // Act
        List<Card> highUrgencyCards = cardService.getCardsByUrgencyLevel(3);
        List<Card> mediumUrgencyCards = cardService.getCardsByUrgencyLevel(2);
        List<Card> lowUrgencyCards = cardService.getCardsByUrgencyLevel(1);

        // Assert
        assertThat(highUrgencyCards).containsExactly(highUrgencyCard);
        assertThat(mediumUrgencyCards).containsExactly(mediumUrgencyCard);
        assertThat(lowUrgencyCards).containsExactly(lowUrgencyCard);
        
        verify(cardRepository).findByUrgencyLevel(3);
        verify(cardRepository).findByUrgencyLevel(2);
        verify(cardRepository).findByUrgencyLevel(1);
    }

    @Test
    @DisplayName("Deve calcular estatísticas de urgência baseadas em data de vencimento")
    void deveCalcularEstatisticasDeUrgenciaBaseadasEmDataVencimento() {
        // Arrange
        Card overdueCard = createCardWithDueDate(LocalDateTime.now().minusDays(1), 1L);
        Card nearDueCard = createCardWithDueDate(LocalDateTime.now().plusDays(1), 2L);
        Card highUrgencyCard = createCardWithDueDate(LocalDateTime.now().withHour(23).withMinute(59), 3L);
        Card mediumUrgencyCard = createCardWithDueDate(LocalDateTime.now().plusDays(1), 4L);
        Card lowUrgencyCard = createCardWithDueDate(LocalDateTime.now().plusDays(3), 5L);
        
        when(cardRepository.findOverdue()).thenReturn(Arrays.asList(overdueCard));
        when(cardRepository.findNearDue(3)).thenReturn(Arrays.asList(nearDueCard));
        when(cardRepository.findByUrgencyLevel(1)).thenReturn(Arrays.asList(lowUrgencyCard));
        when(cardRepository.findByUrgencyLevel(2)).thenReturn(Arrays.asList(mediumUrgencyCard));
        when(cardRepository.findByUrgencyLevel(3)).thenReturn(Arrays.asList(highUrgencyCard));

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

    @Test
    @DisplayName("Deve lidar com diferentes horários do dia para data de vencimento")
    void deveLidarComDiferentesHorariosDoDiaParaDataVencimento() {
        // Arrange
        LocalDateTime earlyMorning = LocalDateTime.now().withHour(6).withMinute(0);
        LocalDateTime noon = LocalDateTime.now().withHour(12).withMinute(0);
        LocalDateTime evening = LocalDateTime.now().withHour(18).withMinute(30);
        LocalDateTime lateNight = LocalDateTime.now().withHour(23).withMinute(59);
        
        testCard.setDueDate(earlyMorning);
        int urgencyEarly = testCard.getUrgencyLevel();
        
        testCard.setDueDate(noon);
        int urgencyNoon = testCard.getUrgencyLevel();
        
        testCard.setDueDate(evening);
        int urgencyEvening = testCard.getUrgencyLevel();
        
        testCard.setDueDate(lateNight);
        int urgencyLate = testCard.getUrgencyLevel();

        // Assert - Todos devem ter o mesmo nível de urgência pois são no mesmo dia
        assertThat(urgencyEarly).isEqualTo(urgencyNoon);
        assertThat(urgencyNoon).isEqualTo(urgencyEvening);
        assertThat(urgencyEvening).isEqualTo(urgencyLate);
        assertThat(urgencyLate).isEqualTo(3); // ALTA - vence hoje
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
