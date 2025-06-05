package org.desviante.ui.components;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

public class BoardEditDialog extends Dialog<String> {
    public BoardEditDialog(String currentName) {
        setTitle("Editar Título do Board");
        setHeaderText(null);

        Label label = new Label("Novo título do board:");
        TextField textField = new TextField(currentName);

        VBox vbox = new VBox(10, label, textField);
        getDialogPane().setContent(vbox);

        ButtonType okButton = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        setResultConverter(dialogButton -> {
            if (dialogButton == okButton) {
                return textField.getText();
            }
            return null;
        });
    }
}