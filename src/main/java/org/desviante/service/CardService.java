package org.desviante.service;

import lombok.RequiredArgsConstructor;
import org.desviante.exception.ResourceNotFoundException;
import org.desviante.model.BoardColumn;
import org.desviante.model.Card;
import org.desviante.model.CardType;
import org.desviante.model.enums.BoardColumnKindEnum;
import org.desviante.model.enums.ProgressType;

import org.desviante.repository.BoardColumnRepository;
import org.desviante.repository.CardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Gerencia as operações de negócio relacionadas aos cards (tarefas).
 * 
 * <p>Responsável por implementar a lógica de negócio para criação, movimentação,
 * atualização e remoção de cards. Esta camada de serviço garante a integridade
 * dos dados através de validações antes das operações de persistência.</p>
 * 
 * <p>Implementa regras de negócio importantes como controle de datas de criação,
 * atualização e conclusão, além de lógica específica para movimentação entre
 * colunas de diferentes tipos (INITIAL, PENDING, FINAL).</p>
 * 
 * <p>Utiliza transações para garantir consistência dos dados, com operações
 * de leitura marcadas como readOnly para otimização de performance.</p>
 * 
 * <p>Progresso e status estão desacoplados: o progresso é independente da coluna
 * onde o card está localizado, permitindo maior flexibilidade ao usuário.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see Card
 * @see BoardColumn
 * @see BoardColumnKindEnum
 * @see CardRepository
 */
@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final BoardColumnRepository columnRepository;
    private final CardTypeService CardTypeService;

    /**
     * Cria um novo card em uma coluna específica.
     * 
     * <p>Valida a existência da coluna pai antes de criar o card,
     * prevenindo a criação de cards "órfãos". Define automaticamente
     * as datas de criação e última atualização, e garante que um
     * novo card nunca esteja concluído (completionDate = null).</p>
     * 
     * <p>O tipo do card determina se ele suporta acompanhamento de progresso.
     * Cards do tipo CARD não utilizam campos de progresso, enquanto outros
     * tipos podem ter totalUnits e currentUnits definidos.</p>
     * 
     * @param title título do novo card
     * @param description descrição do novo card
     * @param parentColumnId identificador da coluna pai
     * @param cardTypeId ID do tipo do card (CARD, BOOK, VIDEO, COURSE)
     * @return card criado com ID gerado
     * @throws ResourceNotFoundException se a coluna pai não for encontrada
     */
    @Transactional
    public Card createCard(String title, String description, Long parentColumnId, Long cardTypeId) {
        return createCard(title, description, parentColumnId, cardTypeId, ProgressType.NONE);
    }
    
    @Transactional
    public Card createCard(String title, String description, Long parentColumnId, Long cardTypeId, ProgressType progressType) {
        // Valida se o título não está vazio
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Título do card não pode ser vazio");
        }

        // Valida se a coluna pai existe antes de criar o card.
        columnRepository.findById(parentColumnId)
                .orElseThrow(() -> new ResourceNotFoundException("Coluna com ID " + parentColumnId + " não encontrada."));

        Card newCard = new Card();
        newCard.setTitle(title.trim());
        newCard.setDescription(description);
        newCard.setBoardColumnId(parentColumnId);
        
        // Se não especificado, usar o tipo CARD padrão
        Long typeId = cardTypeId != null ? cardTypeId : CardTypeService.getDefaultCardTypeId();
        newCard.setCardTypeId(typeId);

        // Carregar o objeto CardType completo para o card
        if (typeId != null) {
            CardType cardType = CardTypeService.getCardTypeById(typeId);
            newCard.setCardType(cardType);
        }

        // Definir o tipo de progresso
        newCard.setProgressType(progressType != null ? progressType : ProgressType.NONE);
        
        // Configurar campos de progresso apenas para cards que suportam progresso
        if (newCard.getProgressTypeOrDefault().isEnabled()) {
            newCard.setTotalUnits(1);
            newCard.setCurrentUnits(0);
        } else {
            // Cards sem progresso não devem ter unidades definidas
            newCard.setTotalUnits(null);
            newCard.setCurrentUnits(null);
        }

        // LÓGICA DE NEGÓCIO: Definir as datas no serviço é a prática correta.
        LocalDateTime now = LocalDateTime.now();
        newCard.setCreationDate(now);
        newCard.setLastUpdateDate(now);
        // Um novo card nunca está concluído.
        newCard.setCompletionDate(null);

        // Define o order_index como o próximo valor disponível na coluna
        // Busca o maior order_index existente para evitar race conditions
        Integer maxOrderIndex = cardRepository.findMaxOrderIndexByColumnId(parentColumnId);
        int nextOrderIndex = (maxOrderIndex != null ? maxOrderIndex : 0) + 1;
        newCard.setOrderIndex(nextOrderIndex);

        Card savedCard = cardRepository.save(newCard);
        
        // Carregar o objeto CardType completo após salvar
        if (savedCard.getCardTypeId() != null) {
            CardType cardType = CardTypeService.getCardTypeById(savedCard.getCardTypeId());
            savedCard.setCardType(cardType);
        }
        
        return savedCard;
    }

    /**
     * Move um card para uma nova coluna com lógica de conclusão automática.
     * 
     * <p>Valida a existência tanto do card quanto da coluna de destino.
     * Atualiza automaticamente a data de última modificação. Implementa
     * lógica de conclusão baseada no tipo da coluna de destino:
     * - Se a coluna for do tipo FINAL: define a data de conclusão
     * - Se for de outro tipo: remove a data de conclusão (null)</p>
     * 
     * @param cardId identificador do card a ser movido
     * @param newColumnId identificador da nova coluna de destino
     * @return card atualizado com nova coluna e datas
     * @throws ResourceNotFoundException se o card ou a coluna não forem encontrados
     */
    @Transactional
    public Card moveCardToColumn(Long cardId, Long newColumnId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card com ID " + cardId + " não encontrado."));

        BoardColumn newColumn = columnRepository.findById(newColumnId)
                .orElseThrow(() -> new ResourceNotFoundException("Coluna de destino com ID " + newColumnId + " não encontrada."));

        card.setBoardColumnId(newColumnId);
        card.setLastUpdateDate(LocalDateTime.now());

        // Define o order_index como o próximo valor disponível na nova coluna
        // Busca o maior order_index existente para evitar race conditions
        Integer maxOrderIndex = cardRepository.findMaxOrderIndexByColumnId(newColumnId);
        int nextOrderIndex = (maxOrderIndex != null ? maxOrderIndex : 0) + 1;
        card.setOrderIndex(nextOrderIndex);

        // LÓGICA DE CONCLUSÃO REFINADA:
        // Se a nova coluna for do tipo FINAL, define a data de conclusão.
        // Caso contrário, garante que a data de conclusão seja nula.
        if (newColumn.getKind() == BoardColumnKindEnum.FINAL) {
            card.setCompletionDate(LocalDateTime.now());
        } else {
            card.setCompletionDate(null);
        }

        return cardRepository.save(card);
    }

    /**
     * Atualiza o título e a descrição de um card existente.
     * 
     * <p>Valida a existência do card antes de permitir a atualização.
     * Atualiza automaticamente a data de última modificação para
     * rastrear quando o card foi alterado pela última vez.</p>
     * 
     * @param cardId identificador do card a ser atualizado
     * @param newTitle novo título do card
     * @param newDescription nova descrição do card
     * @return card atualizado
     * @throws ResourceNotFoundException se o card não for encontrado
     */
    @Transactional
    public Card updateCardDetails(Long cardId, String newTitle, String newDescription) {
        return updateCardDetails(cardId, newTitle, newDescription, null, null);
    }

    /**
     * Atualiza os detalhes de um card (título, descrição e progresso).
     * 
     * <p>Valida a existência do card antes de tentar atualizá-lo,
     * garantindo que apenas cards existentes sejam modificados.
     * Atualiza automaticamente a data de última modificação para
     * refletir a mudança.</p>
     * 
     * <p>Para cards que suportam progresso (BOOK, VIDEO, COURSE),
     * atualiza também os campos totalUnits e currentUnits conforme
     * fornecido nos parâmetros.</p>
     * 
     * @param cardId identificador do card a ser atualizado
     * @param newTitle novo título do card
     * @param newDescription nova descrição do card
     * @param totalUnits total de unidades para progresso (pode ser null)
     * @param currentUnits unidades atuais para progresso (pode ser null)
     * @return card atualizado
     * @throws ResourceNotFoundException se o card não for encontrado
     */
    @Transactional
    public Card updateCardDetails(Long cardId, String newTitle, String newDescription, Integer totalUnits, Integer currentUnits) {
        return updateCardDetails(cardId, newTitle, newDescription, totalUnits, currentUnits, null);
    }
    
    @Transactional
    public Card updateCardDetails(Long cardId, String newTitle, String newDescription, Integer totalUnits, Integer currentUnits, ProgressType progressType) {
        // 1. Encontra o card ou lança uma exceção se não existir.
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card com ID " + cardId + " não encontrado para atualização."));

        // 2. Atualiza as propriedades do objeto.
        card.setTitle(newTitle);
        card.setDescription(newDescription);
        
        // 3. Atualizar campos de progresso se fornecidos
        if (totalUnits != null) {
            // Para CHECKLIST, aceitar qualquer valor pois o progresso é calculado automaticamente
            if (progressType == ProgressType.CHECKLIST) {
                // Para checklist, usar valor padrão que será calculado automaticamente
                card.setTotalUnits(1); // Valor mínimo para evitar erros
            } else {
                // Para outros tipos, validar normalmente
                if (totalUnits <= 0) {
                    throw new IllegalArgumentException("O total deve ser maior que zero.");
                }
                card.setTotalUnits(totalUnits);
            }
        }
        if (currentUnits != null) {
            // Para CHECKLIST, aceitar qualquer valor pois o progresso é calculado automaticamente
            if (progressType == ProgressType.CHECKLIST) {
                // Para checklist, usar valor padrão que será calculado automaticamente
                card.setCurrentUnits(0); // Valor inicial para checklist
            } else {
                // Para outros tipos, validar normalmente
                if (currentUnits < 0) {
                    throw new IllegalArgumentException("O valor atual não pode ser negativo.");
                }
                // Garantir que current não seja maior que total
                if (totalUnits != null && currentUnits > totalUnits) {
                    throw new IllegalArgumentException("O valor atual não pode ser maior que o total.");
                }
                card.setCurrentUnits(currentUnits);
            }
        }
        
        // 4. Atualizar tipo de progresso se fornecido
        if (progressType != null) {
            card.setProgressType(progressType);
        }

        // 5. REGRA DE NEGÓCIO: Sempre atualiza a data da última modificação.
        card.setLastUpdateDate(LocalDateTime.now());

        // 6. Salva e retorna a entidade atualizada.
        return cardRepository.save(card);
    }

    /**
     * Busca um card específico pelo ID.
     * 
     * @param id identificador único do card
     * @return Optional contendo o card se encontrado, vazio caso contrário
     */
    @Transactional(readOnly = true)
    public Optional<Card> getCardById(Long id) {
        Optional<Card> cardOpt = cardRepository.findById(id);
        if (cardOpt.isPresent()) {
            Card card = cardOpt.get();
            // Carregar o objeto CardType completo se necessário
            if (card.getCardTypeId() != null && card.getCardType() == null) {
                try {
                    CardType cardType = CardTypeService.getCardTypeById(card.getCardTypeId());
                    card.setCardType(cardType);
                } catch (ResourceNotFoundException e) {
                    // Se o tipo não for encontrado, deixar como null
                }
            }
        }
        return cardOpt;
    }

    /**
     * Busca cards para múltiplas colunas em uma única operação.
     * 
     * <p>Este método é otimizado para evitar múltiplas chamadas ao banco
     * de dados (problema N+1). O repositório já trata listas vazias
     * adequadamente, garantindo comportamento previsível.</p>
     * 
     * @param columnIds lista de identificadores das colunas
     * @return lista de cards pertencentes às colunas especificadas
     */
    @Transactional(readOnly = true)
    public List<Card> getCardsForColumns(List<Long> columnIds) {
        // O repositório já trata a lista vazia, então a delegação direta é segura.
        List<Card> cards = cardRepository.findByBoardColumnIdIn(columnIds);
        
        // Carregar os tipos de card para todos os cards
        for (Card card : cards) {
            if (card.getCardTypeId() != null && card.getCardType() == null) {
                try {
                    CardType cardType = CardTypeService.getCardTypeById(card.getCardTypeId());
                    card.setCardType(cardType);
                } catch (ResourceNotFoundException e) {
                    // Se o tipo não for encontrado, deixar como null
                }
            }
        }
        
        return cards;
    }

    /**
     * Remove um card do sistema com validação de existência.
     * 
     * <p>Verifica se o card existe antes de tentar removê-lo, tornando
     * a operação mais segura e o comportamento da API mais previsível.
     * Lança exceção específica se o card não for encontrado.</p>
     * 
     * @param id identificador do card a ser removido
     * @throws ResourceNotFoundException se o card não for encontrado
     */
    @Transactional
    public void deleteCard(Long id) {
        // Garante que o card exista antes de tentar deletar.
        // Isso torna a operação mais segura e o comportamento da API mais previsível.
        if (!cardRepository.findById(id).isPresent()) {
            throw new ResourceNotFoundException("Card com ID " + id + " não encontrado para deleção.");
        }
        cardRepository.deleteById(id);
    }



    /**
     * Move um card para cima na mesma coluna.
     * 
     * <p>Troca a posição do card atual com o card anterior na mesma coluna.
     * Se o card já estiver no topo, não faz nada.</p>
     * 
     * @param cardId ID do card a ser movido
     * @return true se o card foi movido, false se já estava no topo
     * @throws ResourceNotFoundException se o card não for encontrado
     */
    @Transactional
    public boolean moveCardUp(Long cardId) {
        Card currentCard = getCardById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card com ID " + cardId + " não encontrado."));

        // Busca o card anterior na mesma coluna
        Optional<Card> previousCard = cardRepository.findPreviousCard(
                currentCard.getBoardColumnId(), 
                currentCard.getOrderIndex()
        );

        if (previousCard.isEmpty()) {
            return false; // Card já está no topo
        }

        Card previous = previousCard.get();
        
        // Troca as posições
        cardRepository.swapCardPositions(
                currentCard.getId(), previous.getOrderIndex(),
                previous.getId(), currentCard.getOrderIndex()
        );

        return true;
    }

    /**
     * Move um card para baixo na mesma coluna.
     * 
     * <p>Troca a posição do card atual com o próximo card na mesma coluna.
     * Se o card já estiver na base, não faz nada.</p>
     * 
     * @param cardId ID do card a ser movido
     * @return true se o card foi movido, false se já estava na base
     * @throws ResourceNotFoundException se o card não for encontrado
     */
    @Transactional
    public boolean moveCardDown(Long cardId) {
        Card currentCard = getCardById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card com ID " + cardId + " não encontrado."));

        // Busca o próximo card na mesma coluna
        Optional<Card> nextCard = cardRepository.findNextCard(
                currentCard.getBoardColumnId(), 
                currentCard.getOrderIndex()
        );

        if (nextCard.isEmpty()) {
            return false; // Card já está na base
        }

        Card next = nextCard.get();
        
        // Troca as posições
        cardRepository.swapCardPositions(
                currentCard.getId(), next.getOrderIndex(),
                next.getId(), currentCard.getOrderIndex()
        );

        return true;
    }

    /**
     * Verifica se um card pode ser movido para cima.
     * 
     * @param cardId ID do card
     * @return true se o card pode ser movido para cima, false caso contrário
     */
    @Transactional(readOnly = true)
    public boolean canMoveCardUp(Long cardId) {
        Optional<Card> cardOpt = getCardById(cardId);
        if (cardOpt.isEmpty()) {
            return false;
        }

        Card card = cardOpt.get();
        return cardRepository.findPreviousCard(card.getBoardColumnId(), card.getOrderIndex()).isPresent();
    }

    /**
     * Verifica se um card pode ser movido para baixo.
     * 
     * @param cardId ID do card
     * @return true se o card pode ser movido para baixo, false caso contrário
     */
    @Transactional(readOnly = true)
    public boolean canMoveCardDown(Long cardId) {
        Optional<Card> cardOpt = getCardById(cardId);
        if (cardOpt.isEmpty()) {
            return false;
        }

        Card card = cardOpt.get();
        return cardRepository.findNextCard(card.getBoardColumnId(), card.getOrderIndex()).isPresent();
    }
}