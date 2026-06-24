package org.example.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.example.backend.entity.ExamRecord;

public class ExamDtos {
    public record GenerateExamRequest(
            String mode,
            Long knowledgePointId,
            @Min(1) @Max(100) Integer count,
            @Min(1) @Max(5) Integer minDifficulty,
            @Min(1) @Max(5) Integer maxDifficulty,
            Integer durationMinutes) {
    }

    public record ExamPaper(String paperId, String mode, Integer durationMinutes, List<?> questions) {
    }

    public record AnswerItem(Long questionId, String answer, Integer answerSeconds, Boolean selfCorrect) {
    }

    public record SubmitExamRequest(String paperId, String mode, Integer durationSeconds, List<AnswerItem> answers) {
    }

    public record AnswerDetail(Long questionId, String stem, String type, String optionsJson,
                               String correctAnswer, String userAnswer, Integer correctStatus) {
    }

    public record ExamDetail(ExamRecord exam, List<AnswerDetail> answers) {
    }
}
