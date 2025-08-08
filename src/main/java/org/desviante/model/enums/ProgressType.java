package org.desviante.model.enums;

/**
 * Enum que define os tipos de progresso disponíveis para os cards.
 * 
 * <p>Este enum permite que os cards tenham diferentes tipos de acompanhamento
 * de progresso, desde nenhum progresso até progresso percentual detalhado.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
public enum ProgressType {
    
    /**
     * Card sem progresso.
     * <p>Cards deste tipo não mostram nenhuma informação de progresso
     * na interface do usuário, mantendo a interface limpa para tarefas
     * simples que não precisam de acompanhamento.</p>
     */
    NONE("Sem Controle"),
    
    /**
     * Progresso percentual baseado em unidades.
     * <p>Cards deste tipo mostram progresso calculado como current/total,
     * exibindo percentual e permitindo edição dos valores.</p>
     */
    PERCENTAGE("Percentual"),
    
    /**
     * Progresso baseado em checklist.
     * <p>Cards deste tipo permitem criar listas de itens que podem ser
     * marcados como concluídos, calculando o progresso com base
     * na quantidade de itens concluídos versus o total.</p>
     */
    CHECKLIST("Checklist"),
    
    /**
     * Progresso customizado (reservado para futuras implementações).
     * <p>Permite implementar tipos de progresso específicos como
     * milestones, checklists, etc.</p>
     */
    CUSTOM("Customizado");
    
    private final String displayName;
    
    ProgressType(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Retorna o nome de exibição do tipo de progresso.
     * 
     * @return nome para exibição na interface
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Verifica se o tipo de progresso é habilitado.
     * 
     * @return true se o progresso deve ser mostrado, false caso contrário
     */
    public boolean isEnabled() {
        return this != NONE;
    }
}
