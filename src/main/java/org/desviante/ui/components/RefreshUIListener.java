package org.desviante.ui.components;

public interface RefreshUIListener {
    void refreshUI(Long cardId, Long targetColumnId);

    void moveCardInView(Long cardId, Long columnId);

    Long getOriginalColumnId(Long cardId);
}