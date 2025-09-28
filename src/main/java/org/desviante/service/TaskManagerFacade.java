package org.desviante.service;

import lombok.RequiredArgsConstructor;
import org.desviante.exception.ResourceNotFoundException;
import org.desviante.model.Board;
import org.desviante.model.BoardColumn;
import org.desviante.model.BoardGroup;
import org.desviante.model.Card;
import org.desviante.model.CardType;
import org.desviante.model.enums.BoardColumnKindEnum;
import org.desviante.repository.CheckListItemRepository;

import org.desviante.service.dto.*;
import org.desviante.config.AppMetadataConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Fachada principal para o gerenciamento de tarefas no sistema.
 * 
 * <p>Esta classe implementa o padrão Facade, fornecendo uma interface simplificada
 * para todas as operações relacionadas ao gerenciamento de quadros, colunas, cards
 * e tarefas. Ela coordena a interação entre os diversos serviços especializados,
 * garantindo consistência transacional e otimizações de performance.</p>
 * 
 * <p><strong>Responsabilidades Principais:</strong></p>
 * <ul>
 *   <li>Gerenciamento de quadros (boards) e suas operações CRUD</li>
 *   <li>Coordenação de operações entre diferentes entidades do sistema</li>
 *   <li>Implementação de regras de negócio para status e progresso</li>
 *   <li>Otimização de consultas através de agrupamento de dados</li>
 *   <li>Gerenciamento de transações para operações complexas</li>
 * </ul>
 * 
 * <p><strong>Otimizações de Performance:</strong></p>
 * <ul>
 *   <li>Busca em lote de dados relacionados para reduzir consultas ao banco</li>
 *   <li>Agrupamento de dados em mapas para acesso O(1)</li>
 *   <li>Uso de transações somente leitura quando apropriado</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see BoardService
 * @see BoardColumnService
 * @see CardService
 * @see TaskService
 * @see BoardGroupService
 * @see CardTypeService
 */
@Service
@RequiredArgsConstructor
public class TaskManagerFacade {

    private final BoardService boardService;
    private final BoardColumnService columnService;
    private final CardService cardService;
    private final EnhancedCardService enhancedCardService;
    private final TaskService taskService;
    private final BoardGroupService boardGroupService;
    private final CardTypeService cardTypeService;
    private final CheckListItemRepository checklistItemRepository;
    private final AppMetadataConfig appMetadataConfig;
    private final CalendarEventService calendarEventService;
    private final GoogleTaskCreationService googleTaskCreationService;
    
    /**
     * Obtém resumos de todos os quadros disponíveis no sistema.
     * 
     * <p>Este método implementa uma otimização de performance ao buscar todos os dados
     * relacionados (quadros, colunas e cards) em consultas separadas e depois agrupá-los
     * em memória para evitar o problema N+1 de consultas.</p>
     * 
     * <p><strong>Comportamento:</strong></p>
 * <ul>
 *   <li>Retorna lista vazia se não houver quadros cadastrados</li>
 *   <li>Calcula estatísticas de progresso para cada quadro</li>
 *   <li>Determina status automático baseado na distribuição dos cards</li>
 * </ul>
 * 
 * @return Lista de resumos dos quadros com estatísticas de progresso
 * @see BoardSummaryDTO
 */
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

    /**
     * Obtém o resumo detalhado de um quadro específico.
     * 
     * <p>Similar ao método {@link #getAllBoardSummaries()}, mas focado em um único
     * quadro. Implementa as mesmas otimizações de performance para consultas eficientes.</p>
 * 
 * @param boardId ID do quadro para obter o resumo
 * @return Resumo do quadro com estatísticas de progresso
 * @throws ResourceNotFoundException se o quadro não for encontrado
 * @see BoardSummaryDTO
 * @see ResourceNotFoundException
 */
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
     * Calcula o resumo de um quadro baseado em suas colunas e cards.
     * 
     * <p>Método privado que centraliza a lógica de cálculo de estatísticas e status
     * dos quadros. Implementa as regras de negócio para determinação automática
     * do status baseado na distribuição dos cards nas colunas.</p>
 * 
 * <p><strong>Regras de Status:</strong></p>
 * <ul>
 *   <li><strong>Vazio:</strong> Quando não há cards no quadro</li>
 *   <li><strong>Não iniciado:</strong> Quando todos os cards estão na coluna inicial</li>
 *   <li><strong>Concluído:</strong> Quando todos os cards estão na coluna final</li>
 *   <li><strong>Em andamento:</strong> Quando há cards em diferentes colunas</li>
 * </ul>
 * 
 * @param board Quadro para calcular o resumo
 * @param columnsByBoardId Mapa de colunas agrupadas por ID do quadro
 * @param cardsByColumnId Mapa de cards agrupados por ID da coluna
 * @return DTO com resumo do quadro e estatísticas
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

    /**
     * Cria um novo quadro com configuração padrão.
     * 
     * <p>Cria um quadro com três colunas padrão (A Fazer, Em Andamento, Concluído)
     * e aplica automaticamente o grupo padrão configurado no sistema, se disponível.</p>
 * 
 * <p><strong>Colunas Criadas:</strong></p>
 * <ul>
 *   <li>Coluna 0: "A Fazer" (INITIAL)</li>
 *   <li>Coluna 1: "Em Andamento" (PENDING)</li>
 *   <li>Coluna 2: "Concluído" (FINAL)</li>
 * </ul>
 * 
 * @param name Nome do novo quadro
 * @return Resumo do quadro criado com status "Vazio"
 * @see BoardSummaryDTO
 */
    @Transactional
    public BoardSummaryDTO createNewBoard(String name) {
        var newBoard = boardService.createBoard(name);
        
        // Aplicar grupo padrão se configurado
        Optional<Long> defaultGroupId = appMetadataConfig.getDefaultBoardGroupId();
        if (defaultGroupId.isPresent() && defaultGroupId.get() != null) {
            newBoard.setGroupId(defaultGroupId.get());
            boardService.updateBoard(newBoard);
        }
        // Se não há grupo padrão configurado, o board será criado sem grupo (group_id = null)
        
        columnService.createColumn("A Fazer", 0, BoardColumnKindEnum.INITIAL, newBoard.getId());
        columnService.createColumn("Em Andamento", 1, BoardColumnKindEnum.PENDING, newBoard.getId());
        columnService.createColumn("Concluído", 2, BoardColumnKindEnum.FINAL, newBoard.getId());
        // Um board recém-criado está sempre "Vazio".
        return new BoardSummaryDTO(newBoard.getId(), newBoard.getName(), 0, 0, 0, "Vazio", newBoard.getGroup());
    }

    /**
     * Cria um novo quadro associado a um grupo específico.
     * 
     * <p>Similar ao método {@link #createNewBoard(String)}, mas permite especificar
     * diretamente o grupo ao qual o quadro será associado, ignorando a configuração
     * padrão do sistema.</p>
 * 
 * @param name Nome do novo quadro
 * @param groupId ID do grupo ao qual o quadro será associado
 * @return Resumo do quadro criado com status "Vazio"
 * @see BoardSummaryDTO
 */
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

    /**
     * Obtém detalhes completos de um quadro específico.
     * 
     * <p>Retorna informações detalhadas do quadro, incluindo todas as suas colunas
     * e cards organizados por coluna. Implementa otimizações de performance similares
     * aos métodos de resumo.</p>
     * 
     * @param boardId ID do quadro para obter os detalhes
     * @return DTO com detalhes completos do quadro
     * @throws ResourceNotFoundException se o quadro não for encontrado
     * @see BoardDetailDTO
     * @see ResourceNotFoundException
     */
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
                    
                    // Ordenar cards por order_index para preservar a ordem definida pelo usuário
                    List<CardDetailDTO> cardDTOs = cardsForColumn.stream()
                            .sorted((c1, c2) -> Integer.compare(c1.getOrderIndex(), c2.getOrderIndex()))
                            .map(card -> new CardDetailDTO(
                                    card.getId(),
                                    card.getTitle(),
                                    card.getDescription(),
                                    card.getCardType() != null ? card.getCardType().getName() : null,
                                    card.getTotalUnits(),
                                    card.getCurrentUnits(),
                                    formatDateTime(card.getCreationDate()),
                                    formatDateTime(card.getLastUpdateDate()),
                                    formatDateTime(card.getCompletionDate()),
                                    formatDateTime(card.getScheduledDate()),
                                    formatDateTime(card.getDueDate()),
                                    column.getKind(), // Adicionar o tipo da coluna
                                    card.getProgressTypeOrDefault() // Adicionar o tipo de progresso
                            ))
                            .collect(Collectors.toList());
                    return new BoardColumnDetailDTO(column.getId(), column.getName(), cardDTOs);
                })
                .collect(Collectors.toList());

        return new BoardDetailDTO(board.getId(), board.getName(), columnDTOs);
    }

    /**
     * Cria um novo card no sistema.
     * 
     * <p>Delega a criação do card para o serviço especializado e retorna
     * os detalhes completos do card criado, incluindo informações da coluna
     * e tipo de progresso.</p>
     * 
     * @param request DTO com dados para criação do card
     * @return DTO com detalhes completos do card criado
     * @throws ResourceNotFoundException se a coluna pai não for encontrada
     * @see CreateCardRequestDTO
     * @see CardDetailDTO
     * @see ResourceNotFoundException
     */
    public CardDetailDTO createNewCard(CreateCardRequestDTO request) {
        Card newCard = cardService.createCard(
                request.title(),
                request.description(),
                request.parentColumnId(),
                request.cardTypeId(),
                request.progressType()
        );

        // Obter o tipo da coluna para incluir no DTO
        BoardColumn column = columnService.getColumnById(request.parentColumnId())
                .orElseThrow(() -> new ResourceNotFoundException("Coluna com ID " + request.parentColumnId() + " não encontrada."));

        return new CardDetailDTO(
                newCard.getId(),
                newCard.getTitle(),
                newCard.getDescription(),
                newCard.getCardType() != null ? newCard.getCardType().getName() : null,
                newCard.getTotalUnits(),
                newCard.getCurrentUnits(),
                formatDateTime(newCard.getCreationDate()),
                formatDateTime(newCard.getLastUpdateDate()),
                formatDateTime(newCard.getCompletionDate()),
                formatDateTime(newCard.getScheduledDate()),
                formatDateTime(newCard.getDueDate()),
                column.getKind(), // Adicionar o tipo da coluna
                newCard.getProgressTypeOrDefault() // Adicionar o tipo de progresso
        );
    }

    /**
     * Move um card para uma nova coluna.
     * 
     * <p>Atualiza a posição do card no quadro, movendo-o para a coluna especificada.
     * O progresso e status do card permanecem desacoplados da movimentação.</p>
     * 
     * @param cardId ID do card a ser movido
     * @param newColumnId ID da nova coluna de destino
     * @return DTO com detalhes atualizados do card
     * @throws ResourceNotFoundException se o card ou a coluna não forem encontrados
     * @see CardDetailDTO
     * @see ResourceNotFoundException
     */
    public CardDetailDTO moveCard(Long cardId, Long newColumnId) {
        // Mover o card sem sincronizar progresso - progresso e status desacoplados
        Card updatedCard = enhancedCardService.moveCardToColumn(cardId, newColumnId);

        // Obter o tipo da nova coluna
        BoardColumn newColumn = columnService.getColumnById(newColumnId)
                .orElseThrow(() -> new ResourceNotFoundException("Coluna com ID " + newColumnId + " não encontrada."));

        return new CardDetailDTO(
                updatedCard.getId(),
                updatedCard.getTitle(),
                updatedCard.getDescription(),
                updatedCard.getCardType() != null ? updatedCard.getCardType().getName() : null,
                updatedCard.getTotalUnits(),
                updatedCard.getCurrentUnits(),
                formatDateTime(updatedCard.getCreationDate()),
                formatDateTime(updatedCard.getLastUpdateDate()),
                formatDateTime(updatedCard.getCompletionDate()),
                formatDateTime(updatedCard.getScheduledDate()),
                formatDateTime(updatedCard.getDueDate()),
                newColumn.getKind(), // Adicionar o tipo da nova coluna
                updatedCard.getProgressTypeOrDefault() // Adicionar o tipo de progresso
        );
    }

    /**
     * Remove um card do sistema.
     * 
     * <p>Delega a exclusão do card para o serviço especializado.
     * Esta operação é irreversível e remove todos os dados associados ao card.</p>
     * 
     * @param cardId ID do card a ser removido
     */
    public void deleteCard(Long cardId) {
        enhancedCardService.deleteCard(cardId);
    }

    /**
     * Atualiza o nome de um quadro.
     * 
     * @param boardId ID do quadro a ser atualizado
     * @param newName Novo nome para o quadro
     */
    @Transactional
    public void updateBoardName(Long boardId, String newName) {
        boardService.updateBoardName(boardId, newName);
    }

    /**
     * Atualiza o grupo associado a um quadro.
     * 
     * @param boardId ID do quadro a ser atualizado
     * @param groupId ID do novo grupo para o quadro
     * @throws ResourceNotFoundException se o quadro não for encontrado
     * @see ResourceNotFoundException
     */
    @Transactional
    public void updateBoardGroup(Long boardId, Long groupId) {
        Board board = boardService.getBoardById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board com ID " + boardId + " não encontrado."));
        
        board.setGroupId(groupId);
        boardService.updateBoard(board);
    }

    /**
     * Remove um quadro do sistema.
     * 
     * <p>Esta operação é irreversível e remove todos os dados associados ao quadro,
     * incluindo colunas, cards e relacionamentos.</p>
     * 
     * @param boardId ID do quadro a ser removido
     */
    @Transactional
    public void deleteBoard(Long boardId) {
        boardService.deleteBoard(boardId);
    }

    /**
     * Atualiza os detalhes de um card existente.
     * 
     * <p>Permite modificar título, descrição, unidades de progresso e tipo de progresso
     * de um card. O card permanece na mesma coluna após a atualização.</p>
     * 
     * @param cardId ID do card a ser atualizado
     * @param request DTO com os novos dados do card
     * @return DTO com detalhes atualizados do card
     * @throws ResourceNotFoundException se o card ou a coluna não forem encontrados
     * @see UpdateCardDetailsDTO
     * @see CardDetailDTO
     * @see ResourceNotFoundException
     */
    public CardDetailDTO updateCardDetails(Long cardId, UpdateCardDetailsDTO request) {
        // 1. Delega a lógica de negócio para o serviço correspondente.
        Card updatedCard = cardService.updateCardDetails(
                cardId, 
                request.title(), 
                request.description(),
                request.totalUnits(),
                request.currentUnits(),
                request.progressType()
        );

        // 2. Obter o tipo da coluna atual
        BoardColumn column = columnService.getColumnById(updatedCard.getBoardColumnId())
                .orElseThrow(() -> new ResourceNotFoundException("Coluna com ID " + updatedCard.getBoardColumnId() + " não encontrada."));

        // 3. Converte a entidade persistida de volta para um DTO para a resposta da API.
        return new CardDetailDTO(
                updatedCard.getId(),
                updatedCard.getTitle(),
                updatedCard.getDescription(),
                updatedCard.getCardType() != null ? updatedCard.getCardType().getName() : null,
                updatedCard.getTotalUnits(),
                updatedCard.getCurrentUnits(),
                formatDateTime(updatedCard.getCreationDate()),
                formatDateTime(updatedCard.getLastUpdateDate()),
                formatDateTime(updatedCard.getCompletionDate()),
                formatDateTime(updatedCard.getScheduledDate()),
                formatDateTime(updatedCard.getDueDate()),
                column.getKind(), // Adicionar o tipo da coluna
                updatedCard.getProgressTypeOrDefault() // Adicionar o tipo de progresso
        );
    }

    /**
     * Atualiza o tipo de card de um card existente.
     * 
     * @param cardId identificador do card a ser atualizado
     * @param newCardTypeId novo ID do tipo de card
     * @return card atualizado
     * @throws ResourceNotFoundException se o card não for encontrado
     * @throws IllegalArgumentException se o cardTypeId for inválido
     */
    public CardDetailDTO updateCardType(Long cardId, Long newCardTypeId) {
        // 1. Atualiza o tipo do card usando o CardService
        Card updatedCard = cardService.updateCardType(cardId, newCardTypeId);

        // 2. Obter o tipo da coluna atual
        BoardColumn column = columnService.getColumnById(updatedCard.getBoardColumnId())
                .orElseThrow(() -> new ResourceNotFoundException("Coluna com ID " + updatedCard.getBoardColumnId() + " não encontrada."));

        // 3. Converte a entidade persistida de volta para um DTO para a resposta da API
        return new CardDetailDTO(
                updatedCard.getId(),
                updatedCard.getTitle(),
                updatedCard.getDescription(),
                updatedCard.getCardType() != null ? updatedCard.getCardType().getName() : null,
                updatedCard.getTotalUnits(),
                updatedCard.getCurrentUnits(),
                formatDateTime(updatedCard.getCreationDate()),
                formatDateTime(updatedCard.getLastUpdateDate()),
                formatDateTime(updatedCard.getCompletionDate()),
                formatDateTime(updatedCard.getScheduledDate()),
                formatDateTime(updatedCard.getDueDate()),
                column.getKind(),
                updatedCard.getProgressTypeOrDefault()
        );
    }

    /**
     * Formata uma data/hora para exibição na interface.
     * 
     * <p>Método utilitário privado que converte LocalDateTime para string
     * no formato "dd/MM/yy HH:mm". Retorna null se a data for null.</p>
     * 
     * @param localDateTime Data/hora a ser formatada
     * @return String formatada ou null se a data for null
     */
    private String formatDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
        return localDateTime.format(formatter);
    }

    /**
     * Cria uma nova tarefa associada a um card.
     * 
     * <p>Integra o card com o Google Tasks, criando uma tarefa sincronizada
     * que pode ser gerenciada externamente.</p>
     * 
     * @param request DTO com dados para criação da tarefa
     * @see CreateTaskRequestDTO
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

    /**
     * Define as datas de agendamento e vencimento de um card.
     * 
     * <p>Permite definir quando um card deve ser agendado e qual é o prazo
     * limite para sua conclusão. Essas datas são usadas para sincronização
     * com o calendário e cálculo de urgência.</p>
     * 
     * @param cardId identificador do card
     * @param scheduledDate data de agendamento (pode ser null)
     * @param dueDate data de vencimento (pode ser null)
     * @throws ResourceNotFoundException se o card não for encontrado
     * @throws IllegalArgumentException se as datas forem inválidas
     */
    public void setSchedulingDates(Long cardId, LocalDateTime scheduledDate, LocalDateTime dueDate) {
        System.out.println("🔧 TASK MANAGER FACADE - setSchedulingDates chamado para card ID: " + cardId);
        System.out.println("🔧 TASK MANAGER FACADE - Scheduled Date: " + scheduledDate);
        System.out.println("🔧 TASK MANAGER FACADE - Due Date: " + dueDate);
        System.out.println("🔧 TASK MANAGER FACADE - enhancedCardService disponível: " + (enhancedCardService != null));
        
        try {
            // Usar o EnhancedCardService que publica eventos de agendamento
            System.out.println("🔧 TASK MANAGER FACADE - Chamando enhancedCardService.setSchedulingDates()...");
            enhancedCardService.setSchedulingDates(cardId, scheduledDate, dueDate);
            System.out.println("✅ TASK MANAGER FACADE - enhancedCardService.setSchedulingDates() executado com sucesso");
        } catch (Exception e) {
            System.err.println("❌ TASK MANAGER FACADE - Erro ao chamar enhancedCardService.setSchedulingDates(): " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        
        System.out.println("✅ TASK MANAGER FACADE - setSchedulingDates executado com sucesso");
    }

    /**
     * Obtém todos os grupos de quadros disponíveis no sistema.
     * 
     * @return Lista de todos os grupos de quadros
     * @see BoardGroup
     */
    public List<BoardGroup> getAllBoardGroups() {
        return boardGroupService.getAllBoardGroups();
    }

    /**
     * Cria um novo grupo de quadros.
     * 
     * @param name Nome do novo grupo
     * @param description Descrição do grupo
     * @param icon Ícone representativo do grupo
     * @return Grupo criado com ID gerado
     * @see BoardGroup
     */
    public BoardGroup createBoardGroup(String name, String description, String icon) {
        return boardGroupService.createBoardGroup(name, description, icon);
    }

    /**
     * Atualiza um grupo de quadros existente.
     * 
     * @param groupId ID do grupo a ser atualizado
     * @param name Novo nome para o grupo
     * @param description Nova descrição para o grupo
     * @param icon Novo ícone para o grupo
     * @return Grupo atualizado
     * @see BoardGroup
     */
    public BoardGroup updateBoardGroup(Long groupId, String name, String description, String icon) {
        return boardGroupService.updateBoardGroup(groupId, name, description, icon);
    }

    /**
     * Remove um grupo de quadros do sistema.
     * 
     * <p>Esta operação é irreversível. Os quadros associados ao grupo
     * permanecerão no sistema, mas sem associação a grupo.</p>
     * 
     * @param groupId ID do grupo a ser removido
     */
    public void deleteBoardGroup(Long groupId) {
        boardGroupService.deleteBoardGroup(groupId);
    }

    /**
     * Obtém todos os quadros associados a um grupo específico.
     * 
     * @param groupId ID do grupo para filtrar os quadros
     * @return Lista de resumos dos quadros do grupo
     * @see BoardSummaryDTO
     */
    public List<BoardSummaryDTO> getBoardsByGroup(Long groupId) {
        return boardGroupService.getBoardsByGroup(groupId);
    }

    /**
     * Obtém todos os quadros que não estão associados a nenhum grupo.
     * 
     * <p>Implementa as mesmas otimizações de performance dos outros métodos
     * de listagem de quadros, agrupando consultas para eficiência.</p>
     * 
     * @return Lista de resumos dos quadros sem grupo
     * @see BoardSummaryDTO
     */
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

    /**
     * Obtém o serviço de tipos de card para uso na interface.
     * 
     * <p>Método de acesso direto ao serviço especializado, permitindo
     * que a interface acesse funcionalidades específicas do CardTypeService.</p>
     * 
     * @return Instância do CardTypeService
     * @see CardTypeService
     */
    public CardTypeService getCardTypeService() {
        return cardTypeService;
    }
    
    /**
     * Obtém o serviço de grupos de quadros para uso na interface.
     * 
     * @return Instância do BoardGroupService
     * @see BoardGroupService
     */
    public BoardGroupService getBoardGroupService() {
        return boardGroupService;
    }
    
    /**
     * Obtém o repositório de itens de checklist para uso na interface.
     * 
     * @return Instância do CheckListItemRepository
     * @see CheckListItemRepository
     */
    public CheckListItemRepository getChecklistItemRepository() {
        return checklistItemRepository;
    }

    /**
     * Obtém todas as opções de tipos de card disponíveis.
     * 
     * <p>Converte os tipos de card em DTOs de opção para uso na interface,
     * fornecendo dados formatados para seleção pelo usuário.</p>
     *
     * @return Lista de opções de tipos de card
     * @see CardTypeOptionDTO
     */
    @Transactional(readOnly = true)
    public List<CardTypeOptionDTO> getAllCardTypeOptions() {
        return cardTypeService.getAllCardTypes().stream()
                .map(CardTypeDTO::from)
                .map(CardTypeOptionDTO::fromCardType)
                .collect(Collectors.toList());
    }

    /**
     * Lista todos os tipos de card disponíveis.
     * 
     * @return Lista de todos os tipos de card
     */
    @Transactional(readOnly = true)
    public List<CardType> getAllCardTypes() {
        return cardTypeService.getAllCardTypes();
    }

    /**
     * Sugere o tipo de card padrão baseado nas configurações do sistema.
     * 
     * <p>Utiliza as configurações de metadados da aplicação para determinar
     * qual tipo de card deve ser sugerido como padrão para novos cards.</p>
     *
     * @return ID do tipo de card sugerido como padrão
     */
    @Transactional(readOnly = true)
    public Long suggestDefaultCardTypeId() {
        return cardTypeService.suggestDefaultCardTypeId();
    }

    /**
     * Sugere o tipo de progresso padrão baseado nas configurações do sistema.
     * 
     * <p>Utiliza as configurações de metadados da aplicação para determinar
     * qual tipo de progresso deve ser sugerido como padrão para novos cards.</p>
     *
     * @return Tipo de progresso sugerido como padrão
     */
    @Transactional(readOnly = true)
    public org.desviante.model.enums.ProgressType suggestDefaultProgressType() {
        return cardTypeService.suggestDefaultProgressType();
    }

    /**
     * Sugere o grupo padrão baseado nas configurações do sistema.
     * 
     * <p>Utiliza as configurações de metadados da aplicação para determinar
     * qual grupo deve ser sugerido como padrão para novos quadros.</p>
     *
     * @return ID do grupo sugerido como padrão, ou null se não houver grupos
     */
    @Transactional(readOnly = true)
    public Long suggestDefaultBoardGroupId() {
        return boardGroupService.suggestDefaultBoardGroupId();
    }

    /**
     * Sugere o grupo padrão como objeto completo.
     * 
     * <p>Similar ao método {@link #suggestDefaultBoardGroupId()}, mas retorna
     * o objeto completo do grupo em vez de apenas o ID.</p>
     *
     * @return Grupo padrão sugerido, ou null se não houver grupos
     * @see BoardGroup
     */
    @Transactional(readOnly = true)
    public BoardGroup suggestDefaultBoardGroup() {
        return boardGroupService.suggestDefaultBoardGroup();
    }

    /**
     * Move um card para cima na mesma coluna.
     * 
     * <p>Altera a ordem do card dentro da coluna, movendo-o uma posição acima.
     * A operação falha silenciosamente se o card já estiver no topo da coluna.</p>
     * 
     * @param cardId ID do card a ser movido
     * @return true se o card foi movido, false se já estava no topo
     */
    public boolean moveCardUp(Long cardId) {
        return cardService.moveCardUp(cardId);
    }

    /**
     * Move um card para baixo na mesma coluna.
     * 
     * <p>Altera a ordem do card dentro da coluna, movendo-o uma posição abaixo.
     * A operação falha silenciosamente se o card já estiver na base da coluna.</p>
     * 
     * @param cardId ID do card a ser movido
     * @return true se o card foi movido, false se já estava na base
     */
    public boolean moveCardDown(Long cardId) {
        return cardService.moveCardDown(cardId);
    }

    /**
     * Verifica se um card pode ser movido para cima.
     * 
     * <p>Determina se o card está em uma posição que permite movimentação
     * para cima dentro da coluna.</p>
     * 
     * @param cardId ID do card
     * @return true se o card pode ser movido para cima, false caso contrário
     */
    public boolean canMoveCardUp(Long cardId) {
        return cardService.canMoveCardUp(cardId);
    }

    /**
     * Verifica se um card pode ser movido para baixo.
     * 
     * <p>Determina se o card está em uma posição que permite movimentação
     * para baixo dentro da coluna.</p>
     * 
     * @param cardId ID do card
     * @return true se o card pode ser movido para baixo, false caso contrário
     */
    public boolean canMoveCardDown(Long cardId) {
        return cardService.canMoveCardDown(cardId);
    }

    /**
     * Obtém um card pelo seu ID.
     * 
     * @param cardId ID do card a ser buscado
     * @return Optional contendo o card se encontrado, ou vazio caso contrário
     * @see Card
     */
    public Optional<Card> getCardById(Long cardId) {
        return cardService.getCardById(cardId);
    }

    /**
     * Obtém detalhes completos de um card pelo seu ID.
     * 
     * <p>Converte a entidade Card em um DTO detalhado, incluindo informações
     * da coluna e tipo de progresso para exibição na interface.</p>
     * 
     * @param cardId ID do card a ser buscado
     * @return Optional contendo os detalhes do card se encontrado, ou vazio caso contrário
     * @see CardDetailDTO
     */
    public Optional<CardDetailDTO> getCardDetailById(Long cardId) {
        return cardService.getCardById(cardId)
                .map(card -> {
                    // Obter o tipo da coluna para incluir no DTO
                    BoardColumn column = columnService.getColumnById(card.getBoardColumnId())
                            .orElse(null);
                    
                    return new CardDetailDTO(
                            card.getId(),
                            card.getTitle(),
                            card.getDescription(),
                            card.getCardType() != null ? card.getCardType().getName() : null,
                            card.getTotalUnits(),
                            card.getCurrentUnits(),
                            formatDateTime(card.getCreationDate()),
                            formatDateTime(card.getLastUpdateDate()),
                            formatDateTime(card.getCompletionDate()),
                            formatDateTime(card.getScheduledDate()),
                            formatDateTime(card.getDueDate()),
                            column != null ? column.getKind() : null,
                            card.getProgressTypeOrDefault()
                    );
                });
    }

    /**
     * Cria um evento no calendário baseado nas informações de agendamento de um card.
     * 
     * <p>Esta operação é independente e não afeta o estado do card.
     * Falhas na criação do evento não causam rollback nas datas do card.</p>
     * 
     * @param cardId identificador do card
     * @return true se o evento foi criado com sucesso, false caso contrário
     */
    public boolean createCalendarEvent(Long cardId) {
        return calendarEventService.createCalendarEvent(cardId);
    }

    /**
     * Cria uma tarefa no Google Tasks baseada nas informações de um card.
     * 
     * <p>Esta operação é independente e não afeta o estado do card.
     * Falhas na criação da tarefa não causam rollback nas datas do card.</p>
     * 
     * @param cardId identificador do card
     * @return true se a tarefa foi criada com sucesso, false caso contrário
     */
    public boolean createGoogleTask(Long cardId) {
        return googleTaskCreationService.createGoogleTask(cardId);
    }

    /**
     * Verifica se um card pode ter um evento criado no calendário.
     * 
     * @param cardId identificador do card
     * @return true se o card pode ter evento criado, false caso contrário
     */
    public boolean canCreateCalendarEvent(Long cardId) {
        return calendarEventService.canCreateCalendarEvent(cardId);
    }

    /**
     * Verifica se um card pode ter uma tarefa criada no Google Tasks.
     * 
     * @param cardId identificador do card
     * @return true se o card pode ter tarefa criada, false caso contrário
     */
    public boolean canCreateGoogleTask(Long cardId) {
        return googleTaskCreationService.canCreateGoogleTask(cardId);
    }

    /**
     * Deleta um evento do calendário.
     * 
     * <p>Esta operação é independente e não afeta o estado do card.
     * Falhas na exclusão do evento não causam rollback nas datas do card.</p>
     * 
     * @param eventId identificador do evento
     * @return true se o evento foi deletado com sucesso, false caso contrário
     */
    public boolean deleteCalendarEvent(String eventId) {
        return calendarEventService.deleteCalendarEvent(eventId);
    }

    /**
     * Verifica se um evento pode ser deletado.
     * 
     * @param eventId identificador do evento
     * @return true se o evento pode ser deletado, false caso contrário
     */
    public boolean canDeleteCalendarEvent(String eventId) {
        return calendarEventService.canDeleteCalendarEvent(eventId);
    }
}