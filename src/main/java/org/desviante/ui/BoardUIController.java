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
import org.desviante.persistence.dao.BoardDAO;
import org.desviante.persistence.dao.BoardColumnDAO;
import org.desviante.persistence.entity.BoardEntity;
import org.desviante.service.BoardService;
import org.desviante.ui.components.BoardTableComponent;
import org.desviante.ui.components.BoardEditDialog;
import org.desviante.ui.components.BoardDoubleClickListener;
import org.desviante.util.AlertUtils;

import java.sql.Connection;
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
        createBoardButton.setOnAction(e -> createBoard(tableView));

        Button editBoardButton = new Button("Editar Board");
        editBoardButton.setOnAction(e -> editBoard(tableView));

        Button deleteBoardButton = new Button("Excluir Board");
        deleteBoardButton.setOnAction(e -> deleteSelectedBoard(tableView));

        Button refreshButton = new Button("Atualizar");
        refreshButton.setOnAction(e -> BoardTableComponent.loadBoards(tableView, boardList, columnDisplay));

        // Botão para autenticar com o Google Calendar
        Button googleAuthButton = new Button("Vincular Google");
        GoogleAuthController.setupGoogleAuthButton(googleAuthButton);

        Button createCardButton = getCreateCardButton(tableView);

        VBox actionButtons = new VBox(10, createBoardButton, editBoardButton, deleteBoardButton, refreshButton, createCardButton, googleAuthButton);
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
                        cardController, boardController, boardList, tableView, columnDisplay
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

    private static BoardEntity getBoardEntity(String boardName, Connection connection) throws SQLException {
        var boardDAO = new BoardDAO(connection);
        var boardService = new BoardService(boardDAO);
        var boardColumnDAO = new BoardColumnDAO(connection);
        var newBoard = new BoardEntity();
        newBoard.setName(boardName);
        boardService.insert(newBoard);

        // Insere as colunas padrão para o novo board
        boardColumnDAO.insertDefaultColumns(newBoard.getId());
        // Associa as colunas recém inseridas ao board
        newBoard.setBoardColumns(boardColumnDAO.findByBoardId(newBoard.getId()));
        return newBoard;
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
                var newBoard = boardController.createBoard(boardName.trim());
                boardList.add(newBoard);
                tableView.refresh();
                AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Board criado com sucesso!");
            } catch (SQLException ex) {
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Erro", "Erro ao criar o board: " + ex.getMessage());
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
                boolean deleted = boardController.deleteBoard(selectedBoard);
                if (deleted) {
                    // Recarrega a lista de boards e a visualização das colunas
                    BoardTableComponent.loadBoards(tableView, boardList, columnDisplay);
                    AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Board excluído com sucesso!");
                } else {
                    AlertUtils.showAlert(Alert.AlertType.WARNING, "Erro", "O board não foi encontrado para exclusão.");
                }
            } catch (SQLException ex) {
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
