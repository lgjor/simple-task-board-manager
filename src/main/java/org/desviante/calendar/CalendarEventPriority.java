package org.desviante.calendar;

/**
 * Define os níveis de prioridade para eventos do calendário.
 * 
 * <p>A prioridade influencia a exibição visual dos eventos no calendário,
 * determinando cores, ícones e posicionamento. Eventos com prioridade
 * mais alta são destacados para chamar atenção do usuário.</p>
 * 
 * <p><strong>Níveis de Prioridade:</strong></p>
 * <ul>
 *   <li><strong>LOW:</strong> Prioridade baixa - eventos informativos</li>
 *   <li><strong>STANDARD:</strong> Prioridade padrão - eventos normais</li>
 *   <li><strong>HIGH:</strong> Prioridade alta - eventos importantes</li>
 *   <li><strong>URGENT:</strong> Prioridade urgente - eventos críticos</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see CalendarEvent
 */
public enum CalendarEventPriority {
    
    /**
     * Prioridade baixa.
     * 
     * <p>Eventos informativos que não requerem ação imediata.
     * Exibidos com cores suaves e baixa proeminência visual.</p>
     */
    LOW("Baixa", 1, "Eventos informativos"),
    
    /**
     * Prioridade padrão.
     * 
     * <p>Eventos normais que seguem o fluxo regular de trabalho.
     * Exibidos com cores padrão e proeminência normal.</p>
     */
    STANDARD("Padrão", 2, "Eventos normais"),
    
    /**
     * Prioridade alta.
     * 
     * <p>Eventos importantes que requerem atenção especial.
     * Exibidos com cores vibrantes e alta proeminência visual.</p>
     */
    HIGH("Alta", 3, "Eventos importantes"),
    
    /**
     * Prioridade urgente.
     * 
     * <p>Eventos críticos que requerem ação imediata.
     * Exibidos com cores de alerta e máxima proeminência visual.</p>
     */
    URGENT("Urgente", 4, "Eventos críticos");

    private final String displayName;
    private final int level;
    private final String description;

    /**
     * Construtor do enum.
     * 
     * @param displayName nome para exibição
     * @param level nível numérico da prioridade (1-4)
     * @param description descrição da prioridade
     */
    CalendarEventPriority(String displayName, int level, String description) {
        this.displayName = displayName;
        this.level = level;
        this.description = description;
    }

    /**
     * Obtém o nome para exibição da prioridade.
     * 
     * @return nome formatado para exibição
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Obtém o nível numérico da prioridade.
     * 
     * <p>Valores de 1 (baixa) a 4 (urgente). Usado para ordenação
     * e comparação de prioridades.</p>
     * 
     * @return nível numérico da prioridade
     */
    public int getLevel() {
        return level;
    }

    /**
     * Obtém a descrição da prioridade.
     * 
     * @return descrição detalhada da prioridade
     */
    public String getDescription() {
        return description;
    }

    /**
     * Verifica se esta prioridade é maior que outra.
     * 
     * @param other outra prioridade para comparação
     * @return true se esta prioridade é maior que a outra
     */
    public boolean isHigherThan(CalendarEventPriority other) {
        return this.level > other.level;
    }

    /**
     * Verifica se esta prioridade é menor que outra.
     * 
     * @param other outra prioridade para comparação
     * @return true se esta prioridade é menor que a outra
     */
    public boolean isLowerThan(CalendarEventPriority other) {
        return this.level < other.level;
    }

    /**
     * Verifica se esta prioridade é igual a outra.
     * 
     * @param other outra prioridade para comparação
     * @return true se as prioridades são iguais
     */
    public boolean isEqualTo(CalendarEventPriority other) {
        return this.level == other.level;
    }

    /**
     * Obtém a cor associada à prioridade.
     * 
     * <p>Retorna um código de cor hexadecimal que representa
     * visualmente o nível de prioridade.</p>
     * 
     * @return código de cor hexadecimal
     */
    public String getColor() {
        return switch (this) {
            case LOW -> "#95A5A6";        // Cinza claro
            case STANDARD -> "#3498DB";   // Azul padrão
            case HIGH -> "#F39C12";       // Laranja
            case URGENT -> "#E74C3C";     // Vermelho
        };
    }

    /**
     * Obtém a cor de fundo associada à prioridade.
     * 
     * <p>Retorna uma versão mais clara da cor principal para
     * uso como cor de fundo em elementos da interface.</p>
     * 
     * @return código de cor hexadecimal para fundo
     */
    public String getBackgroundColor() {
        return switch (this) {
            case LOW -> "#ECF0F1";        // Cinza muito claro
            case STANDARD -> "#EBF3FD";   // Azul muito claro
            case HIGH -> "#FEF9E7";       // Amarelo muito claro
            case URGENT -> "#FADBD8";     // Vermelho muito claro
        };
    }

    /**
     * Obtém o ícone associado à prioridade.
     * 
     * <p>Retorna um código de ícone que representa visualmente
     * o nível de prioridade.</p>
     * 
     * @return código do ícone
     */
    public String getIconCode() {
        return switch (this) {
            case LOW -> "🔵";
            case STANDARD -> "🔷";
            case HIGH -> "🔶";
            case URGENT -> "🔴";
        };
    }

    /**
     * Obtém a prioridade padrão do sistema.
     * 
     * @return prioridade padrão (STANDARD)
     */
    public static CalendarEventPriority getDefault() {
        return STANDARD;
    }

    /**
     * Obtém a prioridade mais alta disponível.
     * 
     * @return prioridade mais alta (URGENT)
     */
    public static CalendarEventPriority getHighest() {
        return URGENT;
    }

    /**
     * Obtém a prioridade mais baixa disponível.
     * 
     * @return prioridade mais baixa (LOW)
     */
    public static CalendarEventPriority getLowest() {
        return LOW;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
