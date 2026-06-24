package org.example.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("knowledge_point_stat")
public class KnowledgePointStat extends BaseEntity {
    private Long userId;
    private Long knowledgePointId;
    private Integer totalQuestions;
    private Integer correctCount;
    private BigDecimal masteryDegree;
    private LocalDateTime lastTestTime;
}
