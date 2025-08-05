package org.desviante.exception;

/**
 * Exception lançada quando ocorrem erros na comunicação com a API do Google Tasks.
 * 
 * <p>Representa falhas específicas relacionadas à integração com a API externa
 * do Google Tasks, incluindo problemas de autenticação, comunicação de rede,
 * formatação de dados, ou limitações da API. Esta exception encapsula tanto
 * a mensagem de erro quanto a causa original para facilitar debugging e
 * tratamento adequado dos problemas de integração.</p>
 * 
 * <p>É utilizada principalmente pelo GoogleTasksApiService para indicar que
 * uma operação de sincronização falhou devido a problemas na API externa.
 * A exception mantém a causa original (Throwable) para permitir análise
 * detalhada do problema, incluindo stack traces completos e informações
 * específicas da API do Google.</p>
 * 
 * <p>Esta exception é capturada pelos serviços que utilizam a integração
 * com Google Tasks, permitindo fallback para operações locais quando a
 * API externa não está disponível ou falha temporariamente.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see RuntimeException
 * @see GoogleTasksApiService
 */
public class GoogleApiServiceException extends RuntimeException {
    
    /**
     * Constrói uma nova GoogleApiServiceException com a mensagem e causa especificadas.
     * 
     * <p>A mensagem deve descrever o problema específico da integração com
     * a API do Google Tasks, enquanto a causa deve conter a exception
     * original que gerou o problema (IOException, IllegalArgumentException, etc.).</p>
     * 
     * <p>Exemplos de uso incluem problemas de credenciais, falhas de rede,
     * formatação inadequada de dados, ou limitações da API do Google.</p>
     * 
     * @param message mensagem descritiva do erro de integração com Google Tasks
     * @param cause exception original que causou o problema (pode ser null)
     */
    public GoogleApiServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}