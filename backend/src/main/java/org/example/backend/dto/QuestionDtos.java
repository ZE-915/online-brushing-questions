package org.example.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class QuestionDtos {
    public record QuestionRequest(
            @NotNull Long subjectId,
            @NotNull Long knowledgePointId,
            @NotBlank String type,
            @NotBlank String stem,
            String optionsJson,
            @NotBlank String answer,
            String analysis,
            @Min(1) @Max(5) Integer difficulty) {
    }

    public record QuestionQuery(String keyword, Long subjectId, Long knowledgePointId, String type, Integer difficulty) {
    }

    public record QuestionListItem(Long id, Long subjectId, Long knowledgePointId, String type,
                                   String stem, String optionsJson, String answer, String analysis,
                                   Integer difficulty, Integer answerStatus) {
    }
}
