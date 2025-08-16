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
    
    /**
     * Construtor para configuração da interface de progresso.
     * 
     * @param progressContainer container principal do progresso
     * @param progressSection seção específica do progresso
     * @param totalLabel label para unidades totais
     * @param totalSpinner spinner para unidades totais
     * @param currentLabel label para unidades atuais
     * @param currentSpinner spinner para unidades atuais
     * @param progressLabel label principal do progresso
     * @param progressValueLabel label para valor do progresso
     * @param statusValueLabel label para status do progresso
     * @param progressTypeContainer container para tipo de progresso
     */
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
    
    /**
     * Obtém o container principal do progresso.
     * 
     * @return container principal do progresso
     */
    public VBox getProgressContainer() { return progressContainer; }
    
    /**
     * Obtém a seção específica do progresso.
     * 
     * @return seção específica do progresso
     */
    public VBox getProgressSection() { return progressSection; }
    
    /**
     * Obtém o label para unidades totais.
     * 
     * @return label para unidades totais
     */
    public Label getTotalLabel() { return totalLabel; }
    
    /**
     * Obtém o spinner para unidades totais.
     * 
     * @return spinner para unidades totais
     */
    public Spinner<Integer> getTotalSpinner() { return totalSpinner; }
    
    /**
     * Obtém o label para unidades atuais.
     * 
     * @return label para unidades atuais
     */
    public Label getCurrentLabel() { return currentLabel; }
    
    /**
     * Obtém o spinner para unidades atuais.
     * 
     * @return spinner para unidades atuais
     */
    public Spinner<Integer> getCurrentSpinner() { return currentSpinner; }
    
    /**
     * Obtém o label principal do progresso.
     * 
     * @return label principal do progresso
     */
    public Label getProgressLabel() { return progressLabel; }
    
    /**
     * Obtém o label para valor do progresso.
     * 
     * @return label para valor do progresso
     */
    public Label getProgressValueLabel() { return progressValueLabel; }
    
    /**
     * Obtém o label para status do progresso.
     * 
     * @return label para status do progresso
     */
    public Label getStatusValueLabel() { return statusValueLabel; }
    
    /**
     * Obtém o container para tipo de progresso.
     * 
     * @return container para tipo de progresso
     */
    public HBox getProgressTypeContainer() { return progressTypeContainer; }
}
