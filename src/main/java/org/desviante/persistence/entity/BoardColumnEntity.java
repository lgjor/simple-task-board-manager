package org.desviante.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "boards_columns")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class BoardColumnEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @Column(name = "order_index")
    private int order_index;

    @Enumerated(EnumType.STRING) // Boa prática para mapear Enums
    private BoardColumnKindEnum kind;

    @ToString.Exclude
    @OneToMany(
            mappedBy = "boardColumn", // Indica que CardEntity gerencia a relação
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<CardEntity> cards = new ArrayList<>();

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY) // Define a relação: Muitas Colunas para Um Board
    @JoinColumn(name = "board_id", nullable = false) // Define a coluna de chave estrangei
    private BoardEntity board;

    public void addCard(CardEntity card) {
        cards.add(card);
        card.setBoardColumn(this);
    }

    public void removeCard(CardEntity card) {
        cards.remove(card);
        card.setBoardColumn(null);
    }
}
