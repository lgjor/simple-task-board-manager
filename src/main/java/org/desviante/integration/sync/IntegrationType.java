package org.desviante.integration.sync;

/**
 * Enumeração dos tipos de integração suportados pelo sistema.
 * 
 * <p>Esta enumeração define todos os tipos de integração externa
 * que o sistema suporta, permitindo extensibilidade e manutenção
 * centralizada dos tipos disponíveis.</p>
 * 
 * <p><strong>Princípios SOLID Aplicados:</strong></p>
 * <ul>
 *   <li><strong>SRP:</strong> Responsável apenas pela definição de tipos de integração</li>
 *   <li><strong>OCP:</strong> Extensível através de novos tipos</li>
 *   <li><strong>LSP:</strong> Implementa contratos consistentes</li>
 *   <li><strong>ISP:</strong> Interface específica para tipos de integração</li>
 *   <li><strong>DIP:</strong> Abstração para o sistema de sincronização</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see SyncStatus
 * @see IntegrationSyncStatus
 */
public enum IntegrationType {
    
    /**
     * Integração com Google Tasks.
     * 
     * <p>Sincroniza cards agendados com tarefas no Google Tasks,
     * permitindo gerenciamento unificado de tarefas entre o sistema
     * local e a plataforma do Google.</p>
     */
    GOOGLE_TASKS("Google Tasks", "Sincronização com Google Tasks"),
    
    /**
     * Integração com sistema de calendário.
     * 
     * <p>Sincroniza cards agendados com eventos no calendário,
     * permitindo visualização temporal das tarefas e melhor
     * planejamento de atividades.</p>
     */
    CALENDAR("Calendário", "Sincronização com sistema de calendário");
    
    /**
     * Nome descritivo do tipo de integração.
     */
    private final String displayName;
    
    /**
     * Descrição detalhada do tipo de integração.
     */
    private final String description;
    
    /**
     * Construtor do enum.
     * 
     * @param displayName nome descritivo
     * @param description descrição detalhada
     */
    IntegrationType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    /**
     * Obtém o nome descritivo do tipo de integração.
     * 
     * @return nome descritivo
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Obtém a descrição detalhada do tipo de integração.
     * 
     * @return descrição detalhada
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Verifica se este é um tipo de integração válido.
     * 
     * @return true se é um tipo válido
     */
    public boolean isValid() {
        return this != null;
    }
    
    /**
     * Obtém o tipo de integração pelo nome.
     * 
     * @param name nome do tipo (case-insensitive)
     * @return tipo correspondente ou null se não encontrado
     */
    public static IntegrationType fromName(String name) {
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
     * Verifica se um nome representa um tipo válido.
     * 
     * @param name nome a ser verificado
     * @return true se é um tipo válido
     */
    public static boolean isValidName(String name) {
        return fromName(name) != null;
    }
    
    /**
     * Obtém todos os tipos de integração disponíveis como array de strings.
     * 
     * @return array com os nomes dos tipos
     */
    public static String[] getAvailableTypes() {
        IntegrationType[] types = values();
        String[] names = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            names[i] = types[i].name();
        }
        return names;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
