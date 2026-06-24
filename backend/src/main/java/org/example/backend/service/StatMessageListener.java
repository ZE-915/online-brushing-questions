package org.example.backend.service;

import org.example.backend.config.RabbitConfig;
import org.example.backend.entity.ExamRecord;
import org.example.backend.mapper.ExamRecordMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class StatMessageListener {
    private final ExamRecordMapper examRecordMapper;
    private final AnalyticsService analyticsService;

    public StatMessageListener(ExamRecordMapper examRecordMapper, AnalyticsService analyticsService) {
        this.examRecordMapper = examRecordMapper;
        this.analyticsService = analyticsService;
    }

    @RabbitListener(queues = RabbitConfig.STAT_QUEUE)
    public void updateStats(Long examRecordId) {
        ExamRecord exam = examRecordMapper.selectById(examRecordId);
        if (exam != null) {
            analyticsService.rebuildStats(exam.getUserId());
        }
    }
}
