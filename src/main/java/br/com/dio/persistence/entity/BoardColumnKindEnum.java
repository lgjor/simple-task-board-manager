package br.com.dio.persistence.entity;

import java.util.Arrays;
import java.util.stream.Stream;

public enum BoardColumnKindEnum {
    INITIAL, FINAL, CANCEL, PENDING, IN_PROGRESS;

    public static BoardColumnKindEnum findByName(String name) {
        return Arrays.stream(values())
                .filter(kind -> kind.name().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo não encontrado para o nome: " + name));
    }
}