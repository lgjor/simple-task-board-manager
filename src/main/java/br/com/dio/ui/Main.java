package br.com.dio.ui;

import br.com.dio.persistence.entity.BoardColumnKindEnum;
import br.com.dio.persistence.entity.BoardEntity;
import br.com.dio.service.BoardQueryService;
import br.com.dio.service.BoardService;
import br.com.dio.ui.components.BoardAccordion;
import javafx.application.Platform;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import br.com.dio.persistence.entity.CardEntity;
import br.com.dio.service.CardService;
import br.com.dio.persistence.dao.BoardColumnDAO;
import br.com.dio.persistence.entity.BoardColumnEntity;
import java.sql.Connection;
import br.com.dio.ui.components.CardDragAndDropListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.dio.ui.components.CardDragAndDrop;

import java.sql.SQLException;
import java.util.Objects;
import javafx.scene.layout.Priority;
import br.com.dio.service.BoardStatusService;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.sql.Timestamp;

import static br.com.dio.persistence.config.ConnectionConfig.getConnection;

public class Main extends Application {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private final ObservableList<BoardEntity> boardList = FXCollections.observableArrayList();
    private TableView<BoardEntity> tableView;
    private BorderPane root;

    @Override
    public void start(Stage primaryStage) {
        try (Connection connection = getConnection()) {
            BoardColumnDAO boardColumnDAO = new BoardColumnDAO(connection);
            boardColumnDAO.setRefreshBoardCallback(this::refreshBoardView);

            primaryStage.setTitle("Gerenciador de Boards");

            // Layout principal
            root = new BorderPane();
            root.setPadding(new Insets(10));

            // Inicializa a tabela de boards
            tableView = createBoardTable();
            loadBoards(tableView);

            // Botões de ação
            VBox actionButtons = createActionButtons(tableView);

            // Exibe as colunas do board selecionado
            VBox columnDisplay = new VBox();
            columnDisplay.setId("column-display");
            columnDisplay.setSpacing(10);

            // Configurar o columnDisplay
            BorderPane.setMargin(columnDisplay, new Insets(10, 0, 0, 0));

            // Configurar o listener da tabela...
            configureTableViewListener(columnDisplay);

            // Adiciona componentes ao layout principal
            root.setCenter(tableView);
            root.setRight(actionButtons);
            root.setBottom(columnDisplay);

            // Cena e exibição
            Scene scene = new Scene(root, 1024, 800);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
            primaryStage.setScene(scene);

            primaryStage.show();
        } catch (SQLException e) {
            logger.error("Erro ao carregar o board", e);
            showErrorAlert("Erro de Conexão", "Não foi possível conectar ao banco de dados: " + e.getMessage());
            Platform.exit();
        }
    }

    /**
     * Cria a tabela de boards
     */
    private TableView<BoardEntity> createBoardTable() {
        TableView<BoardEntity> tableView = new TableView<>();
        tableView.setPlaceholder(new Label("Nenhum board disponível"));

        TableColumn<BoardEntity, Long> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(data -> data.getValue().idProperty().asObject());

        TableColumn<BoardEntity, String> nameColumn = new TableColumn<>("Nome");
        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());

        // Coluna de status
        TableColumn<BoardEntity, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(data -> {
            BoardEntity board = data.getValue();
            return new ReadOnlyStringWrapper(BoardStatusService.determineBoardStatus(board));
        });

        tableView.getColumns().setAll(List.of(idColumn, nameColumn, statusColumn));
        tableView.setItems(boardList);

        return tableView;
    }

    private void configureTableViewListener(VBox columnDisplay) {
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                try (var connection = getConnection()) {
                    var queryService = new BoardQueryService(connection);
                    // Busca as informações atualizadas do board no banco
                    var refreshedBoardOptional = queryService.findById(newSelection.getId());
                    if (refreshedBoardOptional.isPresent()) {
                        var refreshedBoard = refreshedBoardOptional.get();

                        // Atualiza a visualização vertical dos cards com drag and drop
                        columnDisplay.getChildren().clear();

                        // Cria as colunas com suporte a drag and drop
                        HBox boardColumns = new HBox(10);
                        boardColumns.setPrefWidth(Double.MAX_VALUE);

                        // Fazer o HBox crescer para ocupar o espaço disponível
                        VBox.setVgrow(boardColumns, Priority.ALWAYS);

                        for (BoardColumnEntity column : refreshedBoard.getBoardColumns()) {
                            VBox columnBox = createColumnBoxWithDragDrop(column, refreshedBoard);
                            boardColumns.getChildren().add(columnBox);
                        }

                        columnDisplay.getChildren().add(boardColumns);
                    }
                } catch (Exception e) {
                    logger.error("Erro ao carregar o board", e);
                    showErrorAlert("Erro", "Erro ao carregar o board: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Cria os botões de ação
     */
    private VBox createActionButtons(TableView<BoardEntity> tableView) {
        Button createBoardButton = new Button("Criar Board");
        createBoardButton.setOnAction(e -> createBoard(tableView));

        Button deleteBoardButton = new Button("Excluir Board");
        deleteBoardButton.setOnAction(e -> deleteSelectedBoard(tableView));

        Button refreshButton = new Button("Atualizar");
        refreshButton.setOnAction(e -> loadBoards(tableView));

        Button createCardButton = getCreateCardButton(tableView);

        VBox actionButtons = new VBox(10, createBoardButton, deleteBoardButton, refreshButton, createCardButton);
        actionButtons.setPadding(new Insets(10));

        return actionButtons;
    }

    private Button getCreateCardButton(TableView<BoardEntity> tableView) {
        Button createCardButton = new Button("Criar Card");
        createCardButton.setOnAction(e -> {
            BoardEntity selectedBoard = tableView.getSelectionModel().getSelectedItem();
            if (selectedBoard != null) {
                new BoardAccordion().create(selectedBoard);
                new TableView<>();
                new VBox();// Certifique-se de inicializar o VBox
                createCard(selectedBoard);
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

    /**
     * Cria uma caixa de cards com suporte a drag and drop
     */
    private VBox createColumnBoxWithDragDrop(BoardColumnEntity column, BoardEntity board) {
        VBox columnBox = new VBox();
        columnBox.setId("column-" + column.getId());

        // Configurações de estilo e layout...
        columnBox.setStyle("-fx-border-color: #CCCCCC; -fx-background-color: #F5F5F5; -fx-padding: 10; -fx-spacing: 5; -fx-border-radius: 5;");
        HBox.setHgrow(columnBox, Priority.ALWAYS);

        // Título da coluna
        Label columnTitle = new Label(column.getName());
        columnTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        columnBox.getChildren().add(columnTitle);

        // Separador
        Separator separator = new Separator();
        columnBox.getChildren().add(separator);

        // Área para cards
        VBox cardsArea = new VBox();
        cardsArea.setSpacing(5);
        cardsArea.setMinHeight(300);
        cardsArea.setStyle("-fx-padding: 5;");
        VBox.setVgrow(cardsArea, Priority.ALWAYS);
        columnBox.getChildren().add(cardsArea);

        CardDragAndDropListener listener = new CardDragAndDropListener(
                tableView,
                this::loadBoards
        );

        // Cria o gerenciador de drag and drop com um novo listener
        CardDragAndDrop dragAndDrop = new CardDragAndDrop(listener);

        // Configura a área de cards e a coluna como alvos de drop
        dragAndDrop.setupDropTarget(cardsArea, column.getId());
        dragAndDrop.setupDropTarget(columnBox, column.getId());

        // Adiciona os cards à área de cards
        for (CardEntity card : column.getCards()) {
            VBox cardBox = createCardBox(card);
            dragAndDrop.setupDragSource(cardBox, card.getId());
            cardsArea.getChildren().add(cardBox);
        }
        return columnBox;
    }

    /**
     * Cria uma caixa de card
     * Adiciona eventos de clique e drag and drop
     */
    private VBox createCardBox(CardEntity card) {
        // Define o formatador de data
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // Busca as datas atualizadas do banco
        try (Connection connection = getConnection()) {
            String sql = "SELECT creation_date, last_update_date, completion_date FROM CARDS WHERE id = ?";
            try (var preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setLong(1, card.getId());
                try (var resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        // Atualiza as datas do objeto card
                        card.setCreationDate(resultSet.getTimestamp("creation_date").toLocalDateTime());

                        Timestamp lastUpdate = resultSet.getTimestamp("last_update_date");
                        if (lastUpdate != null) {
                            card.setLastUpdateDate(lastUpdate.toLocalDateTime());
                        }

                        Timestamp completionDate = resultSet.getTimestamp("completion_date");
                        if (completionDate != null) {
                            card.setCompletionDate(completionDate.toLocalDateTime());
                        }
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar datas do card", e);
            System.err.println("Erro ao buscar datas do card: " + e.getMessage());
        }

        VBox cardBox = new VBox();
        cardBox.setId("card-" + card.getId());
        cardBox.setStyle("-fx-border-color: #DDDDDD; -fx-background-color: white; -fx-padding: 8; " +
                "-fx-spacing: 3; -fx-border-radius: 3; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 1);");

        // Título do card (inicialmente como Label)
        Label titleLabel = new Label(card.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold;");
        cardBox.getChildren().add(titleLabel);

        // Descrição do card (inicialmente como Label)
        Label descLabel = new Label(card.getDescription());
        descLabel.setWrapText(true); // Permite quebra de linha
        cardBox.getChildren().add(descLabel);

        // Data de criação formatada
        Label dateLabel = new Label("Criado em: " +
                card.getCreationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        dateLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 10px;");
        cardBox.getChildren().add(dateLabel);

        // Label para a última atualização
        Label lastUpdateLabel = new Label("Última atualização: " +
                (card.getLastUpdateDate() != null ? card.getLastUpdateDate().format(formatter) : "Não atualizado"));
        lastUpdateLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 10px;");
        cardBox.getChildren().add(lastUpdateLabel);

        // Label para a data de conclusão
        Label completionLabel = new Label("Concluido em: " +
                (card.getCompletionDate() != null ? card.getCompletionDate().format(formatter) : "Não concluído"));
        completionLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 10px;");
        cardBox.getChildren().add(completionLabel);

        // Adiciona evento de duplo clique para editar o card inline
        cardBox.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                // Impede que o evento de drag seja acionado durante a edição
                event.consume();

                // Substitui o Label do título por um TextField
                TextField titleField = new TextField(card.getTitle());
                titleField.setStyle("-fx-font-weight: bold;");
                cardBox.getChildren().set(cardBox.getChildren().indexOf(titleLabel), titleField);

                // Substitui o Label da descrição por um TextArea
                TextArea descArea = new TextArea(card.getDescription());
                descArea.setWrapText(true);
                descArea.setPrefRowCount(3);
                cardBox.getChildren().set(cardBox.getChildren().indexOf(descLabel), descArea);

                // Adiciona botões de salvar e cancelar
                HBox buttons = new HBox(5);
                Button saveButton = new Button("Salvar");
                Button cancelButton = new Button("Cancelar");
                buttons.getChildren().addAll(saveButton, cancelButton);
                cardBox.getChildren().add(buttons);

                // Dá foco ao campo de título
                Platform.runLater(titleField::requestFocus);

                // Ação do botão Salvar
                saveButton.setOnAction(e -> {
                    String newTitle = titleField.getText().trim();
                    String newDescription = descArea.getText().trim();

                    if (newTitle.isEmpty() || newDescription.isEmpty()) {
                        showErrorAlert("Campos inválidos", "Título e descrição não podem estar vazios.");
                        return;
                    }

                    // Atualiza o card no banco de dados
                    Connection connection = null;
                    try {
                        connection = getConnection();
                        boolean originalAutoCommit = connection.getAutoCommit();
                        connection.setAutoCommit(false);

                        LocalDateTime now = LocalDateTime.now();
                        String sql = "UPDATE CARDS SET title = ?, description = ?, last_update_date = ? WHERE id = ?";

                        try (var preparedStatement = connection.prepareStatement(sql)) {
                            preparedStatement.setString(1, newTitle);
                            preparedStatement.setString(2, newDescription);
                            preparedStatement.setTimestamp(3, Timestamp.valueOf(now));
                            preparedStatement.setLong(4, card.getId());

                            int rowsAffected = preparedStatement.executeUpdate();

                            if (rowsAffected > 0) {
                                connection.commit();

                                // Atualiza o objeto card
                                card.setTitle(newTitle);
                                card.setDescription(newDescription);
                                card.setLastUpdateDate(now);

                                // Atualiza os labels
                                titleLabel.setText(newTitle);
                                descLabel.setText(newDescription);
                                lastUpdateLabel.setText("Ultima atualizacao: " + now.format(formatter));

                                // Remove campos de edição e botões
                                int titleIndex = cardBox.getChildren().indexOf(titleField);
                                int descIndex = cardBox.getChildren().indexOf(descArea);
                                int buttonsIndex = cardBox.getChildren().indexOf(buttons);

                                if (titleIndex >= 0) cardBox.getChildren().set(titleIndex, titleLabel);
                                if (descIndex >= 0) cardBox.getChildren().set(descIndex, descLabel);
                                if (buttonsIndex >= 0) cardBox.getChildren().remove(buttonsIndex);

                                refreshBoardView(tableView.getSelectionModel().getSelectedItem().getId());
                            } else {
                                connection.rollback();
                                showErrorAlert("Erro ao atualizar", "Nenhum registro foi atualizado no banco de dados.");
                                restoreOriginalView(cardBox, titleLabel, descLabel, titleField, descArea, buttons);
                            }

                            connection.setAutoCommit(originalAutoCommit);
                        }
                    } catch (SQLException ex) {
                        logger.error("Erro ao atualizar o card", ex);
                        if (connection != null) {
                            try {
                                connection.rollback();
                            } catch (SQLException rollbackEx) {
                                logger.error("Erro ao realizar rollback da transacao", rollbackEx);
                            }
                        }
                        showErrorAlert("Erro ao atualizar card", "Ocorreu um erro ao atualizar o card: " + ex.getMessage());
                        restoreOriginalView(cardBox, titleLabel, descLabel, titleField, descArea, buttons);
                    } finally {
                        if (connection != null) {
                            try {
                                connection.close();
                            } catch (SQLException closeEx) {
                                logger.error("Erro ao fechar a conexao", closeEx);
                            }
                        }
                    }
                });

                // Ação do botão Cancelar
                cancelButton.setOnAction(e -> {
                    restoreOriginalView(cardBox, titleLabel, descLabel, titleField, descArea, buttons);
                });
            }
        });

        return cardBox;
    }

    /**
     /* Método para criar um novo board
     */
    private void createBoard(TableView<BoardEntity> tableView) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Criar Board");
        dialog.setHeaderText(null);
        dialog.setContentText("Digite o nome do novo board:");

        dialog.showAndWait().ifPresent(boardName -> {
            if (boardName.trim().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Nome Inválido");
                alert.setHeaderText(null);
                alert.setContentText("O nome do board não pode estar vazio.");
                alert.showAndWait();
                return;
            }

            try (var connection = getConnection()) {
                var newBoard = getBoardEntity(boardName, connection);

                boardList.add(newBoard);
                tableView.refresh();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Sucesso");
                alert.setHeaderText(null);
                alert.setContentText("Board criado com sucesso!");
                alert.showAndWait();
            } catch (SQLException ex) {
                logger.error("Erro ao criar o card", ex);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erro");
                alert.setHeaderText(null);
                alert.setContentText("Erro ao criar o board: " + ex.getMessage());
                alert.showAndWait();
            }
        });
    }

    private static BoardEntity getBoardEntity(String boardName, Connection connection) throws SQLException {
        var boardService = new BoardService(connection);
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

    private void restoreOriginalView(VBox cardBox, Label titleLabel, Label descLabel,
                                     TextField titleField, TextArea descArea, HBox buttons) {
        int titleIndex = cardBox.getChildren().indexOf(titleField);
        int descIndex = cardBox.getChildren().indexOf(descArea);
        if (titleIndex >= 0) cardBox.getChildren().set(titleIndex, titleLabel);
        if (descIndex >= 0) cardBox.getChildren().set(descIndex, descLabel);
        cardBox.getChildren().remove(buttons);
    }

    /**
     * Atualiza a visualização do board com o ID especificado
     */
    private void refreshBoardView(Long boardId) {
        // Pequeno delay para garantir que a transação foi finalizada
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);  // Garante consistência da leitura

            // Busca o board atualizado
            BoardQueryService queryService = new BoardQueryService(connection);
            var boardOptional = queryService.findById(boardId);

            if (boardOptional.isPresent()) {
                BoardEntity refreshedBoard = boardOptional.get();

                // Garante que a atualização da UI ocorra na thread do JavaFX
                Platform.runLater(() -> {
                    // Obtém a área de exibição das colunas
                    VBox columnDisplay = (VBox) root.getBottom();
                    if (columnDisplay != null) {
                        columnDisplay.getChildren().clear();

                        // Cria as colunas com suporte a drag and drop
                        HBox boardColumns = new HBox(10);
                        boardColumns.setPrefWidth(Double.MAX_VALUE);
                        VBox.setVgrow(boardColumns, Priority.ALWAYS);

                        try {
                            // Nova conexão para operações dentro do Platform.runLater
                            getConnection();
                            for (BoardColumnEntity column : refreshedBoard.getBoardColumns()) {
                                VBox columnBox = createColumnBoxWithDragDrop(column, refreshedBoard);
                                boardColumns.getChildren().add(columnBox);
                            }
                            columnDisplay.getChildren().add(boardColumns);

                            // Força atualização da TableView
                            tableView.refresh();

                            System.out.println("Visualização do board atualizada com sucesso");
                        } catch (SQLException e) {
                            System.err.println("Erro ao criar colunas: " + e.getMessage());
                            logger.error("Erro ao criar colunas", e);
                        }
                    } else {
                        System.err.println("Área de exibição das colunas não encontrada");
                    }
                });

                connection.commit();
            } else {
                System.err.println("Board não encontrado: " + boardId);
            }
        } catch (Exception e) {
            System.err.println("Erro ao atualizar a visualização do board: " + e.getMessage());
            logger.error("Erro ao atualizar a visualização do board", e);
        }
    }

    /**
     * Exclui o board selecionado
     */
    private void deleteSelectedBoard(TableView<BoardEntity> tableView) {
        BoardEntity selectedBoard = tableView.getSelectionModel().getSelectedItem();
        if (selectedBoard != null) {
            try (var connection = getConnection()) {
                var boardService = new BoardService(connection);
                boolean deleted = boardService.delete(selectedBoard.getId());
                if (deleted) {
                    boardList.remove(selectedBoard); // Remove o board da lista
                    tableView.refresh(); // Atualiza a tabela
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Sucesso");
                    alert.setHeaderText(null);
                    alert.setContentText("Board excluído com sucesso!");
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Erro");
                    alert.setHeaderText(null);
                    alert.setContentText("O board não foi encontrado para exclusão.");
                    alert.showAndWait();
                }
            } catch (SQLException ex) {
                logger.error("Erro ao excluir o board", ex);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erro");
                alert.setHeaderText(null);
                alert.setContentText("Erro ao excluir o board: " + ex.getMessage());
                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Nenhum Board Selecionado");
            alert.setHeaderText(null);
            alert.setContentText("Por favor, selecione um board para excluir.");
            alert.showAndWait();
        }
    }

    /**
     * Carrega os boards na tabela
     */
    private void loadBoards(TableView<BoardEntity> tableView) {
        // Salva o ID do board selecionado antes de limpar a lista
        BoardEntity selectedBefore = tableView.getSelectionModel().getSelectedItem();
        Long selectedId = selectedBefore != null ? selectedBefore.getId() : null;
        // Limpa a lista de boards
        boardList.clear();

        try (var connection = getConnection()) {
            var queryService = new BoardQueryService(connection);
            var boards = queryService.findAll();

            // Para cada board, carregamos as colunas e os cards
            for (BoardEntity board : boards) {
                // Carrega as colunas do board com seus cards
                var optionalBoard = queryService.findById(board.getId());
                if (optionalBoard.isPresent()) {
                    BoardEntity fullBoard = optionalBoard.get();
                    board.setBoardColumns(fullBoard.getBoardColumns());

                    // Verifica se as colunas foram carregadas corretamente
                    System.out.println("Board carregado: " + board.getName() +
                            ", Colunas: " + (board.getBoardColumns() != null ?
                            board.getBoardColumns().size() : "null"));
                } else {
                    System.err.println("Não foi possível carregar o board completo: " + board.getId());
                }
            }

            boardList.addAll(boards);
            tableView.refresh();

            // Atualiza a visualização das colunas/cards do board selecionado
            if (selectedId != null) {
                for (BoardEntity board : boardList) {
                    if (board.getId().equals(selectedId)) {
                        tableView.getSelectionModel().select(board);
                        refreshBoardView(board.getId());
                        return;
                    }
                }
            }

            // Se não houver seleção anterior, seleciona o primeiro (caso exista)
            if (!boardList.isEmpty()) {
                tableView.getSelectionModel().selectFirst();
                refreshBoardView(boardList.get(0).getId());
            }
        } catch (SQLException ex) {
            logger.error("Erro ao carregar boards", ex);
            showErrorAlert("Erro ao carregar boards", "Ocorreu um erro ao carregar os boards: " + ex.getMessage());
        }
    }

    /**
     * Cria um card no board selecionado
     */
    private void createCard(BoardEntity board) {
        TextInputDialog titleDialog = new TextInputDialog();
        titleDialog.setTitle("Criar Card");
        titleDialog.setHeaderText(null);
        titleDialog.setContentText("Digite o título do novo card:");

        titleDialog.showAndWait().ifPresent(cardTitle -> {
            if (cardTitle.trim().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Título Inválido");
                alert.setHeaderText(null);
                alert.setContentText("O título do card não pode estar vazio.");
                alert.showAndWait();
                return;
            }

            TextInputDialog descriptionDialog = new TextInputDialog();
            descriptionDialog.setTitle("Criar Card");
            descriptionDialog.setHeaderText(null);
            descriptionDialog.setContentText("Digite a descrição do novo card:");

            descriptionDialog.showAndWait().ifPresent(cardDescription -> {
                if (cardDescription.trim().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Descrição Inválida");
                    alert.setHeaderText(null);
                    alert.setContentText("A descrição do card não pode estar vazia.");
                    alert.showAndWait();
                    return;
                }

                try (var connection = getConnection()) {
                    var cardService = new CardService(connection);
                    var queryService = new BoardQueryService(connection);

                    // Obtém o board atualizado com suas colunas já existentes
                    var optionalBoard = queryService.findById(board.getId());
                    if (optionalBoard.isEmpty() || optionalBoard.get().getBoardColumns().isEmpty()) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Erro");
                        alert.setHeaderText(null);
                        alert.setContentText("O board não possui colunas configuradas.");
                        alert.showAndWait();
                        return;
                    }

                    var fullBoard = optionalBoard.get();
                    // Busca a coluna do tipo INITIAL dinamicamente
                    BoardColumnEntity initialColumn = fullBoard.getBoardColumns()
                            .stream()
                            .filter(column -> column.getKind().equals(BoardColumnKindEnum.INITIAL))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("Coluna INITIAL não encontrada."));

                    // Cria o novo card associando a coluna default
                    var newCard = new CardEntity();
                    newCard.setTitle(cardTitle);
                    newCard.setDescription(cardDescription);
                    newCard.setBoardColumn(initialColumn);

                    cardService.create(newCard);

                    // Atualiza a interface com o board atualizado
                    var updatedBoard = queryService.findById(board.getId()).orElseThrow();

                    // Atualiza a visualização
                    VBox updatedColumnDisplay = (VBox) root.getBottom();
                    updatedColumnDisplay.getChildren().clear();

                    // Cria as colunas com suporte a drag and drop
                    HBox boardColumns = new HBox(10); // Espaçamento de 10 entre as colunas
                    boardColumns.setPrefWidth(Double.MAX_VALUE); // Ocupa toda a largura disponível

                    // Fazer o HBox crescer para ocupar o espaço disponível
                    VBox.setVgrow(boardColumns, Priority.ALWAYS);

                    for (BoardColumnEntity column : updatedBoard.getBoardColumns()) {
                        VBox columnBox = createColumnBoxWithDragDrop(column, updatedBoard);
                        boardColumns.getChildren().add(columnBox);
                    }

                    updatedColumnDisplay.getChildren().add(boardColumns);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Sucesso");
                    alert.setHeaderText(null);
                    alert.setContentText("Card criado com sucesso!");
                    alert.showAndWait();
                } catch (SQLException ex) {
                    logger.error("Erro ao criar o card", ex);
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erro");
                    alert.setHeaderText(null);
                    alert.setContentText("Erro ao criar o card: " + ex.getMessage());
                    alert.showAndWait();
                }
            });
        });
    }


    /**
     * Exibe um alerta de erro com um título e uma mensagem.
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}