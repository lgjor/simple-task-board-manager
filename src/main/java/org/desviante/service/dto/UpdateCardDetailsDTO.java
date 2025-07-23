package org.desviante.service.dto;

// Usamos um record para um DTO imutável e conciso.
// @NotBlank pode ser adicionado se você usar spring-boot-starter-validation
public record UpdateCardDetailsDTO(
        String title,
        String description
) {
}