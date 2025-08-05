package org.desviante.service.dto;

import java.time.LocalDateTime;

/**
 * DTO interno para passar dados para o GoogleTasksApiService.
 */
public record CreateTaskRequest(
        String listTitle,
        String title,
        String notes,
        LocalDateTime due
) {}