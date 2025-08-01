package org.desviante.service;

import lombok.RequiredArgsConstructor;
import org.desviante.exception.ResourceNotFoundException;
import org.desviante.model.Board;
import org.desviante.model.BoardColumn;
import org.desviante.model.BoardGroup;
import org.desviante.model.Card;
import org.desviante.model.enums.BoardColumnKindEnum;
import org.desviante.service.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskManagerFacade {

    private final BoardService boardService;
    private final BoardColumnService columnService;
    private final CardService cardService;
    private final TaskService taskService;
    private final BoardGroupService boardGroupService;

    @Transactional(readOnly = true)
    public List<BoardSummaryDTO> getAllBoardSummaries() {
        List<Board> allBoards = boardService.getAllBoards();
        if (allBoards.isEmpty()) {
            return Collections.emptyList();
        }

        // Otimização: Busca todos os dados necessários de uma vez
        List<Long> boardIds = allBoards.stream().map(Board::getId).toList();
        List<BoardColumn> allColumns = columnService.getColumnsForBoards(boardIds);
        List<Long> allColumnIds = allColumns.stream().map(BoardColumn::getId).toList();
        List<Card> allCards = cardService.getCardsForColumns(allColumnIds);

        // Agrupa os dados em mapas para acesso rápido
        Map<Long, List<BoardColumn>> columnsByBoardId = allColumns.stream()
                .collect(Collectors.groupingBy(BoardColumn::getBoardId));
        Map<Long, List<Card>> cardsByColumnId = allCards.stream()
                .collect(Collectors.groupingBy(Card::getBoardColumnId));

        // Usa o método de cálculo centralizado
        return allBoards.stream()
                .map(board -> calculateBoardSummary(board, columnsByBoardId, cardsByColumnId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BoardSummaryDTO getBoardSummary(Long boardId) {
        Board board = boardService.getBoardById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board com ID " + boardId + " não encontrado."));

        // A lógica de busca de dados é a mesma, mas agora para um único board
        List<BoardColumn> columns = columnService.getColumnsForBoard(boardId);
        List<Long> columnIds = columns.stream().map(BoardColumn::getId).toList();
        List<Card> cards = cardService.getCardsForColumns(columnIds);

        // Agrupa os dados em mapas para o método de cálculo
        Map<Long, List<BoardColumn>> columnsByBoardId = columns.stream()
                .collect(Collectors.groupingBy(BoardColumn::getBoardId));
        Map<Long, List<Card>> cardsByColumnId = cards.stream()
                .collect(Collectors.groupingBy(Card::getBoardColumnId));

        // Usa o mesmo método de cálculo centralizado
        return calculateBoardSummary(board, columnsByBoardId, cardsByColumnId);
    }

    /**
     * MÉTODO PRIVADO E CENTRALIZADO, AGORA COM LÓGICA DE STATUS.
     */
    private BoardSummaryDTO calculateBoardSummary(Board board, Map<Long, List<BoardColumn>> columnsByBoardId, Map<Long, List<Card>> cardsByColumnId) {
        List<BoardColumn> boardColumns = columnsByBoardId.getOrDefault(board.getId(), Collections.emptyList());

        Map<Long, BoardColumn> columnMap = boardColumns.stream()
                .collect(Collectors.toMap(BoardColumn::getId, Function.identity()));

        List<Card> boardCards = boardColumns.stream()
                .flatMap(col -> cardsByColumnId.getOrDefault(col.getId(), Collections.emptyList()).stream())
                .toList();

        int totalCards = boardCards.size();

        // Regra de negócio: Vazio
        if (totalCards == 0) {
            return new BoardSummaryDTO(board.getId(), board.getName(), 0, 0, 0, "Vazio", board.getGroup());
        }

        long initialCount = 0;
        long pendingCount = 0;
        long finalCount = 0;

        for (Card card : boardCards) {
            BoardColumn parentColumn = columnMap.get(card.getBoardColumnId());
            if (parentColumn != null) {
                switch (parentColumn.getKind()) {
                    case INITIAL -> initialCount++;
                    case PENDING -> pendingCount++;
                    case FINAL -> finalCount++;
                }
            }
        }

        // --- NOVA LÓGICA DE STATUS DO BOARD ---
        String boardStatus;
        if (initialCount == totalCards) {
            boardStatus = "Não iniciado";
        } else if (finalCount == totalCards) {
            boardStatus = "Concluído";
        } else {
            boardStatus = "Em andamento";
        }
        // --- FIM DA LÓGICA DE STATUS ---

        // Lógica de arredondamento simples por truncamento
        int percentInitial = (int) (100.0 * initialCount / totalCards);
        int percentPending = (int) (100.0 * pendingCount / totalCards);
        int percentFinal = (int) (100.0 * finalCount / totalCards);

        return new BoardSummaryDTO(
                board.getId(),
                board.getName(),
                percentInitial,
                percentPending,
                percentFinal,
                boardStatus, // Passa o novo status
                board.getGroup() // Inclui informações do grupo
        );
    }

    @Transactional
    public BoardSummaryDTO createNewBoard(String name) {
        var newBoard = boardService.createBoard(name);
        columnService.createColumn("A Fazer", 0, BoardColumnKindEnum.INITIAL, newBoard.getId());
        columnService.createColumn("Em Andamento", 1, BoardColumnKindEnum.PENDING, newBoard.getId());
        columnService.createColumn("Concluído", 2, BoardColumnKindEnum.FINAL, newBoard.getId());
        // Um board recém-criado está sempre "Vazio".
        return new BoardSummaryDTO(newBoard.getId(), newBoard.getName(), 0, 0, 0, "Vazio", newBoard.getGroup());
    }

    @Transactional
    public BoardSummaryDTO createNewBoardWithGroup(String name, Long groupId) {
        var newBoard = boardService.createBoard(name);
        
        // Definir o grupo do board
        newBoard.setGroupId(groupId);
        boardService.updateBoard(newBoard);
        
        columnService.createColumn("A Fazer", 0, BoardColumnKindEnum.INITIAL, newBoard.getId());
        columnService.createColumn("Em Andamento", 1, BoardColumnKindEnum.PENDING, newBoard.getId());
        columnService.createColumn("Concluído", 2, BoardColumnKindEnum.FINAL, newBoard.getId());
        // Um board recém-criado está sempre "Vazio".
        return new BoardSummaryDTO(newBoard.getId(), newBoard.getName(), 0, 0, 0, "Vazio", newBoard.getGroup());
    }

    // ... O resto da classe permanece o mesmo ...
    @Transactional(readOnly = true)
    public BoardDetailDTO getBoardDetails(Long boardId) {
        var board = boardService.getBoardById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board com ID " + boardId + " não encontrado."));

        var columns = columnService.getColumnsForBoard(boardId);
        if (columns.isEmpty()) {
            return new BoardDetailDTO(board.getId(), board.getName(), Collections.emptyList());
        }

        List<Long> columnIds = columns.stream().map(BoardColumn::getId).collect(Collectors.toList());
        List<Card> allCards = cardService.getCardsForColumns(columnIds);

        Map<Long, List<Card>> cardsByColumnId = allCards.stream()
                .collect(Collectors.groupingBy(Card::getBoardColumnId));

        List<BoardColumnDetailDTO> columnDTOs = columns.stream()
                .map(column -> {
                    List<Card> cardsForColumn = cardsByColumnId.getOrDefault(column.getId(), Collections.emptyList());
                    List<CardDetailDTO> cardDTOs = cardsForColumn.stream()
                            .map(card -> new CardDetailDTO(
                                    card.getId(),
                                    card.getTitle(),
                                    card.getDescription(),
                                    formatDateTime(card.getCreationDate()),
                                    formatDateTime(card.getLastUpdateDate()),
                                    formatDateTime(card.getCompletionDate())
                            ))
                            .collect(Collectors.toList());
                    return new BoardColumnDetailDTO(column.getId(), column.getName(), cardDTOs);
                })
                .collect(Collectors.toList());

        return new BoardDetailDTO(board.getId(), board.getName(), columnDTOs);
    }

    @Transactional
    public CardDetailDTO createNewCard(CreateCardRequestDTO request) {
        Card newCard = cardService.createCard(
                request.title(),
                request.description(),
                request.parentColumnId()
        );

        return new CardDetailDTO(
                newCard.getId(),
                newCard.getTitle(),
                newCard.getDescription(),
                formatDateTime(newCard.getCreationDate()),
                formatDateTime(newCard.getLastUpdateDate()),
                formatDateTime(newCard.getCompletionDate())
        );
    }

    @Transactional
    public CardDetailDTO moveCard(Long cardId, Long newColumnId) {
        Card updatedCard = cardService.moveCardToColumn(cardId, newColumnId);

        return new CardDetailDTO(
                updatedCard.getId(),
                updatedCard.getTitle(),
                updatedCard.getDescription(),
                formatDateTime(updatedCard.getCreationDate()),
                formatDateTime(updatedCard.getLastUpdateDate()),
                formatDateTime(updatedCard.getCompletionDate())
        );
    }

    @Transactional
    public void deleteCard(Long cardId) {
        cardService.deleteCard(cardId);
    }

    @Transactional
    public void updateBoardName(Long boardId, String newName) {
        boardService.updateBoardName(boardId, newName);
    }

    @Transactional
    public void updateBoardGroup(Long boardId, Long groupId) {
        Board board = boardService.getBoardById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board com ID " + boardId + " não encontrado."));
        
        board.setGroupId(groupId);
        boardService.updateBoard(board);
    }

    @Transactional
    public void deleteBoard(Long boardId) {
        boardService.deleteBoard(boardId);
    }

    /**
     * MÉTODO: Atualiza os detalhes de um card.
     */
    @Transactional
    public CardDetailDTO updateCardDetails(Long cardId, UpdateCardDetailsDTO request) {
        // 1. Delega a lógica de negócio para o serviço correspondente.
        Card updatedCard = cardService.updateCardDetails(cardId, request.title(), request.description());

        // 2. Converte a entidade persistida de volta para um DTO para a resposta da API.
        // CORREÇÃO: Aplicar o método formatDateTime para converter as datas em Strings.
        return new CardDetailDTO(
                updatedCard.getId(),
                updatedCard.getTitle(),
                updatedCard.getDescription(),
                formatDateTime(updatedCard.getCreationDate()),
                formatDateTime(updatedCard.getLastUpdateDate()),
                formatDateTime(updatedCard.getCompletionDate())
        );
    }

    private String formatDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
        return localDateTime.format(formatter);
    }

    /**
     * Cria uma nova Task associada a um Card.
     */
    @Transactional
    public void createTaskForCard(CreateTaskRequestDTO request) {
        taskService.createTask(
                request.listTitle(),
                request.title(),
                request.notes(),
                request.due(), // <-- This is now a LocalDateTime
                request.cardId()
        );
        System.out.println("Fachada: Criando tarefa para o card ID " + request.cardId());
    }

    // Novos métodos:
    public List<BoardGroup> getAllBoardGroups() {
        return boardGroupService.getAllBoardGroups();
    }

    public BoardGroup createBoardGroup(String name, String description, String color, String icon) {
        return boardGroupService.createBoardGroup(name, description, color, icon);
    }

    public BoardGroup updateBoardGroup(Long groupId, String name, String description, String color, String icon) {
        return boardGroupService.updateBoardGroup(groupId, name, description, color, icon);
    }

    public void deleteBoardGroup(Long groupId) {
        boardGroupService.deleteBoardGroup(groupId);
    }

    public List<BoardSummaryDTO> getBoardsByGroup(Long groupId) {
        return boardGroupService.getBoardsByGroup(groupId);
    }

    @Transactional(readOnly = true)
    public List<BoardSummaryDTO> getBoardsWithoutGroup() {
        List<Board> boardsWithoutGroup = boardService.getBoardsWithoutGroup();
        if (boardsWithoutGroup.isEmpty()) {
            return Collections.emptyList();
        }

        // Otimização: Busca todos os dados necessários de uma vez
        List<Long> boardIds = boardsWithoutGroup.stream().map(Board::getId).toList();
        List<BoardColumn> allColumns = columnService.getColumnsForBoards(boardIds);
        List<Long> allColumnIds = allColumns.stream().map(BoardColumn::getId).toList();
        List<Card> allCards = cardService.getCardsForColumns(allColumnIds);

        // Agrupa os dados em mapas para acesso rápido
        Map<Long, List<BoardColumn>> columnsByBoardId = allColumns.stream()
                .collect(Collectors.groupingBy(BoardColumn::getBoardId));
        Map<Long, List<Card>> cardsByColumnId = allCards.stream()
                .collect(Collectors.groupingBy(Card::getBoardColumnId));

        // Usa o método de cálculo centralizado
        return boardsWithoutGroup.stream()
                .map(board -> calculateBoardSummary(board, columnsByBoardId, cardsByColumnId))
                .collect(Collectors.toList());
    }
}