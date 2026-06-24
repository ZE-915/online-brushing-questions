package org.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SubjectDtos {
    public record SubjectRequest(@NotBlank String name, String description) {
    }

    public record KnowledgePointRequest(@NotNull Long subjectId, @NotBlank String name, String description) {
    }

    public record KnowledgePointUpdateRequest(@NotBlank String name, String description) {
    }
}
