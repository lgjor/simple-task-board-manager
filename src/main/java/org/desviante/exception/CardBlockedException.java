package org.desviante.exception;

/**
 * Exception lançada quando uma operação em um card é bloqueada por regras de negócio.
 * 
 * <p>Representa uma situação onde uma operação específica em um card não pode
 * ser executada devido a restrições de regras de negócio. Esta exception é
 * utilizada para sinalizar que um card está em um estado que impede a
 * realização da operação solicitada, como movimentação, edição ou exclusão.</p>
 * 
 * <p>É utilizada principalmente pelos serviços para indicar que uma operação
 * falhou devido ao estado atual do card, que pode estar bloqueado por
 * dependências, status específico, ou outras regras de negócio implementadas
 * no sistema. A mensagem de erro deve explicar claramente o motivo do
 * bloqueio para facilitar a compreensão do usuário.</p>
 * 
 * <p>Esta exception é capturada pelos controllers e convertida em respostas
 * apropriadas para a interface do usuário, fornecendo feedback claro sobre
 * por que a operação não pode ser realizada e possivelmente sugerindo
 * ações alternativas.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see RuntimeException
 */
public class CardBlockedException extends RuntimeException {

    /**
     * Constrói uma nova CardBlockedException com a mensagem especificada.
     * 
     * <p>A mensagem deve explicar claramente o motivo do bloqueio do card,
     * incluindo informações sobre o estado atual do card e as regras de
     * negócio que impedem a operação. Exemplo: "Card não pode ser movido
     * pois está em estado bloqueado."</p>
     * 
     * @param message mensagem descritiva explicando o motivo do bloqueio do card
     */
    public CardBlockedException(final String message) {
        super(message);
    }
}
