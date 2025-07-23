package org.desviante.view.component;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.desviante.service.dto.CardDetailDTO;
import org.desviante.service.dto.UpdateCardDetailsDTO;

import java.util.function.BiConsumer;

public class CardViewController {

    @FXML private VBox cardPane;
    @FXML private Label titleLabel;
    @FXML private TextField titleField;
    @FXML private Label descriptionLabel;
    @FXML private TextArea descriptionArea;

    // --- COMPONENTES DO RODAPÉ ATUALIZADOS ---
    @FXML private Separator footerSeparator;
    @FXML private VBox footerPane;
    @FXML private HBox creationDateBox;
    @FXML private Label creationDateLabel;
    @FXML private HBox lastUpdateDateBox;
    @FXML private Label lastUpdateDateLabel;
    @FXML private HBox completionDateBox;
    @FXML private Label completionDateLabel;

    // --- COMPONENTES DE CONTROLE DE EDIÇÃO ---
    @FXML private HBox editControlsBox;
    @FXML private Button saveButton;

    private CardDetailDTO cardData;
    private Long sourceColumnId;
    private BiConsumer<Long, UpdateCardDetailsDTO> onSaveCallback;

    @FXML
    public void initialize() {
        setupDragAndDrop();
        setupEditMode();
    }

    public void setData(CardDetailDTO card, Long sourceColumnId, BiConsumer<Long, UpdateCardDetailsDTO> onSaveCallback) {
        this.cardData = card;
        this.sourceColumnId = sourceColumnId;
        this.onSaveCallback = onSaveCallback;
        updateDisplayData(card);
    }

    public void updateDisplayData(CardDetailDTO card) {
        this.cardData = card;
        titleLabel.setText(card.title());
        descriptionLabel.setText(card.description());
        updateFooter(card);
    }

    /**
     * Lógica atualizada para gerenciar a visibilidade de cada linha de data.
     */
    private void updateFooter(CardDetailDTO card) {
        boolean creationVisible = setDateRow(creationDateBox, creationDateLabel, card.creationDate());
        boolean updateVisible = setDateRow(lastUpdateDateBox, lastUpdateDateLabel, card.lastUpdateDate());
        boolean completionVisible = setDateRow(completionDateBox, completionDateLabel, card.completionDate());

        // Torna o rodapé e o separador visíveis apenas se houver alguma data para mostrar
        boolean hasAnyDate = creationVisible || updateVisible || completionVisible;
        footerSeparator.setVisible(hasAnyDate);
        footerSeparator.setManaged(hasAnyDate);
        footerPane.setVisible(hasAnyDate);
        footerPane.setManaged(hasAnyDate);
    }

    /**
     * Método auxiliar para gerenciar uma linha de data no rodapé.
     * Define o texto da data e controla a visibilidade do container HBox.
     * @return true se a linha estiver visível, false caso contrário.
     */
    private boolean setDateRow(HBox container, Label dateLabel, String dateValue) {
        boolean isVisible = dateValue != null && !dateValue.isBlank();
        if (isVisible) {
            dateLabel.setText(dateValue);
        }
        container.setVisible(isVisible);
        container.setManaged(isVisible);
        return isVisible;
    }

    public void updateSourceColumn(Long newSourceColumnId) {
        this.sourceColumnId = newSourceColumnId;
    }

    private void setupDragAndDrop() {
        cardPane.setOnDragDetected(event -> {
            if (cardData == null) return;
            Dragboard db = cardPane.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(cardData.id() + ":" + sourceColumnId);
            db.setContent(content);
            event.consume();
        });
    }

    private void setupEditMode() {
        cardPane.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                switchToEditMode();
            }
        });

        titleField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleSave();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                switchToDisplayMode();
            }
        });

        descriptionArea.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && !event.isShiftDown()) {
                event.consume();
                handleSave();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                switchToDisplayMode();
            }
        });
    }

    private void switchToEditMode() {
        titleField.setText(cardData.title());
        descriptionArea.setText(cardData.description());

        // Esconde os elementos de visualização
        titleLabel.setVisible(false);
        descriptionLabel.setVisible(false);
        footerSeparator.setVisible(false);
        footerPane.setVisible(false);

        // Mostra os elementos de edição
        titleField.setVisible(true);
        titleField.setManaged(true);
        descriptionArea.setVisible(true);
        descriptionArea.setManaged(true);

        // Mostra o botão de salvar
        editControlsBox.setVisible(true);
        editControlsBox.setManaged(true);

        titleField.requestFocus();
    }

    private void switchToDisplayMode() {
        updateDisplayData(this.cardData);

        // Mostra os elementos de visualização
        titleLabel.setVisible(true);
        descriptionLabel.setVisible(true);

        // Esconde os elementos de edição
        titleField.setVisible(false);
        titleField.setManaged(false);
        descriptionArea.setVisible(false);
        descriptionArea.setManaged(false);

        // Esconde o botão de salvar
        editControlsBox.setVisible(false);
        editControlsBox.setManaged(false);
    }

    @FXML
    private void handleSave() {
        String newTitle = titleField.getText();
        if (newTitle == null || newTitle.isBlank()) {
            switchToDisplayMode(); // Apenas cancela se o título for inválido
            return;
        }

        UpdateCardDetailsDTO updatedDetails = new UpdateCardDetailsDTO(
                newTitle,
                descriptionArea.getText()
        );

        if (onSaveCallback != null) {
            onSaveCallback.accept(cardData.id(), updatedDetails);
        }

        this.cardData = new CardDetailDTO(
                cardData.id(),
                updatedDetails.title(),
                updatedDetails.description(),
                cardData.creationDate(),
                "agora",
                cardData.completionDate()
        );

        switchToDisplayMode();
    }
}