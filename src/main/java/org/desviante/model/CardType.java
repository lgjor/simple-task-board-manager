package org.desviante.model;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Representa um tipo de card criado pelo usuário.
 * 
 * <p>Permite que os usuários criem seus próprios tipos de card com labels
 * de unidade personalizados (ex: "páginas", "minutos", "aulas", "etapas").</p>
 * 
 * <p>Cada tipo de card possui um nome único e um label de unidade que
 * será usado na interface para mostrar o progresso dos cards deste tipo.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/lgjor">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardType {
    
    /**
     * Identificador único do tipo de card.
     * 
     * @return identificador único do tipo de card
     * @param id novo identificador único do tipo de card
     */
    private Long id;
    
    /**
     * Nome do tipo de card (ex: "Livro", "Vídeo", "Curso").
     * Deve ser único no sistema.
     * 
     * @return nome do tipo de card
     * @param name novo nome do tipo de card
     */
    private String name;
    
    /**
     * Label da unidade de progresso (ex: "páginas", "minutos", "aulas").
     * Usado na interface para mostrar o progresso dos cards.
     * 
     * @return label da unidade de progresso
     * @param unitLabel novo label da unidade de progresso
     */
    private String unitLabel;
    
    /**
     * Data de criação do tipo de card.
     * 
     * @return data de criação do tipo de card
     * @param creationDate nova data de criação do tipo de card
     */
    private LocalDateTime creationDate;
    
    /**
     * Data da última atualização do tipo de card.
     * 
     * @return data da última atualização do tipo de card
     * @param lastUpdateDate nova data da última atualização do tipo de card
     */
    private LocalDateTime lastUpdateDate;
} 