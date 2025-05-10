package br.com.dio.persistence.entity;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static br.com.dio.persistence.entity.BoardColumnKindEnum.CANCEL;
import static br.com.dio.persistence.entity.BoardColumnKindEnum.INITIAL;

@Data
public class BoardEntity {

    private Long id;
    private String name;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<BoardColumnEntity> boardColumns = new ArrayList<>();

    // Validações p/ que cada board tenha: 1 col. inicial, cancelamento e final, com pelo menos 3 colunas.
    public void validateColumns() {
        long initialCount = boardColumns.stream().filter(c -> c.getKind() == BoardColumnKindEnum.INITIAL).count();
        long cancelCount = boardColumns.stream().filter(c -> c.getKind() == BoardColumnKindEnum.CANCEL).count();
        long finalCount = boardColumns.stream().filter(c -> c.getKind() == BoardColumnKindEnum.FINAL).count();

        if (initialCount != 1) {
            throw new IllegalStateException("O board deve ter exatamente uma coluna inicial.");
        }
        if (cancelCount > 1) {
            throw new IllegalStateException("O board pode ter no máximo uma coluna de cancelamento.");
        }
        if (finalCount != 1) {
            throw new IllegalStateException("O board deve ter exatamente uma coluna final.");
        }
        if (boardColumns.size() < 3) {
            throw new IllegalStateException("O board deve ter pelo menos 3 colunas.");
        }
    }

    public BoardColumnEntity getInitialColumn() {
        return boardColumns.stream()
                .filter(bc -> bc.getKind().equals(INITIAL))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Nenhuma coluna inicial encontrada para o board."));
    }

    public BoardColumnEntity getCancelColumn(){
        return getFilteredColumn(bc -> bc.getKind().equals(CANCEL));
    }



    private BoardColumnEntity getFilteredColumn(Predicate<BoardColumnEntity> filter){
        return boardColumns.stream()
                .filter(filter)
                .findFirst().orElseThrow();
    }

    public LongProperty idProperty() {
        return new SimpleLongProperty(this, "id", id);
    }
    public StringProperty nameProperty() {
        return new SimpleStringProperty(this, "name", name);
    }

    // Lógica para mover cards entre colunas
    public void moveCardToNextColumn(CardEntity card) {
        if (card.isBlocked()) {
            throw new IllegalStateException("O card está bloqueado e não pode ser movido.");
        }

        BoardColumnEntity currentColumn = card.getBoardColumn();
        BoardColumnEntity nextColumn = boardColumns.stream()
                .filter(c -> c.getOrder() == currentColumn.getOrder() + 1)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Não há próxima coluna disponível."));

        card.setBoardColumn(nextColumn);
    }

    // Bloqueio de cards
    public void blockCard(CardEntity card, String reason) {
        if (card.isBlocked()) {
            throw new IllegalStateException("O card já está bloqueado.");
        }
        card.setBlocked(true);
        card.setBlockReason(reason);
    }

    // Desloqueio de cards
    public void unblockCard(CardEntity card, String reason) {
        if (!card.isBlocked()) {
            throw new IllegalStateException("O card não está bloqueado.");
        }
        card.setBlocked(false);
        card.setUnblockReason(reason);
    }

}


