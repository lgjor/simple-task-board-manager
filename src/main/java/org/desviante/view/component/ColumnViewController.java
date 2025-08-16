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
import org.desviante.service.TaskManagerFacade;
import org.desviante.service.dto.BoardColumnDetailDTO;
import org.desviante.service.dto.CardDetailDTO;
import org.desviante.service.dto.CardTypeOptionDTO;
import org.desviante.service.dto.CreateCardRequestDTO;
import org.desviante.service.dto.UpdateCardDetailsDTO;
import org.desviante.model.enums.ProgressType;

import java.util.List;
import java.util.function.BiConsumer;
// CORREÇÃO: A linha 'import java.util.function.Runnable;' foi removida.
// A interface Runnable está em java.lang e é importada automaticamente.
import java.util.Optional;

/**
 * Controlador para uma coluna individual do quadro.
 * 
 * <p>Este controlador gerencia a exibição e interação com uma coluna específica
 * do quadro, incluindo a exibição de cards, drag and drop, e atualizações
 * de dados em tempo real.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
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
    
    /**
     * Construtor padrão da classe ColumnViewController.
     * 
     * <p>Inicializa o controlador de coluna do quadro.
     * A interface será configurada quando o método initialize() for chamado.</p>
     */
    public ColumnViewController() {
        // Construtor padrão - interface será configurada via FXML
    }

    /**
     * Inicializa o controlador e configura a interface.
     * 
     * <p>Este método é chamado automaticamente pelo JavaFX após a
     * construção do controlador.</p>
     */
    @FXML
    public void initialize() {
        setupDragAndDrop();
    }

    /**
     * Define os dados da coluna e configura o controlador.
     * 
     * @param facade fachada principal para gerenciamento de tarefas
     * @param boardName nome do quadro ao qual a coluna pertence
     * @param columnData dados da coluna
     * @param onCardDrop callback para quando um card é solto na coluna
     * @param onDataChange callback para quando os dados mudam
     * @param onCardUpdate callback para quando um card é atualizado
     */
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

    /**
     * Obtém o ID da coluna.
     * 
     * @return ID da coluna
     */
    public Long getColumnId() {
        return this.columnData.id();
    }

    /**
     * Adiciona um card à coluna.
     * 
     * @param cardNode nó visual do card a ser adicionado
     */
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
        titleField.setPromptText("Título do card *");
        titleField.setStyle("-fx-prompt-text-fill: gray;");
        
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Descrição (opcional)");
        
        ComboBox<CardTypeOptionDTO> typeComboBox = new ComboBox<>();
        
        // Carregar tipos de card
        List<CardTypeOptionDTO> typeOptions = facade.getAllCardTypeOptions();
        typeComboBox.getItems().addAll(typeOptions);
        
        // Definir valor padrão baseado nas configurações do sistema
        if (!typeOptions.isEmpty()) {
            // Tentar usar o tipo padrão configurado
            Long suggestedTypeId = facade.suggestDefaultCardTypeId();
            System.out.println("Tipo de card sugerido: " + suggestedTypeId);
            
            if (suggestedTypeId != null) {
                // Encontrar o tipo sugerido na lista
                Optional<CardTypeOptionDTO> suggestedType = typeOptions.stream()
                        .filter(type -> type.cardTypeId().equals(suggestedTypeId))
                        .findFirst();
                if (suggestedType.isPresent()) {
                    System.out.println("Tipo sugerido encontrado: " + suggestedType.get().getDisplayName());
                    typeComboBox.setValue(suggestedType.get());
                } else {
                    // Se o tipo sugerido não for encontrado, usar o primeiro disponível
                    System.out.println("Tipo sugerido não encontrado, usando primeiro disponível: " + typeOptions.get(0).getDisplayName());
                    typeComboBox.setValue(typeOptions.get(0));
                }
            } else {
                // Se não há tipo sugerido, usar o primeiro disponível
                System.out.println("Nenhum tipo sugerido, usando primeiro disponível: " + typeOptions.get(0).getDisplayName());
                typeComboBox.setValue(typeOptions.get(0));
            }
        }
        
        typeComboBox.setCellFactory(param -> new ListCell<CardTypeOptionDTO>() {
            @Override
            protected void updateItem(CardTypeOptionDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(item.getDisplayName());
                }
            }
        });
        typeComboBox.setButtonCell(typeComboBox.getCellFactory().call(null));

        // ComboBox para tipo de progresso (sem CUSTOM)
        ComboBox<ProgressType> progressTypeComboBox = new ComboBox<>();
        progressTypeComboBox.getItems().addAll(
                ProgressType.PERCENTAGE,
                ProgressType.CHECKLIST,
                ProgressType.NONE
        );
        
        // Definir valor padrão baseado nas configurações do sistema
        org.desviante.model.enums.ProgressType suggestedProgressType = facade.suggestDefaultProgressType();
        System.out.println("Tipo de progresso sugerido: " + suggestedProgressType);
        
        if (suggestedProgressType != null) {
            progressTypeComboBox.setValue(suggestedProgressType);
            System.out.println("Tipo de progresso definido: " + suggestedProgressType);
        } else {
            progressTypeComboBox.setValue(ProgressType.NONE); // Fallback
            System.out.println("Usando tipo de progresso padrão: NONE");
        }
        
        progressTypeComboBox.setCellFactory(param -> new ListCell<ProgressType>() {
            @Override
            protected void updateItem(ProgressType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(item.getDisplayName());
                }
            }
        });
        progressTypeComboBox.setButtonCell(progressTypeComboBox.getCellFactory().call(null));

        // Label de erro para o título
        Label titleErrorLabel = new Label();
        titleErrorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
        titleErrorLabel.setVisible(false);

        grid.add(new Label("Título:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(titleErrorLabel, 1, 1);
        grid.add(new Label("Descrição:"), 0, 2);
        grid.add(descriptionArea, 1, 2);
        grid.add(new Label("Tipo:"), 0, 3);
        grid.add(typeComboBox, 1, 3);
        grid.add(new Label("Progresso:"), 0, 4);
        grid.add(progressTypeComboBox, 1, 4);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(titleField::requestFocus);

        // Obter referência ao botão Criar
        Button createButton = (Button) dialog.getDialogPane().lookupButton(createButtonType);
        
        // Função para validar o título
        Runnable validateTitle = () -> {
            String title = titleField.getText();
            boolean isValid = title != null && !title.trim().isEmpty();
            
            if (!isValid) {
                titleErrorLabel.setText("O título é obrigatório");
                titleErrorLabel.setVisible(true);
                createButton.setDisable(true);
            } else {
                titleErrorLabel.setVisible(false);
                createButton.setDisable(false);
            }
        };

        // Adicionar listener para validar em tempo real
        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateTitle.run();
        });

        // Validar inicialmente
        validateTitle.run();

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                String title = titleField.getText();
                if (title == null || title.trim().isEmpty()) {
                    // Mostrar erro e não fechar o diálogo
                    titleErrorLabel.setText("O título é obrigatório");
                    titleErrorLabel.setVisible(true);
                    titleField.requestFocus();
                    return null; // Retorna null para manter o diálogo aberto
                }
                return new CardCreationData(title.trim(), descriptionArea.getText(), typeComboBox.getValue(), progressTypeComboBox.getValue());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            String title = result.title();
            String description = result.description();
            CardTypeOptionDTO selectedType = result.type();
            ProgressType selectedProgressType = result.progressType();

            try {
                var request = new CreateCardRequestDTO(title, description, this.columnData.id(), selectedType.cardTypeId(), selectedProgressType);
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

                // Notificar mudança de dados
                if (onDataChange != null) {
                    onDataChange.run();
                }
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Erro ao criar o card: " + e.getMessage()).showAndWait();
            }
        });
    }

    /**
     * Classe auxiliar para encapsular os dados de criação de card.
     */
    private static class CardCreationData {
        private final String title;
        private final String description;
        private final CardTypeOptionDTO type;
        private final ProgressType progressType;

        public CardCreationData(String title, String description, CardTypeOptionDTO type, ProgressType progressType) {
            this.title = title;
            this.description = description;
            this.type = type;
            this.progressType = progressType;
        }

        public String title() { return title; }
        public String description() { return description; }
        public CardTypeOptionDTO type() { return type; }
        public ProgressType progressType() { return progressType; }
    }
}