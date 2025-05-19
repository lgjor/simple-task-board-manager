package org.desviante.persistence.dao;

import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import org.desviante.persistence.entity.BoardEntity;

@FunctionalInterface
public interface RefreshBoardCallback {
    void refresh(Long boardId, TableView<BoardEntity> tableView, VBox columnDisplay);
}
