package org.desviante.service;

import lombok.RequiredArgsConstructor;
import org.desviante.exception.ResourceNotFoundException;
import org.desviante.model.BoardColumn;
import org.desviante.model.Card;
import org.desviante.model.enums.BoardColumnKindEnum;
import org.desviante.model.enums.CardType;
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
     * @param type tipo do card (CARD, BOOK, VIDEO, COURSE)
     * @return card criado com ID gerado
     * @throws ResourceNotFoundException se a coluna pai não for encontrada
     */
    @Transactional
    public Card createCard(String title, String description, Long parentColumnId, CardType type) {
        // Valida se a coluna pai existe antes de criar o card.
        columnRepository.findById(parentColumnId)
                .orElseThrow(() -> new ResourceNotFoundException("Coluna com ID " + parentColumnId + " não encontrada."));

        Card newCard = new Card();
        newCard.setTitle(title);
        newCard.setDescription(description);
        newCard.setBoardColumnId(parentColumnId);
        newCard.setType(type);

        // Configurar campos de progresso baseado no tipo do card
        if (type != CardType.CARD) {
            // Cards que suportam progresso devem ter totalUnits inicializado com valor válido
            newCard.setTotalUnits(1);
            newCard.setCurrentUnits(0);
        } else {
            // Cards do tipo CARD não usam progresso
            newCard.setTotalUnits(null);
            newCard.setCurrentUnits(null);
        }

        // LÓGICA DE NEGÓCIO: Definir as datas no serviço é a prática correta.
        LocalDateTime now = LocalDateTime.now();
        newCard.setCreationDate(now);
        newCard.setLastUpdateDate(now);
        // Um novo card nunca está concluído.
        newCard.setCompletionDate(null);

        return cardRepository.save(newCard);
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
        // 1. Encontra o card ou lança uma exceção se não existir.
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card com ID " + cardId + " não encontrado para atualização."));

        // 2. Atualiza as propriedades do objeto.
        card.setTitle(newTitle);
        card.setDescription(newDescription);
        
        // 3. Atualizar campos de progresso se fornecidos
        if (totalUnits != null) {
            // Garantir que total seja sempre válido (mínimo 1)
            if (totalUnits <= 0) {
                throw new IllegalArgumentException("O total deve ser maior que zero.");
            }
            card.setTotalUnits(totalUnits);
        }
        if (currentUnits != null) {
            // Garantir que current seja sempre válido (mínimo 0)
            if (currentUnits < 0) {
                throw new IllegalArgumentException("O valor atual não pode ser negativo.");
            }
            // Garantir que current não seja maior que total
            if (totalUnits != null && currentUnits > totalUnits) {
                throw new IllegalArgumentException("O valor atual não pode ser maior que o total.");
            }
            card.setCurrentUnits(currentUnits);
        }

        // 4. REGRA DE NEGÓCIO: Sempre atualiza a data da última modificação.
        card.setLastUpdateDate(LocalDateTime.now());

        // 5. Salva e retorna a entidade atualizada.
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
        return cardRepository.findById(id);
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
        return cardRepository.findByBoardColumnIdIn(columnIds);
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

}