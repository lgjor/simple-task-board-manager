package org.desviante.model;

import lombok.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * Representa uma tarefa sincronizada com o Google Tasks.
 * 
 * <p>Esta classe é responsável por manter a sincronização entre as tarefas
 * do sistema local e as tarefas do Google Tasks. Cada Task representa uma
 * tarefa que pode ser enviada para ou recebida da API do Google Tasks,
 * permitindo integração bidirecional entre os sistemas.</p>
 * 
 * <p>Os campos notes e due foram especificamente criados para compatibilidade
 * com a API do Google Tasks, garantindo que as informações sejam preservadas
 * durante a sincronização.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see Card
 * @see java.time.LocalDateTime
 * @see java.time.OffsetDateTime
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Task {
    
    /**
     * Identificador único da tarefa.
     * <p>Usado como chave primária na persistência e para operações
     * de igualdade e hash code.</p>
     * 
     * @return identificador único da tarefa
     * @param id novo identificador único da tarefa
     */
    private Long id;

    /**
     * Título da lista do Google Tasks.
     * <p>Representa o nome da lista no Google Tasks onde esta tarefa
     * está ou será sincronizada.</p>
     * 
     * @return título da lista do Google Tasks
     * @param listTitle novo título da lista do Google Tasks
     */
    private String listTitle;

    /**
     * Título da tarefa.
     * <p>Representa o nome ou título descritivo da tarefa que será
     * sincronizada com o Google Tasks.</p>
     * 
     * @return título da tarefa
     * @param title novo título da tarefa
     */
    private String title;

    /**
     * Data de vencimento da tarefa.
     * <p>Campo criado especificamente para integração com a API do Google Tasks.
     * Usa OffsetDateTime para preservar informações de fuso horário durante
     * a sincronização com o Google Tasks.</p>
     * 
     * @return data de vencimento da tarefa
     * @param due nova data de vencimento da tarefa
     * @see java.time.OffsetDateTime
     */
    private OffsetDateTime due;

    /**
     * Notas ou observações da tarefa.
     * <p>Campo criado especificamente para integração com a API do Google Tasks.
     * Permite armazenar informações adicionais que serão sincronizadas
     * com as notas da tarefa no Google Tasks, são as descrições das tarefas no Google Tasks.</p>
     * 
     * @return notas ou observações da tarefa
     * @param notes novas notas ou observações da tarefa
     */
    private String notes;

    /**
     * Identificador da tarefa no Google Tasks.
     * <p>Armazena o ID único da tarefa no Google Tasks para permitir
     * sincronização e atualizações sem criar duplicatas.</p>
     * 
     * @return identificador da tarefa no Google Tasks
     * @param googleTaskId novo identificador da tarefa no Google Tasks
     */
    private String googleTaskId;

    /**
     * Indica se a tarefa já foi enviada para o Google Tasks.
     * <p>Usado para controlar o estado de sincronização e evitar
     * reenvios desnecessários para a API do Google Tasks.</p>
     * 
     * @return true se a tarefa já foi enviada para o Google Tasks
     * @param sent novo valor indicando se a tarefa já foi enviada para o Google Tasks
     */
    private boolean sent;

    /**
     * Identificador do card associado a esta tarefa.
     * <p>Representa a chave estrangeira para a tabela 'cards'.
     * Em uma abordagem com JDBC, os relacionamentos são representados
     * diretamente pelos IDs, em vez de referências a objetos.</p>
     * 
     * @return identificador do card associado a esta tarefa
     * @param cardId novo identificador do card associado a esta tarefa
     * @see Card
     */
    private Long cardId;

    /**
     * Data e hora de criação da tarefa.
     * <p>Este campo é automaticamente preenchido quando uma nova tarefa é criada
     * e não deve ser modificado posteriormente. Útil para auditoria e histórico.</p>
     * 
     * @return data e hora de criação da tarefa
     * @param creationDate nova data e hora de criação da tarefa
     */
    private LocalDateTime creationDate;

    /**
     * Data e hora da última atualização da tarefa.
     * <p>Este campo é atualizado automaticamente sempre que a tarefa é modificada,
     * permitindo acompanhar quando foi a última alteração realizada.</p>
     * 
     * @return data e hora da última atualização da tarefa
     * @param lastUpdateDate nova data e hora da última atualização da tarefa
     */
    private LocalDateTime lastUpdateDate;
    
    /**
     * Obtém a data e hora da última atualização da tarefa.
     * 
     * @return data e hora da última atualização da tarefa
     */
    public LocalDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }
    
    /**
     * Define a data e hora da última atualização da tarefa.
     * 
     * @param lastUpdateDate nova data e hora da última atualização da tarefa
     */
    public void setLastUpdateDate(LocalDateTime lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }
}