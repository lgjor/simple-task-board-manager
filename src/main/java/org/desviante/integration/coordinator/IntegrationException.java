package org.desviante.integration.coordinator;

/**
 * Exceção lançada quando ocorre erro durante a coordenação de integrações.
 * 
 * <p>Esta exceção encapsula erros que podem ocorrer durante o processo
 * de coordenação de integrações entre o sistema local e sistemas externos,
 * incluindo falhas de sincronização, problemas de configuração ou erros
 * de comunicação com APIs externas.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
public class IntegrationException extends Exception {
    
    private final String integrationType;
    private final Long cardId;
    
    /**
     * Construtor com mensagem de erro.
     * 
     * @param message mensagem descritiva do erro
     */
    public IntegrationException(String message) {
        super(message);
        this.integrationType = null;
        this.cardId = null;
    }
    
    /**
     * Construtor com mensagem de erro e causa.
     * 
     * @param message mensagem descritiva do erro
     * @param cause exceção que causou este erro
     */
    public IntegrationException(String message, Throwable cause) {
        super(message, cause);
        this.integrationType = null;
        this.cardId = null;
    }
    
    /**
     * Construtor com mensagem de erro, tipo de integração e ID do card.
     * 
     * @param message mensagem descritiva do erro
     * @param integrationType tipo de integração que falhou
     * @param cardId ID do card relacionado ao erro
     */
    public IntegrationException(String message, String integrationType, Long cardId) {
        super(message);
        this.integrationType = integrationType;
        this.cardId = cardId;
    }
    
    /**
     * Construtor completo com todos os parâmetros.
     * 
     * @param message mensagem descritiva do erro
     * @param cause exceção que causou este erro
     * @param integrationType tipo de integração que falhou
     * @param cardId ID do card relacionado ao erro
     */
    public IntegrationException(String message, Throwable cause, String integrationType, Long cardId) {
        super(message, cause);
        this.integrationType = integrationType;
        this.cardId = cardId;
    }
    
    /**
     * Obtém o tipo de integração que falhou.
     * 
     * @return tipo de integração, ou null se não disponível
     */
    public String getIntegrationType() {
        return integrationType;
    }
    
    /**
     * Obtém o ID do card relacionado ao erro.
     * 
     * @return ID do card, ou null se não disponível
     */
    public Long getCardId() {
        return cardId;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        if (integrationType != null) {
            sb.append(" [IntegrationType: ").append(integrationType).append("]");
        }
        if (cardId != null) {
            sb.append(" [CardId: ").append(cardId).append("]");
        }
        return sb.toString();
    }
}
