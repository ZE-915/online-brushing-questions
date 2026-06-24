package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.backend.entity.KnowledgePoint;

@Mapper
public interface KnowledgePointMapper extends BaseMapper<KnowledgePoint> {
}
