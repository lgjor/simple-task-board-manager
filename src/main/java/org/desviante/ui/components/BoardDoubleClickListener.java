package org.desviante.ui.components;

import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;

public class BoardDoubleClickListener {
    public static <T> void attach(TableView<T> tableView, Runnable editAction) {
        tableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tableView.getSelectionModel().getSelectedItem() != null) {
                editAction.run();
            }
        });
    }
}