package org.example.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("question")
public class Question extends BaseEntity {
    private Long userId;
    private Long subjectId;
    private Long knowledgePointId;
    private String type;
    private String stem;
    private String optionsJson;
    private String answer;
    private String analysis;
    private Integer difficulty;
}
