package org.desviante.model.enums;

/**
 * Define os tipos de colunas em um quadro do sistema de gerenciamento de tarefas.
 * 
 * <p>Cada tipo de coluna define comportamentos específicos no fluxo de trabalho,
 * como permissões para adicionar tarefas, regras de movimentação entre colunas,
 * e status automáticos que podem ser aplicados às tarefas.</p>
 * 
 * <p>Estes tipos são usados pelo sistema para aplicar lógicas de negócio
 * específicas, como mover automaticamente uma tarefa para a próxima coluna
 * ou marcar uma tarefa como concluída quando chega na coluna final.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see org.desviante.model.BoardColumn
 */
public enum BoardColumnKindEnum {
    
    /**
     * Coluna inicial de um fluxo de trabalho.
     * <p>Representa o ponto de entrada onde novas tarefas são adicionadas ao quadro.
     * Geralmente permite adicionar novas tarefas e é a primeira coluna do processo.</p>
     * 
     * <p><strong>Exemplos:</strong> "A Fazer", "Backlog", "To Do"</p>
     */
    INITIAL,
    
    /**
     * Coluna intermediária do fluxo de trabalho.
     * <p>Representa etapas intermediárias onde as tarefas estão sendo processadas.
     * Permite movimentação de tarefas entre colunas e pode ter regras específicas
     * de tempo ou responsabilidade.</p>
     * 
     * <p><strong>Exemplos:</strong> "Em Andamento", "Em Revisão", "In Progress"</p>
     */
    PENDING,
    
    /**
     * Coluna final de um fluxo de trabalho.
     * <p>Representa o ponto de conclusão onde as tarefas são finalizadas.
     * Geralmente marca automaticamente as tarefas como concluídas e pode
     * ter restrições para adicionar novas tarefas.</p>
     * 
     * <p><strong>Exemplos:</strong> "Concluído", "Done", "Finalizado"</p>
     */
    FINAL
}