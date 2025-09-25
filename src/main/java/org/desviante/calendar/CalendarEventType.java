package org.desviante.calendar;

/**
 * Define os tipos de eventos disponíveis no calendário.
 * 
 * <p>Cada tipo de evento pode ter comportamentos específicos de exibição,
 * edição e sincronização com o sistema principal. Os tipos são usados
 * para categorizar eventos e aplicar regras específicas.</p>
 * 
 * <p><strong>Tipos Disponíveis:</strong></p>
 * <ul>
 *   <li><strong>CARD:</strong> Eventos originados de cards do sistema</li>
 *   <li><strong>TASK:</strong> Eventos originados de tasks do Google</li>
 *   <li><strong>MEETING:</strong> Reuniões e compromissos</li>
 *   <li><strong>REMINDER:</strong> Lembretes e notificações</li>
 *   <li><strong>DEADLINE:</strong> Prazos e marcos importantes</li>
 *   <li><strong>CUSTOM:</strong> Eventos personalizados do usuário</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see CalendarEvent
 */
public enum CalendarEventType {
    
    /**
     * Evento originado de um card do sistema.
     * 
     * <p>Este tipo de evento é criado automaticamente baseado em cards
     * e mantém sincronização bidirecional com o sistema de boards.</p>
     */
    CARD("Card", "Evento originado de um card do sistema"),
    
    /**
     * Evento originado de uma task do Google Tasks.
     * 
     * <p>Este tipo de evento é sincronizado com a API do Google Tasks
     * e reflete mudanças feitas tanto no sistema local quanto no Google.</p>
     */
    TASK("Task", "Evento originado de uma task do Google"),
    
    /**
     * Reunião ou compromisso agendado.
     * 
     * <p>Eventos criados manualmente pelo usuário para marcar
     * reuniões, compromissos ou outros eventos importantes.</p>
     */
    MEETING("Reunião", "Reunião ou compromisso agendado"),
    
    /**
     * Lembrete ou notificação.
     * 
     * <p>Eventos criados para lembrar o usuário de tarefas importantes
     * ou marcos que precisam de atenção.</p>
     */
    REMINDER("Lembrete", "Lembrete ou notificação"),
    
    /**
     * Prazo ou marco importante.
     * 
     * <p>Eventos que marcam prazos importantes, entregas ou marcos
     * de projetos que requerem atenção especial.</p>
     */
    DEADLINE("Prazo", "Prazo ou marco importante"),
    
    /**
     * Evento personalizado do usuário.
     * 
     * <p>Eventos criados pelo usuário que não se enquadram nas
     * outras categorias específicas.</p>
     */
    CUSTOM("Personalizado", "Evento personalizado do usuário");

    private final String displayName;
    private final String description;

    /**
     * Construtor do enum.
     * 
     * @param displayName nome para exibição
     * @param description descrição do tipo
     */
    CalendarEventType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Obtém o nome para exibição do tipo de evento.
     * 
     * @return nome formatado para exibição
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Obtém a descrição do tipo de evento.
     * 
     * @return descrição detalhada do tipo
     */
    public String getDescription() {
        return description;
    }

    /**
     * Verifica se o tipo de evento é relacionado a entidades do sistema.
     * 
     * <p>Tipos relacionados a entidades (CARD, TASK) mantêm sincronização
     * com o sistema principal e podem ter restrições de edição.</p>
     * 
     * @return true se o tipo está relacionado a entidades do sistema
     */
    public boolean isSystemRelated() {
        return this == CARD || this == TASK;
    }

    /**
     * Verifica se o tipo de evento é editável pelo usuário.
     * 
     * <p>Alguns tipos podem ter restrições de edição baseadas em
     * regras de negócio ou sincronização com sistemas externos.</p>
     * 
     * @return true se o tipo pode ser editado pelo usuário
     */
    public boolean isUserEditable() {
        return this != CARD && this != TASK;
    }

    /**
     * Obtém o ícone associado ao tipo de evento.
     * 
     * <p>Retorna um código de ícone que pode ser usado na interface
     * para representar visualmente o tipo de evento.</p>
     * 
     * @return código do ícone
     */
    public String getIconCode() {
        return switch (this) {
            case CARD -> "📋";
            case TASK -> "✅";
            case MEETING -> "👥";
            case REMINDER -> "🔔";
            case DEADLINE -> "⏰";
            case CUSTOM -> "📅";
        };
    }

    /**
     * Obtém a cor padrão associada ao tipo de evento.
     * 
     * <p>Retorna um código de cor hexadecimal que pode ser usado
     * como cor padrão para eventos deste tipo.</p>
     * 
     * @return código de cor hexadecimal
     */
    public String getDefaultColor() {
        return switch (this) {
            case CARD -> "#4A90E2";
            case TASK -> "#7ED321";
            case MEETING -> "#F5A623";
            case REMINDER -> "#D0021B";
            case DEADLINE -> "#9013FE";
            case CUSTOM -> "#50E3C2";
        };
    }

    @Override
    public String toString() {
        return displayName;
    }
}
