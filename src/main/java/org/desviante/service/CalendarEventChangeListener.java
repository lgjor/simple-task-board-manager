package org.desviante.service;

/**
 * Interface para notificar mudanças em eventos do calendário.
 * 
 * <p>Permite que diferentes componentes da aplicação sejam notificados
 * quando eventos são criados, atualizados ou deletados, permitindo
 * atualizações em tempo real da interface do usuário.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
public interface CalendarEventChangeListener {
    
    /**
     * Notifica que um evento foi deletado.
     * 
     * @param cardId ID do card relacionado ao evento deletado
     */
    void onEventDeleted(Long cardId);
    
    /**
     * Notifica que um evento foi criado.
     * 
     * @param cardId ID do card relacionado ao evento criado
     */
    void onEventCreated(Long cardId);
}
