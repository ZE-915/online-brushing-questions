package org.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.List;
import org.example.backend.common.BizException;
import org.example.backend.dto.SubjectDtos.KnowledgePointRequest;
import org.example.backend.dto.SubjectDtos.KnowledgePointUpdateRequest;
import org.example.backend.dto.SubjectDtos.SubjectRequest;
import org.example.backend.entity.KnowledgePoint;
import org.example.backend.entity.Subject;
import org.example.backend.mapper.KnowledgePointMapper;
import org.example.backend.mapper.SubjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogService {
    private final SubjectMapper subjectMapper;
    private final KnowledgePointMapper knowledgePointMapper;

    public CatalogService(SubjectMapper subjectMapper, KnowledgePointMapper knowledgePointMapper) {
        this.subjectMapper = subjectMapper;
        this.knowledgePointMapper = knowledgePointMapper;
    }

    public List<Subject> subjects(Long userId) {
        return subjectMapper.selectList(new LambdaQueryWrapper<Subject>()
                .eq(Subject::getUserId, userId)
                .orderByDesc(Subject::getCreateTime));
    }

    public Subject saveSubject(Long userId, SubjectRequest request) {
        Subject subject = new Subject();
        subject.setUserId(userId);
        subject.setName(request.name());
        subject.setDescription(request.description());
        subjectMapper.insert(subject);
        return subject;
    }

    public Subject updateSubject(Long userId, Long id, SubjectRequest request) {
        Subject subject = subjectMapper.selectOne(new LambdaQueryWrapper<Subject>()
                .eq(Subject::getUserId, userId).eq(Subject::getId, id));
        if (subject == null) throw new BizException(9030, "科目不存在");
        subject.setName(request.name());
        subject.setDescription(request.description());
        subjectMapper.updateById(subject);
        return subject;
    }

    public List<KnowledgePoint> knowledgePoints(Long userId, Long subjectId) {
        LambdaQueryWrapper<KnowledgePoint> wrapper = new LambdaQueryWrapper<KnowledgePoint>()
                .eq(KnowledgePoint::getUserId, userId)
                .orderByDesc(KnowledgePoint::getCreateTime);
        if (subjectId != null) {
            wrapper.eq(KnowledgePoint::getSubjectId, subjectId);
        }
        return knowledgePointMapper.selectList(wrapper);
    }

    public KnowledgePoint saveKnowledgePoint(Long userId, KnowledgePointRequest request) {
        KnowledgePoint point = new KnowledgePoint();
        point.setUserId(userId);
        point.setSubjectId(request.subjectId());
        point.setName(request.name());
        point.setDescription(request.description());
        knowledgePointMapper.insert(point);
        return point;
    }

    public KnowledgePoint updateKnowledgePoint(Long userId, Long id, KnowledgePointUpdateRequest request) {
        KnowledgePoint point = knowledgePointMapper.selectOne(new LambdaQueryWrapper<KnowledgePoint>()
                .eq(KnowledgePoint::getUserId, userId).eq(KnowledgePoint::getId, id));
        if (point == null) throw new BizException(9031, "知识点不存在");
        point.setName(request.name());
        point.setDescription(request.description());
        knowledgePointMapper.updateById(point);
        return point;
    }

    public void deleteKnowledgePoint(Long userId, Long id) {
        long count = knowledgePointMapper.selectCount(new LambdaQueryWrapper<KnowledgePoint>()
                .eq(KnowledgePoint::getUserId, userId).eq(KnowledgePoint::getId, id));
        if (count == 0) throw new BizException(9031, "知识点不存在");
        try {
            knowledgePointMapper.delete(new LambdaQueryWrapper<KnowledgePoint>()
                    .eq(KnowledgePoint::getUserId, userId).eq(KnowledgePoint::getId, id));
        } catch (Exception e) {
            throw new BizException(9032, "该知识点下有关联题目，无法删除");
        }
    }

    @Transactional
    public void deleteSubject(Long userId, Long id) {
        knowledgePointMapper.delete(new LambdaQueryWrapper<KnowledgePoint>()
                .eq(KnowledgePoint::getUserId, userId).eq(KnowledgePoint::getSubjectId, id));
        subjectMapper.delete(new LambdaQueryWrapper<Subject>().eq(Subject::getUserId, userId).eq(Subject::getId, id));
    }
}
