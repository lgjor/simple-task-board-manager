package org.desviante.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.desviante.exception.ResourceNotFoundException;
import org.desviante.model.Board;
import org.desviante.model.BoardColumn;
import org.desviante.model.BoardGroup;
import org.desviante.model.Card;
import org.desviante.repository.BoardGroupRepository;
import org.desviante.repository.BoardRepository;
import org.desviante.service.dto.BoardSummaryDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardGroupService {

    private final BoardGroupRepository boardGroupRepository;
    private final BoardRepository boardRepository;
    private final BoardColumnService columnService;
    private final CardService cardService;

    @Transactional(readOnly = true)
    public List<BoardGroup> getAllBoardGroups() {
        return boardGroupRepository.findAll();
    }
    
    @Transactional
    public BoardGroup createBoardGroup(String name, String description, String icon) {
        // Validação dos parâmetros obrigatórios
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Nome do grupo é obrigatório");
        }
        
        // Validação de unicidade do nome (case-insensitive)
        String trimmedName = name.trim();
        if (boardGroupRepository.findByName(trimmedName).isPresent()) {
            throw new IllegalArgumentException("Já existe um grupo com o nome '" + trimmedName + "'. Escolha um nome diferente.");
        }
        
        // Gerar cor aleatória no backend
        String color = generateRandomColor();
        
        // Criação do novo grupo
        BoardGroup newGroup = new BoardGroup();
        newGroup.setName(trimmedName);
        newGroup.setDescription(description != null ? description.trim() : "");
        newGroup.setColor(color);
        newGroup.setIcon(icon != null ? icon : "📁"); // Usar ícone fornecido ou padrão
        newGroup.setCreationDate(LocalDateTime.now());
        // Removido setDefault - não precisamos mais de grupo padrão
        
        return boardGroupRepository.save(newGroup);
    }

    @Transactional
    public BoardGroup updateBoardGroup(Long groupId, String name, String description, String icon) {
        // Validação do grupo existente
        BoardGroup existingGroup = boardGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo com ID " + groupId + " não encontrado."));
        
        // Validações de entrada
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Nome do grupo é obrigatório");
        }
        
        // Removida validação de grupo padrão - não precisamos mais proteger grupo especial
        
        // Validação de unicidade do nome (case-insensitive, excluindo o próprio grupo)
        String trimmedName = name.trim();
        if (boardGroupRepository.findByNameExcludingId(trimmedName, groupId).isPresent()) {
            throw new IllegalArgumentException("Já existe um grupo com o nome '" + trimmedName + "'. Escolha um nome diferente.");
        }
        
        // Atualização dos campos
        existingGroup.setName(trimmedName);
        existingGroup.setDescription(description != null ? description.trim() : "");
        // Manter a cor existente - não alterar a cor no update
        existingGroup.setIcon(icon != null ? icon : "📁"); // Ícone padrão se não fornecido
        
        return boardGroupRepository.save(existingGroup);
    }

    @Transactional
    public void deleteBoardGroup(Long groupId) {
        // Validação do grupo existente
        BoardGroup existingGroup = boardGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo com ID " + groupId + " não encontrado."));
        
        // Removida validação de grupo padrão - não precisamos mais proteger grupo especial
        
        // Verificar se existem boards associados ao grupo
        List<Board> boardsInGroup = boardRepository.findByGroupId(groupId);
        if (boardsInGroup != null && !boardsInGroup.isEmpty()) {
            throw new IllegalArgumentException("Não é possível deletar o grupo '" + existingGroup.getName() + 
                    "' pois existem " + boardsInGroup.size() + " board(s) associado(s). " +
                    "Mova os boards para outro grupo antes de deletar.");
        }
        
        // Deletar o grupo
        boardGroupRepository.deleteById(groupId);
    }

    @Transactional(readOnly = true)
    public List<BoardSummaryDTO> getBoardsByGroup(Long groupId) {
        // Validação do grupo
        Optional<BoardGroup> group = boardGroupRepository.findById(groupId);
        if (group.isEmpty()) {
            throw new ResourceNotFoundException("Grupo com ID " + groupId + " não encontrado.");
        }
        
        // Busca todos os boards do grupo
        List<Board> boards = boardRepository.findByGroupId(groupId);
        
        if (boards.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Busca todas as colunas para todos os boards
        List<Long> boardIds = boards.stream().map(Board::getId).collect(Collectors.toList());
        List<BoardColumn> allColumns = columnService.getColumnsForBoards(boardIds);
        
        // Agrupa colunas por board
        Map<Long, List<BoardColumn>> columnsByBoardId = allColumns.stream()
                .collect(Collectors.groupingBy(BoardColumn::getBoardId));
        
        // Busca todos os cards para todas as colunas
        List<Long> columnIds = allColumns.stream().map(BoardColumn::getId).collect(Collectors.toList());
        List<Card> allCards = cardService.getCardsForColumns(columnIds);
        
        // Agrupa cards por coluna
        Map<Long, List<Card>> cardsByColumnId = allCards.stream()
                .collect(Collectors.groupingBy(Card::getBoardColumnId));
        
        // Calcula resumo para cada board
        return boards.stream()
                .map(board -> calculateBoardSummary(board, columnsByBoardId, cardsByColumnId))
                .collect(Collectors.toList());
    }
    
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

        // Lógica de status do board
        String boardStatus;
        if (initialCount == totalCards) {
            boardStatus = "Não iniciado";
        } else if (finalCount == totalCards) {
            boardStatus = "Concluído";
        } else {
            boardStatus = "Em andamento";
        }

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
                boardStatus,
                board.getGroup()
        );
    }

    /**
     * Gera uma cor hexadecimal aleatória
     */
    private String generateRandomColor() {
        // Array de cores predefinidas para garantir boa legibilidade
        String[] predefinedColors = {
            "#FF6B6B", // Vermelho
            "#4ECDC4", // Turquesa
            "#45B7D1", // Azul
            "#96CEB4", // Verde claro
            "#FFEAA7", // Amarelo
            "#DDA0DD", // Lavanda
            "#98D8C8", // Verde água
            "#F7DC6F", // Dourado
            "#BB8FCE", // Roxo
            "#85C1E9", // Azul claro
            "#F8C471", // Laranja
            "#82E0AA", // Verde
            "#F1948A", // Rosa
            "#85C1E9", // Azul
            "#F7DC6F"  // Amarelo
        };
        
        // Selecionar uma cor aleatória do array
        int randomIndex = (int) (Math.random() * predefinedColors.length);
        return predefinedColors[randomIndex];
    }
}
