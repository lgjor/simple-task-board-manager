package org.desviante.ui.components;

public interface DragDropListenerInterface {
        void onCardMoved(Long cardId, Long targetColumnId);
        boolean isCardInFinalColumn(Long cardId);
        boolean isTargetColumnFinal(Long columnId);
}