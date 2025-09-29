package org.desviante.integration.sync;

/**
 * Enumeração dos status possíveis para sincronização.
 * 
 * <p>Esta enumeração define todos os estados possíveis de uma
 * operação de sincronização, permitindo rastreamento preciso
 * do status e implementação de lógicas de retry e tratamento de erro.</p>
 * 
 * <p><strong>Princípios SOLID Aplicados:</strong></p>
 * <ul>
 *   <li><strong>SRP:</strong> Responsável apenas pela definição de status de sincronização</li>
 *   <li><strong>OCP:</strong> Extensível através de novos status</li>
 *   <li><strong>LSP:</strong> Implementa contratos consistentes</li>
 *   <li><strong>ISP:</strong> Interface específica para status</li>
 *   <li><strong>DIP:</strong> Abstração para o sistema de sincronização</li>
 * </ul>
 * 
 * <p><strong>Estados e Transições:</strong></p>
 * <ul>
 *   <li><strong>PENDING → SYNCED:</strong> Sincronização bem-sucedida</li>
 *   <li><strong>PENDING → RETRY:</strong> Falha temporária, tentando novamente</li>
 *   <li><strong>PENDING → ERROR:</strong> Falha permanente</li>
 *   <li><strong>RETRY → SYNCED:</strong> Retry bem-sucedido</li>
 *   <li><strong>RETRY → ERROR:</strong> Retry falhou, atingiu limite</li>
 *   <li><strong>RETRY → RETRY:</strong> Retry falhou, tentando novamente</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see IntegrationType
 * @see IntegrationSyncStatus
 */
public enum SyncStatus {
    
    /**
     * Sincronização bem-sucedida.
     * 
     * <p>Indica que a entidade local foi sincronizada com sucesso
     * com o sistema externo. Este é o estado final desejado.</p>
     */
    SYNCED("Sincronizado", "Sincronização realizada com sucesso"),
    
    /**
     * Aguardando sincronização.
     * 
     * <p>Indica que uma operação de sincronização está pendente
     * e ainda não foi processada. Estado inicial para novas
     * sincronizações.</p>
     */
    PENDING("Pendente", "Aguardando sincronização"),
    
    /**
     * Falha na sincronização.
     * 
     * <p>Indica que a sincronização falhou e não será mais
     * tentada automaticamente. Geralmente indica um erro
     * permanente ou que o limite de tentativas foi atingido.</p>
     */
    ERROR("Erro", "Falha na sincronização"),
    
    /**
     * Tentativa de retry em andamento.
     * 
     * <p>Indica que uma sincronização falhou anteriormente
     * e está sendo tentada novamente. Estado temporário
     * durante operações de retry.</p>
     */
    RETRY("Tentando Novamente", "Tentativa de retry em andamento");
    
    /**
     * Nome descritivo do status.
     */
    private final String displayName;
    
    /**
     * Descrição detalhada do status.
     */
    private final String description;
    
    /**
     * Construtor do enum.
     * 
     * @param displayName nome descritivo
     * @param description descrição detalhada
     */
    SyncStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    /**
     * Obtém o nome descritivo do status.
     * 
     * @return nome descritivo
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Obtém a descrição detalhada do status.
     * 
     * @return descrição detalhada
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Verifica se este é um status final (não transitório).
     * 
     * @return true se é um status final (SYNCED ou ERROR)
     */
    public boolean isFinal() {
        return this == SYNCED || this == ERROR;
    }
    
    /**
     * Verifica se este é um status transitório (pode mudar).
     * 
     * @return true se é um status transitório (PENDING ou RETRY)
     */
    public boolean isTransient() {
        return this == PENDING || this == RETRY;
    }
    
    /**
     * Verifica se indica sucesso.
     * 
     * @return true se o status é SYNCED
     */
    public boolean isSuccess() {
        return this == SYNCED;
    }
    
    /**
     * Verifica se indica falha.
     * 
     * @return true se o status é ERROR
     */
    public boolean isFailure() {
        return this == ERROR;
    }
    
    /**
     * Verifica se indica que está em processo.
     * 
     * @return true se o status é PENDING ou RETRY
     */
    public boolean isInProgress() {
        return this == PENDING || this == RETRY;
    }
    
    /**
     * Verifica se permite retry.
     * 
     * @return true se permite retry (PENDING ou RETRY)
     */
    public boolean allowsRetry() {
        return this == PENDING || this == RETRY;
    }
    
    /**
     * Obtém o próximo status lógico após retry.
     * 
     * @return próximo status ou null se não aplicável
     */
    public SyncStatus getNextRetryStatus() {
        return switch (this) {
            case PENDING -> RETRY;
            case RETRY -> RETRY; // Continua tentando
            case SYNCED, ERROR -> null; // Não permite retry
        };
    }
    
    /**
     * Obtém o status de erro correspondente.
     * 
     * @return status ERROR
     */
    public SyncStatus getErrorStatus() {
        return ERROR;
    }
    
    /**
     * Obtém o status de sucesso correspondente.
     * 
     * @return status SYNCED
     */
    public SyncStatus getSuccessStatus() {
        return SYNCED;
    }
    
    /**
     * Obtém o status pelo nome.
     * 
     * @param name nome do status (case-insensitive)
     * @return status correspondente ou null se não encontrado
     */
    public static SyncStatus fromName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        
        try {
            return valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Verifica se um nome representa um status válido.
     * 
     * @param name nome a ser verificado
     * @return true se é um status válido
     */
    public static boolean isValidName(String name) {
        return fromName(name) != null;
    }
    
    /**
     * Obtém todos os status disponíveis como array de strings.
     * 
     * @return array com os nomes dos status
     */
    public static String[] getAvailableStatuses() {
        SyncStatus[] statuses = values();
        String[] names = new String[statuses.length];
        for (int i = 0; i < statuses.length; i++) {
            names[i] = statuses[i].name();
        }
        return names;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
