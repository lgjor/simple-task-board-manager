package org.desviante.model;

import lombok.*;
import org.desviante.model.enums.BoardColumnKindEnum;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
public class BoardColumn {
    private Long id;

    private String name;

    private int orderIndex;

    private BoardColumnKindEnum kind;

    /**
     * Representa a chave estrangeira para a tabela 'boards'.
     * Em uma abordagem com JDBC, os relacionamentos são representados
     * diretamente pelos IDs, em vez de referências a objetos.
     */
    private Long boardId;
}
