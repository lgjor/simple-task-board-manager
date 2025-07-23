package org.desviante.service;

import lombok.RequiredArgsConstructor;
import org.desviante.exception.ResourceNotFoundException;
import org.desviante.model.BoardColumn;
import org.desviante.model.Card;
import org.desviante.model.enums.BoardColumnKindEnum;
import org.desviante.repository.BoardColumnRepository;
import org.desviante.repository.CardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final BoardColumnRepository columnRepository;

    @Transactional
    public Card createCard(String title, String description, Long parentColumnId) {
        // Valida se a coluna pai existe antes de criar o card.
        columnRepository.findById(parentColumnId)
                .orElseThrow(() -> new ResourceNotFoundException("Coluna com ID " + parentColumnId + " não encontrada."));

        Card newCard = new Card();
        newCard.setTitle(title);
        newCard.setDescription(description);
        newCard.setBoardColumnId(parentColumnId);

        // LÓGICA DE NEGÓCIO: Definir as datas no serviço é a prática correta.
        LocalDateTime now = LocalDateTime.now();
        newCard.setCreationDate(now);
        newCard.setLastUpdateDate(now);
        // Um novo card nunca está concluído.
        newCard.setCompletionDate(null);

        return cardRepository.save(newCard);
    }

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
     */
    @Transactional
    public Card updateCardDetails(Long cardId, String newTitle, String newDescription) {
        // 1. Encontra o card ou lança uma exceção se não existir.
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card com ID " + cardId + " não encontrado para atualização."));

        // 2. Atualiza as propriedades do objeto.
        card.setTitle(newTitle);
        card.setDescription(newDescription);

        // 3. REGRA DE NEGÓCIO: Sempre atualiza a data da última modificação.
        card.setLastUpdateDate(LocalDateTime.now());

        // 4. Salva e retorna a entidade atualizada.
        return cardRepository.save(card);
    }

    @Transactional(readOnly = true)
    public Optional<Card> getCardById(Long id) {
        return cardRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Card> getCardsForColumns(List<Long> columnIds) {
        // O repositório já trata a lista vazia, então a delegação direta é segura.
        return cardRepository.findByBoardColumnIdIn(columnIds);
    }

    @Transactional
    public void deleteCard(Long id) {
        // MELHORIA: Garante que o card existe antes de tentar deletar.
        // Isso torna a operação mais segura e o comportamento da API mais previsível.
        if (!cardRepository.findById(id).isPresent()) {
            throw new ResourceNotFoundException("Card com ID " + id + " não encontrado para deleção.");
        }
        cardRepository.deleteById(id);
    }
}