package org.desviante.exception;

/**
 * Exception lançada quando uma operação é tentada em um card que já foi finalizado.
 * 
 * <p>Representa uma situação onde uma operação específica não pode ser executada
 * porque o card já foi marcado como concluído ou finalizado. Esta exception é
 * utilizada para sinalizar que o card está em estado final e não pode mais
 * ser modificado, movimentado ou ter seu status alterado.</p>
 * 
 * <p>É utilizada principalmente pelos serviços para indicar que uma operação
 * falhou porque o card já foi finalizado, possivelmente com data de conclusão
 * definida ou status específico de "concluído". A mensagem de erro deve
 * explicar claramente que o card está finalizado e por que a operação não
 * pode ser realizada, facilitando a compreensão do usuário.</p>
 * 
 * <p>Esta exception é capturada pelos controllers e convertida em respostas
 * apropriadas para a interface do usuário, fornecendo feedback claro sobre
 * o estado final do card e possivelmente sugerindo visualização dos dados
 * em vez de tentar modificações.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see RuntimeException
 */
public class CardFinishedException extends RuntimeException {

    /**
     * Constrói uma nova CardFinishedException com a mensagem especificada.
     * 
     * <p>A mensagem deve explicar claramente que o card está finalizado e
     * por que a operação não pode ser realizada. Exemplo: "Card já foi
     * finalizado e não pode ser modificado."</p>
     * 
     * @param message mensagem descritiva explicando que o card está finalizado
     */
    public CardFinishedException(final String message) {
        super(message);
    }
}
