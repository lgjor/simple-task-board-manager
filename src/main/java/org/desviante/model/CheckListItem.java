package org.desviante.model;

import java.time.LocalDateTime;

/**
 * Representa um item individual de um checklist associado a um card.
 * 
 * <p>Esta classe modela um item de checklist que pode ser marcado como concluído
 * ou pendente, mantendo informações sobre sua criação, conclusão e posição
 * na lista de itens do card.</p>
 * 
 * <p><strong>Características Principais:</strong></p>
 * <ul>
 *   <li><strong>Identificação:</strong> ID único e referência ao card pai</li>
 *   <li><strong>Conteúdo:</strong> Texto descritivo do item</li>
 *   <li><strong>Estado:</strong> Status de conclusão (concluído/pendente)</li>
 *   <li><strong>Ordenação:</strong> Posição na lista para controle de exibição</li>
 *   <li><strong>Rastreamento:</strong> Timestamps de criação e conclusão</li>
 * </ul>
 * 
 * <p><strong>Comportamento Automático:</strong> O setter do campo {@code completed}
 * automaticamente atualiza o campo {@code completedAt} quando o status muda,
 * facilitando o rastreamento de quando o item foi concluído.</p>
 * 
 * <p><strong>Uso Típico:</strong> Instâncias desta classe são utilizadas para
 * representar tarefas, sub-tarefas ou itens de verificação dentro de um card,
 * permitindo que usuários acompanhem o progresso de forma granular.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see LocalDateTime
 */
public class CheckListItem {
    
    /**
     * Identificador único do item de checklist.
     * Gerado automaticamente pelo banco de dados.
     */
    private Long id;
    
    /**
     * Identificador do card ao qual este item pertence.
     * Referência para estabelecer o relacionamento pai-filho.
     */
    private Long cardId;
    
    /**
     * Texto descritivo do item de checklist.
     * Representa a descrição da tarefa ou verificação a ser realizada.
     */
    private String text;
    
    /**
     * Status de conclusão do item.
     * {@code true} se o item foi concluído, {@code false} se está pendente.
     */
    private boolean completed;
    
    /**
     * Posição do item na lista de itens do checklist.
     * Utilizado para controlar a ordem de exibição dos itens.
     */
    private int orderIndex;
    
    /**
     * Data e hora de criação do item.
     * Definida automaticamente no momento da instanciação.
     */
    private LocalDateTime createdAt;
    
    /**
     * Data e hora de conclusão do item.
     * Definida automaticamente quando o item é marcado como concluído.
     * Pode ser {@code null} se o item ainda não foi concluído.
     */
    private LocalDateTime completedAt;
    
    /**
     * Construtor padrão que inicializa o item com data de criação atual.
     * 
     * <p>Este construtor é útil para frameworks de persistência e criação
     * de instâncias vazias que serão populadas posteriormente.</p>
     */
    public CheckListItem() {
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * Construtor que inicializa o item com texto e define valores padrão.
     * 
     * <p>Utilizado para criar novos itens de checklist com texto específico.
     * O item é criado como não concluído e com data de criação atual.</p>
     * 
     * @param text texto descritivo do item de checklist
     * @throws IllegalArgumentException se o texto for {@code null} ou vazio
     */
    public CheckListItem(String text) {
        this();
        this.text = text;
        this.completed = false;
    }
    
    /**
     * Construtor completo que inicializa todos os campos do item.
     * 
     * <p>Utilizado para reconstruir instâncias a partir de dados persistidos
     * ou para criar itens com estado específico.</p>
     * 
     * @param id identificador único do item
     * @param cardId identificador do card ao qual o item pertence
     * @param text texto descritivo do item
     * @param completed status de conclusão do item
     * @param orderIndex posição do item na lista de itens
     * @throws IllegalArgumentException se algum parâmetro obrigatório for inválido
     */
    public CheckListItem(Long id, Long cardId, String text, boolean completed, int orderIndex) {
        this();
        this.id = id;
        this.cardId = cardId;
        this.text = text;
        this.completed = completed;
        this.orderIndex = orderIndex;
    }
    
    // Getters e Setters
    
    /**
     * Retorna o identificador único do item.
     * 
     * @return identificador único do item, ou {@code null} se não foi definido
     */
    public Long getId() {
        return id;
    }
    
    /**
     * Define o identificador único do item.
     * 
     * @param id identificador único a ser definido
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    /**
     * Retorna o identificador do card ao qual o item pertence.
     * 
     * @return identificador do card pai, ou {@code null} se não foi definido
     */
    public Long getCardId() {
        return cardId;
    }
    
    /**
     * Define o identificador do card ao qual o item pertence.
     * 
     * @param cardId identificador do card pai a ser definido
     */
    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }
    
    /**
     * Retorna o texto descritivo do item.
     * 
     * @return texto do item, ou {@code null} se não foi definido
     */
    public String getText() {
        return text;
    }
    
    /**
     * Define o texto descritivo do item.
     * 
     * @param text texto a ser definido para o item
     */
    public void setText(String text) {
        this.text = text;
    }
    
    /**
     * Verifica se o item está concluído.
     * 
     * @return {@code true} se o item foi concluído, {@code false} caso contrário
     */
    public boolean isCompleted() {
        return completed;
    }
    
    /**
     * Define o status de conclusão do item.
     * 
     * <p><strong>Comportamento Automático:</strong> Este setter automaticamente
     * gerencia o campo {@code completedAt}:</p>
     * <ul>
     *   <li>Se marcado como concluído ({@code true}) e {@code completedAt} for {@code null},
     *       define automaticamente a data/hora atual</li>
     *   <li>Se marcado como não concluído ({@code false}), limpa o campo {@code completedAt}</li>
     * </ul>
     * 
     * @param completed novo status de conclusão do item
     */
    public void setCompleted(boolean completed) {
        this.completed = completed;
        if (completed && this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        } else if (!completed) {
            this.completedAt = null;
        }
    }
    
    /**
     * Retorna a posição do item na lista de itens do checklist.
     * 
     * @return posição do item na lista (baseada em zero)
     */
    public int getOrderIndex() {
        return orderIndex;
    }
    
    /**
     * Define a posição do item na lista de itens do checklist.
     * 
     * @param orderIndex nova posição do item na lista
     */
    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
    
    /**
     * Retorna a data e hora de criação do item.
     * 
     * @return data e hora de criação, nunca {@code null}
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Define a data e hora de criação do item.
     * 
     * @param createdAt nova data e hora de criação
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    /**
     * Retorna a data e hora de conclusão do item.
     * 
     * @return data e hora de conclusão, ou {@code null} se o item não foi concluído
     */
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    /**
     * Define a data e hora de conclusão do item.
     * 
     * <p><strong>Nota:</strong> Este setter permite sobrescrever o comportamento
     * automático do setter {@code setCompleted(boolean)}. Use com cuidado para
     * manter a consistência dos dados.</p>
     * 
     * @param completedAt nova data e hora de conclusão, ou {@code null} para limpar
     */
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    /**
     * Retorna uma representação em string do item de checklist.
     * 
     * <p>A representação inclui os campos principais: id, cardId, text,
     * completed e orderIndex. Campos de data não são incluídos para
     * manter a saída concisa.</p>
     * 
     * @return string representando o item de checklist
     */
    @Override
    public String toString() {
        return "ChecklistItem{" +
                "id=" + id +
                ", cardId=" + cardId +
                ", text='" + text + '\'' +
                ", completed=" + completed +
                ", orderIndex=" + orderIndex +
                '}';
    }
}
