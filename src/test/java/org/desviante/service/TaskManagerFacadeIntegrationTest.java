package org.desviante.service;

import org.desviante.config.DataConfig;
import org.desviante.config.TestDataSourceConfig;
import org.desviante.exception.ResourceNotFoundException;
import org.desviante.model.Board;
import org.desviante.model.BoardColumn;
import org.desviante.model.BoardGroup;
import org.desviante.model.Card;
import org.desviante.model.enums.BoardColumnKindEnum;
import org.desviante.service.dto.*;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste de integração para a TaskManagerFacade.
 * Valida a orquestração dos serviços e a tradução para DTOs.
 */
@SpringJUnitConfig(classes = TaskManagerFacadeIntegrationTest.TestConfig.class)
@Sql(scripts = "/test-schema.sql") // Garante que o schema do banco de dados seja criado
@Transactional // Garante que cada teste rode em uma transação isolada e seja revertido
class TaskManagerFacadeIntegrationTest {

    /**
     * Configuração de contexto para este teste.
     * 1. Importa a configuração de dados real (DataConfig).
     * 2. Escaneia e carrega todos os serviços do pacote 'org.desviante.service'.
     * 3. EXCLUI o GoogleTasksApiService real para evitar chamadas de rede.
     * 4. Fornece um bean MOCK do GoogleTasksApiService para satisfazer a dependência do TaskService.
     */
    @Configuration
    @Import(TestDataSourceConfig.class)
    @ComponentScan(basePackages = {"org.desviante.service", "org.desviante.repository"},
            excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = GoogleTasksApiService.class))
    static class TestConfig {
        @Bean
        public GoogleTasksApiService googleTasksApiService() {
            return mock(GoogleTasksApiService.class);
        }
    }

    @Autowired
    private TaskManagerFacade taskManagerFacade;

    // Injetamos o BoardService diretamente para facilitar a criação de dados de teste.
    @Autowired
    private BoardService boardService;

    @Autowired
    private BoardColumnService columnService;

    @Autowired
    private CardService cardService;

    @Test
    @DisplayName("Deve retornar uma lista de resumos de boards corretamente")
    void getAllBoardSummaries_shouldReturnCorrectDTOs() {
        // Act: Chamar o método da fachada que está sendo testado.
        List<BoardSummaryDTO> summaries = taskManagerFacade.getAllBoardSummaries();

        // Assert
        assertNotNull(summaries);
        assertEquals(1, summaries.size(), "A lista de resumos deveria conter 1 board (board de exemplo).");

        // Verifica se o DTO foi criado corretamente com ID.
        BoardSummaryDTO boardSummary = summaries.get(0);
        assertNotNull(boardSummary.id());
        assertEquals("Board de Exemplo", boardSummary.name());
        // O board de exemplo não tem grupo (é null)
        assertNull(boardSummary.group(), "O board de exemplo não deve ter grupo");
    }

    @Test
    @DisplayName("Deve retornar a hierarquia completa de um board com colunas e cards")
    void getBoardDetails_shouldReturnFullHierarchy_whenBoardExists() {
        // --- Arrange ---
        // 1. Cria a estrutura de dados no banco de dados de teste.
        Board board = boardService.createBoard("Meu Projeto Principal");
        BoardColumn col1 = columnService.createColumn("To Do", 0, BoardColumnKindEnum.INITIAL, board.getId());
        BoardColumn col2 = columnService.createColumn("Done", 1, BoardColumnKindEnum.FINAL, board.getId());

        cardService.createCard("Tarefa 1", "Descrição 1", col1.getId());
        cardService.createCard("Tarefa 2", "Descrição 2", col1.getId());
        cardService.createCard("Tarefa 3", "Descrição 3", col2.getId());

        // --- Act ---
        // 2. Chama o método da fachada que estamos testando.
        BoardDetailDTO result = taskManagerFacade.getBoardDetails(board.getId());

        // --- Assert ---
        // 3. Valida a estrutura do DTO retornado.
        assertNotNull(result);
        assertEquals(board.getId(), result.id());
        assertEquals("Meu Projeto Principal", result.name());

        // Valida as colunas
        assertEquals(2, result.columns().size(), "Deveria haver 2 colunas.");
        BoardColumnDetailDTO columnDTO1 = result.columns().get(0);
        assertEquals("To Do", columnDTO1.name());

        BoardColumnDetailDTO columnDTO2 = result.columns().get(1);
        assertEquals("Done", columnDTO2.name());

        // Valida os cards dentro das colunas
        assertEquals(2, columnDTO1.cards().size(), "A coluna 'To Do' deveria ter 2 cards.");
        assertEquals(1, columnDTO2.cards().size(), "A coluna 'Done' deveria ter 1 card.");
        assertEquals("Tarefa 1", columnDTO1.cards().get(0).title());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao buscar detalhes de um board inexistente")
    void getBoardDetails_shouldThrowException_whenBoardNotFound() {
        // Arrange
        Long nonExistentBoardId = 999L;

        // Act & Assert
        // Verifica se a chamada ao método com um ID inválido lança a exceção esperada.
        assertThrows(ResourceNotFoundException.class, () -> {
            taskManagerFacade.getBoardDetails(nonExistentBoardId);
        });
    }

    @Test
    @DisplayName("Deve criar um novo card e retornar seu DTO correspondente")
    void createNewCard_shouldCreateCardAndReturnDTO() {
        // --- Arrange ---
        // 1. Criar a estrutura pai necessária (board e coluna) para ter um ID válido.
        Board board = boardService.createBoard("Board de Teste");
        BoardColumn column = columnService.createColumn("Coluna de Teste", 0, BoardColumnKindEnum.INITIAL, board.getId());

        // 2. Criar o objeto de requisição que a UI enviaria.
        var request = new CreateCardRequestDTO("Nova Tarefa via Fachada", "Descrição da tarefa.", column.getId());

        // --- Act ---
        // 3. Chamar o método da fachada que estamos testando.
        CardDetailDTO resultDTO = taskManagerFacade.createNewCard(request);

        // --- Assert ---
        // 4. Validar o DTO retornado pela fachada.
        assertNotNull(resultDTO);
        assertNotNull(resultDTO.id(), "O DTO do card retornado deve ter um ID.");
        assertEquals("Nova Tarefa via Fachada", resultDTO.title());

        // 5. VERIFICAÇÃO CRUCIAL: Validar que o card foi realmente persistido no banco de dados.
        //    Isso confirma que a transação funcionou de ponta a ponta.
        Optional<Card> persistedCardOpt = cardService.getCardById(resultDTO.id());
        assertTrue(persistedCardOpt.isPresent(), "O card deveria ter sido salvo no banco de dados.");
        assertEquals("Descrição da tarefa.", persistedCardOpt.get().getDescription());
    }

    @Test
    @DisplayName("Deve mover um card para outra coluna e retornar o DTO atualizado")
    void moveCard_shouldUpdateColumnIdAndReturnDTO() {
        // --- Arrange ---
        // 1. Criar uma estrutura com um board, duas colunas e um card na primeira coluna.
        Board board = boardService.createBoard("Board de Teste de Movimentação");
        BoardColumn initialColumn = columnService.createColumn("Coluna A", 0, BoardColumnKindEnum.INITIAL, board.getId());
        BoardColumn targetColumn = columnService.createColumn("Coluna B", 1, BoardColumnKindEnum.PENDING, board.getId());
        Card cardToMove = cardService.createCard("Card para Mover", "...", initialColumn.getId());

        // --- Act ---
        // 2. Chamar o método da fachada para mover o card para a segunda coluna.
        taskManagerFacade.moveCard(cardToMove.getId(), targetColumn.getId());

        // --- Assert ---
        // 3. VERIFICAÇÃO CRUCIAL: Buscar o card diretamente do banco de dados para garantir que a mudança foi persistida.
        Optional<Card> persistedCardOpt = cardService.getCardById(cardToMove.getId());
        assertTrue(persistedCardOpt.isPresent(), "O card ainda deve existir no banco.");

        Card persistedCard = persistedCardOpt.get();
        assertEquals(targetColumn.getId(), persistedCard.getBoardColumnId(), "O ID da coluna do card deveria ter sido atualizado para o da Coluna B.");
        assertNotEquals(initialColumn.getId(), persistedCard.getBoardColumnId(), "O ID da coluna do card não deveria mais ser o da Coluna A.");
    }

    @Test
    @DisplayName("Deve deletar um card com sucesso")
    void deleteCard_shouldRemoveCardFromDatabase() {
        // --- Arrange ---
        // 1. Criar a estrutura completa para ter um card para deletar.
        Board board = boardService.createBoard("Board de Teste de Deleção");
        BoardColumn column = columnService.createColumn("Coluna A", 0, BoardColumnKindEnum.INITIAL, board.getId());
        Card cardToDelete = cardService.createCard("Card a ser Deletado", "...", column.getId());
        Long cardId = cardToDelete.getId();

        // Verificação de sanidade: garantir que o card existe antes de tentarmos deletá-lo.
        assertTrue(cardService.getCardById(cardId).isPresent(), "O card deveria existir antes da deleção.");

        // --- Act ---
        // 2. Chamar o método da fachada que estamos testando.
        taskManagerFacade.deleteCard(cardId);

        // --- Assert ---
        // 3. VERIFICAÇÃO CRUCIAL: Tentar buscar o card novamente e garantir que ele não foi encontrado.
        //    Isso prova que a operação de delete funcionou de ponta a ponta.
        Optional<Card> deletedCardOpt = cardService.getCardById(cardId);
        assertTrue(deletedCardOpt.isEmpty(), "O card não deveria ser encontrado após a deleção.");
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao tentar deletar um card inexistente")
    void deleteCard_shouldThrowException_whenCardNotFound() {
        // --- Arrange ---
        // 1. Criar um ID que sabemos que não existe no banco de dados.
        Long nonExistentCardId = 99999L;

        // Verificação de sanidade: garantir que o card realmente não existe.
        assertTrue(cardService.getCardById(nonExistentCardId).isEmpty(), 
                "O card não deveria existir antes da tentativa de deleção.");

        // --- Act & Assert ---
        // 2. Chamar o método da fachada e verificar se a exceção correta é lançada.
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> taskManagerFacade.deleteCard(nonExistentCardId),
                "Deve lançar ResourceNotFoundException ao tentar deletar um card inexistente."
        );

        // 3. Verificar se a mensagem de erro é apropriada.
        assertTrue(exception.getMessage().contains("não encontrado"), 
                "A mensagem de erro deve indicar que o card não foi encontrado.");

        // 4. VERIFICAÇÃO CRUCIAL: Confirmar que nenhum card foi afetado no banco de dados.
        //    Isso garante que a operação de delete não teve efeitos colaterais.
        assertTrue(cardService.getCardById(nonExistentCardId).isEmpty(), 
                "O card ainda não deveria existir após a tentativa de deleção.");
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao tentar deletar um card com ID nulo")
    void deleteCard_shouldThrowException_whenCardIdIsNull() {
        // --- Arrange ---
        // 1. Não precisamos criar nada, pois vamos testar com ID nulo.
        //    A verificação de sanidade é que não deve haver efeitos colaterais.

        // --- Act & Assert ---
        // 2. Chamar o método da fachada com ID nulo e verificar se a exceção correta é lançada.
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> taskManagerFacade.deleteCard(null),
                "Deve lançar ResourceNotFoundException ao tentar deletar um card com ID nulo."
        );

        // 3. Verificar se a mensagem de erro é apropriada e contém informações sobre o ID nulo.
        assertTrue(exception.getMessage().contains("não encontrado"), 
                "A mensagem de erro deve indicar que o card não foi encontrado.");
        assertTrue(exception.getMessage().contains("null") || exception.getMessage().contains("nulo"), 
                "A mensagem de erro deve mencionar que o ID é nulo ou inválido.");

        // 4. VERIFICAÇÃO CRUCIAL: Confirmar que nenhum card foi afetado no banco de dados.
        //    Isso garante que a operação de delete não teve efeitos colaterais mesmo com ID nulo.
        //    Vamos verificar se todos os cards existentes ainda estão lá.
        var allBoards = boardService.getAllBoards();
        for (Board board : allBoards) {
            var boardDetails = taskManagerFacade.getBoardDetails(board.getId());
            for (var column : boardDetails.columns()) {
                for (var card : column.cards()) {
                    assertTrue(cardService.getCardById(card.id()).isPresent(), 
                            "Cards existentes não deveriam ser afetados pela tentativa de deleção com ID nulo.");
                }
            }
        }
    }

    /**
     * NOVO TESTE: Garante que o título e a descrição de um card são atualizados corretamente.
     */
    @Test
    @DisplayName("Deve atualizar o título e a descrição de um card")
    void updateCardDetails_shouldUpdateTitleAndDescription() {
        // --- Arrange ---
        // 1. Criar a estrutura necessária para ter um card para editar.
        Board board = boardService.createBoard("Board de Teste");
        BoardColumn column = columnService.createColumn("Coluna de Teste", 0, BoardColumnKindEnum.INITIAL, board.getId());
        Card originalCard = cardService.createCard("Título Antigo", "Descrição Antiga", column.getId());

        // 2. Criar o DTO de requisição com os novos dados.
        var request = new UpdateCardDetailsDTO("Título Novo e Melhorado", "Descrição nova e mais detalhada.");

        // --- Act ---
        // 3. Chamar o método da fachada que estamos testando.
        CardDetailDTO resultDTO = taskManagerFacade.updateCardDetails(originalCard.getId(), request);

        // --- Assert ---
        // 4. Validar o DTO retornado pela fachada (que contém Strings).
        assertNotNull(resultDTO);
        assertEquals(originalCard.getId(), resultDTO.id());
        assertEquals("Título Novo e Melhorado", resultDTO.title());
        assertEquals("Descrição nova e mais detalhada.", resultDTO.description());

        // 5. VERIFICAÇÃO CRUCIAL: Buscar o card diretamente do banco para garantir a persistência.
        Card persistedCard = cardService.getCardById(originalCard.getId()).orElseThrow();
        assertEquals("Título Novo e Melhorado", persistedCard.getTitle());
        assertEquals("Descrição nova e mais detalhada.", persistedCard.getDescription());

        // CORREÇÃO: Mover a validação da data para a entidade persistida, que contém os objetos LocalDateTime.
        // A data de atualização deve ser posterior ou igual à de criação (em um teste rápido, podem ser iguais).
        assertTrue(
                !persistedCard.getLastUpdateDate().isBefore(persistedCard.getCreationDate()),
                "A data de atualização não pode ser anterior à data de criação."
        );
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao tentar atualizar um card inexistente")
    void updateCardDetails_shouldThrowException_whenCardNotFound() {
        // --- Arrange ---
        // 1. Criar um ID que sabemos que não existe no banco de dados.
        Long nonExistentCardId = 99999L;

        // Verificação de sanidade: garantir que o card realmente não existe.
        assertTrue(cardService.getCardById(nonExistentCardId).isEmpty(), 
                "O card não deveria existir antes da tentativa de atualização.");

        // 2. Criar o DTO de requisição com dados válidos.
        var request = new UpdateCardDetailsDTO("Título Tentativo", "Descrição tentativa");

        // --- Act & Assert ---
        // 3. Chamar o método da fachada e verificar se a exceção correta é lançada.
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> taskManagerFacade.updateCardDetails(nonExistentCardId, request),
                "Deve lançar ResourceNotFoundException ao tentar atualizar um card inexistente."
        );

        // 4. Verificar se a mensagem de erro é apropriada.
        assertTrue(exception.getMessage().contains("não encontrado"), 
                "A mensagem de erro deve indicar que o card não foi encontrado.");
        assertTrue(exception.getMessage().contains("atualização") || exception.getMessage().contains("update"), 
                "A mensagem de erro deve mencionar que é uma operação de atualização.");

        // 5. VERIFICAÇÃO CRUCIAL: Confirmar que nenhum card foi afetado no banco de dados.
        //    Isso garante que a operação de update não teve efeitos colaterais.
        assertTrue(cardService.getCardById(nonExistentCardId).isEmpty(), 
                "O card ainda não deveria existir após a tentativa de atualização.");

        // 6. VERIFICAÇÃO ADICIONAL: Confirmar que cards existentes não foram afetados.
        //    Vamos verificar se todos os cards existentes ainda estão lá e inalterados.
        var allBoards = boardService.getAllBoards();
        for (Board board : allBoards) {
            var boardDetails = taskManagerFacade.getBoardDetails(board.getId());
            for (var column : boardDetails.columns()) {
                for (var card : column.cards()) {
                    assertTrue(cardService.getCardById(card.id()).isPresent(), 
                            "Cards existentes não deveriam ser afetados pela tentativa de atualização de card inexistente.");
                }
            }
        }
    }

    @Test
    @DisplayName("Deve atualizar o grupo de um board com sucesso")
    void updateBoardGroup_shouldUpdateBoardGroupSuccessfully() {
        // Given: Criar um board e um grupo
        BoardGroup group = taskManagerFacade.createBoardGroup("Test Group", "Test Description", "#FF5733");
        BoardSummaryDTO board = taskManagerFacade.createNewBoard("Test Board");
        
        // When: Atualizar o grupo do board
        taskManagerFacade.updateBoardGroup(board.id(), group.getId());
        
        // Then: Verificar se o board foi atualizado
        List<BoardSummaryDTO> boards = taskManagerFacade.getAllBoardSummaries();
        BoardSummaryDTO updatedBoard = boards.stream()
                .filter(b -> b.id().equals(board.id()))
                .findFirst()
                .orElse(null);
        
        assertThat(updatedBoard).isNotNull();
        assertThat(updatedBoard.group()).isNotNull();
        assertThat(updatedBoard.group().getName()).isEqualTo("Test Group");
    }

    @Test
    @DisplayName("Deve remover o grupo de um board (definir como null)")
    void updateBoardGroup_shouldRemoveBoardGroupSuccessfully() {
        // Given: Criar um board com grupo
        BoardGroup group = taskManagerFacade.createBoardGroup("Test Group", "Test Description", "#FF5733");
        BoardSummaryDTO board = taskManagerFacade.createNewBoardWithGroup("Test Board", group.getId());
        
        // When: Remover o grupo do board (definir como null)
        taskManagerFacade.updateBoardGroup(board.id(), null);
        
        // Then: Verificar se o grupo foi removido
        List<BoardSummaryDTO> boards = taskManagerFacade.getAllBoardSummaries();
        BoardSummaryDTO updatedBoard = boards.stream()
                .filter(b -> b.id().equals(board.id()))
                .findFirst()
                .orElse(null);
        
        assertThat(updatedBoard).isNotNull();
        assertThat(updatedBoard.group()).isNull();
    }
}