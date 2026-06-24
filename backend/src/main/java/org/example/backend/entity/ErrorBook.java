package org.example.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("error_book")
public class ErrorBook extends BaseEntity {
    private Long userId;
    private Long questionId;
    private Integer wrongCount;
    private Boolean marked;
    private String note;
    private LocalDateTime lastWrongTime;
}
