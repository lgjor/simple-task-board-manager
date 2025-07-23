package org.desviante.repository;

import org.desviante.config.DataConfig;
import org.desviante.model.Board;
import org.desviante.model.BoardColumn;
import org.desviante.model.Card;
import org.desviante.model.enums.BoardColumnKindEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql; // Import necessário
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit; // Import necessário
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(classes = DataConfig.class)
@Sql(scripts = "/test-schema.sql") // CORREÇÃO: Garante que o schema seja criado antes dos testes.
public class CardRepositoryTest {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private BoardColumnRepository columnRepository;

    private BoardColumn testColumn;

    @BeforeEach
    void setup() {
        Board testBoard = boardRepository.save(new Board(null, "Board de Teste", LocalDateTime.now()));
        testColumn = columnRepository.save(new BoardColumn(null, "Coluna de Teste", 0, BoardColumnKindEnum.INITIAL, testBoard.getId()));
    }

    @Test
    @DisplayName("Deve salvar um novo card com todos os dados necessários")
    void save_shouldInsertNewCard() {
        // ARRANGE
        LocalDateTime now = LocalDateTime.now();
        Card newCard = new Card(null, "Novo Card", "Descrição do card", now, now, null, testColumn.getId());

        // ACT
        Card savedCard = cardRepository.save(newCard);

        // ASSERT
        assertNotNull(savedCard);
        assertNotNull(savedCard.getId());
        assertEquals("Novo Card", savedCard.getTitle());
        assertNull(savedCard.getCompletionDate());
        assertEquals(testColumn.getId(), savedCard.getBoardColumnId());

        // ASERÇÃO ROBUSTA PARA DATAS
        assertEquals(now.truncatedTo(ChronoUnit.SECONDS), savedCard.getCreationDate().truncatedTo(ChronoUnit.SECONDS));
        assertEquals(now.truncatedTo(ChronoUnit.SECONDS), savedCard.getLastUpdateDate().truncatedTo(ChronoUnit.SECONDS));
    }

    @Test
    @DisplayName("Deve encontrar um card pelo seu ID")
    void findById_shouldReturnCard_whenExists() {
        // ARRANGE
        Card cardToSave = new Card(null, "Card para Busca", "...", LocalDateTime.now(), LocalDateTime.now(), null, testColumn.getId());
        Card savedCard = cardRepository.save(cardToSave);

        // ACT
        Optional<Card> foundCardOpt = cardRepository.findById(savedCard.getId());

        // ASSERT
        assertTrue(foundCardOpt.isPresent());
        assertEquals(savedCard.getId(), foundCardOpt.get().getId());
    }

    @Test
    @DisplayName("Deve atualizar um card existente, modificando a data de atualização")
    void save_shouldUpdateExistingCard() {
        // ARRANGE
        LocalDateTime creationTime = LocalDateTime.now().minusHours(1);
        Card cardToSave = new Card(null, "Título Original", "...", creationTime, creationTime, null, testColumn.getId());
        Card savedCard = cardRepository.save(cardToSave);

        // ACT
        LocalDateTime finalUpdateTime = LocalDateTime.now();
        savedCard.setTitle("Título Atualizado");
        savedCard.setLastUpdateDate(finalUpdateTime);
        cardRepository.save(savedCard);

        // ASSERT
        Optional<Card> updatedCardOpt = cardRepository.findById(savedCard.getId());
        assertTrue(updatedCardOpt.isPresent());
        Card updatedCard = updatedCardOpt.get();

        assertEquals("Título Atualizado", updatedCard.getTitle());
        // ASERÇÃO ROBUSTA PARA DATAS
        assertEquals(creationTime.truncatedTo(ChronoUnit.SECONDS), updatedCard.getCreationDate().truncatedTo(ChronoUnit.SECONDS), "A data de criação não deve mudar.");
        assertEquals(finalUpdateTime.truncatedTo(ChronoUnit.SECONDS), updatedCard.getLastUpdateDate().truncatedTo(ChronoUnit.SECONDS), "A data de atualização deve ser a mais recente.");
    }

    @Test
    @DisplayName("Deve encontrar todos os cards de uma lista de IDs de colunas")
    void findByBoardColumnIdIn_shouldReturnMatchingCards() {
        // ARRANGE
        LocalDateTime now = LocalDateTime.now();
        cardRepository.save(new Card(null, "Card 1", "...", now, now, null, testColumn.getId()));
        cardRepository.save(new Card(null, "Card 2", "...", now, now, null, testColumn.getId()));

        // ACT
        List<Card> foundCards = cardRepository.findByBoardColumnIdIn(List.of(testColumn.getId()));

        // ASSERT
        assertNotNull(foundCards);
        assertEquals(2, foundCards.size());
    }
}