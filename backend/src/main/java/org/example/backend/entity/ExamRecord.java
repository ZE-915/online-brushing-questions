package org.example.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_record")
public class ExamRecord extends BaseEntity {
    private Long userId;
    private String name;
    private String mode;
    private Integer totalCount;
    private Integer correctCount;
    private Integer durationSeconds;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
