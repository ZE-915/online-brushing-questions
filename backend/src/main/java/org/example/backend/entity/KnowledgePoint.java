package org.example.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("knowledge_point")
public class KnowledgePoint extends BaseEntity {
    private Long userId;
    private Long subjectId;
    private String name;
    private String description;
}
