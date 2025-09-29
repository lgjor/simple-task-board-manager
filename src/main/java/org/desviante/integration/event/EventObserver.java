package org.desviante.integration.event;

/**
 * Interface para observadores de eventos de domínio.
 * 
 * <p>Esta interface define o contrato que todos os observadores de eventos
 * devem implementar, seguindo o padrão Observer para desacoplar a geração
 * de eventos da sua processamento.</p>
 * 
 * <p><strong>Princípios SOLID Aplicados:</strong></p>
 * <ul>
 *   <li><strong>SRP:</strong> Responsável apenas por observar e processar eventos</li>
 *   <li><strong>OCP:</strong> Extensível através de novos tipos de observadores</li>
 *   <li><strong>LSP:</strong> Implementado por todos os observadores de eventos</li>
 *   <li><strong>ISP:</strong> Interface específica para observação de eventos</li>
 *   <li><strong>DIP:</strong> Depende da abstração DomainEvent</li>
 * </ul>
 * 
 * <p><strong>Características:</strong></p>
 * <ul>
 *   <li>Genérico para permitir type-safety</li>
 *   <li>Método canHandle para verificação de compatibilidade</li>
 *   <li>Processamento assíncrono suportado</li>
 *   <li>Tratamento de erro isolado por observador</li>
 * </ul>
 * 
 * @param <T> tipo específico de evento que este observador processa
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see DomainEvent
 * @see EventPublisher
 */
public interface EventObserver<T extends DomainEvent> {
    
    /**
     * Processa um evento de domínio.
     * 
     * <p>Este método é chamado pelo EventPublisher quando um evento
     * compatível é publicado. O observador deve implementar a lógica
     * específica para processar o evento recebido.</p>
     * 
     * <p><strong>Considerações de Implementação:</strong></p>
     * <ul>
     *   <li>Deve ser thread-safe se o sistema for multi-threaded</li>
     *   <li>Deve tratar exceções adequadamente</li>
     *   <li>Deve ser idempotente quando possível</li>
     *   <li>Deve ser eficiente para não bloquear outros observadores</li>
     * </ul>
     * 
     * @param event evento a ser processado
     * @throws Exception se ocorrer erro durante o processamento
     */
    void handle(T event) throws Exception;
    
    /**
     * Verifica se este observador pode processar um evento específico.
     * 
     * <p>Este método permite que o EventPublisher determine se um
     * observador deve ser notificado sobre um evento específico,
     * evitando notificações desnecessárias.</p>
     * 
     * <p><strong>Critérios de Compatibilidade:</strong></p>
     * <ul>
     *   <li>Verificação de tipo de evento</li>
     *   <li>Verificação de contexto específico</li>
     *   <li>Verificação de condições de negócio</li>
     *   <li>Verificação de disponibilidade do observador</li>
     * </ul>
     * 
     * @param event evento a ser verificado
     * @return true se este observador pode processar o evento
     */
    boolean canHandle(DomainEvent event);
    
    /**
     * Obtém a prioridade do observador para ordenação de processamento.
     * 
     * <p>Observadores com prioridade mais alta são executados primeiro.
     * Isso é útil para garantir que certos processamentos aconteçam
     * antes de outros (ex: validação antes de persistência).</p>
     * 
     * @return prioridade do observador (maior número = maior prioridade)
     */
    default int getPriority() {
        return 0;
    }
    
    /**
     * Obtém o nome do observador para logging e debugging.
     * 
     * @return nome descritivo do observador
     */
    default String getObserverName() {
        return this.getClass().getSimpleName();
    }
}
