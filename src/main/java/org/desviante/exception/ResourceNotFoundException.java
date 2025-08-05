package org.desviante.exception;

/**
 * Exception lançada quando um recurso solicitado não é encontrado no sistema.
 * 
 * <p>Representa uma situação onde um recurso específico (board, card, coluna,
 * grupo, etc.) não existe no banco de dados ou não pode ser localizado com
 * os critérios fornecidos. Esta exception é utilizada para sinalizar erros
 * de "não encontrado" de forma consistente em toda a aplicação.</p>
 * 
 * <p>É utilizada principalmente pelos serviços para indicar que uma operação
 * falhou porque o recurso alvo não existe. A mensagem de erro deve ser
 * descritiva e incluir informações como o tipo de recurso e o identificador
 * utilizado na busca, facilitando o debugging e a correção do problema.</p>
 * 
 * <p>Esta exception é capturada pelos controllers e convertida em respostas
 * HTTP apropriadas (geralmente 404 Not Found), fornecendo feedback claro
 * para a interface do usuário sobre a situação do erro.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see RuntimeException
 */
public class ResourceNotFoundException extends RuntimeException {
    
    /**
     * Constrói uma nova ResourceNotFoundException com a mensagem especificada.
     * 
     * <p>A mensagem deve ser descritiva e incluir informações úteis para
     * identificação do problema, como o tipo de recurso e o identificador
     * utilizado na busca. Exemplo: "Board com ID 123 não encontrado."</p>
     * 
     * @param message mensagem descritiva do erro, incluindo tipo de recurso e identificador
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}