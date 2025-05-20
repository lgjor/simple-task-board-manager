package org.desviante.ui;

import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
import java.util.Optional;

import org.desviante.controller.BoardController;
import org.desviante.controller.CardController;
import org.desviante.persistence.entity.BoardEntity;
import org.desviante.service.CardService;
import org.desviante.util.AlertUtils;

import static org.desviante.persistence.config.ConnectionConfig.getConnection;

import java.sql.Connection;

public class CardUIController {

    public CardUIController(CardController cardController, BoardController boardController,
                            ObservableList<BoardEntity> boardList, TableView<BoardEntity> tableView, VBox columnDisplay) {
    }

    // Métodos para criar, excluir e atualizar cards

    /**
     * Cria um card no board selecionado
     */
    public void createCard(BoardEntity board, String title, String description) {
        try (Connection connection = getConnection()) {
            CardService cardService = new CardService(connection);
            cardService.createAndInsertCard(board, title, description);
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Card criado com sucesso!");
            // Atualize a interface aqui, se necessário
        } catch (Exception e) {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro", "Erro ao criar card: " + e.getMessage());
        }
    }

    public void showCreateCardDialog(BoardEntity board) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Criar Card");

        TextField titleField = new TextField();
        titleField.setPromptText("Informe um título para criar o card");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Descrição do card");

        VBox content = new VBox(10, new Label("Título: (Campo obrigatório)"), titleField, new Label("Descrição:"), descriptionArea);
        dialog.getDialogPane().setContent(content);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String title = titleField.getText();
            String description = descriptionArea.getText();
            if (title == null || title.trim().isEmpty()) {
                AlertUtils.showAlert(Alert.AlertType.WARNING, "Título obrigatório", "O título do card não pode estar vazio.");
                return;
            }
            createCard(board, title, description);
        }
    }
}
