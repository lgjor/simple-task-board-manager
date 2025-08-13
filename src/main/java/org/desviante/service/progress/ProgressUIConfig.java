package org.desviante.service.progress;

import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Configuração da interface do usuário para progresso.
 * Encapsula os componentes da UI relacionados ao progresso.
 */
public class ProgressUIConfig {
    
    private final VBox progressContainer;
    private final VBox progressSection;
    private final Label totalLabel;
    private final Spinner<Integer> totalSpinner;
    private final Label currentLabel;
    private final Spinner<Integer> currentSpinner;
    private final Label progressLabel;
    private final Label progressValueLabel;
    private final Label statusValueLabel;
    private final HBox progressTypeContainer;
    
    public ProgressUIConfig(
            VBox progressContainer,
            VBox progressSection,
            Label totalLabel,
            Spinner<Integer> totalSpinner,
            Label currentLabel,
            Spinner<Integer> currentSpinner,
            Label progressLabel,
            Label progressValueLabel,
            Label statusValueLabel,
            HBox progressTypeContainer) {
        
        this.progressContainer = progressContainer;
        this.progressSection = progressSection;
        this.totalLabel = totalLabel;
        this.totalSpinner = totalSpinner;
        this.currentLabel = currentLabel;
        this.currentSpinner = currentSpinner;
        this.progressLabel = progressLabel;
        this.progressValueLabel = progressValueLabel;
        this.statusValueLabel = statusValueLabel;
        this.progressTypeContainer = progressTypeContainer;
    }
    
    // Getters
    public VBox getProgressContainer() { return progressContainer; }
    public VBox getProgressSection() { return progressSection; }
    public Label getTotalLabel() { return totalLabel; }
    public Spinner<Integer> getTotalSpinner() { return totalSpinner; }
    public Label getCurrentLabel() { return currentLabel; }
    public Spinner<Integer> getCurrentSpinner() { return currentSpinner; }
    public Label getProgressLabel() { return progressLabel; }
    public Label getProgressValueLabel() { return progressValueLabel; }
    public Label getStatusValueLabel() { return statusValueLabel; }
    public HBox getProgressTypeContainer() { return progressTypeContainer; }
}
