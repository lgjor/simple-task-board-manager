package org.desviante.persistence.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Data
public class BoardColumnEntity {
    private Long id;
    private String name;
    private int order_index;
    private BoardColumnKindEnum kind;
    private List<CardEntity> cards = new ArrayList<>();
    @Setter
    @Getter
    private BoardEntity board;

    public void addCard(CardEntity card) {
        cards.add(card);
    }

    public void removeCard(CardEntity card) {
        cards.remove(card);
    }
}
