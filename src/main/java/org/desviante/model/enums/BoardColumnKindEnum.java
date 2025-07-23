package org.desviante.model.enums;

/**
 * Define os tipos de colunas em um quadro.
 * Estes tipos podem ser usados para aplicar lógicas especiais,
 * como mover um card para a próxima coluna ou marcar um card como concluído.
 */
public enum BoardColumnKindEnum {
    /**
     * A coluna inicial de um fluxo de trabalho (ex: "To Do").
     */
    INITIAL,
    /**
     * Uma coluna intermediária (ex: "In Progress").
     */
    PENDING,
    /**
     * A coluna final de um fluxo de trabalho (ex: "Done").
     */
    FINAL
}