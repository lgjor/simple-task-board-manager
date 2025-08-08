package org.desviante.view.component;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.desviante.service.ChecklistItemService;
import org.desviante.service.dto.ChecklistItemDTO;

import java.util.List;
import java.util.Optional;

/**
 * Controller para o componente de checklist.
 * Gerencia a interface de usuário para itens do checklist.
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
public class ChecklistViewController {
    
    @FXML private VBox checklistContainer;
    @FXML private Label progressLabel;
    @FXML private VBox itemsContainer;
    @FXML private TextField newItemTextField;
    @FXML private Button addItemButton;
    @FXML private Button clearCompletedButton;
    @FXML private Button removeAllButton;
    
    private ChecklistItemService checklistItemService;
    private Long currentCardId;
    private List<ChecklistItemDTO> currentItems;
    
    /**
     * Inicializa o controller.
     * 
     * @param checklistItemService service para gerenciar itens
     */
    public void initialize(ChecklistItemService checklistItemService) {
        this.checklistItemService = checklistItemService;
        setupEventHandlers();
    }
    
    /**
     * Configura os event handlers dos componentes.
     */
    private void setupEventHandlers() {
        // Adicionar novo item
        addItemButton.setOnAction(e -> addNewItem());
        addItemButton.setTooltip(new Tooltip("Adicionar"));
        newItemTextField.setOnAction(e -> addNewItem());
        
        // Limpar itens concluídos
        clearCompletedButton.setOnAction(e -> clearCompletedItems());
        
        // Remover todos os itens
        removeAllButton.setOnAction(e -> removeAllItems());
        
        // Focar no campo de texto quando o componente é mostrado
        checklistContainer.setOnMouseClicked(e -> newItemTextField.requestFocus());
    }
    
    /**
     * Carrega os itens do checklist para um card específico.
     * 
     * @param cardId identificador do card
     */
    public void loadChecklistItems(Long cardId) {
        this.currentCardId = cardId;
        refreshItems();
    }
    
    /**
     * Atualiza a lista de itens.
     */
    private void refreshItems() {
        if (currentCardId == null) {
            return;
        }
        
        try {
            currentItems = checklistItemService.getItemsByCardId(currentCardId);
            updateItemsDisplay();
            updateProgressDisplay();
        } catch (Exception e) {
            showError("Erro ao carregar itens do checklist", e.getMessage());
        }
    }
    
    /**
     * Atualiza a exibição dos itens.
     */
    private void updateItemsDisplay() {
        itemsContainer.getChildren().clear();
        
        for (ChecklistItemDTO item : currentItems) {
            HBox itemBox = createItemBox(item);
            itemsContainer.getChildren().add(itemBox);
        }
    }
    
    /**
     * Cria um HBox para representar um item do checklist.
     * 
     * @param item item a ser representado
     * @return HBox com o item
     */
    private HBox createItemBox(ChecklistItemDTO item) {
        HBox itemBox = new HBox(8);
        itemBox.getStyleClass().add("checklist-item-box");
        itemBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Checkbox para marcar como concluído
        CheckBox completedCheckBox = new CheckBox();
        completedCheckBox.getStyleClass().add("checklist-item-checkbox");
        completedCheckBox.setSelected(item.completed());
        completedCheckBox.setOnAction(e -> toggleItemCompleted(item, completedCheckBox.isSelected()));

        // Label com o texto do item
        Label itemLabel = new Label(item.text());
        itemLabel.getStyleClass().add("checklist-item-label");
        itemLabel.setWrapText(true);
        itemLabel.setMaxWidth(Double.MAX_VALUE);
        if (item.completed()) {
            itemLabel.getStyleClass().add("completed");
        }

        // Botão para editar com ícone de caneta
        Button editButton = new Button();
        editButton.getStyleClass().addAll("checklist-item-button", "checklist-edit-button");
        editButton.setOnAction(e -> editItem(item));
        editButton.setTooltip(new Tooltip("Editar"));
        javafx.scene.image.ImageView penIcon = org.desviante.util.IconManager.createIconViewWithoutWhiteBackground("270f", 14, 14, 0.15);
        if (penIcon == null) {
            penIcon = org.desviante.util.IconManager.createIconViewWithoutWhiteBackground("270f-fe0f", 14, 14, 0.15);
        }
        if (penIcon == null) {
            penIcon = org.desviante.util.IconManager.createIconViewWithoutWhiteBackground("1f58a", 14, 14, 0.15);
        }
        if (penIcon == null) {
            editButton.setText("✏");
        } else {
            editButton.setGraphic(penIcon);
        }

        // Botão para remover com texto "X"
        Button removeButton = new Button("X");
        removeButton.getStyleClass().addAll("checklist-item-button", "checklist-remove-button");
        removeButton.setOnAction(e -> removeItem(item));
        removeButton.setTooltip(new Tooltip("Remover"));

        // Adicionar componentes ao HBox
        itemBox.getChildren().addAll(completedCheckBox, itemLabel, editButton, removeButton);
        HBox.setHgrow(itemLabel, javafx.scene.layout.Priority.ALWAYS);

        return itemBox;
    }
    
    /**
     * Adiciona um novo item ao checklist.
     */
    private void addNewItem() {
        // Garantir que temos um card associado
        if (currentCardId == null) {
            showError("Card não salvo", "Salve o card antes de adicionar itens ao checklist.");
            return;
        }

        String text = newItemTextField.getText().trim();
        if (text.isEmpty()) {
            return;
        }
        
        try {
            ChecklistItemDTO newItem = checklistItemService.addItem(currentCardId, text);
            newItemTextField.clear();
            refreshItems();
        } catch (Exception e) {
            showError("Erro ao adicionar item", e.getMessage());
        }
    }
    
    /**
     * Marca um item como concluído ou não concluído.
     * 
     * @param item item a ser alterado
     * @param completed novo estado de conclusão
     */
    private void toggleItemCompleted(ChecklistItemDTO item, boolean completed) {
        try {
            checklistItemService.toggleItemCompleted(item.id(), completed);
            refreshItems();
        } catch (Exception e) {
            showError("Erro ao atualizar item", e.getMessage());
        }
    }
    
    /**
     * Edita o texto de um item.
     * 
     * @param item item a ser editado
     */
    private void editItem(ChecklistItemDTO item) {
        TextInputDialog dialog = new TextInputDialog(item.text());
        dialog.setTitle("Editar Item");
        dialog.setHeaderText("Editar texto do item");
        dialog.setContentText("Novo texto:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newText -> {
            try {
                checklistItemService.updateItemText(item.id(), newText);
                refreshItems();
            } catch (Exception e) {
                showError("Erro ao editar item", e.getMessage());
            }
        });
    }
    
    /**
     * Remove um item do checklist.
     * 
     * @param item item a ser removido
     */
    private void removeItem(ChecklistItemDTO item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Remoção");
        alert.setHeaderText("Remover Item");
        alert.setContentText("Tem certeza que deseja remover o item '" + item.text() + "'?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                checklistItemService.removeItem(item.id());
                refreshItems();
            } catch (Exception e) {
                showError("Erro ao remover item", e.getMessage());
            }
        }
    }
    
    /**
     * Remove todos os itens concluídos.
     */
    private void clearCompletedItems() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Limpeza");
        alert.setHeaderText("Limpar Itens Concluídos");
        alert.setContentText("Tem certeza que deseja remover todos os itens concluídos?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                long completedCount = currentItems.stream()
                    .filter(ChecklistItemDTO::completed)
                    .count();
                
                for (ChecklistItemDTO item : currentItems) {
                    if (item.completed()) {
                        checklistItemService.removeItem(item.id());
                    }
                }
                
                refreshItems();
                showInfo("Limpeza Concluída", 
                    String.format("%d itens concluídos foram removidos.", completedCount));
            } catch (Exception e) {
                showError("Erro ao limpar itens concluídos", e.getMessage());
            }
        }
    }
    
    /**
     * Remove todos os itens do checklist.
     */
    private void removeAllItems() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Remoção");
        alert.setHeaderText("Remover Todos os Itens");
        alert.setContentText("Tem certeza que deseja remover todos os itens do checklist?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                int removedCount = checklistItemService.removeAllItemsFromCard(currentCardId);
                refreshItems();
                showInfo("Remoção Concluída", 
                    String.format("%d itens foram removidos.", removedCount));
            } catch (Exception e) {
                showError("Erro ao remover todos os itens", e.getMessage());
            }
        }
    }
    
    /**
     * Atualiza a exibição do progresso.
     */
    private void updateProgressDisplay() {
        if (currentItems == null) {
            progressLabel.setText("0/0");
            return;
        }
        
        long completedCount = currentItems.stream()
            .filter(ChecklistItemDTO::completed)
            .count();
        
        progressLabel.setText(String.format("%d/%d", completedCount, currentItems.size()));
    }
    
    /**
     * Mostra uma mensagem de erro.
     * 
     * @param title título do erro
     * @param message mensagem do erro
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Mostra uma mensagem de informação.
     * 
     * @param title título da informação
     * @param message mensagem da informação
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informação");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Obtém o container principal do checklist.
     * 
     * @return VBox do checklist
     */
    public VBox getChecklistContainer() {
        return checklistContainer;
    }
    
    /**
     * Verifica se há itens no checklist.
     * 
     * @return true se há itens, false caso contrário
     */
    public boolean hasItems() {
        return currentItems != null && !currentItems.isEmpty();
    }
    
    /**
     * Obtém o número de itens concluídos.
     * 
     * @return número de itens concluídos
     */
    public int getCompletedCount() {
        if (currentItems == null) {
            return 0;
        }
        return (int) currentItems.stream()
            .filter(ChecklistItemDTO::completed)
            .count();
    }
    
    /**
     * Obtém o número total de itens.
     * 
     * @return número total de itens
     */
    public int getTotalCount() {
        return currentItems != null ? currentItems.size() : 0;
    }
}
