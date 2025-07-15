package org.desviante.ui;

import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.desviante.controller.CardController;
import org.desviante.persistence.entity.BoardEntity;
import org.desviante.ui.components.BoardTableComponent; // Import necessário para o refresh
import org.desviante.util.AlertUtils;

import java.util.Optional;

public class CardUIController {

    private final CardController cardController;
    private final ObservableList<BoardEntity> boardList;
    private final TableView<BoardEntity> tableView;
    private final VBox columnDisplay;

    public CardUIController(CardController cardController,
                            ObservableList<BoardEntity> boardList,
                            TableView<BoardEntity> tableView,
                            VBox columnDisplay) {
        this.cardController = cardController;
        this.boardList = boardList;
        this.tableView = tableView;
        this.columnDisplay = columnDisplay;
    }

    /**
     * Orquestra a criação de um card no board selecionado, delegando para o CardController.
     * Após a criação, atualiza a interface gráfica.
     */
    public void createCard(BoardEntity board, String title, String description) {
        try {
            cardController.createCard(board, title, description);

            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Card criado com sucesso!");

            // Após criar o card, recarrega a visualização do board
            BoardTableComponent.loadBoards(tableView, boardList, columnDisplay);

        } catch (Exception e) {
            // Captura qualquer exceção (ex: validação do controller, erro do serviço)
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro", "Erro ao criar card: " + e.getMessage());
        }
    }

    /**
     * Exibe um diálogo para o usuário inserir os dados de um novo card.
     */
    public void showCreateCardDialog(BoardEntity board) {
        if (board == null) {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Ação Inválida", "Selecione um board antes de criar um card.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Criar Novo Card em: " + board.getName());

        TextField titleField = new TextField();
        titleField.setPromptText("Informe um título para criar o card");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Descrição do card");

        VBox content = new VBox(10, new Label("Título: (Campo obrigatório)"), titleField, new Label("Descrição:"), descriptionArea);
        dialog.getDialogPane().setContent(content);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // A validação de título vazio já é feita pelo controller, mas é bom manter na UI
        // para um feedback mais rápido ao usuário.
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String title = titleField.getText();
            String description = descriptionArea.getText();
            if (title == null || title.trim().isEmpty()) {
                AlertUtils.showAlert(Alert.AlertType.WARNING, "Título obrigatório", "O título do card não pode estar vazio.");
                return; // Impede a chamada com título inválido
            }
            // Chama o nosso método 'createCard' refatorado
            createCard(board, title, description);
        }
    }
}