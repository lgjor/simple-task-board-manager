package org.desviante.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import org.desviante.controller.BoardController;
import org.desviante.controller.CardController;
import org.desviante.controller.GoogleAuthController;
import org.desviante.persistence.entity.BoardEntity;
import org.desviante.ui.components.BoardTableComponent;
import org.desviante.ui.components.BoardEditDialog;
import org.desviante.ui.components.BoardDoubleClickListener;
import org.desviante.util.AlertUtils;
import org.desviante.integration.google.GoogleTasksLinkApp;

import java.sql.SQLException;

public class BoardUIController {
    private final BoardController boardController;
    private final CardController cardController;
    private final ObservableList<BoardEntity> boardList;
    private final TableView<BoardEntity> tableView;
    private final VBox columnDisplay;

    public BoardUIController(BoardController boardController, CardController cardController,
                             ObservableList<BoardEntity> boardList, TableView<BoardEntity> tableView, VBox columnDisplay) {
        this.boardController = boardController;
        this.cardController = cardController;
        this.boardList = boardList;
        this.tableView = tableView;
        this.columnDisplay = columnDisplay;
        BoardDoubleClickListener.attach(tableView, () -> editBoard(tableView));
    }

    // Métodos para criar, excluir e atualizar boards
    /**
     * Cria os botões de ação
     */
    public VBox createActionButtons(TableView<BoardEntity> tableView) {
        Button createBoardButton = new Button("Criar Board");
        createBoardButton.setId("createBoardButton"); // Adiciona um ID para o teste encontrar o botão
        createBoardButton.setOnAction(e -> createBoard(tableView));

        Button editBoardButton = new Button("Editar Board");
        editBoardButton.setOnAction(e -> editBoard(tableView));

        Button deleteBoardButton = new Button("Excluir Board");
        deleteBoardButton.setOnAction(e -> deleteSelectedBoard(tableView));

        Button refreshButton = new Button("Atualizar");
        refreshButton.setOnAction(e -> BoardTableComponent.loadBoards(tableView, boardList, columnDisplay));

        // Botão para autenticar com o Google Task
        Button googleAuthButton = new Button("Vincular Google");
        GoogleAuthController.setupGoogleAuthButton(googleAuthButton);

        // Botão para acessar o Google Task
        Button googleTaskLink = new Button("Google Task");
        googleTaskLink.setOnAction(event -> GoogleTasksLinkApp.abrirGoogleTasks());

        Button createCardButton = getCreateCardButton(tableView);

        VBox actionButtons = new VBox(10, createBoardButton, editBoardButton, deleteBoardButton, refreshButton, createCardButton, googleAuthButton, googleTaskLink);
        actionButtons.setPadding(new Insets(10));

        return actionButtons;
    }

    private Button getCreateCardButton(TableView<BoardEntity> tableView) {
        Button createCardButton = new Button("Criar Card");
        createCardButton.setOnAction(e -> {
            BoardEntity selectedBoard = tableView.getSelectionModel().getSelectedItem();
            if (selectedBoard != null) {
                // Supondo que você tenha uma instância de CardUIController disponível:
                CardUIController cardUIController = new CardUIController(
                        cardController,
                        boardList,
                        tableView,
                        columnDisplay
                );
                cardUIController.showCreateCardDialog(selectedBoard);
                Platform.runLater(() -> {
                    BoardTableComponent.loadBoards(tableView, boardList, columnDisplay);
                });
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Nenhum Board Selecionado");
                alert.setHeaderText(null);
                alert.setContentText("Por favor, selecione um board para criar um card.");
                alert.showAndWait();
            }
        });
        return createCardButton;
    }

    private void createBoard(TableView<BoardEntity> tableView) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Criar Board");
        dialog.setHeaderText(null);
        dialog.setContentText("Digite o nome do novo board:");

        dialog.showAndWait().ifPresent(boardName -> {
            if (boardName == null || boardName.trim().isEmpty()) {
                AlertUtils.showAlert(Alert.AlertType.WARNING, "Nome Inválido", "O nome do board não pode estar vazio.");
                return;
            }
            try {
                // 1. Cria o board através do controller.
                boardController.createBoard(boardName.trim());

                // 2. Invoca o metodo de recarregamento completo.
                //    Isso garante que a lista seja atualizada e as colunas da tabela sejam reconstruídas.
                BoardTableComponent.loadBoards(tableView, boardList, columnDisplay);
                AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Board criado com sucesso!");
            } catch (Exception e) {
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro", "Erro ao criar o board: " + e.getMessage());
            }
        });
    }

    private void editBoard(TableView<BoardEntity> tableView) {
        BoardEntity selectedBoard = tableView.getSelectionModel().getSelectedItem();
        if (selectedBoard == null) {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Nenhum Board Selecionado", "Selecione um board para editar.");
            return;
        }

        BoardEditDialog dialog = new BoardEditDialog(selectedBoard.getName());
        var result = dialog.showAndWait();

        if (result.isPresent()) {
            String newName = result.get().trim();
            if (newName.isEmpty()) {
                AlertUtils.showAlert(Alert.AlertType.WARNING, "Título inválido", "O título não pode estar vazio.");
                return;
            }

            try {
                selectedBoard.setName(newName);
                boardController.updateBoard(selectedBoard);
                tableView.refresh();
                AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Board atualizado com sucesso!");
            } catch (Exception ex) {
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro", "Erro ao atualizar o board: " + ex.getMessage());
            }
        }
    }

    private void deleteSelectedBoard(TableView<BoardEntity> tableView) {
        BoardEntity selectedBoard = tableView.getSelectionModel().getSelectedItem();
        if (selectedBoard != null) {
            try {
                boardController.deleteBoard(selectedBoard);

                // Se chegar a esta linha, a operação foi bem-sucedida.
                BoardTableComponent.loadBoards(tableView, boardList, columnDisplay);
                AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Board excluído com sucesso!");

            } catch (Exception ex) { // 3. Mude de SQLException para a genérica Exception
                //    Qualquer erro lançado pelo serviço ou controller será capturado aqui.
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro", "Erro ao excluir o board: " + ex.getMessage());
            }
        } else {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Nenhum Board Selecionado", "Por favor, selecione um board para excluir.");
        }
    }

    private void createCard(BoardEntity board) {
        // Exemplo: abrir diálogo para coletar título e descrição
        String title = "Título do Card"; // Substitua por coleta real do usuário
        String description = "Descrição do Card"; // Substitua por coleta real do usuário
        try {
            cardController.createCard(board, title, description);
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Card criado", "Card criado com sucesso!");
        } catch (Exception e) {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro", "Erro ao criar card: " + e.getMessage());
        }
    }
}
