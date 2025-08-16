package org.desviante.service.progress;

/**
 * Resultado da validação do progresso.
 * Encapsula se a validação foi bem-sucedida e mensagens de erro.
 */
public class ProgressValidationResult {
    
    private final boolean isValid;
    private final String errorMessage;
    
    /**
     * Construtor para resultado de validação do progresso.
     * 
     * @param isValid true se a validação foi bem-sucedida, false caso contrário
     * @param errorMessage mensagem de erro (pode ser null se válido)
     */
    public ProgressValidationResult(boolean isValid, String errorMessage) {
        this.isValid = isValid;
        this.errorMessage = errorMessage;
    }
    
    /**
     * Cria um resultado de validação bem-sucedido.
     * 
     * @return resultado de validação válido
     */
    public static ProgressValidationResult success() {
        return new ProgressValidationResult(true, null);
    }
    
    /**
     * Cria um resultado de validação com erro.
     * 
     * @param errorMessage mensagem de erro
     * @return resultado de validação com erro
     */
    public static ProgressValidationResult error(String errorMessage) {
        return new ProgressValidationResult(false, errorMessage);
    }
    
    /**
     * Verifica se a validação foi bem-sucedida.
     * 
     * @return true se válido, false caso contrário
     */
    public boolean isValid() { return isValid; }
    
    /**
     * Obtém a mensagem de erro da validação.
     * 
     * @return mensagem de erro ou null se válido
     */
    public String getErrorMessage() { return errorMessage; }
}
