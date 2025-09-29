package org.desviante.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.desviante.config.AppMetadataConfig;
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
import lombok.extern.slf4j.Slf4j;

/**
 * Gerencia as operações de negócio relacionadas aos grupos de quadros.
 * 
 * <p>Responsável por implementar a lógica de negócio para criação, atualização,
 * consulta e remoção de grupos de quadros. Esta camada de serviço implementa
 * validações importantes como unicidade de nomes, verificação de dependências
 * antes da remoção e geração automática de cores para identificação visual.</p>
 * 
 * <p>Implementa funcionalidades avançadas como cálculo de resumos de quadros
 * por grupo, incluindo estatísticas de progresso e status baseados na
 * distribuição de cards entre colunas de diferentes tipos.</p>
 * 
 * <p>Utiliza transações para garantir consistência dos dados, com operações
 * de leitura marcadas como readOnly para otimização de performance.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see BoardGroup
 * @see BoardSummaryDTO
 * @see BoardGroupRepository
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BoardGroupService {

    private final BoardGroupRepository boardGroupRepository;
    private final BoardRepository boardRepository;
    private final BoardColumnService columnService;
    private final CardService cardService;
    private final AppMetadataConfig appMetadataConfig;

    /**
     * Busca todos os grupos de quadros disponíveis no sistema.
     * 
     * @return lista de todos os grupos
     */
    @Transactional(readOnly = true)
    public List<BoardGroup> getAllBoardGroups() {
        return boardGroupRepository.findAll();
    }
    
    /**
     * Cria um novo grupo de quadros com validações de integridade.
     * 
     * <p>Valida que o nome é obrigatório e único (case-insensitive).
     * Gera automaticamente uma cor aleatória para identificação visual
     * e define um ícone padrão se não fornecido.</p>
     * 
     * @param name nome do novo grupo
     * @param description descrição opcional do grupo
     * @param icon ícone opcional do grupo (usa "📁" como padrão)
     * @return grupo criado com ID gerado
     * @throws IllegalArgumentException se o nome for vazio ou já existir
     */
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

    /**
     * Atualiza um grupo de quadros existente.
     * 
     * <p>Valida a existência do grupo e a unicidade do novo nome
     * (excluindo o próprio grupo da verificação). Mantém a cor
     * existente para preservar a identidade visual do grupo.</p>
     * 
     * @param groupId identificador do grupo a ser atualizado
     * @param name novo nome do grupo
     * @param description nova descrição do grupo
     * @param icon novo ícone do grupo
     * @return grupo atualizado
     * @throws ResourceNotFoundException se o grupo não for encontrado
     * @throws IllegalArgumentException se o nome for vazio ou já existir
     */
    @Transactional
    public BoardGroup updateBoardGroup(Long groupId, String name, String description, String icon) {
        // Validação do grupo existente
        BoardGroup existingGroup = boardGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo com ID " + groupId + " não encontrado."));
        
        // Validações de entrada
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Nome do grupo é obrigatório");
        }
            
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

    /**
     * Remove um grupo de quadros com validação de dependências.
     * 
     * <p>Verifica se existem quadros associados ao grupo antes de permitir
     * a remoção, garantindo integridade referencial. Se houver quadros
     * associados, lança exceção informando quantos quadros precisam ser
     * movidos antes da remoção.</p>
     * 
     * @param groupId identificador do grupo a ser removido
     * @throws ResourceNotFoundException se o grupo não for encontrado
     * @throws IllegalArgumentException se existirem quadros associados ao grupo
     */
    @Transactional
    public void deleteBoardGroup(Long groupId) {
        // Validação do grupo existente
        BoardGroup existingGroup = boardGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo com ID " + groupId + " não encontrado."));
        
        // Verificar se o grupo é o padrão configurado
        Optional<Long> defaultGroupId = appMetadataConfig.getDefaultBoardGroupId();
        if (defaultGroupId.isPresent() && defaultGroupId.get().equals(groupId)) {
            throw new IllegalArgumentException("Não é possível deletar o grupo '" + existingGroup.getName() + 
                    "' pois ele está configurado como grupo padrão no sistema. " +
                    "Altere a configuração padrão antes de deletar o grupo.");
        }
        
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

    /**
     * Busca resumos de todos os quadros de um grupo específico.
     * 
     * <p>Calcula estatísticas detalhadas de cada quadro, incluindo
     * percentuais de progresso baseados na distribuição de cards
     * entre colunas de diferentes tipos (INITIAL, PENDING, FINAL).
     * Também determina o status geral do quadro baseado na distribuição.</p>
     * 
     * <p>Otimiza consultas através de busca em lote de colunas e cards
     * para evitar problemas N+1 de performance.</p>
     * 
     * @param groupId identificador do grupo
     * @return lista de resumos dos quadros do grupo
     * @throws ResourceNotFoundException se o grupo não for encontrado
     */
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
    
    /**
     * Calcula o resumo estatístico de um quadro específico.
     * 
     * <p>Analisa a distribuição de cards entre colunas de diferentes tipos
     * para determinar percentuais de progresso e status geral do quadro.
     * Status possíveis: "Vazio", "Não iniciado", "Em andamento", "Concluído".</p>
     * 
     * @param board quadro para cálculo do resumo
     * @param columnsByBoardId mapa de colunas agrupadas por quadro
     * @param cardsByColumnId mapa de cards agrupados por coluna
     * @return resumo estatístico do quadro
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
     * Verifica se existe um grupo com o nome especificado.
     * 
     * <p>Utilizada para validação de unicidade de nomes de grupos.
     * A verificação é case-insensitive para evitar duplicatas com diferenças
     * apenas de maiúsculas/minúsculas.</p>
     * 
     * @param name nome do grupo a ser verificado
     * @return true se o grupo existe, false caso contrário
     */
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return boardGroupRepository.existsByName(name);
    }

    /**
     * Gera uma cor hexadecimal aleatória para identificação visual do grupo.
     * 
     * <p>Utiliza um conjunto predefinido de cores para garantir boa
     * legibilidade e contraste adequado na interface do usuário.</p>
     * 
     * @return cor hexadecimal no formato #RRGGBB
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

    /**
     * Sugere um ID de grupo padrão baseado na configuração da aplicação.
     * 
     * <p>Este método verifica primeiro se há um grupo padrão configurado no AppMetadataConfig.
     * Se houver um grupo válido configurado, ele é sempre respeitado.
     * Se for explicitamente configurado como "Sem Grupo" (null), retorna null.
     * Só usa fallback quando não há configuração ou o grupo configurado não existe.</p>
     *
     * @return ID do grupo sugerido como padrão, ou null se não houver grupos ou se "Sem Grupo" for configurado
     */
    @Transactional(readOnly = true)
    public Long suggestDefaultBoardGroupId() {
        log.debug("Iniciando sugestão de grupo padrão...");
        
        // Primeiro, verificar se há um grupo padrão configurado
        Optional<Long> configuredDefaultId = appMetadataConfig.getDefaultBoardGroupId();
        
        // ⭐ CORREÇÃO: Verificar se o Optional está vazio (indicando null) ou se contém um valor
        if (configuredDefaultId.isPresent()) {
            Long groupId = configuredDefaultId.get();
            
            if (groupId == null) {
                // ⭐ CONFIGURAÇÃO EXPLÍCITA PARA "SEM GRUPO" - RETORNAR NULL
                log.debug("Configuração explícita para 'Sem Grupo' - retornando null");
                return null; // ⭐ IMPORTANTE: retornar null para "Sem Grupo"
            } else {
                // Verificar se o grupo configurado ainda existe
                try {
                    BoardGroup configuredGroup = boardGroupRepository.findById(groupId)
                            .orElse(null);
                    if (configuredGroup != null) {
                        log.debug("Usando grupo padrão configurado: {} (ID: {})", 
                                 configuredGroup.getName(), configuredGroup.getId());
                        return configuredGroup.getId(); // ⭐ SEMPRE retornar o grupo configurado se existir
                    } else {
                        log.warn("Grupo padrão configurado com ID {} não encontrado no banco, usando fallback", groupId);
                    }
                } catch (Exception e) {
                    log.warn("Erro ao buscar grupo padrão configurado (ID: {}): {}, usando fallback", 
                            groupId, e.getMessage());
                }
            }
        } else {
            // ⭐ IMPORTANTE: Optional.empty() significa que o campo é null (explicitamente configurado como "Sem Grupo")
            log.debug("Configuração explícita para 'Sem Grupo' (Optional.empty) - retornando null");
            return null; // ⭐ RETORNAR NULL para "Sem Grupo"
        }
        
        // ⭐ FALLBACK: só usar quando não há grupo configurado ou o grupo configurado não existe
        log.debug("Usando fallback inteligente para encontrar grupo apropriado");
        List<BoardGroup> allGroups = getAllBoardGroups();
        if (!allGroups.isEmpty()) {
            // Tentar encontrar um grupo com nome específico como fallback
            Optional<BoardGroup> fallbackGroup = allGroups.stream()
                    .filter(group -> "Trabalho".equalsIgnoreCase(group.getName()) ||
                                   "Livros".equalsIgnoreCase(group.getName()) ||
                                   "Pessoal".equalsIgnoreCase(group.getName()))
                    .findFirst();
            
            if (fallbackGroup.isPresent()) {
                BoardGroup selectedFallback = fallbackGroup.get();
                log.debug("Usando grupo específico como fallback: {} (ID: {})", 
                         selectedFallback.getName(), selectedFallback.getId());
                return selectedFallback.getId();
            } else {
                // Se não encontrar grupo específico, usar o primeiro disponível
                BoardGroup firstGroup = allGroups.get(0);
                log.debug("Usando primeiro grupo disponível como fallback: {} (ID: {})", 
                         firstGroup.getName(), firstGroup.getId());
                return firstGroup.getId();
            }
        }
        
        // Nenhum grupo disponível
        log.warn("Nenhum grupo disponível para sugestão");
        return null;
    }

    /**
     * Obtém o grupo padrão sugerido como objeto completo.
     * 
     * <p>Este método retorna o objeto BoardGroup completo do grupo padrão sugerido,
     * útil para interfaces que precisam de informações completas do grupo.</p>
     *
     * @return grupo padrão sugerido, ou null se não houver grupos
     */
    @Transactional(readOnly = true)
    public BoardGroup suggestDefaultBoardGroup() {
        log.debug("Obtendo grupo padrão sugerido como objeto completo...");
        
        Long suggestedGroupId = suggestDefaultBoardGroupId();
        if (suggestedGroupId != null) {
            BoardGroup suggestedGroup = boardGroupRepository.findById(suggestedGroupId).orElse(null);
            if (suggestedGroup != null) {
                log.debug("Grupo padrão sugerido: {} (ID: {})", 
                         suggestedGroup.getName(), suggestedGroup.getId());
            } else {
                log.warn("Grupo sugerido com ID {} não encontrado no banco", suggestedGroupId);
            }
            return suggestedGroup;
        }
        
        log.debug("Nenhum grupo padrão sugerido (ID é null)");
        return null;
    }
}