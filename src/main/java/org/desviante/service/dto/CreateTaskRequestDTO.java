package org.desviante.service.dto;

import java.time.LocalDateTime;

/**
 * DTO para encapsular dados de requisição da UI para criação de tarefas.
 * 
 * <p>Representa os dados que a interface do usuário envia para a fachada,
 * incluindo o cardId para associar a tarefa ao card local. Este DTO é
 * utilizado como estrutura de entrada para operações de criação de tarefas
 * iniciadas pela interface do usuário.</p>
 * 
 * <p>Contém todos os dados necessários para criar uma tarefa no Google
 * Tasks, além do identificador do card local para estabelecer o
 * relacionamento entre a tarefa externa e o card interno.</p>
 */
public record CreateTaskRequestDTO(
        String listTitle,       // Título da lista no Google Tasks
        String title,               // Título da tarefa a ser criada
        String notes,               // Notas detalhadas da tarefa
        LocalDateTime due,          // Data e horário de vencimento
        Long cardId                 // ID do card associado (para relacionamento local)
) {
}