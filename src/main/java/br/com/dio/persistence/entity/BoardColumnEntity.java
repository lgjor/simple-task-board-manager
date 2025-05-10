package br.com.dio.persistence.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
public class BoardColumnEntity {
    private Long id;
    private String name;
    private int order;
    private BoardColumnKindEnum kind;
    private List<CardEntity> cards = new ArrayList<>();
    private BoardEntity board;

    public void addCard(CardEntity card) {
        cards.add(card);
    }

    public void removeCard(CardEntity card) {
        cards.remove(card);
    }
}
