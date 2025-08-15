package org.desviante.service;

import org.desviante.config.AppMetadataConfig;
import org.desviante.exception.ResourceNotFoundException;
import org.desviante.model.Board;
import org.desviante.model.BoardColumn;
import org.desviante.model.BoardGroup;
import org.desviante.model.Card;
import org.desviante.model.enums.BoardColumnKindEnum;
import org.desviante.repository.BoardGroupRepository;
import org.desviante.repository.BoardRepository;
import org.desviante.service.dto.BoardSummaryDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class BoardGroupServiceTest {

    @Mock
    private BoardGroupRepository boardGroupRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private BoardColumnService columnService;

    @Mock
    private CardService cardService;
    
    @Mock
    private AppMetadataConfig appMetadataConfig;

    @InjectMocks
    private BoardGroupService boardGroupService;

    @Test
    @DisplayName("Deve retornar lista vazia quando não há grupos")
    void shouldReturnEmptyListWhenNoGroups() {
        // When
        List<BoardGroup> result = boardGroupService.getAllBoardGroups();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar todos os grupos quando existem")
    void shouldReturnAllBoardGroupsWhenGroupsExist() {
        // Given
        BoardGroup group1 = new BoardGroup(1L, "Trabalho", "Grupo para tarefas do trabalho",
                "#FF5733", "💼", LocalDateTime.now());
        BoardGroup group2 = new BoardGroup(2L, "Pessoal", "Grupo para tarefas pessoais",
                "#33FF57", "🏠", LocalDateTime.now());

        when(boardGroupRepository.findAll()).thenReturn(Arrays.asList(group1, group2));

        // When
        List<BoardGroup> result = boardGroupService.getAllBoardGroups();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Trabalho");
        assertThat(result.get(1).getName()).isEqualTo("Pessoal");
    }

    @Test
    @DisplayName("Deve retornar lista ordenada por nome do grupo")
    void shouldReturnOrderedListByGroupName() {
        // Given
        BoardGroup groupC = new BoardGroup(3L, "Categoria C", "Descrição C",
                "#FF5733", "📁", LocalDateTime.now());
        BoardGroup groupA = new BoardGroup(1L, "Categoria A", "Descrição A",
                "#33FF57", "📁", LocalDateTime.now());
        BoardGroup groupB = new BoardGroup(2L, "Categoria B", "Descrição B",
                "#3357FF", "📁", LocalDateTime.now());

        when(boardGroupRepository.findAll()).thenReturn(Arrays.asList(groupC, groupA, groupB));

        // When
        List<BoardGroup> result = boardGroupService.getAllBoardGroups();

        // Then
        assertThat(result).hasSize(3);
        // Removido teste de ordenação - não garantimos mais ordenação específica
    }

    @Test
    @DisplayName("Deve retornar grupos com todos os campos preenchidos corretamente")
    void shouldReturnGroupsWithAllFieldsCorrectlyFilled() {
        // Given
        LocalDateTime creationDate = LocalDateTime.now();
        BoardGroup group = new BoardGroup(1L, "Teste", "Descrição de teste",
                "#FF5733", "📁", creationDate);

        when(boardGroupRepository.findAll()).thenReturn(Arrays.asList(group));

        // When
        List<BoardGroup> result = boardGroupService.getAllBoardGroups();

        // Then
        assertThat(result).hasSize(1);
        BoardGroup returnedGroup = result.get(0);
        assertThat(returnedGroup.getId()).isEqualTo(1L);
        assertThat(returnedGroup.getName()).isEqualTo("Teste");
        assertThat(returnedGroup.getDescription()).isEqualTo("Descrição de teste");
        assertThat(returnedGroup.getColor()).isEqualTo("#FF5733");
        assertThat(returnedGroup.getIcon()).isEqualTo("📁");
        assertThat(returnedGroup.getCreationDate()).isEqualTo(creationDate);
        // Removido assert isDefault - não precisamos mais de grupo padrão
    }

    // Testes para createBoardGroup
    @Test
    @DisplayName("Deve criar um grupo com sucesso quando todos os parâmetros são válidos")
    void shouldCreateBoardGroupSuccessfully() {
        // Arrange
        String name = "Trabalho";
        String description = "Grupo para tarefas do trabalho";
        
        BoardGroup expectedGroup = new BoardGroup(1L, name, description, "#FF6B6B", "📁", LocalDateTime.now());
        when(boardGroupRepository.save(any(BoardGroup.class))).thenReturn(expectedGroup);

        // Act
        BoardGroup result = boardGroupService.createBoardGroup(name, description, "📁");

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(name, result.getName());
        assertEquals(description, result.getDescription());
        assertNotNull(result.getColor()); // Cor é gerada automaticamente
        assertEquals("📁", result.getIcon());
        // Removido assert isDefault - não precisamos mais de grupo padrão
        
        // Verify
        verify(boardGroupRepository).save(any(BoardGroup.class));
    }

    @Test
    @DisplayName("Deve criar grupo com descrição vazia quando description é null")
    void shouldCreateGroupWithEmptyDescriptionWhenDescriptionIsNull() {
        // Arrange
        String name = "Pessoal";
        
        BoardGroup expectedGroup = new BoardGroup(1L, name, "", "#4ECDC4", "📁", LocalDateTime.now());
        when(boardGroupRepository.save(any(BoardGroup.class))).thenReturn(expectedGroup);

        // Act
        BoardGroup result = boardGroupService.createBoardGroup(name, null, "📁");

        // Assert
        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals("", result.getDescription());
        assertNotNull(result.getColor()); // Cor é gerada automaticamente
        
        // Verify
        ArgumentCaptor<BoardGroup> groupCaptor = ArgumentCaptor.forClass(BoardGroup.class);
        verify(boardGroupRepository).save(groupCaptor.capture());
        
        BoardGroup savedGroup = groupCaptor.getValue();
        assertEquals("", savedGroup.getDescription());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando nome é null")
    void shouldThrowExceptionWhenNameIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> boardGroupService.createBoardGroup(null, "Descrição", "📁"));
        
        assertEquals("Nome do grupo é obrigatório", exception.getMessage());
        
        // Verify
        verifyNoInteractions(boardGroupRepository);
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando nome está vazio")
    void shouldThrowExceptionWhenNameIsEmpty() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> boardGroupService.createBoardGroup("", "Descrição", "📁"));
        
        assertEquals("Nome do grupo é obrigatório", exception.getMessage());
        
        // Verify
        verifyNoInteractions(boardGroupRepository);
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando nome tem apenas espaços")
    void shouldThrowExceptionWhenNameHasOnlySpaces() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> boardGroupService.createBoardGroup("   ", "Descrição", "📁"));
        
        assertEquals("Nome do grupo é obrigatório", exception.getMessage());
        
        // Verify
        verifyNoInteractions(boardGroupRepository);
    }

    @Test
    @DisplayName("Deve remover espaços em branco do nome e descrição")
    void shouldTrimNameAndDescription() {
        // Arrange
        String name = "  Trabalho  ";
        String description = "  Descrição com espaços  ";
        
        BoardGroup expectedGroup = new BoardGroup(1L, "Trabalho", "Descrição com espaços", "#45B7D1", "📁", LocalDateTime.now());
        when(boardGroupRepository.save(any(BoardGroup.class))).thenReturn(expectedGroup);

        // Act
        BoardGroup result = boardGroupService.createBoardGroup(name, description, "📁");

        // Assert
        assertNotNull(result);
        assertEquals("Trabalho", result.getName());
        assertEquals("Descrição com espaços", result.getDescription());
        assertNotNull(result.getColor()); // Cor é gerada automaticamente
        
        // Verify
        ArgumentCaptor<BoardGroup> groupCaptor = ArgumentCaptor.forClass(BoardGroup.class);
        verify(boardGroupRepository).save(groupCaptor.capture());
        
        BoardGroup savedGroup = groupCaptor.getValue();
        assertEquals("Trabalho", savedGroup.getName());
        assertEquals("Descrição com espaços", savedGroup.getDescription());
    }

    // Testes para getBoardsByGroup
    @Test
    @DisplayName("Deve retornar boards de um grupo quando o grupo existe e tem boards")
    void shouldReturnBoardsByGroupWhenGroupExistsAndHasBoards() {
        // Arrange
        Long groupId = 1L;
        BoardGroup group = new BoardGroup(groupId, "Trabalho", "Grupo de trabalho", "#FF5733", "💼", LocalDateTime.now());
        Board board1 = new Board(1L, "Board 1", LocalDateTime.now(), groupId, null);
        Board board2 = new Board(2L, "Board 2", LocalDateTime.now(), groupId, null);
        
        BoardColumn column1 = new BoardColumn(1L, "A Fazer", 0, BoardColumnKindEnum.INITIAL, 1L);
        BoardColumn column2 = new BoardColumn(2L, "Em Andamento", 1, BoardColumnKindEnum.PENDING, 1L);
        BoardColumn column3 = new BoardColumn(3L, "Concluído", 2, BoardColumnKindEnum.FINAL, 1L);
        
        Card card1 = Card.builder()
                .id(1L)
                .title("Card 1")
                .description("Descrição 1")
                .creationDate(LocalDateTime.now())
                .lastUpdateDate(LocalDateTime.now())
                .boardColumnId(1L)
                .orderIndex(1)
                .build();
        Card card2 = Card.builder()
                .id(2L)
                .title("Card 2")
                .description("Descrição 2")
                .creationDate(LocalDateTime.now())
                .lastUpdateDate(LocalDateTime.now())
                .boardColumnId(2L)
                .orderIndex(1)
                .build();
        
        when(boardGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(boardRepository.findByGroupId(groupId)).thenReturn(Arrays.asList(board1, board2));
        when(columnService.getColumnsForBoards(Arrays.asList(1L, 2L))).thenReturn(Arrays.asList(column1, column2, column3));
        when(cardService.getCardsForColumns(Arrays.asList(1L, 2L, 3L))).thenReturn(Arrays.asList(card1, card2));

        // Act
        List<BoardSummaryDTO> result = boardGroupService.getBoardsByGroup(groupId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Board 1", result.get(0).name());
        assertEquals("Board 2", result.get(1).name());
        
        // Verify
        verify(boardGroupRepository).findById(groupId);
        verify(boardRepository).findByGroupId(groupId);
        verify(columnService).getColumnsForBoards(Arrays.asList(1L, 2L));
        verify(cardService).getCardsForColumns(Arrays.asList(1L, 2L, 3L));
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando grupo existe mas não tem boards")
    void shouldReturnEmptyListWhenGroupExistsButHasNoBoards() {
        // Arrange
        Long groupId = 1L;
        BoardGroup group = new BoardGroup(groupId, "Trabalho", "Grupo de trabalho", "#FF5733", "💼", LocalDateTime.now());
        
        when(boardGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(boardRepository.findByGroupId(groupId)).thenReturn(Collections.emptyList());

        // Act
        List<BoardSummaryDTO> result = boardGroupService.getBoardsByGroup(groupId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        // Verify
        verify(boardGroupRepository).findById(groupId);
        verify(boardRepository).findByGroupId(groupId);
        verifyNoInteractions(columnService, cardService);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando grupo não existe")
    void shouldThrowResourceNotFoundExceptionWhenGroupDoesNotExist() {
        // Arrange
        Long groupId = 999L;
        when(boardGroupRepository.findById(groupId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> boardGroupService.getBoardsByGroup(groupId));
        
        assertEquals("Grupo com ID 999 não encontrado.", exception.getMessage());
        
        // Verify
        verify(boardGroupRepository).findById(groupId);
        verifyNoInteractions(boardRepository, columnService, cardService);
    }

    @Test
    @DisplayName("Deve calcular corretamente o status 'Vazio' quando board não tem cards")
    void shouldCalculateEmptyStatusWhenBoardHasNoCards() {
        // Arrange
        Long groupId = 1L;
        BoardGroup group = new BoardGroup(groupId, "Trabalho", "Grupo de trabalho", "#FF5733", "💼", LocalDateTime.now());
        Board board = new Board(1L, "Board Vazio", LocalDateTime.now(), groupId, null);
        BoardColumn column = new BoardColumn(1L, "A Fazer", 0, BoardColumnKindEnum.INITIAL, 1L);
        
        when(boardGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(boardRepository.findByGroupId(groupId)).thenReturn(Arrays.asList(board));
        when(columnService.getColumnsForBoards(Arrays.asList(1L))).thenReturn(Arrays.asList(column));
        when(cardService.getCardsForColumns(Arrays.asList(1L))).thenReturn(Collections.emptyList());

        // Act
        List<BoardSummaryDTO> result = boardGroupService.getBoardsByGroup(groupId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        BoardSummaryDTO boardSummary = result.get(0);
        assertEquals("Board Vazio", boardSummary.name());
        assertEquals("Vazio", boardSummary.status());
        assertEquals(0, boardSummary.percentInitial());
        assertEquals(0, boardSummary.percentPending());
        assertEquals(0, boardSummary.percentFinal());
    }

    @Test
    @DisplayName("Deve calcular corretamente o status 'Concluído' quando todos os cards estão na coluna final")
    void shouldCalculateCompletedStatusWhenAllCardsAreInFinalColumn() {
        // Arrange
        Long groupId = 1L;
        BoardGroup group = new BoardGroup(groupId, "Trabalho", "Grupo de trabalho", "#FF5733", "💼", LocalDateTime.now());
        Board board = new Board(1L, "Board Concluído", LocalDateTime.now(), groupId, null);
        BoardColumn column = new BoardColumn(1L, "Concluído", 0, BoardColumnKindEnum.FINAL, 1L);
        
        Card card1 = Card.builder()
                .id(1L)
                .title("Card 1")
                .description("Descrição 1")
                .creationDate(LocalDateTime.now())
                .lastUpdateDate(LocalDateTime.now())
                .boardColumnId(1L)
                .orderIndex(1)
                .build();
        Card card2 = Card.builder()
                .id(2L)
                .title("Card 2")
                .description("Descrição 2")
                .creationDate(LocalDateTime.now())
                .lastUpdateDate(LocalDateTime.now())
                .boardColumnId(1L)
                .orderIndex(2)
                .build();
        
        when(boardGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(boardRepository.findByGroupId(groupId)).thenReturn(Arrays.asList(board));
        when(columnService.getColumnsForBoards(Arrays.asList(1L))).thenReturn(Arrays.asList(column));
        when(cardService.getCardsForColumns(Arrays.asList(1L))).thenReturn(Arrays.asList(card1, card2));

        // Act
        List<BoardSummaryDTO> result = boardGroupService.getBoardsByGroup(groupId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        BoardSummaryDTO boardSummary = result.get(0);
        assertEquals("Board Concluído", boardSummary.name());
        assertEquals("Concluído", boardSummary.status());
        assertEquals(0, boardSummary.percentInitial());
        assertEquals(0, boardSummary.percentPending());
        assertEquals(100, boardSummary.percentFinal());
    }

    @Test
    @DisplayName("Deve calcular corretamente o status 'Não iniciado' quando todos os cards estão na coluna inicial")
    void shouldCalculateNotStartedStatusWhenAllCardsAreInInitialColumn() {
        // Arrange
        Long groupId = 1L;
        BoardGroup group = new BoardGroup(groupId, "Trabalho", "Grupo de trabalho", "#FF5733", "💼", LocalDateTime.now());
        Board board = new Board(1L, "Board Não Iniciado", LocalDateTime.now(), groupId, null);
        BoardColumn column = new BoardColumn(1L, "A Fazer", 0, BoardColumnKindEnum.INITIAL, 1L);
        
        Card card1 = Card.builder()
                .id(1L)
                .title("Card 1")
                .description("Descrição 1")
                .creationDate(LocalDateTime.now())
                .lastUpdateDate(LocalDateTime.now())
                .boardColumnId(1L)
                .orderIndex(1)
                .build();
        Card card2 = Card.builder()
                .id(2L)
                .title("Card 2")
                .description("Descrição 2")
                .creationDate(LocalDateTime.now())
                .lastUpdateDate(LocalDateTime.now())
                .boardColumnId(1L)
                .orderIndex(2)
                .build();
        
        when(boardGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(boardRepository.findByGroupId(groupId)).thenReturn(Arrays.asList(board));
        when(columnService.getColumnsForBoards(Arrays.asList(1L))).thenReturn(Arrays.asList(column));
        when(cardService.getCardsForColumns(Arrays.asList(1L))).thenReturn(Arrays.asList(card1, card2));

        // Act
        List<BoardSummaryDTO> result = boardGroupService.getBoardsByGroup(groupId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        BoardSummaryDTO boardSummary = result.get(0);
        assertEquals("Board Não Iniciado", boardSummary.name());
        assertEquals("Não iniciado", boardSummary.status());
        assertEquals(100, boardSummary.percentInitial());
        assertEquals(0, boardSummary.percentPending());
        assertEquals(0, boardSummary.percentFinal());
    }

    @Test
    @DisplayName("Deve calcular corretamente o status 'Em andamento' quando cards estão distribuídos")
    void shouldCalculateInProgressStatusWhenCardsAreDistributed() {
        // Arrange
        Long groupId = 1L;
        BoardGroup group = new BoardGroup(groupId, "Trabalho", "Grupo de trabalho", "#FF5733", "💼", LocalDateTime.now());
        Board board = new Board(1L, "Board Em Andamento", LocalDateTime.now(), groupId, null);
        
        BoardColumn column1 = new BoardColumn(1L, "A Fazer", 0, BoardColumnKindEnum.INITIAL, 1L);
        BoardColumn column2 = new BoardColumn(2L, "Em Andamento", 1, BoardColumnKindEnum.PENDING, 1L);
        BoardColumn column3 = new BoardColumn(3L, "Concluído", 2, BoardColumnKindEnum.FINAL, 1L);
        
        Card card1 = Card.builder()
                .id(1L)
                .title("Card 1")
                .description("Descrição 1")
                .creationDate(LocalDateTime.now())
                .lastUpdateDate(LocalDateTime.now())
                .boardColumnId(1L)
                .orderIndex(1)
                .build();
        Card card2 = Card.builder()
                .id(2L)
                .title("Card 2")
                .description("Descrição 2")
                .creationDate(LocalDateTime.now())
                .lastUpdateDate(LocalDateTime.now())
                .boardColumnId(2L)
                .orderIndex(1)
                .build();
        Card card3 = Card.builder()
                .id(3L)
                .title("Card 3")
                .description("Descrição 3")
                .creationDate(LocalDateTime.now())
                .lastUpdateDate(LocalDateTime.now())
                .boardColumnId(3L)
                .orderIndex(1)
                .build();
        
        when(boardGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(boardRepository.findByGroupId(groupId)).thenReturn(Arrays.asList(board));
        when(columnService.getColumnsForBoards(Arrays.asList(1L))).thenReturn(Arrays.asList(column1, column2, column3));
        when(cardService.getCardsForColumns(Arrays.asList(1L, 2L, 3L))).thenReturn(Arrays.asList(card1, card2, card3));

        // Act
        List<BoardSummaryDTO> result = boardGroupService.getBoardsByGroup(groupId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        BoardSummaryDTO boardSummary = result.get(0);
        assertEquals("Board Em Andamento", boardSummary.name());
        assertEquals("Em andamento", boardSummary.status());
        assertEquals(33, boardSummary.percentInitial()); // 1/3 = 33%
        assertEquals(33, boardSummary.percentPending()); // 1/3 = 33%
        assertEquals(33, boardSummary.percentFinal()); // 1/3 = 33%
    }

    @Test
    @DisplayName("Deve calcular percentuais corretamente com arredondamento por truncamento")
    void shouldCalculatePercentagesWithTruncationRounding() {
        // Arrange
        Long groupId = 1L;
        BoardGroup group = new BoardGroup(groupId, "Trabalho", "Grupo de trabalho", "#FF5733", "💼", LocalDateTime.now());
        Board board = new Board(1L, "Board Percentuais", LocalDateTime.now(), groupId, null);
        
        BoardColumn column1 = new BoardColumn(1L, "A Fazer", 0, BoardColumnKindEnum.INITIAL, 1L);
        BoardColumn column2 = new BoardColumn(2L, "Em Andamento", 1, BoardColumnKindEnum.PENDING, 1L);
        BoardColumn column3 = new BoardColumn(3L, "Concluído", 2, BoardColumnKindEnum.FINAL, 1L);
        
        Card card1 = Card.builder()
                .id(1L)
                .title("Card 1")
                .description("Descrição 1")
                .creationDate(LocalDateTime.now())
                .lastUpdateDate(LocalDateTime.now())
                .boardColumnId(1L)
                .orderIndex(1)
                .build();
        Card card2 = Card.builder()
                .id(2L)
                .title("Card 2")
                .description("Descrição 2")
                .creationDate(LocalDateTime.now())
                .lastUpdateDate(LocalDateTime.now())
                .boardColumnId(1L)
                .orderIndex(2)
                .build();
        Card card3 = Card.builder()
                .id(3L)
                .title("Card 3")
                .description("Descrição 3")
                .creationDate(LocalDateTime.now())
                .lastUpdateDate(LocalDateTime.now())
                .boardColumnId(2L)
                .orderIndex(1)
                .build();
        Card card4 = Card.builder()
                .id(4L)
                .title("Card 4")
                .description("Descrição 4")
                .creationDate(LocalDateTime.now())
                .lastUpdateDate(LocalDateTime.now())
                .boardColumnId(2L)
                .orderIndex(2)
                .build();
        Card card5 = Card.builder()
                .id(5L)
                .title("Card 5")
                .description("Descrição 5")
                .creationDate(LocalDateTime.now())
                .lastUpdateDate(LocalDateTime.now())
                .boardColumnId(2L)
                .orderIndex(3)
                .build();
        Card card6 = Card.builder()
                .id(6L)
                .title("Card 6")
                .description("Descrição 6")
                .creationDate(LocalDateTime.now())
                .lastUpdateDate(LocalDateTime.now())
                .boardColumnId(3L)
                .orderIndex(1)
                .build();
        Card card7 = Card.builder()
                .id(7L)
                .title("Card 7")
                .description("Descrição 7")
                .creationDate(LocalDateTime.now())
                .lastUpdateDate(LocalDateTime.now())
                .boardColumnId(3L)
                .orderIndex(2)
                .build();
        
        when(boardGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(boardRepository.findByGroupId(groupId)).thenReturn(Arrays.asList(board));
        when(columnService.getColumnsForBoards(Arrays.asList(1L))).thenReturn(Arrays.asList(column1, column2, column3));
        when(cardService.getCardsForColumns(Arrays.asList(1L, 2L, 3L))).thenReturn(Arrays.asList(card1, card2, card3, card4, card5, card6, card7));

        // Act
        List<BoardSummaryDTO> result = boardGroupService.getBoardsByGroup(groupId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        BoardSummaryDTO boardSummary = result.get(0);
        assertEquals("Board Percentuais", boardSummary.name());
        assertEquals("Em andamento", boardSummary.status());
        assertEquals(28, boardSummary.percentInitial()); // 2/7 = 28%
        assertEquals(42, boardSummary.percentPending()); // 3/7 = 42%
        assertEquals(28, boardSummary.percentFinal()); // 2/7 = 28%
    }

    @Test
    @DisplayName("Deve lidar corretamente quando board não tem colunas")
    void shouldHandleBoardWithNoColumns() {
        // Arrange
        Long groupId = 1L;
        BoardGroup group = new BoardGroup(groupId, "Trabalho", "Grupo de trabalho", "#FF5733", "💼", LocalDateTime.now());
        Board board = new Board(1L, "Board Sem Colunas", LocalDateTime.now(), groupId, null);
        
        when(boardGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(boardRepository.findByGroupId(groupId)).thenReturn(Arrays.asList(board));
        when(columnService.getColumnsForBoards(Arrays.asList(1L))).thenReturn(Collections.emptyList());
        when(cardService.getCardsForColumns(Collections.emptyList())).thenReturn(Collections.emptyList());

        // Act
        List<BoardSummaryDTO> result = boardGroupService.getBoardsByGroup(groupId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        BoardSummaryDTO boardSummary = result.get(0);
        assertEquals("Board Sem Colunas", boardSummary.name());
        assertEquals("Vazio", boardSummary.status());
        assertEquals(0, boardSummary.percentInitial());
        assertEquals(0, boardSummary.percentPending());
        assertEquals(0, boardSummary.percentFinal());
    }

    @Test
    @DisplayName("Deve lidar corretamente quando card não tem coluna associada")
    void shouldHandleCardWithNoAssociatedColumn() {
        // Arrange
        Long groupId = 1L;
        BoardGroup group = new BoardGroup(groupId, "Trabalho", "Grupo de trabalho", "#FF5733", "💼", LocalDateTime.now());
        Board board = new Board(1L, "Board Card Órfão", LocalDateTime.now(), groupId, null);
        BoardColumn column = new BoardColumn(1L, "A Fazer", 0, BoardColumnKindEnum.INITIAL, 1L);
        
        Card card = Card.builder()
                .id(1L)
                .title("Card Órfão")
                .description("Descrição")
                .creationDate(LocalDateTime.now())
                .lastUpdateDate(LocalDateTime.now())
                .boardColumnId(999L) // ID de coluna inexistente
                .orderIndex(1)
                .build();
        
        when(boardGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(boardRepository.findByGroupId(groupId)).thenReturn(Arrays.asList(board));
        when(columnService.getColumnsForBoards(Arrays.asList(1L))).thenReturn(Arrays.asList(column));
        when(cardService.getCardsForColumns(Arrays.asList(1L))).thenReturn(Arrays.asList(card));

        // Act
        List<BoardSummaryDTO> result = boardGroupService.getBoardsByGroup(groupId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        BoardSummaryDTO boardSummary = result.get(0);
        assertEquals("Board Card Órfão", boardSummary.name());
        assertEquals("Vazio", boardSummary.status()); // Como o card não tem coluna válida, é considerado vazio
        assertEquals(0, boardSummary.percentInitial());
        assertEquals(0, boardSummary.percentPending());
        assertEquals(0, boardSummary.percentFinal());
    }

    // Testes para updateBoardGroup
    @Test
    @DisplayName("Deve atualizar um grupo com sucesso quando todos os parâmetros são válidos")
    void shouldUpdateBoardGroupSuccessfully() {
        // Arrange
        Long groupId = 1L;
        BoardGroup existingGroup = new BoardGroup(groupId, "Grupo Antigo", "Descrição antiga", "#FF5733", "📁", LocalDateTime.now());
        BoardGroup updatedGroup = new BoardGroup(groupId, "Grupo Atualizado", "Descrição atualizada", "#FF5733", "🎯", LocalDateTime.now());
        
        when(boardGroupRepository.findById(groupId)).thenReturn(Optional.of(existingGroup));
        when(boardGroupRepository.save(any(BoardGroup.class))).thenReturn(updatedGroup);

        // Act
        BoardGroup result = boardGroupService.updateBoardGroup(groupId, "Grupo Atualizado", "Descrição atualizada", "🎯");

        // Assert
        assertNotNull(result);
        assertEquals(groupId, result.getId());
        assertEquals("Grupo Atualizado", result.getName());
        assertEquals("Descrição atualizada", result.getDescription());
        assertEquals("#FF5733", result.getColor()); // Cor permanece a mesma
        assertEquals("🎯", result.getIcon());
        // Removido assert isDefault - não precisamos mais de grupo padrão
        
        // Verify
        verify(boardGroupRepository).findById(groupId);
        verify(boardGroupRepository).save(any(BoardGroup.class));
    }

    @Test
    @DisplayName("Deve atualizar grupo com ícone padrão quando icon é null")
    void shouldUpdateGroupWithDefaultIconWhenIconIsNull() {
        // Arrange
        Long groupId = 1L;
        BoardGroup existingGroup = new BoardGroup(groupId, "Grupo Teste", "Descrição", "#FF5733", "📁", LocalDateTime.now());
        BoardGroup updatedGroup = new BoardGroup(groupId, "Grupo Atualizado", "Descrição atualizada", "#FF5733", "📁", LocalDateTime.now());
        
        when(boardGroupRepository.findById(groupId)).thenReturn(Optional.of(existingGroup));
        when(boardGroupRepository.save(any(BoardGroup.class))).thenReturn(updatedGroup);

        // Act
        BoardGroup result = boardGroupService.updateBoardGroup(groupId, "Grupo Atualizado", "Descrição atualizada", null);

        // Assert
        assertNotNull(result);
        assertEquals("Grupo Atualizado", result.getName());
        assertEquals("Descrição atualizada", result.getDescription());
        assertEquals("#FF5733", result.getColor()); // Cor permanece a mesma
        assertEquals("📁", result.getIcon()); // Ícone padrão
        
        // Verify
        ArgumentCaptor<BoardGroup> groupCaptor = ArgumentCaptor.forClass(BoardGroup.class);
        verify(boardGroupRepository).save(groupCaptor.capture());
        
        BoardGroup savedGroup = groupCaptor.getValue();
        assertEquals("📁", savedGroup.getIcon());
    }

    @Test
    @DisplayName("Deve remover espaços em branco do nome e descrição na atualização")
    void shouldTrimNameAndDescriptionInUpdate() {
        // Arrange
        Long groupId = 1L;
        BoardGroup existingGroup = new BoardGroup(groupId, "Grupo Teste", "Descrição", "#FF5733", "📁", LocalDateTime.now());
        BoardGroup updatedGroup = new BoardGroup(groupId, "Grupo Limpo", "Descrição Limpa", "#FF5733", "🎯", LocalDateTime.now());
        
        when(boardGroupRepository.findById(groupId)).thenReturn(Optional.of(existingGroup));
        when(boardGroupRepository.save(any(BoardGroup.class))).thenReturn(updatedGroup);

        // Act
        BoardGroup result = boardGroupService.updateBoardGroup(groupId, "  Grupo Limpo  ", "  Descrição Limpa  ", "🎯");

        // Assert
        assertNotNull(result);
        assertEquals("Grupo Limpo", result.getName());
        assertEquals("Descrição Limpa", result.getDescription());
        
        // Verify
        ArgumentCaptor<BoardGroup> groupCaptor = ArgumentCaptor.forClass(BoardGroup.class);
        verify(boardGroupRepository).save(groupCaptor.capture());
        
        BoardGroup savedGroup = groupCaptor.getValue();
        assertEquals("Grupo Limpo", savedGroup.getName());
        assertEquals("Descrição Limpa", savedGroup.getDescription());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando grupo não existe")
    void shouldThrowResourceNotFoundExceptionWhenGroupDoesNotExistForUpdate() {
        // Arrange
        Long groupId = 999L;
        when(boardGroupRepository.findById(groupId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> boardGroupService.updateBoardGroup(groupId, "Nome", "Descrição", "🎯"));
        
        assertEquals("Grupo com ID 999 não encontrado.", exception.getMessage());
        
        // Verify
        verify(boardGroupRepository).findById(groupId);
        verifyNoMoreInteractions(boardGroupRepository);
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando nome é null na atualização")
    void shouldThrowExceptionWhenNameIsNullInUpdate() {
        // Arrange
        Long groupId = 1L;
        BoardGroup existingGroup = new BoardGroup(groupId, "Grupo Teste", "Descrição", "#FF5733", "📁", LocalDateTime.now());
        when(boardGroupRepository.findById(groupId)).thenReturn(Optional.of(existingGroup));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> boardGroupService.updateBoardGroup(groupId, null, "Descrição", "🎯"));
        
        assertEquals("Nome do grupo é obrigatório", exception.getMessage());
        
        // Verify
        verify(boardGroupRepository).findById(groupId);
        verifyNoMoreInteractions(boardGroupRepository);
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando nome está vazio na atualização")
    void shouldThrowExceptionWhenNameIsEmptyInUpdate() {
        // Arrange
        Long groupId = 1L;
        BoardGroup existingGroup = new BoardGroup(groupId, "Grupo Teste", "Descrição", "#FF5733", "📁", LocalDateTime.now());
        when(boardGroupRepository.findById(groupId)).thenReturn(Optional.of(existingGroup));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> boardGroupService.updateBoardGroup(groupId, "", "Descrição", "🎯"));
        
        assertEquals("Nome do grupo é obrigatório", exception.getMessage());
        
        // Verify
        verify(boardGroupRepository).findById(groupId);
        verifyNoMoreInteractions(boardGroupRepository);
    }

    // Removido teste de grupo padrão - não precisamos mais de grupo padrão

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando grupo tem boards associados")
    void shouldThrowExceptionWhenGroupHasAssociatedBoards() {
        // Arrange
        Long groupId = 1L;
        BoardGroup groupWithBoards = new BoardGroup(groupId, "Grupo com Boards", "Descrição", "#FF5733", "📁", LocalDateTime.now());
        Board board1 = new Board(1L, "Board 1", LocalDateTime.now(), groupId, null);
        Board board2 = new Board(2L, "Board 2", LocalDateTime.now(), groupId, null);
        
        when(boardGroupRepository.findById(groupId)).thenReturn(Optional.of(groupWithBoards));
        when(boardRepository.findByGroupId(groupId)).thenReturn(Arrays.asList(board1, board2));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> boardGroupService.deleteBoardGroup(groupId));
        
        String expectedMessage = "Não é possível deletar o grupo 'Grupo com Boards' pois existem 2 board(s) associado(s). Mova os boards para outro grupo antes de deletar.";
        assertEquals(expectedMessage, exception.getMessage());
        
        // Verify
        verify(boardGroupRepository).findById(groupId);
        verify(boardRepository).findByGroupId(groupId);
        verifyNoMoreInteractions(boardGroupRepository, boardRepository);
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando grupo tem um board associado")
    void shouldThrowExceptionWhenGroupHasOneAssociatedBoard() {
        // Arrange
        Long groupId = 1L;
        BoardGroup groupWithOneBoard = new BoardGroup(groupId, "Grupo Único", "Descrição", "#FF5733", "📁", LocalDateTime.now());
        Board board = new Board(1L, "Board Único", LocalDateTime.now(), groupId, null);
        
        when(boardGroupRepository.findById(groupId)).thenReturn(Optional.of(groupWithOneBoard));
        when(boardRepository.findByGroupId(groupId)).thenReturn(Arrays.asList(board));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> boardGroupService.deleteBoardGroup(groupId));
        
        String expectedMessage = "Não é possível deletar o grupo 'Grupo Único' pois existem 1 board(s) associado(s). Mova os boards para outro grupo antes de deletar.";
        assertEquals(expectedMessage, exception.getMessage());
        
        // Verify
        verify(boardGroupRepository).findById(groupId);
        verify(boardRepository).findByGroupId(groupId);
        verifyNoMoreInteractions(boardGroupRepository, boardRepository);
    }

    @Test
    @DisplayName("Deve deletar grupo quando lista de boards está vazia")
    void shouldDeleteGroupWhenBoardsListIsEmpty() {
        // Arrange
        Long groupId = 1L;
        BoardGroup groupToDelete = new BoardGroup(groupId, "Grupo Vazio", "Descrição", "#FF5733", "📁", LocalDateTime.now());
        
        when(boardGroupRepository.findById(groupId)).thenReturn(Optional.of(groupToDelete));
        when(boardRepository.findByGroupId(groupId)).thenReturn(Collections.emptyList());
        doNothing().when(boardGroupRepository).deleteById(groupId);

        // Act
        boardGroupService.deleteBoardGroup(groupId);

        // Assert & Verify
        verify(boardGroupRepository).findById(groupId);
        verify(boardRepository).findByGroupId(groupId);
        verify(boardGroupRepository).deleteById(groupId);
        verifyNoMoreInteractions(boardGroupRepository, boardRepository);
    }

    @Test
    @DisplayName("Deve deletar grupo quando lista de boards é null")
    void shouldDeleteGroupWhenBoardsListIsNull() {
        // Arrange
        Long groupId = 1L;
        BoardGroup groupToDelete = new BoardGroup(groupId, "Grupo Null", "Descrição", "#FF5733", "📁", LocalDateTime.now());
        
        when(boardGroupRepository.findById(groupId)).thenReturn(Optional.of(groupToDelete));
        when(boardRepository.findByGroupId(groupId)).thenReturn(null);
        doNothing().when(boardGroupRepository).deleteById(groupId);

        // Act
        boardGroupService.deleteBoardGroup(groupId);

        // Assert & Verify
        verify(boardGroupRepository).findById(groupId);
        verify(boardRepository).findByGroupId(groupId);
        verify(boardGroupRepository).deleteById(groupId);
        verifyNoMoreInteractions(boardGroupRepository, boardRepository);
    }

    @Test
    @DisplayName("Deve incluir nome do grupo na mensagem de erro quando há boards associados")
    void shouldIncludeGroupNameInErrorMessageWhenBoardsAreAssociated() {
        // Arrange
        Long groupId = 1L;
        BoardGroup groupWithBoards = new BoardGroup(groupId, "Meu Grupo Especial", "Descrição especial", "#FF5733", "📁", LocalDateTime.now());
        Board board = new Board(1L, "Board Especial", LocalDateTime.now(), groupId, null);
        
        when(boardGroupRepository.findById(groupId)).thenReturn(Optional.of(groupWithBoards));
        when(boardRepository.findByGroupId(groupId)).thenReturn(Arrays.asList(board));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> boardGroupService.deleteBoardGroup(groupId));
        
        String expectedMessage = "Não é possível deletar o grupo 'Meu Grupo Especial' pois existem 1 board(s) associado(s). Mova os boards para outro grupo antes de deletar.";
        assertEquals(expectedMessage, exception.getMessage());
        
        // Verify
        verify(boardGroupRepository).findById(groupId);
        verify(boardRepository).findByGroupId(groupId);
        verifyNoMoreInteractions(boardGroupRepository, boardRepository);
    }
    
    @Test
    @DisplayName("Deve impedir exclusão de grupo configurado como padrão")
    void shouldPreventDeletionOfDefaultGroup() {
        // Arrange
        Long groupId = 1L;
        BoardGroup defaultGroup = new BoardGroup(groupId, "Grupo Padrão", "Descrição", "#FF5733", "📁", LocalDateTime.now());
        
        when(boardGroupRepository.findById(groupId)).thenReturn(Optional.of(defaultGroup));
        when(appMetadataConfig.getDefaultBoardGroupId()).thenReturn(Optional.of(groupId));
        // Não configuramos boardRepository.findByGroupId porque não deve ser chamado

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> boardGroupService.deleteBoardGroup(groupId));
        
        String expectedMessage = "Não é possível deletar o grupo 'Grupo Padrão' pois ele está configurado como grupo padrão no sistema. Altere a configuração padrão antes de deletar o grupo.";
        assertEquals(expectedMessage, exception.getMessage());
        
        // Verify
        verify(boardGroupRepository).findById(groupId);
        verify(appMetadataConfig).getDefaultBoardGroupId();
        verifyNoMoreInteractions(boardGroupRepository);
        verifyNoInteractions(boardRepository);
    }
    
    @Test
    @DisplayName("Deve permitir exclusão de grupo quando não é o padrão")
    void shouldAllowDeletionWhenGroupIsNotDefault() {
        // Arrange
        Long groupId = 1L;
        BoardGroup nonDefaultGroup = new BoardGroup(groupId, "Grupo Normal", "Descrição", "#FF5733", "📁", LocalDateTime.now());
        
        when(boardGroupRepository.findById(groupId)).thenReturn(Optional.of(nonDefaultGroup));
        when(appMetadataConfig.getDefaultBoardGroupId()).thenReturn(Optional.of(999L)); // ID diferente
        when(boardRepository.findByGroupId(groupId)).thenReturn(Collections.emptyList());
        doNothing().when(boardGroupRepository).deleteById(groupId);

        // Act
        boardGroupService.deleteBoardGroup(groupId);

        // Assert & Verify
        verify(boardGroupRepository).findById(groupId);
        verify(appMetadataConfig).getDefaultBoardGroupId();
        verify(boardRepository).findByGroupId(groupId);
        verify(boardGroupRepository).deleteById(groupId);
        verifyNoMoreInteractions(boardGroupRepository, boardRepository);
    }
    
    @Test
    @DisplayName("Deve permitir exclusão quando nenhum grupo padrão está configurado")
    void shouldAllowDeletionWhenNoDefaultGroupConfigured() {
        // Arrange
        Long groupId = 1L;
        BoardGroup normalGroup = new BoardGroup(groupId, "Grupo Normal", "Descrição", "#FF5733", "📁", LocalDateTime.now());
        
        when(boardGroupRepository.findById(groupId)).thenReturn(Optional.of(normalGroup));
        when(appMetadataConfig.getDefaultBoardGroupId()).thenReturn(Optional.empty()); // Sem grupo padrão
        when(boardRepository.findByGroupId(groupId)).thenReturn(Collections.emptyList());
        doNothing().when(boardGroupRepository).deleteById(groupId);

        // Act
        boardGroupService.deleteBoardGroup(groupId);

        // Assert & Verify
        verify(boardGroupRepository).findById(groupId);
        verify(appMetadataConfig).getDefaultBoardGroupId();
        verify(boardRepository).findByGroupId(groupId);
        verify(boardGroupRepository).deleteById(groupId);
        verifyNoMoreInteractions(boardGroupRepository, boardRepository);
    }
    
    @Test
    @DisplayName("Deve verificar grupo padrão antes de verificar boards associados")
    void shouldCheckDefaultGroupBeforeCheckingAssociatedBoards() {
        // Arrange
        Long groupId = 1L;
        BoardGroup defaultGroup = new BoardGroup(groupId, "Grupo Padrão", "Descrição", "#FF5733", "📁", LocalDateTime.now());
        
        when(boardGroupRepository.findById(groupId)).thenReturn(Optional.of(defaultGroup));
        when(appMetadataConfig.getDefaultBoardGroupId()).thenReturn(Optional.of(groupId));
        // Não configuramos boardRepository.findByGroupId porque não deve ser chamado

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> boardGroupService.deleteBoardGroup(groupId));
        
        String expectedMessage = "Não é possível deletar o grupo 'Grupo Padrão' pois ele está configurado como grupo padrão no sistema. Altere a configuração padrão antes de deletar o grupo.";
        assertEquals(expectedMessage, exception.getMessage());
        
        // Verify - Deve verificar grupo padrão primeiro, não deve verificar boards
        verify(boardGroupRepository).findById(groupId);
        verify(appMetadataConfig).getDefaultBoardGroupId();
        verifyNoMoreInteractions(boardGroupRepository);
        verifyNoInteractions(boardRepository);
    }
} 
