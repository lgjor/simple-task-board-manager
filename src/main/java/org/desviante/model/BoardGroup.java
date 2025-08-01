package org.desviante.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BoardGroup {
    private Long id;
    private String name;
    private String description;
    private String color;
    private String icon;
    private LocalDateTime creationDate;
    
    @Override
    public String toString() {
        return name != null ? name : "Sem Grupo";
    }
}