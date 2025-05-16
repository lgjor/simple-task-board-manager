package org.desviante.dto;

import org.desviante.persistence.entity.BoardColumnKindEnum;

public record BoardColumnInfoDTO(Long id, String name, int order_index, BoardColumnKindEnum kind) {
}

