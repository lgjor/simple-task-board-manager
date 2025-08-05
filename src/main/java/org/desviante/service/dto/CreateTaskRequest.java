package org.desviante.service.dto;

import java.time.LocalDateTime;

/**
 * DTO para encapsular dados de requisição para criação de tarefas no Google Tasks.
 * 
 * <p>Representa os dados necessários para criar uma nova tarefa na API do
 * Google Tasks, encapsulando as informações que o sistema local envia para
 * o serviço externo. Este DTO é utilizado como estrutura de entrada para
 * operações de sincronização com o Google Tasks, fornecendo todos os dados
 * essenciais para a criação de tarefas na plataforma externa.</p>
 * 
 * <p>Contém o título da lista do Google Tasks, título da tarefa, notas
 * detalhadas e data de vencimento. A data de vencimento é mantida como
 * LocalDateTime para preservar informações de horário específico, que são
 * anexadas nas notas como solução para limitações da API do Google Tasks.</p>
 * 
 * <p>Utilizado principalmente pelo TaskService para encapsular dados antes
 * de enviar para o GoogleTasksApiService, mantendo a separação entre a
 * lógica de negócio local e a comunicação com a API externa do Google.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see TaskService
 * @see GoogleTasksApiService
 */
public record CreateTaskRequest(
        String listTitle,           // Título da lista no Google Tasks
        String title,               // Título da tarefa a ser criada
        String notes,               // Notas detalhadas da tarefa
        LocalDateTime due           // Data e horário de vencimento
) {}