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
 * 
 * @param listTitle título da lista no Google Tasks
 * @param title título da tarefa a ser criada
 * @param notes notas detalhadas da tarefa
 * @param due data e horário de vencimento
 * @param cardId ID do card associado (para relacionamento local)
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
public record CreateTaskRequestDTO(
        String listTitle,
        String title,
        String notes,
        LocalDateTime due,
        Long cardId
) {
}