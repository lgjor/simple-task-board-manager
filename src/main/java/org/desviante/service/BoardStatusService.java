package org.desviante.service;

import org.desviante.persistence.entity.BoardColumnEntity;
import org.desviante.persistence.entity.BoardColumnKindEnum;
import org.desviante.persistence.entity.BoardEntity;

/**
 * Serviço responsável por determinar o status dos boards com base no conteúdo de suas colunas.
 */
public class BoardStatusService {

    /**
     * Determina o status atual do board com base no conteúdo de suas colunas.
     *
     * @param board O board a ser analisado
     * @return Uma string representando o status do board
     */
    public static String determineBoardStatus(BoardEntity board) {
        // Se o board não tiver colunas, retorna "Não configurado"
        if (board.getBoardColumns() == null || board.getBoardColumns().isEmpty()) {
            return "Não configurado";
        }

        // Imprime informações de debug para verificar as colunas do board
        System.out.println("Board ID: " + board.getId() + ", Nome: " + board.getName());
        System.out.println("Número de colunas: " + board.getBoardColumns().size());

        for (BoardColumnEntity column : board.getBoardColumns()) {
            System.out.println("Coluna: " + column.getName() +
                    ", Tipo: " + column.getKind() +
                    ", Ordem: " + column.getOrder_index() +
                    ", Cards: " + (column.getCards() != null ? column.getCards().size() : "null"));
        }

        // Encontra as colunas por tipo e nome
        BoardColumnEntity initialColumn = null;
        BoardColumnEntity inProgressColumn = null;
        BoardColumnEntity completedColumn = null;

        for (BoardColumnEntity column : board.getBoardColumns()) {
            String columnName = column.getName().toLowerCase();

            // Identifica as colunas por nome e tipo
            if (column.getKind() == BoardColumnKindEnum.INITIAL ||
                    columnName.toLowerCase().contains("inicial") ||
                    columnName.toLowerCase().contains("initial") ||
                    columnName.toLowerCase().contains("backlog") ||
                    columnName.toLowerCase().contains("a fazer") ||
                    columnName.toLowerCase().contains("to do")) {
                initialColumn = column;
            }
            else if (column.getKind() == BoardColumnKindEnum.PENDING ||
                    columnName.toLowerCase().contains("andamento") ||
                    columnName.toLowerCase().contains("progress") ||
                    columnName.toLowerCase().contains("doing")) {
                inProgressColumn = column;
            }
            else if (column.getKind() == BoardColumnKindEnum.FINAL ||
                    columnName.toLowerCase().contains("concluído") ||
                    columnName.toLowerCase().contains("final") ||
                    columnName.toLowerCase().contains("concluido") ||
                    columnName.toLowerCase().contains("done") ||
                    columnName.toLowerCase().contains("finalizado")) {
                completedColumn = column;
            }
        }

        // Se não conseguir identificar por nome e tipo, tenta identificar por ordem
        if (initialColumn == null || inProgressColumn == null || completedColumn == null) {
            for (BoardColumnEntity column : board.getBoardColumns()) {
                if (initialColumn == null && column.getOrder_index() == 1) {
                    initialColumn = column;
                }
                else if (inProgressColumn == null && column.getOrder_index() == 2) {
                    inProgressColumn = column;
                }
                else if (completedColumn == null && column.getOrder_index() == 3) {
                    completedColumn = column;
                }
            }
        }

        // Verifica se as colunas foram encontradas
        if (initialColumn == null || inProgressColumn == null || completedColumn == null) {
            System.out.println("Configuração incompleta: Inicial=" + (initialColumn != null) +
                    ", EmAndamento=" + (inProgressColumn != null) +
                    ", Concluído=" + (completedColumn != null));
            return "Configuração incompleta";
        }

        // Verifica o status com base no conteúdo das colunas
        boolean hasInitialCards = initialColumn.getCards() != null && !initialColumn.getCards().isEmpty();
        boolean hasInProgressCards = inProgressColumn.getCards() != null && !inProgressColumn.getCards().isEmpty();
        boolean hasCompletedCards = completedColumn.getCards() != null && !completedColumn.getCards().isEmpty();

        // Lógica de determinação do status
        if (!hasInitialCards && !hasInProgressCards && !hasCompletedCards) {
            return "Vazio";
        } else if (!hasInitialCards && !hasInProgressCards && hasCompletedCards) {
            return "Concluído";
        } else if (hasInProgressCards || hasCompletedCards && hasInitialCards ) {
            return "Em andamento";
        } else {
            return "Não iniciado";
        }
    }
}