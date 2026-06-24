package org.example.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("answer_record")
public class AnswerRecord extends BaseEntity {
    private Long userId;
    private Long examRecordId;
    private Long questionId;
    private String userAnswer;
    private Integer correctStatus;
    private Integer answerSeconds;
}
