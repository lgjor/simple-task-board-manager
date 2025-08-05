package org.desviante.view.component;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.desviante.model.enums.CardType;
import org.desviante.service.TaskManagerFacade;
import org.desviante.service.dto.BoardColumnDetailDTO;
import org.desviante.service.dto.CardDetailDTO;
import org.desviante.service.dto.CreateCardRequestDTO;
import org.desviante.service.dto.UpdateCardDetailsDTO;

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
                
                // Adicionar efeito visual quando o card pode ser solto
                rootVBox.setStyle(rootVBox.getStyle() + "; -fx-background-color: rgba(0,123,255,0.1); -fx-border-color: #007BFF; -fx-border-width: 2; -fx-border-style: dashed;");
            }
            event.consume();
        });

        rootVBox.setOnDragEntered(event -> {
            if (event.getGestureSource() != rootVBox && event.getDragboard().hasString()) {
                // Efeito visual mais intenso quando o card entra na área da coluna
                rootVBox.setStyle(rootVBox.getStyle() + "; -fx-background-color: rgba(0,123,255,0.2); -fx-border-color: #0056B3; -fx-border-width: 3; -fx-border-style: solid;");
            }
            event.consume();
        });

        rootVBox.setOnDragExited(event -> {
            // Remover efeitos visuais quando o card sai da área da coluna
            String currentStyle = rootVBox.getStyle();
            currentStyle = currentStyle.replace("; -fx-background-color: rgba(0,123,255,0.1); -fx-border-color: #007BFF; -fx-border-width: 2; -fx-border-style: dashed;", "");
            currentStyle = currentStyle.replace("; -fx-background-color: rgba(0,123,255,0.2); -fx-border-color: #0056B3; -fx-border-width: 3; -fx-border-style: solid;", "");
            rootVBox.setStyle(currentStyle);
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
            
            // Remover efeitos visuais após o drop
            String currentStyle = rootVBox.getStyle();
            currentStyle = currentStyle.replace("; -fx-background-color: rgba(0,123,255,0.1); -fx-border-color: #007BFF; -fx-border-width: 2; -fx-border-style: dashed;", "");
            currentStyle = currentStyle.replace("; -fx-background-color: rgba(0,123,255,0.2); -fx-border-color: #0056B3; -fx-border-width: 3; -fx-border-style: solid;", "");
            rootVBox.setStyle(currentStyle);
            
            event.setDropCompleted(success);
            event.consume();
        });
    }

    @FXML
    private void handleCreateCard() {
        Dialog<CardCreationData> dialog = new Dialog<>();
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
        
        ComboBox<CardType> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll(CardType.values());
        typeComboBox.setValue(CardType.CARD); // Valor padrão
        typeComboBox.setCellFactory(param -> new ListCell<CardType>() {
            @Override
            protected void updateItem(CardType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(item.name() + " (" + item.getUnitLabel() + ")");
                }
            }
        });
        typeComboBox.setButtonCell(typeComboBox.getCellFactory().call(null));

        grid.add(new Label("Título:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Descrição:"), 0, 1);
        grid.add(descriptionArea, 1, 1);
        grid.add(new Label("Tipo:"), 0, 2);
        grid.add(typeComboBox, 1, 2);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(titleField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return new CardCreationData(titleField.getText(), descriptionArea.getText(), typeComboBox.getValue());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            String title = result.title();
            String description = result.description();
            CardType type = result.type();

            if (title != null && !title.trim().isEmpty()) {
                try {
                    var request = new CreateCardRequestDTO(title, description, this.columnData.id(), type);
                    CardDetailDTO newCardDTO = facade.createNewCard(request);

                    FXMLLoader cardLoader = new FXMLLoader(getClass().getResource("/view/card-view.fxml"));
                    Parent cardNode = cardLoader.load();
                    CardViewController cardController = cardLoader.getController();
                    cardNode.setUserData(cardController);

                    cardController.setData(
                            this.facade,
                            this.boardName,
                            newCardDTO,
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

    /**
     * Classe auxiliar para encapsular os dados de criação de card.
     */
    private static class CardCreationData {
        private final String title;
        private final String description;
        private final CardType type;

        public CardCreationData(String title, String description, CardType type) {
            this.title = title;
            this.description = description;
            this.type = type;
        }

        public String title() { return title; }
        public String description() { return description; }
        public CardType type() { return type; }
    }
}