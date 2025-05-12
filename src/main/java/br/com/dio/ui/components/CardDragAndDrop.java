// Arquivo: src/main/java/br/com/dio/ui/components/CardDragAndDrop.java
package br.com.dio.ui.components;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import br.com.dio.ui.components.CardDragAndDropListener;

import java.util.Optional;

public class CardDragAndDrop {

    private final CardDragAndDropListener listener;

    public CardDragAndDrop(CardDragAndDropListener listener) {
        this.listener = listener;
    }

    public void setupDragSource(Node cardNode, Long cardId) {
        cardNode.setOnDragDetected(event -> {
            Dragboard dragboard = cardNode.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(cardId.toString());
            dragboard.setContent(content);
            cardNode.setStyle(cardNode.getStyle() + "; -fx-opacity: 0.7;");
            event.consume();
        });

        cardNode.setOnDragDone(event -> {
            String style = cardNode.getStyle().replace("-fx-opacity: 0.7;", "");
            cardNode.setStyle(style);
            event.consume();
        });
    }

    public void setupDropTarget(Node target, Long columnId) {
        // Handler para aceitar a transferência enquanto arrasta
        target.setOnDragOver(event -> {
            if (event.getGestureSource() != target && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        target.setOnDragEntered(event -> {
            target.setStyle(target.getStyle() + "; -fx-background-color: #E0E0E0;");
            event.consume();
        });

        target.setOnDragExited(event -> {
            String style = target.getStyle().replace("; -fx-background-color: #E0E0E0;", "");
            target.setStyle(style);
            event.consume();
        });

        target.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            boolean success = false;
            if (dragboard.hasString()) {
                try {
                    Long cardId = Long.parseLong(dragboard.getString());
                    listener.onCardMoved(cardId, columnId);
                    success = true;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }
}