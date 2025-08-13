package org.desviante.service.progress;

/**
 * Resultado da validação do progresso.
 * Encapsula se a validação foi bem-sucedida e mensagens de erro.
 */
public class ProgressValidationResult {
    
    private final boolean isValid;
    private final String errorMessage;
    
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
    
    // Getters
    public boolean isValid() { return isValid; }
    public String getErrorMessage() { return errorMessage; }
}
