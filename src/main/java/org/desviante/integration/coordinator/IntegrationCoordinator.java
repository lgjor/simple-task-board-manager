package org.desviante.integration.coordinator;

import org.desviante.model.Card;

/**
 * Interface para coordenador de integrações do sistema.
 * 
 * <p>Esta interface define o contrato para coordenação de integrações
 * entre o sistema local e sistemas externos (Google Tasks, Calendário, etc.).
 * O coordenador atua como um orquestrador central que decide quais
 * integrações devem ser executadas baseado no estado dos cards.</p>
 * 
 * <p><strong>Princípios SOLID Aplicados:</strong></p>
 * <ul>
 *   <li><strong>SRP:</strong> Responsável apenas pela coordenação de integrações</li>
 *   <li><strong>OCP:</strong> Extensível através de novas estratégias de integração</li>
 *   <li><strong>LSP:</strong> Implementado por diferentes coordenadores</li>
 *   <li><strong>ISP:</strong> Interface específica para coordenação</li>
 *   <li><strong>DIP:</strong> Depende de abstrações (Card, integradores)</li>
 * </ul>
 * 
 * <p><strong>Responsabilidades:</strong></p>
 * <ul>
 *   <li>Decidir quando executar integrações</li>
 *   <li>Coordenar múltiplas integrações em paralelo</li>
 *   <li>Gerenciar dependências entre integrações</li>
 *   <li>Tratar falhas e rollback</li>
 *   <li>Manter rastreabilidade das operações</li>
 * </ul>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see Card
 */
public interface IntegrationCoordinator {
    
    /**
     * Coordena integrações quando um card é agendado.
     * 
     * <p>Este método é chamado quando um card recebe uma data de agendamento,
     * indicando que ele deve ser sincronizado com sistemas externos.</p>
     * 
     * <p><strong>Operações típicas:</strong></p>
     * <ul>
     *   <li>Criar evento no calendário</li>
     *   <li>Criar task no Google Tasks</li>
     *   <li>Atualizar status de sincronização</li>
     *   <li>Enviar notificações se necessário</li>
     * </ul>
     * 
     * @param card card que foi agendado
     */
    void onCardScheduled(Card card);
    
    /**
     * Coordena integrações quando um card é desagendado.
     * 
     * <p>Este método é chamado quando um card tem sua data de agendamento
     * removida, indicando que as integrações externas devem ser atualizadas.</p>
     * 
     * <p><strong>Operações típicas:</strong></p>
     * <ul>
     *   <li>Remover evento do calendário</li>
     *   <li>Remover task do Google Tasks</li>
     *   <li>Atualizar status de sincronização</li>
     *   <li>Limpar referências externas</li>
     * </ul>
     * 
     * @param card card que foi desagendado
     */
    void onCardUnscheduled(Card card);
    
    /**
     * Coordena integrações quando um card é atualizado.
     * 
     * <p>Este método é chamado quando um card agendado é modificado,
     * indicando que as integrações externas devem ser atualizadas
     * para refletir as mudanças.</p>
     * 
     * <p><strong>Operações típicas:</strong></p>
     * <ul>
     *   <li>Atualizar evento no calendário</li>
     *   <li>Atualizar task no Google Tasks</li>
     *   <li>Sincronizar mudanças de título/descrição</li>
     *   <li>Atualizar datas de vencimento</li>
     * </ul>
     * 
     * @param card card que foi atualizado
     * @param previousCard versão anterior do card (pode ser null)
     */
    void onCardUpdated(Card card, Card previousCard);
    
    /**
     * Coordena integrações quando um card é movido entre colunas.
     * 
     * <p>Este método é chamado quando um card é movido para uma coluna
     * diferente, podendo afetar seu status de agendamento ou conclusão.</p>
     * 
     * @param card card que foi movido
     * @param previousColumnId ID da coluna anterior
     * @param newColumnId ID da nova coluna
     */
    void onCardMoved(Card card, Long previousColumnId, Long newColumnId);
    
    /**
     * Coordena integrações quando um card é excluído.
     * 
     * <p>Este método é chamado quando um card é excluído do sistema,
     * indicando que todas as integrações externas devem ser limpas.</p>
     * 
     * @param cardId ID do card que foi excluído
     */
    void onCardDeleted(Long cardId);
    
    /**
     * Verifica se o coordenador está disponível para processar integrações.
     * 
     * @return true se o coordenador está disponível
     */
    boolean isAvailable();
    
    /**
     * Obtém estatísticas de integrações processadas.
     * 
     * @return estatísticas das integrações
     */
    IntegrationStats getStats();
}
