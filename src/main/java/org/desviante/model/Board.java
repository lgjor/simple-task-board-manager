package org.desviante.model;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Representa um quadro (board) no sistema de gerenciamento de tarefas.
 * 
 * <p>Esta classe implementa o padrão de domínio para um quadro, seguindo o princípio
 * de responsabilidade única (SRP) do SOLID. Um Board é responsável por encapsular
 * as informações básicas de um quadro de tarefas, incluindo seu nome, data de criação
 * e associação com um grupo.</p>
 * 
 * <p>A classe utiliza anotações do Lombok para reduzir código boilerplate,
 * mantendo a legibilidade e seguindo o princípio DRY (Don't Repeat Yourself).</p>
 * 
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see BoardGroup
 * @see java.time.LocalDateTime
 */
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
public class Board {
    
    /**
     * Identificador único do quadro.
     * <p>Este campo é usado como chave primária na persistência e para
     * operações de igualdade e hash code.</p>
     */
    private Long id;
    
    /**
     * Nome do quadro.
     * <p>Representa o nome descritivo do quadro que será exibido na interface.</p>
     */
    private String name;
    
    /**
     * Data e hora de criação do quadro.
     * <p>Este campo é automaticamente preenchido quando um novo quadro é criado
     * e não deve ser modificado posteriormente.</p>
     */
    private LocalDateTime creationDate;
    
    /**
     * Identificador do grupo ao qual o quadro pertence.
     * <p>Este campo mantém a referência ao grupo através do ID, permitindo
     * consultas eficientes sem carregar o objeto completo do grupo.</p>
     */
    private Long groupId;
    
    /**
     * Referência ao objeto do grupo associado ao quadro.
     * <p>Este campo é usado para carregamento eager quando necessário,
     * permitindo acesso direto às propriedades do grupo sem consultas adicionais.</p>
     * 
     * @see BoardGroup
     */
    private BoardGroup group;

    /**
     * Obtém o nome do grupo associado ao quadro.
     * <p>Este método implementa o princípio de encapsulamento, fornecendo
     * uma interface limpa para acessar o nome do grupo mesmo quando o grupo
     * não está carregado ou é nulo.</p>
     * 
     * <p><strong>Comportamento:</strong></p>
     * <ul>
     *   <li>Se o grupo estiver carregado e não for nulo, retorna o nome do grupo</li>
     *   <li>Se o grupo for nulo, retorna "Sem Grupo" como valor padrão</li>
     * </ul>
     * 
     * @return O nome do grupo ou "Sem Grupo" se não houver grupo associado
     * @see BoardGroup#getName()
     */
    public String getGroupName() {
        return group != null ? group.getName() : "Sem Grupo";
    }
    
    /**
     * Obtém a cor do grupo associado ao quadro.
     * <p>Este método fornece acesso à cor do grupo para fins de apresentação
     * na interface do usuário, seguindo o princípio de responsabilidade única.</p>
     * 
     * <p><strong>Comportamento:</strong></p>
     * <ul>
     *   <li>Se o grupo estiver carregado e não for nulo, retorna a cor do grupo</li>
     *   <li>Se o grupo for nulo, retorna "#CCCCCC" (cinza claro) como cor padrão</li>
     * </ul>
     * 
     * @return A cor do grupo em formato hexadecimal ou "#CCCCCC" se não houver grupo associado
     * @see BoardGroup#getColor()
     */
    public String getGroupColor() {
        return group != null ? group.getColor() : "#CCCCCC";
    }
}