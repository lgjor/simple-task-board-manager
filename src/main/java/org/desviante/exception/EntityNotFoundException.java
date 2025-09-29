package org.desviante.exception;

/**
 * Exception genérica lançada quando uma entidade não é encontrada no sistema.
 * 
 * <p>Representa uma situação onde uma entidade específica (board, card, coluna,
 * grupo, tarefa, etc.) não existe no banco de dados ou não pode ser localizada
 * com os critérios fornecidos. Esta exception é utilizada como uma versão
 * genérica da ResourceNotFoundException, fornecendo uma abordagem mais ampla
 * para tratamento de entidades não encontradas.</p>
 * 
 * <p>É utilizada principalmente pelos serviços e repositories para indicar que
 * uma operação falhou porque a entidade alvo não existe. A mensagem de erro
 * deve ser descritiva e incluir informações como o tipo de entidade e o
 * identificador utilizado na busca, facilitando o debugging e a correção
 * do problema.</p>
 * 
 * <p>Esta exception é capturada pelos controllers e convertida em respostas
 * HTTP apropriadas (geralmente 404 Not Found), fornecendo feedback claro
 * para a interface do usuário sobre a situação do erro.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see RuntimeException
 * @see ResourceNotFoundException
 */
public class EntityNotFoundException extends RuntimeException {

    /**
     * Constrói uma nova EntityNotFoundException com a mensagem especificada.
     * 
     * <p>A mensagem deve ser descritiva e incluir informações úteis para
     * identificação do problema, como o tipo de entidade e o identificador
     * utilizado na busca. Exemplo: "Entidade Board com ID 123 não encontrada."</p>
     * 
     * @param message mensagem descritiva do erro, incluindo tipo de entidade e identificador
     */
    public EntityNotFoundException(String message) {
        super(message);
    }
}
