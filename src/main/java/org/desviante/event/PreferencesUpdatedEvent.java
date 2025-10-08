package org.desviante.event;

import org.springframework.context.ApplicationEvent;

/**
 * Evento publicado quando as preferências da aplicação são atualizadas.
 * 
 * <p>Este evento é disparado após o salvamento bem-sucedido das preferências
 * para notificar outros componentes da aplicação sobre as mudanças realizadas.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see ApplicationEvent
 */
public class PreferencesUpdatedEvent extends ApplicationEvent {
    
    /**
     * Construtor do evento de preferências atualizadas.
     * 
     * @param source objeto que publicou o evento
     */
    public PreferencesUpdatedEvent(Object source) {
        super(source);
    }
}
