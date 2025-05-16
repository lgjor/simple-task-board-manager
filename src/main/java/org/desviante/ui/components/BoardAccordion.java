package org.desviante.ui.components;

import org.desviante.persistence.entity.BoardEntity;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class BoardAccordion {

    public Accordion create(BoardEntity board) {
        Accordion accordion = new Accordion();

        if (board.getBoardColumns() != null) {
            board.getBoardColumns().forEach(column -> {
                TitledPane columnPane = new TitledPane();
                columnPane.setText(column.getName());

                ListView<String> cardListView = new ListView<>();
                cardListView.setPlaceholder(new Label("Nenhum card disponível"));

                column.getCards().forEach(card -> cardListView.getItems().add(card.getTitle()));

                // Define um tamanho fixo para o conteúdo
                cardListView.setPrefHeight(200); // Altura fixa
                cardListView.setPrefWidth(300);  // Largura fixa

                columnPane.setContent(cardListView);
                columnPane.setExpanded(true); // Expande a aba por padrão
                accordion.getPanes().add(columnPane);
            });
        }

        // Expande a primeira aba por padrão
        if (!accordion.getPanes().isEmpty()) {
            accordion.setExpandedPane(accordion.getPanes().get(0));
        }

        return accordion;
    }

    public HBox createHorizontal(BoardEntity board) {
        HBox hbox = new HBox();
        hbox.setSpacing(10); // Espaçamento entre as colunas

        if (board.getBoardColumns() != null) {
            board.getBoardColumns().forEach(column -> {
                VBox columnBox = new VBox();
                columnBox.setStyle("-fx-border-color: black; -fx-padding: 10; -fx-spacing: 5;");
                columnBox.getChildren().add(new Label(column.getName()));

                ListView<String> cardListView = new ListView<>();
                cardListView.setPlaceholder(new Label("Nenhum card disponível"));

                column.getCards().forEach(card -> cardListView.getItems().add(card.getTitle()));

                cardListView.setPrefHeight(200); // Altura fixa
                cardListView.setPrefWidth(200);  // Largura fixa

                columnBox.getChildren().add(cardListView);
                hbox.getChildren().add(columnBox);
            });
        }

        return hbox;
    }
}