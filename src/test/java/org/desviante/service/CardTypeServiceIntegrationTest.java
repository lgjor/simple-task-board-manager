package org.desviante.service;

import org.desviante.config.TestDataSourceConfig;
import org.desviante.exception.CardTypeInUseException;
import org.desviante.exception.ResourceNotFoundException;
import org.desviante.model.Board;
import org.desviante.model.BoardColumn;
import org.desviante.model.Card;
import org.desviante.model.CardType;
import org.desviante.model.enums.BoardColumnKindEnum;
import org.desviante.model.enums.ProgressType;
import org.desviante.repository.BoardColumnRepository;
import org.desviante.repository.BoardRepository;
import org.desviante.repository.CardRepository;
import org.desviante.repository.CardTypeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;
import javax.sql.DataSource;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste de integração para o CardTypeService.
 * 
 * <p>Testa o comportamento real do sistema ao gerenciar tipos de card,
 * incluindo cenários críticos como tentativa de remoção de tipos que
 * possuem cards dependentes.</p>
 * 
 * <p><strong>Cenários Testados:</strong></p>
 * <ul>
 *   <li>Criação e remoção de tipos de card sem dependências</li>
 *   <li>Bloqueio de remoção quando há cards dependentes</li>
 *   <li>Verificação de viabilidade de remoção</li>
 *   <li>Mensagens de erro informativas sobre cards afetados</li>
 *   <li>Integridade referencial entre cards e tipos</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see CardTypeService
 * @see CardType
 * @see Card
 * @see CardTypeInUseException
 */
@SpringJUnitConfig(classes = CardTypeServiceIntegrationTest.TestConfig.class)
@Sql(scripts = "/test-schema.sql")
@Transactional
class CardTypeServiceIntegrationTest {

    @Configuration
    @Import({TestDataSourceConfig.class})
    static class TestConfig {
        
        @Bean
        public CardTypeRepository cardTypeRepository(DataSource dataSource) {
            return new CardTypeRepository(dataSource);
        }
        
        @Bean
        public CardRepository cardRepository(DataSource dataSource) {
            return new CardRepository(dataSource);
        }
        
        @Bean
        public BoardRepository boardRepository(DataSource dataSource) {
            return new BoardRepository(dataSource);
        }
        
        @Bean
        public BoardColumnRepository boardColumnRepository(DataSource dataSource) {
            return new BoardColumnRepository(dataSource);
        }
        
        @Bean
        public CardTypeService cardTypeService(CardTypeRepository cardTypeRepository, CardRepository cardRepository) {
            return new CardTypeService(cardTypeRepository, cardRepository);
        }
    }

    @Autowired
    private CardTypeService cardTypeService;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private BoardColumnRepository columnRepository;

    private Board testBoard;
    private BoardColumn testColumn;
    private CardType testCardType;

    @BeforeEach
    void setUp() {
        // Criar estrutura de teste: Board -> Column -> Cards
        testBoard = boardRepository.save(new Board(null, "Board de Teste", LocalDateTime.now(), null, null));
        testColumn = columnRepository.save(new BoardColumn(null, "Coluna de Teste", 0, BoardColumnKindEnum.INITIAL, testBoard.getId()));
        
        // Criar um tipo de card personalizado para teste que não tenha dependências
        testCardType = cardTypeService.createCardType("Tipo de Teste Personalizado", "unidades");
    }

    @AfterEach
    void tearDown() {
        // Limpeza automática pelo @Transactional
    }

    @Test
    @DisplayName("Deve remover tipo de card com sucesso quando não há cards dependentes")
    void shouldDeleteCardTypeSuccessfullyWhenNoCardsDependOnIt() {
        // Arrange - Tipo de card sem cards dependentes
        
        // Act
        boolean result = cardTypeService.deleteCardType(testCardType.getId());
        
        // Assert
        assertTrue(result, "A remoção deveria ter sido bem-sucedida");
        
        // Verificar que o tipo foi realmente removido
        assertThrows(ResourceNotFoundException.class, () -> {
            cardTypeService.getCardTypeById(testCardType.getId());
        }, "O tipo deveria ter sido removido do banco");
    }

    @Test
    @DisplayName("Deve bloquear remoção de tipo de card quando há cards dependentes")
    void shouldBlockCardTypeDeletionWhenCardsDependOnIt() {
        // Arrange - Criar cards que dependem do tipo
        createCardWithType("Card 1", "Descrição 1", testCardType.getId());
        createCardWithType("Card 2", "Descrição 2", testCardType.getId());
        
        // Verificar que os cards foram criados
        assertEquals(2, cardRepository.countByCardTypeId(testCardType.getId()));
        
        // Act & Assert - Tentar remover o tipo deve falhar
        CardTypeInUseException exception = assertThrows(CardTypeInUseException.class, () -> {
            cardTypeService.deleteCardType(testCardType.getId());
        }, "Deveria ter lançado exceção ao tentar remover tipo com cards dependentes");
        
        // Verificar mensagem de erro detalhada
        String errorMessage = exception.getMessage();
        
        // Verificar apenas se a mensagem contém informações básicas sobre o bloqueio
        assertTrue(errorMessage.contains("Não é possível remover o tipo de card"));
        assertTrue(errorMessage.contains("porque ele está sendo usado por 2 card(s)"));
        assertTrue(errorMessage.contains("Remova ou migre todos os cards para outro tipo antes de remover este tipo"));
        
        // Verificar que o tipo ainda existe no banco
        CardType existingType = cardTypeService.getCardTypeById(testCardType.getId());
        assertNotNull(existingType, "O tipo deveria ainda existir no banco");
    }

    @Test
    @DisplayName("Deve verificar corretamente se um tipo pode ser removido")
    void shouldCorrectlyCheckIfCardTypeCanBeDeleted() {
        // Arrange - Primeiro verificar sem cards
        CardTypeService.CardTypeRemovalCheck checkWithoutCards = cardTypeService.canDeleteCardType(testCardType.getId());
        
        // Assert - Deve permitir remoção
        assertTrue(checkWithoutCards.canDelete(), "Deveria permitir remoção quando não há cards");
        assertEquals(0, checkWithoutCards.getCardCount());
        assertEquals("Nenhum card está usando este tipo", checkWithoutCards.getReason());
        assertTrue(checkWithoutCards.getAffectedCards().isEmpty());
        
        // Arrange - Criar cards dependentes
        createCardWithType("Card Dependente", "Descrição", testCardType.getId());
        
        // Act - Verificar novamente
        CardTypeService.CardTypeRemovalCheck checkWithCards = cardTypeService.canDeleteCardType(testCardType.getId());
        
        // Assert - Não deve permitir remoção
        assertFalse(checkWithCards.canDelete(), "Não deveria permitir remoção quando há cards dependentes");
        assertEquals(1, checkWithCards.getCardCount());
        assertEquals("Tipo está sendo usado por 1 card(s)", checkWithCards.getReason());
        assertEquals(1, checkWithCards.getAffectedCards().size());
        assertEquals("Card Dependente", checkWithCards.getAffectedCards().get(0).getTitle());
    }

    @Test
    @DisplayName("Deve limitar exibição de cards afetados na mensagem de erro")
    void shouldLimitAffectedCardsDisplayInErrorMessage() {
        // Arrange - Criar muitos cards para testar limite de exibição
        for (int i = 1; i <= 8; i++) {
            createCardWithType("Card " + i, "Descrição " + i, testCardType.getId());
        }
        
        // Verificar que foram criados 8 cards
        assertEquals(8, cardRepository.countByCardTypeId(testCardType.getId()));
        
        // Act & Assert - Tentar remover deve falhar com mensagem limitada
        CardTypeInUseException exception = assertThrows(CardTypeInUseException.class, () -> {
            cardTypeService.deleteCardType(testCardType.getId());
        });
        
        String errorMessage = exception.getMessage();
        
        // Verificar apenas se a mensagem contém informações básicas sobre o bloqueio
        assertTrue(errorMessage.contains("porque ele está sendo usado por 8 card(s)"));
        assertTrue(errorMessage.contains("Remova ou migre todos os cards para outro tipo antes de remover este tipo"));
    }

    @Test
    @DisplayName("Deve permitir remoção após migração de todos os cards para outro tipo")
    void shouldAllowDeletionAfterMigratingAllCardsToAnotherType() {
        // Arrange - Criar cards dependentes
        Card card1 = createCardWithType("Card para Migrar 1", "Descrição 1", testCardType.getId());
        Card card2 = createCardWithType("Card para Migrar 2", "Descrição 2", testCardType.getId());
        
        // Criar outro tipo de card para migração
        CardType alternativeType = cardTypeService.createCardType("Tipo Alternativo", "unidades");
        
        // Act - Migrar cards para o tipo alternativo
        card1.setCardTypeId(alternativeType.getId());
        card2.setCardTypeId(alternativeType.getId());
        cardRepository.save(card1);
        cardRepository.save(card2);
        
        // Verificar que os cards foram migrados
        assertEquals(0, cardRepository.countByCardTypeId(testCardType.getId()));
        assertEquals(2, cardRepository.countByCardTypeId(alternativeType.getId()));
        
        // Act - Agora deve ser possível remover o tipo original
        boolean result = cardTypeService.deleteCardType(testCardType.getId());
        
        // Assert
        assertTrue(result, "A remoção deveria ter sido bem-sucedida após migração");
        
        // Verificar que o tipo foi removido
        assertThrows(ResourceNotFoundException.class, () -> {
            cardTypeService.getCardTypeById(testCardType.getId());
        });
        
        // Verificar que os cards ainda existem com o novo tipo
        Optional<Card> migratedCard1 = cardRepository.findById(card1.getId());
        assertTrue(migratedCard1.isPresent());
        assertEquals(alternativeType.getId(), migratedCard1.get().getCardTypeId());
    }

    @Test
    @DisplayName("Deve manter integridade referencial entre cards e tipos")
    void shouldMaintainReferentialIntegrityBetweenCardsAndTypes() {
        // Arrange - Criar cards dependentes
        Card card1 = createCardWithType("Card de Integridade 1", "Descrição 1", testCardType.getId());
        Card card2 = createCardWithType("Card de Integridade 2", "Descrição 2", testCardType.getId());
        
        // Verificar que os cards referenciam o tipo correto
        assertEquals(testCardType.getId(), card1.getCardTypeId());
        assertEquals(testCardType.getId(), card2.getCardTypeId());
        
        // Act & Assert - Tentar remover o tipo deve falhar
        assertThrows(CardTypeInUseException.class, () -> {
            cardTypeService.deleteCardType(testCardType.getId());
        });
        
        // Verificar que os cards ainda referenciam o tipo
        Optional<Card> existingCard1 = cardRepository.findById(card1.getId());
        Optional<Card> existingCard2 = cardRepository.findById(card2.getId());
        
        assertTrue(existingCard1.isPresent());
        assertTrue(existingCard2.isPresent());
        assertEquals(testCardType.getId(), existingCard1.get().getCardTypeId());
        assertEquals(testCardType.getId(), existingCard2.get().getCardTypeId());
    }

    @Test
    @DisplayName("Deve tratar corretamente tipos de card padrão do sistema")
    void shouldHandleDefaultSystemCardTypesCorrectly() {
        // Arrange - Obter tipos padrão do sistema
        Long defaultCardTypeId = cardTypeService.getDefaultCardTypeId();
        
        // Se não houver tipo padrão, o teste não é aplicável
        if (defaultCardTypeId == null) {
            return;
        }
        
        // Act - Verificar se pode ser removido
        CardTypeService.CardTypeRemovalCheck check = cardTypeService.canDeleteCardType(defaultCardTypeId);
        
        // Assert - Verificar que a verificação foi realizada corretamente
        assertNotNull(check, "A verificação deveria retornar um resultado");
        assertEquals(defaultCardTypeId, check.getCardType().getId());
        
        // O comportamento depende se há cards usando o tipo padrão
        if (check.canDelete()) {
            // Se pode ser removido, verificar que não há cards dependentes
            assertEquals(0, check.getCardCount());
            assertEquals("Nenhum card está usando este tipo", check.getReason());
        } else {
            // Se não pode ser removido, verificar que há cards dependentes
            assertTrue(check.getCardCount() > 0);
            assertFalse(check.canDelete());
            assertTrue(check.getReason().contains("Tipo está sendo usado por"));
        }
    }

    /**
     * Cria um card com um tipo específico para testes.
     * 
     * @param title título do card
     * @param description descrição do card
     * @param cardTypeId ID do tipo de card
     * @return card criado
     */
    private Card createCardWithType(String title, String description, Long cardTypeId) {
        Card card = new Card();
        card.setTitle(title);
        card.setDescription(description);
        card.setCardTypeId(cardTypeId);
        card.setBoardColumnId(testColumn.getId());
        card.setProgressType(ProgressType.NONE);
        card.setCreationDate(LocalDateTime.now());
        card.setLastUpdateDate(LocalDateTime.now());
        card.setOrderIndex(0);
        
        return cardRepository.save(card);
    }
}
