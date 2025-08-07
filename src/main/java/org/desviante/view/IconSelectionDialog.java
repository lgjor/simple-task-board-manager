package org.desviante.view;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.desviante.util.IconManager;
import org.desviante.util.IconSearchMapper;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Diálogo para seleção visual de ícones.
 * 
 * <p>Permite que o usuário escolha um ícone visualmente entre todos
 * os ícones disponíveis no sistema, com preview em tempo real.</p>
 * 
 * @author Aú Desviante - Lucas Godoy <a href="https://github.com/desviante">GitHub</a>
 * @version 1.0
 * @since 1.0
 */
public class IconSelectionDialog {
    
    private static final int ICONS_PER_ROW = 8;
    private static final int ICON_SIZE = 32;
    private static final int GRID_SPACING = 5;
    private static final int ICONS_PER_PAGE = 200; // Limitar ícones por página
    
    private final Dialog<String> dialog;
    private final TextField searchField;
    private final GridPane iconGrid;
    private final Label previewLabel;
    private final ObservableList<String> allIcons;
    private final ObservableList<String> filteredIcons;
    private final Label statusLabel;
    private final Button prevPageButton;
    private final Button nextPageButton;
    private final Label pageLabel;
    
    // Variável para armazenar o ícone selecionado
    private String selectedIconCode;
    
    // Cache para ImageViews
    private final ConcurrentHashMap<String, ImageView> iconCache = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    
    // Paginação
    private int currentPage = 0;
    private int totalPages = 0;
    
    /**
     * Construtor do diálogo de seleção de ícones.
     * 
     * @param currentIcon ícone atualmente selecionado (pode ser null)
     */
    public IconSelectionDialog(String currentIcon) {
        this.dialog = new Dialog<>();
        this.allIcons = FXCollections.observableArrayList();
        this.filteredIcons = FXCollections.observableArrayList();
        
        // Carregar todos os ícones disponíveis
        loadAvailableIcons();
        
        // Configurar o diálogo
        setupDialog();
        
        // Inicializar ícone selecionado
        this.selectedIconCode = currentIcon != null ? currentIcon.trim() : "";
        
        // Criar componentes
        this.searchField = createSearchField();
        this.iconGrid = createIconGrid();
        this.previewLabel = createPreviewLabel();
        this.statusLabel = createStatusLabel();
        this.prevPageButton = createPrevPageButton();
        this.nextPageButton = createNextPageButton();
        this.pageLabel = createPageLabel();
        
        // Configurar layout
        setupLayout();
        
        // Configurar eventos
        setupEvents();
        
        // Aplicar filtro inicial
        filterIcons("");
    }
    
    /**
     * Carrega todos os ícones disponíveis no sistema.
     */
    private void loadAvailableIcons() {
        List<String> icons = IconManager.getAvailableIcons();
        allIcons.addAll(icons);
        filteredIcons.addAll(icons);
    }
    
    /**
     * Configura o diálogo principal.
     */
    private void setupDialog() {
        dialog.setTitle("Selecionar Ícone");
        dialog.setHeaderText("Escolha um ícone para o grupo de board");
        
        ButtonType selectButtonType = new ButtonType("Selecionar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        dialog.getDialogPane().getButtonTypes().addAll(selectButtonType, cancelButtonType);
        
        // Configurar resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == selectButtonType) {
                return selectedIconCode;
            }
            return null;
        });
        
        // Limpar recursos ao fechar
        dialog.setOnCloseRequest(event -> {
            executor.shutdown();
            iconCache.clear();
        });
    }
    
    /**
     * Cria o campo de busca.
     */
    private TextField createSearchField() {
        TextField field = new TextField();
        field.setPromptText("Digite palavras-chave como: livros, casa, trabalho, esportes...");
        field.setPrefWidth(300);
        return field;
    }
    
    /**
     * Cria o grid de ícones.
     */
    private GridPane createIconGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(GRID_SPACING);
        grid.setVgap(GRID_SPACING);
        grid.setPadding(new Insets(10));
        grid.setAlignment(Pos.CENTER);
        
        return grid;
    }
    
    /**
     * Cria o scroll pane para o grid de ícones.
     */
    private ScrollPane createIconScrollPane() {
        ScrollPane scrollPane = new ScrollPane(iconGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        return scrollPane;
    }
    
    /**
     * Cria o label de preview.
     */
    private Label createPreviewLabel() {
        Label label = new Label("Ícone selecionado:");
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        return label;
    }
    

    
    /**
     * Cria o label de status.
     */
    private Label createStatusLabel() {
        Label label = new Label();
        label.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");
        return label;
    }
    
    /**
     * Cria o botão de página anterior.
     */
    private Button createPrevPageButton() {
        Button button = new Button("← Anterior");
        button.setDisable(true);
        return button;
    }
    
    /**
     * Cria o botão de próxima página.
     */
    private Button createNextPageButton() {
        Button button = new Button("Próxima →");
        return button;
    }
    
    /**
     * Cria o label de página.
     */
    private Label createPageLabel() {
        Label label = new Label();
        label.setStyle("-fx-font-size: 12px;");
        return label;
    }
    
    /**
     * Configura o layout do diálogo.
     */
    private void setupLayout() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        // Seção de busca
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.getChildren().addAll(new Label("Buscar:"), searchField);
        
        // Seção de preview
        HBox previewBox = new HBox(10);
        previewBox.setAlignment(Pos.CENTER_LEFT);
        previewBox.getChildren().addAll(previewLabel);
        
        // Seção de paginação
        HBox paginationBox = new HBox(10);
        paginationBox.setAlignment(Pos.CENTER);
        paginationBox.getChildren().addAll(prevPageButton, pageLabel, nextPageButton);
        
        // Adicionar componentes ao layout
        content.getChildren().addAll(searchBox, previewBox, createIconScrollPane(), statusLabel, paginationBox);
        
        dialog.getDialogPane().setContent(content);
    }
    
    /**
     * Configura os eventos do diálogo.
     */
    private void setupEvents() {
        // Evento de busca
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterIcons(newValue);
        });
        
        // Eventos de paginação
        prevPageButton.setOnAction(event -> {
            if (currentPage > 0) {
                currentPage--;
                updateIconGrid();
                updatePaginationControls();
            }
        });
        
        nextPageButton.setOnAction(event -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                updateIconGrid();
                updatePaginationControls();
            }
        });
        
        // Focar no campo de busca quando o diálogo abrir
        Platform.runLater(searchField::requestFocus);
    }
    
    /**
     * Filtra os ícones baseado no texto de busca.
     */
    private void filterIcons(String searchText) {
        filteredIcons.clear();
        
        if (searchText == null || searchText.trim().isEmpty()) {
            filteredIcons.addAll(allIcons);
        } else {
            String lowerSearch = searchText.toLowerCase();
            
            // Primeiro, buscar por palavras-chave usando o IconSearchMapper
            List<String> keywordResults = IconSearchMapper.searchIcons(searchText);
            filteredIcons.addAll(keywordResults);
            
            // Depois, buscar por código do ícone (busca original)
            allIcons.stream()
                .filter(icon -> icon.toLowerCase().contains(lowerSearch))
                .filter(icon -> !filteredIcons.contains(icon)) // Evitar duplicatas
                .forEach(filteredIcons::add);
        }
        
        // Resetar para primeira página
        currentPage = 0;
        updateIconGrid();
        updatePaginationControls();
        updateStatusLabel();
    }
    
    /**
     * Atualiza o grid de ícones com os ícones filtrados (apenas página atual).
     */
    private void updateIconGrid() {
        iconGrid.getChildren().clear();
        
        int startIndex = currentPage * ICONS_PER_PAGE;
        int endIndex = Math.min(startIndex + ICONS_PER_PAGE, filteredIcons.size());
        
        int row = 0;
        int col = 0;
        
        for (int i = startIndex; i < endIndex; i++) {
            String iconCode = filteredIcons.get(i);
            Button iconButton = createIconButton(iconCode);
            iconGrid.add(iconButton, col, row);
            
            col++;
            if (col >= ICONS_PER_ROW) {
                col = 0;
                row++;
            }
        }
    }
    
    /**
     * Atualiza os controles de paginação.
     */
    private void updatePaginationControls() {
        totalPages = (int) Math.ceil((double) filteredIcons.size() / ICONS_PER_PAGE);
        
        prevPageButton.setDisable(currentPage <= 0);
        nextPageButton.setDisable(currentPage >= totalPages - 1);
        
        if (totalPages > 0) {
            pageLabel.setText(String.format("Página %d de %d", currentPage + 1, totalPages));
        } else {
            pageLabel.setText("Nenhum resultado");
        }
    }
    
    /**
     * Atualiza o label de status.
     */
    private void updateStatusLabel() {
        if (filteredIcons.isEmpty()) {
            statusLabel.setText("Nenhum ícone encontrado");
        } else {
            statusLabel.setText(String.format("Mostrando %d de %d ícones", 
                Math.min(ICONS_PER_PAGE, filteredIcons.size()), filteredIcons.size()));
        }
    }
    
    /**
     * Cria um botão para um ícone específico (com cache).
     */
    private Button createIconButton(String iconCode) {
        Button button = new Button();
        button.setPrefSize(ICON_SIZE + 10, ICON_SIZE + 10);
        button.setMinSize(ICON_SIZE + 10, ICON_SIZE + 10);
        button.setMaxSize(ICON_SIZE + 10, ICON_SIZE + 10);
        
        // Configurar tooltip com descrição
        String description = IconSearchMapper.getIconDescription(iconCode);
        String tooltipText = description.equals(iconCode) ? iconCode : iconCode + " - " + description;
        button.setTooltip(new Tooltip(tooltipText));
        
        // Configurar evento de clique
        button.setOnAction(event -> {
            selectedIconCode = iconCode;
            updatePreview(iconCode);
        });
        
        // Carregar ícone de forma assíncrona
        loadIconAsync(button, iconCode);
        
        return button;
    }
    
    /**
     * Carrega o ícone de forma assíncrona para melhor performance.
     */
    private void loadIconAsync(Button button, String iconCode) {
        executor.submit(() -> {
            ImageView iconView = getCachedIconView(iconCode);
            
            Platform.runLater(() -> {
                if (iconView != null) {
                    button.setGraphic(iconView);
                } else {
                    button.setText(iconCode);
                    button.setStyle("-fx-font-size: 8px;");
                }
            });
        });
    }
    
    /**
     * Obtém um ImageView do cache ou cria um novo.
     */
    private ImageView getCachedIconView(String iconCode) {
        return iconCache.computeIfAbsent(iconCode, code -> {
            ImageView view = IconManager.createIconView(code, ICON_SIZE, ICON_SIZE);
            return view != null ? view : new ImageView(); // Retorna ImageView vazio se não encontrar
        });
    }
    
    /**
     * Atualiza o preview do ícone selecionado.
     */
    private void updatePreview(String iconCode) {
        executor.submit(() -> {
            ImageView previewView = IconManager.createIconView(iconCode, 24, 24);
            
            Platform.runLater(() -> {
                if (previewView != null) {
                    previewLabel.setGraphic(previewView);
                } else {
                    previewLabel.setGraphic(null);
                }
            });
        });
    }
    
    /**
     * Mostra o diálogo e retorna o ícone selecionado.
     * 
     * @return Optional contendo o código do ícone selecionado ou vazio se cancelado
     */
    public Optional<String> showAndWait() {
        return dialog.showAndWait();
    }
    
    /**
     * Mostra o diálogo e retorna o ícone selecionado.
     * 
     * @param currentIcon ícone atualmente selecionado
     * @return Optional contendo o código do ícone selecionado ou vazio se cancelado
     */
    public static Optional<String> showIconSelection(String currentIcon) {
        IconSelectionDialog dialog = new IconSelectionDialog(currentIcon);
        return dialog.showAndWait();
    }
}
