package org.desviante.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
public class Board {
    private Long id;
    private String name;
    private LocalDateTime creationDate;
    private Long groupId; // Nova propriedade
    private BoardGroup group; // Para carregamento eager quando necessário


// Métodos auxiliares
    public String getGroupName() {
        return group != null ? group.getName() : "Sem Grupo";
    }
    
    public String getGroupColor() {
        return group != null ? group.getColor() : "#CCCCCC";
    }
}