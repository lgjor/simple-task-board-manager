package org.desviante.service.dto;

// Usar um record Java é perfeito para DTOs imutáveis.
public record CreateTaskRequest(String listTitle, String taskTitle, String taskNotes) {
}