package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.backend.entity.ExamRecord;

@Mapper
public interface ExamRecordMapper extends BaseMapper<ExamRecord> {
}
