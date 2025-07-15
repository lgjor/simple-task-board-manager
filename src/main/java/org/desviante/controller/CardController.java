package org.desviante.controller;

import org.desviante.persistence.entity.BoardEntity;
import org.desviante.persistence.entity.CardEntity;
import org.desviante.service.CardService;

public class CardController {

    private final CardService cardService;

    public CardController() {
        // A instância do serviço agora é um membro da classe,
        // criada uma vez e reutilizada.
        this.cardService = new CardService();
    }

    /**
     * Orquestra a criação de um novo card.
     * Valida os dados de entrada e delega a lógica de negócio e persistência
     * para o CardService.
     *
     * @param board O board ao qual o card pertencerá.
     * @param title O título do novo card.
     * @param description A descrição do novo card.
     * @return A entidade CardEntity criada e persistida.
     */
    public CardEntity createCard(BoardEntity board, String title, String description) {
        // 1. Validação de regras de negócio no nível do controller
        if (board == null || board.getId() == null) {
            throw new IllegalArgumentException("O board fornecido é inválido ou não foi persistido.");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("O título do card não pode ser vazio.");
        }

        // 2. Delega a chamada para o serviço, passando apenas os dados necessários.
        // O serviço cuidará da transação e da persistência.
        return cardService.createCard(board.getId(), title.trim(), description);
    }
}