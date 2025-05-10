package br.com.dio.ui.components;

import javafx.scene.Node;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

public class CardDragAndDrop {

    public interface DragDropListener {
        void onCardMoved(Long cardId, Long targetColumnId);
    }

    private final DragDropListener listener;

    public CardDragAndDrop(DragDropListener listener) {
        this.listener = listener;
    }

    /**
     * Configura um nó como fonte de drag (o card que será arrastado)
     */
    public void setupDragSource(Node cardNode, Long cardId) {
        // Configura o evento de drag
        cardNode.setOnDragDetected(event -> {
            System.out.println("Iniciando drag do card: " + cardId);

            // Inicia o processo de drag
            Dragboard dragboard = cardNode.startDragAndDrop(TransferMode.MOVE);

            // Adiciona o ID do card ao clipboard
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(cardId));
            dragboard.setContent(content);

            // Adiciona efeito visual durante o drag
            cardNode.setStyle(cardNode.getStyle() + "; -fx-opacity: 0.7;");

            event.consume();
        });

        // Restaura o estilo quando o drag termina
        cardNode.setOnDragDone(event -> {
            String style = cardNode.getStyle().replace("-fx-opacity: 0.7;", "");
            cardNode.setStyle(style);

            if (event.getTransferMode() == TransferMode.MOVE) {
                System.out.println("Card movido com sucesso");
            } else {
                System.out.println("Movimento do card cancelado");
            }

            event.consume();
        });
    }


    /**
     * Configura um nó como alvo de drop (a coluna onde o card será solto)
     */
    public void setupDropTarget(Node columnNode, Long columnId) {
        // Aceita o drag quando o mouse passa por cima
        columnNode.setOnDragOver(event -> {
            // Importante: aceitar o modo de transferência ANTES de consumir o evento
            if (event.getGestureSource() != columnNode && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
                System.out.println("DragOver aceito para coluna: " + columnId);
            }
            event.consume();
        });

        // Adiciona efeito visual quando o mouse entra na área
        columnNode.setOnDragEntered(event -> {
            if (event.getGestureSource() != columnNode && event.getDragboard().hasString()) {
                columnNode.setStyle(columnNode.getStyle() + "; -fx-border-color: #00FF00; -fx-border-width: 2;");
                System.out.println("Mouse entrou na coluna: " + columnId);
            }
            event.consume();
        });

        // Remove o efeito visual quando o mouse sai da área
        columnNode.setOnDragExited(event -> {
            String style = columnNode.getStyle().replace("-fx-border-color: #00FF00; -fx-border-width: 2;", "");
            columnNode.setStyle(style);
            System.out.println("Mouse saiu da coluna: " + columnId);
            event.consume();
        });

        // Processa o drop quando o card é solto
        columnNode.setOnDragDropped(event -> {
            System.out.println("Tentando soltar na coluna: " + columnId);
            Dragboard dragboard = event.getDragboard();
            boolean success = false;

            if (dragboard.hasString()) {
                try {
                    // Obtém o ID do card do clipboard
                    Long cardId = Long.parseLong(dragboard.getString());
                    System.out.println("Card " + cardId + " solto na coluna " + columnId);

                    // Notifica o listener para atualizar o banco de dados e a UI
                    if (listener != null) {
                        listener.onCardMoved(cardId, columnId);
                        success = true;
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao processar o drop: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // Importante: definir o resultado do drop ANTES de consumir o evento
            event.setDropCompleted(success);
            event.consume();
        });
    }
}
