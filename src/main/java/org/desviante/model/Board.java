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
}
