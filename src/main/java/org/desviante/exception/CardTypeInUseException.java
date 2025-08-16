package org.desviante.exception;

/**
 * Exception lançada quando uma operação em um tipo de card é bloqueada
 * porque o tipo está sendo usado por cards existentes.
 * 
 * <p>Representa uma situação onde um tipo de card não pode ser removido
 * ou modificado de forma que afete cards existentes, devido a regras
 * de integridade referencial e negócio.</p>
 * 
 * <p>Esta exception é utilizada principalmente pelo CardTypeService
 * para indicar que uma operação de remoção ou modificação falhou porque
 * existem cards que dependem do tipo de card em questão.</p>
 * 
 * <p>A mensagem de erro deve explicar claramente quais cards estão usando
 * o tipo e sugerir ações alternativas, como migrar os cards para outro
 * tipo antes de remover o tipo atual.</p>
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
 * @see org.desviante.service.CardTypeService
 */
public class CardTypeInUseException extends RuntimeException {

    /**
     * Construtor com mensagem de erro.
     * 
     * @param message mensagem explicando por que o tipo de card não pode ser removido
     */
    public CardTypeInUseException(String message) {
        super(message);
    }

    /**
     * Construtor com mensagem de erro e causa.
     * 
     * @param message mensagem explicando por que o tipo de card não pode ser removido
     * @param cause exceção que causou este problema
     */
    public CardTypeInUseException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Construtor padrão.
     */
    public CardTypeInUseException() {
        super();
    }
}
