package org.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.List;
import org.example.backend.common.BizException;
import org.example.backend.dto.QuestionDtos.QuestionQuery;
import org.example.backend.dto.QuestionDtos.QuestionRequest;
import org.example.backend.entity.Question;
import org.example.backend.mapper.QuestionMapper;
import org.springframework.stereotype.Service;

@Service
public class QuestionService {
    private final QuestionMapper questionMapper;

    public QuestionService(QuestionMapper questionMapper) {
        this.questionMapper = questionMapper;
    }

    public List<Question> list(Long userId, QuestionQuery query) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<Question>()
                .eq(Question::getUserId, userId)
                .orderByDesc(Question::getUpdateTime);
        if (query.keyword() != null && !query.keyword().isBlank()) {
            wrapper.like(Question::getStem, query.keyword());
        }
        if (query.subjectId() != null) {
            wrapper.eq(Question::getSubjectId, query.subjectId());
        }
        if (query.knowledgePointId() != null) {
            wrapper.eq(Question::getKnowledgePointId, query.knowledgePointId());
        }
        if (query.type() != null && !query.type().isBlank()) {
            wrapper.eq(Question::getType, query.type());
        }
        if (query.difficulty() != null) {
            wrapper.eq(Question::getDifficulty, query.difficulty());
        }
        return questionMapper.selectList(wrapper);
    }

    public Question get(Long userId, Long id) {
        Question question = questionMapper.selectOne(new LambdaQueryWrapper<Question>()
                .eq(Question::getUserId, userId).eq(Question::getId, id));
        if (question == null) {
            throw new BizException(9010, "题目不存在");
        }
        return question;
    }

    public Question save(Long userId, QuestionRequest request) {
        Question question = new Question();
        copy(userId, request, question);
        questionMapper.insert(question);
        return question;
    }

    public Question update(Long userId, Long id, QuestionRequest request) {
        Question question = get(userId, id);
        copy(userId, request, question);
        questionMapper.updateById(question);
        return question;
    }

    public void delete(Long userId, Long id) {
        questionMapper.delete(new LambdaQueryWrapper<Question>().eq(Question::getUserId, userId).eq(Question::getId, id));
    }

    private void copy(Long userId, QuestionRequest request, Question question) {
        question.setUserId(userId);
        question.setSubjectId(request.subjectId());
        question.setKnowledgePointId(request.knowledgePointId());
        question.setType(request.type());
        question.setStem(request.stem());
        question.setOptionsJson(request.optionsJson());
        question.setAnswer(request.answer());
        question.setAnalysis(request.analysis());
        question.setDifficulty(request.difficulty() == null ? 1 : request.difficulty());
    }
}
