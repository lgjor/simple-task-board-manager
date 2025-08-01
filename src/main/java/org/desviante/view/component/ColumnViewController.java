package org.desviante.view.component;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import org.desviante.service.TaskManagerFacade;
import org.desviante.service.dto.BoardColumnDetailDTO;
import org.desviante.service.dto.CardDetailDTO;
import org.desviante.service.dto.CreateCardRequestDTO;
import org.desviante.service.dto.UpdateCardDetailsDTO;

import java.io.IOException;
import java.util.function.BiConsumer;
// CORREÇÃO: A linha 'import java.util.function.Runnable;' foi removida.
// A interface Runnable está em java.lang e é importada automaticamente.

public class ColumnViewController {

    @FXML
    private VBox rootVBox;
    @FXML
    private Label columnNameLabel;
    @FXML
    private VBox cardsContainer;

    private TaskManagerFacade facade;
    private String boardName;
    private BoardColumnDetailDTO columnData;
    private BiConsumer<Long, Long> onCardDrop;
    private Runnable onDataChange;
    private BiConsumer<Long, UpdateCardDetailsDTO> onCardUpdate;

    @FXML
    public void initialize() {
        setupDragAndDrop();
    }

    public void setData(
            TaskManagerFacade facade,
            String boardName,
            BoardColumnDetailDTO columnData,
            BiConsumer<Long, Long> onCardDrop,
            Runnable onDataChange,
            BiConsumer<Long, UpdateCardDetailsDTO> onCardUpdate
    ) {
        this.facade = facade;
        this.boardName = boardName; // <--- ARMAZENA O NOME DO BOARD
        this.columnData = columnData;
        this.onCardDrop = onCardDrop;
        this.onDataChange = onDataChange;
        this.onCardUpdate = onCardUpdate;
        this.columnNameLabel.setText(columnData.name());
    }

    public Long getColumnId() {
        return this.columnData.id();
    }

    public void addCard(Node cardNode) {
        if (cardNode.getParent() instanceof VBox) {
            ((VBox) cardNode.getParent()).getChildren().remove(cardNode);
        }
        cardsContainer.getChildren().add(cardNode);
    }

    private void setupDragAndDrop() {
        rootVBox.setOnDragOver(event -> {
            if (event.getGestureSource() != rootVBox && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        rootVBox.setOnDragDropped(event -> {
            var db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                String[] data = db.getString().split(":");
                long cardId = Long.parseLong(data[0]);
                if (onCardDrop != null) {
                    onCardDrop.accept(cardId, this.columnData.id());
                }
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    @FXML
    private void handleCreateCard() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Criar Novo Card");
        dialog.setHeaderText("Digite os detalhes para o novo card.");

        ButtonType createButtonType = new ButtonType("Criar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField();
        titleField.setPromptText("Título do card");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Descrição (opcional)");

        grid.add(new Label("Título:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Descrição:"), 0, 1);
        grid.add(descriptionArea, 1, 1);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(titleField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return new Pair<>(titleField.getText(), descriptionArea.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            String title = result.getKey();
            String description = result.getValue();

            if (title != null && !title.trim().isEmpty()) {
                try {
                    var request = new CreateCardRequestDTO(title, description, this.columnData.id());
                    CardDetailDTO newCardDTO = facade.createNewCard(request);

                    FXMLLoader cardLoader = new FXMLLoader(getClass().getResource("/view/card-view.fxml"));
                    Parent cardNode = cardLoader.load();
                    CardViewController cardController = cardLoader.getController();
                    cardNode.setUserData(cardController);

                    cardController.setData(
                            this.facade,      // <--- NOVO PARÂMETRO
                            this.boardName,   // <--- NOVO PARÂMETRO
                            newCardDTO,
                            this.columnData.id(),
                            this.onCardUpdate
                    );

                    addCard(cardNode);

                    if (onDataChange != null) {
                        onDataChange.run();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, "Falha ao criar o novo card: " + e.getMessage()).showAndWait();
                }
            }
        });
    }
}