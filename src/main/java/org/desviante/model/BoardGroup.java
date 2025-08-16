package org.desviante.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa um grupo de quadros no sistema de gerenciamento de tarefas.
 * 
 * <p>Esta classe implementa o padrão de domínio para um grupo de quadros, seguindo
 * o princípio de responsabilidade única (SRP) do SOLID. Um BoardGroup é responsável
 * por encapsular as informações de um grupo que agrupa quadros relacionados,
 * incluindo nome, descrição, cor, ícone e data de criação.</p>
 * 
 * <p>A classe utiliza anotações do Lombok para reduzir código boilerplate,
 * mantendo a legibilidade e seguindo o princípio DRY (Don't Repeat Yourself).
 * O método toString() é sobrescrito para fornecer uma representação adequada
 * do grupo na interface do usuário.</p>
 * 
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 * @see Board
 * @see java.time.LocalDateTime
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BoardGroup {
    
    /**
     * Identificador único do grupo.
     * <p>Este campo é usado como chave primária na persistência e para
     * operações de igualdade e hash code.</p>
     * 
     * @return identificador único do grupo
     * @param id novo identificador único do grupo
     */
    private Long id;
    
    /**
     * Nome do grupo.
     * <p>Representa o nome descritivo do grupo que será exibido na interface
     * e usado para identificação do grupo pelos usuários.</p>
     * 
     * @return nome do grupo
     * @param name novo nome do grupo
     */
    private String name;
    
    /**
     * Descrição detalhada do grupo.
     * <p>Fornece informações adicionais sobre o propósito e conteúdo do grupo,
     * ajudando os usuários a entenderem melhor a organização dos quadros.</p>
     * 
     * @return descrição detalhada do grupo
     * @param description nova descrição detalhada do grupo
     */
    private String description;
    
    /**
     * Cor do grupo em formato hexadecimal.
     * <p>Este campo é usado para personalização visual do grupo na interface,
     * permitindo que cada grupo tenha uma cor distintiva para melhor organização.</p>
     * 
     * <p><strong>Formato esperado:</strong> Código hexadecimal (ex: "#FF5733")</p>
     * 
     * @return cor do grupo em formato hexadecimal
     * @param color nova cor do grupo em formato hexadecimal
     */
    private String color;
    
    /**
     * Ícone associado ao grupo.
     * <p>Representa um identificador ou caminho para um ícone que será exibido
     * junto com o nome do grupo na interface, melhorando a identificação visual.</p>
     * 
     * @return ícone associado ao grupo
     * @param icon novo ícone associado ao grupo
     */
    private String icon;
    
    /**
     * Data e hora de criação do grupo.
     * <p>Este campo é automaticamente preenchido quando um novo grupo é criado
     * e não deve ser modificado posteriormente. Útil para auditoria e histórico.</p>
     * 
     * @return data e hora de criação do grupo
     * @param creationDate nova data e hora de criação do grupo
     */
    private LocalDateTime creationDate;
    
    /**
     * Retorna uma representação em string do grupo.
     * <p>Este método sobrescreve o comportamento padrão do toString() para
     * fornecer uma representação mais adequada do grupo na interface do usuário.
     * Segue o princípio de responsabilidade única, focando apenas na representação
     * textual do objeto.</p>
     * 
     * <p><strong>Comportamento:</strong></p>
     * <ul>
     *   <li>Se o nome do grupo não for nulo, retorna o nome do grupo</li>
     *   <li>Se o nome do grupo for nulo, retorna "Sem Grupo" como valor padrão</li>
     * </ul>
     * 
     * @return O nome do grupo ou "Sem Grupo" se o nome for nulo
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return name != null ? name : "Sem Grupo";
    }
}