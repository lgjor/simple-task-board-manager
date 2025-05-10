package br.com.dio.ui;

import br.com.dio.persistence.entity.BoardColumnKindEnum;
import br.com.dio.persistence.entity.BoardEntity;
import br.com.dio.service.BoardQueryService;
import br.com.dio.service.BoardService;
import br.com.dio.ui.components.BoardAccordion;
import br.com.dio.ui.components.BoardUI;
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
import javafx.scene.text.Text;
import br.com.dio.ui.components.CardDragAndDrop;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import java.sql.SQLException;
import java.util.Objects;
import javafx.scene.layout.Priority;
import br.com.dio.service.BoardStatusService;

import static br.com.dio.persistence.config.ConnectionConfig.getConnection;

public class JavaFXApp extends Application {

    private final ObservableList<BoardEntity> boardList = FXCollections.observableArrayList();
    private TableView<BoardEntity> tableView;
    private BorderPane root;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Gerenciador de Boards");

        // Layout principal
        root = new BorderPane(); // Inicializa o campo root
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

        // Fazer o columnDisplay crescer para ocupar o espaço disponível
        BorderPane.setMargin(columnDisplay, new Insets(10, 0, 0, 0));

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                try (var connection = getConnection()) {
                    var queryService = new BoardQueryService(connection);
                    BoardUI boardUI = new BoardUI(connection);

                    // Busca as informações atualizadas do board no banco
                    var refreshedBoardOptional = queryService.findById(newSelection.getId());
                    if (refreshedBoardOptional.isPresent()) {
                        var refreshedBoard = refreshedBoardOptional.get();

                        // Atualiza a visualização vertical dos cards com drag and drop
                        columnDisplay.getChildren().clear();

                        // Cria as colunas com suporte a drag and drop
                        HBox boardColumns = new HBox(10); // Espaçamento de 10 entre as colunas
                        boardColumns.setPrefWidth(Double.MAX_VALUE); // Ocupa toda a largura disponível

                        // Fazer o HBox crescer para ocupar o espaço disponível
                        VBox.setVgrow(boardColumns, Priority.ALWAYS);

                        for (BoardColumnEntity column : refreshedBoard.getBoardColumns()) {
                            VBox columnBox = createColumnBoxWithDragDrop(column, refreshedBoard, connection);
                            boardColumns.getChildren().add(columnBox);
                        }

                        columnDisplay.getChildren().add(boardColumns);
                        root.setBottom(columnDisplay);
                    } else {
                        System.err.println("Board não encontrado no banco.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // Adiciona componentes ao layout principal
        root.setCenter(tableView);
        root.setRight(actionButtons);
        root.setBottom(columnDisplay); // Coloca a visualização de colunas na parte inferior

        // Cena e exibição
        Scene scene = new Scene(root, 1024, 800); // Aumentar o tamanho inicial da janela
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
        primaryStage.setScene(scene);

        primaryStage.show();
    }

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

        tableView.getColumns().addAll(idColumn, nameColumn, statusColumn); // Adiciona as colunas à tabela
        tableView.setItems(boardList);

        return tableView;
    }

    private VBox createActionButtons(TableView<BoardEntity> tableView) {
        Button createBoardButton = new Button("Criar Board");
        createBoardButton.setOnAction(e -> createBoard(tableView));

        Button deleteBoardButton = new Button("Excluir Board");
        deleteBoardButton.setOnAction(e -> deleteSelectedBoard(tableView));

        Button refreshButton = new Button("Atualizar");
        refreshButton.setOnAction(e -> loadBoards(tableView));

        Button createCardButton = new Button("Criar Card");
        createCardButton.setOnAction(e -> {
            BoardEntity selectedBoard = tableView.getSelectionModel().getSelectedItem();
            if (selectedBoard != null) {
                Accordion accordion = new BoardAccordion().create(selectedBoard);
                TableView<CardEntity> cardTableView = new TableView<>();
                VBox columnDisplay = new VBox(); // Certifique-se de inicializar o VBox
                createCard(selectedBoard, cardTableView, accordion, columnDisplay);
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Nenhum Board Selecionado");
                alert.setHeaderText(null);
                alert.setContentText("Por favor, selecione um board para criar um card.");
                alert.showAndWait();
            }
        });

        VBox actionButtons = new VBox(10, createBoardButton, deleteBoardButton, refreshButton, createCardButton);
        actionButtons.setPadding(new Insets(10));

        return actionButtons;
    }

    private VBox createColumnBox(BoardColumnEntity column, BoardEntity board, Connection connection) {
        VBox columnBox = new VBox();
        columnBox.setId("column-" + column.getId());
        columnBox.setStyle("-fx-border-color: black; -fx-padding: 10; -fx-spacing: 5;");
        columnBox.getChildren().add(new Text(column.getName()));

        // Cria o gerenciador de drag and drop
        CardDragAndDrop dragAndDrop = new CardDragAndDrop((cardId, targetColumnId) -> {
            try (var newConnection = getConnection()) {
                System.out.println("Atualizando card " + cardId + " para coluna " + targetColumnId);

                // Atualiza o card no banco de dados
                BoardColumnDAO boardColumnDAO = new BoardColumnDAO(newConnection);
                boardColumnDAO.updateCardColumn(cardId, targetColumnId);

                // Verifica se a atualização foi bem-sucedida consultando o banco
                var cardService = new CardService(newConnection);
                var updatedCard = cardService.findById(cardId);

                if (updatedCard != null && updatedCard.getBoardColumn().getId().equals(targetColumnId)) {
                    System.out.println("Verificação: Card " + cardId + " agora está na coluna " + updatedCard.getBoardColumn().getId());
                } else {
                    System.err.println("Verificação falhou: Card não foi atualizado corretamente no banco de dados");
                }

                // Atualiza a interface gráfica
                BoardQueryService queryService = new BoardQueryService(newConnection);
                var refreshedBoard = queryService.findById(board.getId()).orElseThrow();

                // Atualiza a UI na thread do JavaFX
                Platform.runLater(() -> {
                    try (var refreshConnection = getConnection()) {
                        // Recria a visualização das colunas
                        VBox columnDisplay = (VBox) root.getBottom();
                        columnDisplay.getChildren().clear();

                        // Cria as colunas com suporte a drag and drop
                        HBox boardColumns = new HBox(10); // Espaçamento de 10 entre as colunas

                        for (BoardColumnEntity refreshedColumn : refreshedBoard.getBoardColumns()) {
                            VBox refreshedColumnBox = createColumnBoxWithDragDrop(refreshedColumn, refreshedBoard, refreshConnection);
                            boardColumns.getChildren().add(refreshedColumnBox);
                        }

                        columnDisplay.getChildren().add(boardColumns);
                    } catch (Exception e) {
                        e.printStackTrace();
                        showErrorAlert("Erro ao atualizar a interface", e.getMessage());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showErrorAlert("Erro ao mover o card", e.getMessage());
                });
            }
        });

        // Configura a coluna como alvo de drop
        dragAndDrop.setupDropTarget(columnBox, column.getId());

        // Adiciona os cards à coluna
        for (CardEntity card : column.getCards()) {
            VBox cardBox = new VBox();
            cardBox.setId("card-" + card.getId());
            cardBox.setStyle("-fx-border-color: gray; -fx-padding: 5; -fx-spacing: 3; -fx-background-color: white;");

            // Adiciona o título e descrição do card
            cardBox.getChildren().add(new Text("Título: " + card.getTitle()));
            cardBox.getChildren().add(new Text("Descrição: " + card.getDescription()));

            // Configura o card como fonte de drag
            dragAndDrop.setupDragSource(cardBox, card.getId());

            // Adiciona o card à coluna
            columnBox.getChildren().add(cardBox);
        }

        return columnBox;
    }

    private VBox createColumnBoxWithDragDrop(BoardColumnEntity column, BoardEntity board, Connection connection) {
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

        // Cria o gerenciador de drag and drop com um novo listener
        CardDragAndDrop dragAndDrop = new CardDragAndDrop((cardId, targetColumnId) -> {
            // Importante: Criar uma nova conexão aqui, não usar a conexão passada como parâmetro
            try (Connection newConnection = getConnection()) {
                System.out.println("Atualizando card " + cardId + " para coluna " + targetColumnId);

                // Atualiza o card no banco de dados
                BoardColumnDAO boardColumnDAO = new BoardColumnDAO(newConnection);
                boardColumnDAO.updateCardColumn(cardId, targetColumnId);

                // Verifica se a atualização foi bem-sucedida
                String checkSql = "SELECT board_column_id FROM CARDS WHERE id = ?";
                try (var statement = newConnection.prepareStatement(checkSql)) {
                    statement.setLong(1, cardId);
                    try (var resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            Long updatedColumnId = resultSet.getLong("board_column_id");
                            System.out.println("Verificação: Card " + cardId + " agora está na coluna " + updatedColumnId);

                            // Se a atualização foi bem-sucedida, atualiza a UI
                            if (updatedColumnId.equals(targetColumnId)) {
                                // Atualiza a interface gráfica na thread do JavaFX
                                Platform.runLater(() -> refreshBoardView(board.getId()));

                                // Atualiza o status na tabela
                                BoardQueryService queryService = new BoardQueryService(newConnection);
                                queryService.findById(board.getId()).ifPresent(updatedBoard -> {
                                    // Encontra e atualiza o item na TableView
                                    for (int i = 0; i < boardList.size(); i++) {
                                        if (boardList.get(i).getId().equals(board.getId())) {
                                            boardList.set(i, updatedBoard);
                                            break;
                                        }
                                    }
                                    tableView.refresh();
                                });
                            } else {
                                System.err.println("Falha na atualização: o card não foi movido para a coluna correta");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Erro ao atualizar o card: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    showErrorAlert("Erro ao mover o card", e.getMessage());
                });
            }
        });

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

    private VBox createCardBox(CardEntity card) {
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

                        // Desativa o autocommit para controlar a transação manualmente
                        boolean originalAutoCommit = connection.getAutoCommit();
                        connection.setAutoCommit(false);

                        // Imprime informações de debug
                        System.out.println("Atualizando card ID: " + card.getId());
                        System.out.println("Novo título: " + newTitle);
                        System.out.println("Nova descrição: " + newDescription);

                        // Salva as alterações no banco de dados usando SQL direto
                        String sql = "UPDATE CARDS SET title = ?, description = ? WHERE id = ?";
                        try (var preparedStatement = connection.prepareStatement(sql)) {
                            preparedStatement.setString(1, newTitle);
                            preparedStatement.setString(2, newDescription);
                            preparedStatement.setLong(3, card.getId());

                            int rowsAffected = preparedStatement.executeUpdate();
                            System.out.println("Linhas afetadas: " + rowsAffected);

                            if (rowsAffected > 0) {
                                // Confirma a transação
                                connection.commit();

                                // Atualiza o objeto card
                                card.setTitle(newTitle);
                                card.setDescription(newDescription);

                                // Atualiza os labels com os novos valores
                                titleLabel.setText(newTitle);
                                descLabel.setText(newDescription);

                                // Restaura os Labels com os novos valores
                                cardBox.getChildren().set(cardBox.getChildren().indexOf(titleField), titleLabel);
                                cardBox.getChildren().set(cardBox.getChildren().indexOf(descArea), descLabel);

                                // Remove os botões
                                cardBox.getChildren().remove(buttons);

                                System.out.println("Card atualizado com sucesso no banco de dados.");

                                // Atualiza a visualização do board para refletir as mudanças
                                BoardEntity selectedBoard = tableView.getSelectionModel().getSelectedItem();
                                if (selectedBoard != null) {
                                    refreshBoardView(selectedBoard.getId());
                                }
                            } else {
                                // Desfaz a transação
                                connection.rollback();

                                System.err.println("Nenhuma linha afetada ao atualizar o card ID: " + card.getId());
                                showErrorAlert("Erro ao atualizar", "Nenhum registro foi atualizado no banco de dados.");

                                // Restaura os Labels com os valores originais
                                cardBox.getChildren().set(cardBox.getChildren().indexOf(titleField), titleLabel);
                                cardBox.getChildren().set(cardBox.getChildren().indexOf(descArea), descLabel);
                                cardBox.getChildren().remove(buttons);
                            }

                            // Restaura o autocommit para o estado original
                            connection.setAutoCommit(originalAutoCommit);
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        System.err.println("Erro SQL: " + ex.getMessage());

                        // Tenta fazer rollback em caso de erro
                        if (connection != null) {
                            try {
                                connection.rollback();
                            } catch (SQLException rollbackEx) {
                                rollbackEx.printStackTrace();
                            }
                        }

                        showErrorAlert("Erro ao atualizar card", "Ocorreu um erro ao atualizar o card: " + ex.getMessage());

                        // Restaura os Labels com os valores originais em caso de erro
                        cardBox.getChildren().set(cardBox.getChildren().indexOf(titleField), titleLabel);
                        cardBox.getChildren().set(cardBox.getChildren().indexOf(descArea), descLabel);
                        cardBox.getChildren().remove(buttons);
                    } finally {
                        // Fecha a conexão
                        if (connection != null) {
                            try {
                                connection.close();
                            } catch (SQLException closeEx) {
                                closeEx.printStackTrace();
                            }
                        }
                    }
                });

                // Ação do botão Cancelar
                cancelButton.setOnAction(e -> {
                    // Restaura os Labels sem alterar os valores
                    cardBox.getChildren().set(cardBox.getChildren().indexOf(titleField), titleLabel);
                    cardBox.getChildren().set(cardBox.getChildren().indexOf(descArea), descLabel);
                    cardBox.getChildren().remove(buttons);
                });
            }
        });

        return cardBox;
    }


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
                var boardService = new BoardService(connection);
                var boardColumnDAO = new BoardColumnDAO(connection);
                var newBoard = new BoardEntity();
                newBoard.setName(boardName);
                boardService.insert(newBoard);

                // Insere as colunas padrão para o novo board
                boardColumnDAO.insertDefaultColumns(newBoard.getId());
                // Associa as colunas recém inseridas ao board
                newBoard.setBoardColumns(boardColumnDAO.findByBoardId(newBoard.getId()));

                boardList.add(newBoard);
                tableView.refresh();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Sucesso");
                alert.setHeaderText(null);
                alert.setContentText("Board criado com sucesso!");
                alert.showAndWait();
            } catch (SQLException ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erro");
                alert.setHeaderText(null);
                alert.setContentText("Erro ao criar o board: " + ex.getMessage());
                alert.showAndWait();
            }
        });
    }

    /**
     * Atualiza a visualização do board com o ID especificado
     */
    private void refreshBoardView(Long boardId) {
        try (Connection connection = getConnection()) {
            // Busca o board atualizado
            BoardQueryService queryService = new BoardQueryService(connection);
            var boardOptional = queryService.findById(boardId);

            if (boardOptional.isPresent()) {
                BoardEntity refreshedBoard = boardOptional.get();

                // Obtém a área de exibição das colunas
                VBox columnDisplay = (VBox) root.getBottom();
                if (columnDisplay != null) {
                    columnDisplay.getChildren().clear();

                    // Cria as colunas com suporte a drag and drop
                    HBox boardColumns = new HBox(10);
                    boardColumns.setPrefWidth(Double.MAX_VALUE);
                    VBox.setVgrow(boardColumns, Priority.ALWAYS);

                    for (BoardColumnEntity column : refreshedBoard.getBoardColumns()) {
                        VBox columnBox = createColumnBoxWithDragDrop(column, refreshedBoard, connection);
                        boardColumns.getChildren().add(columnBox);
                    }

                    columnDisplay.getChildren().add(boardColumns);
                    System.out.println("Visualização do board atualizada com sucesso");
                } else {
                    System.err.println("Área de exibição das colunas não encontrada");
                }
            } else {
                System.err.println("Board não encontrado: " + boardId);
            }
        } catch (Exception e) {
            System.err.println("Erro ao atualizar a visualização do board: " + e.getMessage());
            e.printStackTrace();
        }
    }


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
                ex.printStackTrace();
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

    private void loadBoards(TableView<BoardEntity> tableView) {
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
        } catch (SQLException ex) {
            ex.printStackTrace();
            showErrorAlert("Erro ao carregar boards", "Ocorreu um erro ao carregar os boards: " + ex.getMessage());
        }
    }


    // Cria card utilizando o board selecionado
    private void createCard(BoardEntity board, TableView<CardEntity> cardTableView, Accordion accordion, VBox columnDisplay) {
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
                        VBox columnBox = createColumnBoxWithDragDrop(column, updatedBoard, connection);
                        boardColumns.getChildren().add(columnBox);
                    }

                    updatedColumnDisplay.getChildren().add(boardColumns);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Sucesso");
                    alert.setHeaderText(null);
                    alert.setContentText("Card criado com sucesso!");
                    alert.showAndWait();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erro");
                    alert.setHeaderText(null);
                    alert.setContentText("Erro ao criar o card: " + ex.getMessage());
                    alert.showAndWait();
                }
            });
        });
    }

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