package org.desviante.integration.event;

import java.util.concurrent.CompletableFuture;

/**
 * Interface para publicador de eventos de domínio.
 * 
 * <p>Esta interface define o contrato para publicação e gerenciamento
 * de eventos de domínio no sistema, implementando o padrão Observer
 * para desacoplar a geração de eventos do seu processamento.</p>
 * 
 * <p><strong>Princípios SOLID Aplicados:</strong></p>
 * <ul>
 *   <li><strong>SRP:</strong> Responsável apenas pela publicação de eventos</li>
 *   <li><strong>OCP:</strong> Extensível através de novos tipos de eventos</li>
 *   <li><strong>LSP:</strong> Implementado por diferentes estratégias de publicação</li>
 *   <li><strong>ISP:</strong> Interface específica para publicação de eventos</li>
 *   <li><strong>DIP:</strong> Depende das abstrações DomainEvent e EventObserver</li>
 * </ul>
 * 
 * <p><strong>Características:</strong></p>
 * <ul>
 *   <li>Suporte a publicação síncrona e assíncrona</li>
 *   <li>Gerenciamento de observadores</li>
 *   <li>Tratamento de erros isolado por observador</li>
 *   <li>Ordenação por prioridade</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see DomainEvent
 * @see EventObserver
 */
public interface EventPublisher {
    
    /**
     * Publica um evento de domínio para todos os observadores compatíveis.
     * 
     * <p>Este método notifica todos os observadores registrados que podem
     * processar o evento publicado. A notificação é feita de forma síncrona,
     * garantindo que todos os processamentos sejam concluídos antes do retorno.</p>
     * 
     * <p><strong>Fluxo de Execução:</strong></p>
     * <ol>
     *   <li>Identifica observadores compatíveis com o evento</li>
     *   <li>Ordena observadores por prioridade (maior primeiro)</li>
     *   <li>Executa handle() em cada observador</li>
     *   <li>Coleta e reporta erros se necessário</li>
     * </ol>
     * 
     * @param event evento a ser publicado
     * @throws EventPublishingException se ocorrer erro durante a publicação
     */
    void publish(DomainEvent event);
    
    /**
     * Publica um evento de domínio de forma assíncrona.
     * 
     * <p>Similar ao método publish(), mas executa o processamento
     * em uma thread separada, permitindo que o método retorne
     * imediatamente sem aguardar o processamento completo.</p>
     * 
     * @param event evento a ser publicado
     * @return CompletableFuture que representa o processamento assíncrono
     */
    CompletableFuture<Void> publishAsync(DomainEvent event);
    
    /**
     * Registra um observador para receber eventos compatíveis.
     * 
     * <p>Após o registro, o observador será notificado sobre todos
     * os eventos que ele pode processar (conforme determinado por
     * canHandle()).</p>
     * 
     * @param observer observador a ser registrado
     * @throws IllegalArgumentException se o observador for null
     */
    void subscribe(EventObserver<?> observer);
    
    /**
     * Remove o registro de um observador.
     * 
     * <p>Após a remoção, o observador não receberá mais notificações
     * sobre eventos, mesmo que seja compatível com eles.</p>
     * 
     * @param observer observador a ser removido
     */
    void unsubscribe(EventObserver<?> observer);
    
    /**
     * Obtém o número de observadores registrados.
     * 
     * @return número de observadores atualmente registrados
     */
    int getObserverCount();
    
    /**
     * Verifica se um observador específico está registrado.
     * 
     * @param observer observador a ser verificado
     * @return true se o observador está registrado
     */
    boolean isSubscribed(EventObserver<?> observer);
    
    /**
     * Limpa todos os observadores registrados.
     * 
     * <p>Útil para testes ou reinicialização do sistema.</p>
     */
    void clearObservers();
}
