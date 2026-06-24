package org.example.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("subject")
public class Subject extends BaseEntity {
    private Long userId;
    private String name;
    private String description;
}
