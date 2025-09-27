package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.admin.CreatePricingRuleRequest;
import com.ankurshala.backend.dto.admin.PricingRuleDto;
import com.ankurshala.backend.dto.admin.UpdatePricingRuleRequest;
import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AdminPricingService {

    @Autowired
    private PricingRuleRepository pricingRuleRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private TopicRepository topicRepository;

    public Page<PricingRuleDto> getPricingRules(Long boardId, Long gradeId, Long subjectId, 
                                               Long chapterId, Long topicId, Boolean active, 
                                               Pageable pageable) {
        Page<PricingRule> rules = pricingRuleRepository.findPricingRulesWithFilters(
                boardId, gradeId, subjectId, chapterId, topicId, active, pageable);
        
        return rules.map(this::convertToDto);
    }

    public PricingRuleDto createPricingRule(CreatePricingRuleRequest request) {
        // Validate that at least one taxonomy field is provided
        if (request.getBoardId() == null && request.getGradeId() == null && 
            request.getSubjectId() == null && request.getChapterId() == null && 
            request.getTopicId() == null) {
            throw new RuntimeException("At least one taxonomy field must be provided");
        }

        PricingRule rule = new PricingRule();
        
        // Set taxonomy references
        if (request.getBoardId() != null) {
            Board board = boardRepository.findById(request.getBoardId())
                    .orElseThrow(() -> new RuntimeException("Board not found"));
            rule.setBoard(board);
        }
        
        if (request.getGradeId() != null) {
            Grade grade = gradeRepository.findById(request.getGradeId())
                    .orElseThrow(() -> new RuntimeException("Grade not found"));
            rule.setGrade(grade);
        }
        
        if (request.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(request.getSubjectId())
                    .orElseThrow(() -> new RuntimeException("Subject not found"));
            rule.setSubject(subject);
        }
        
        if (request.getChapterId() != null) {
            Chapter chapter = chapterRepository.findById(request.getChapterId())
                    .orElseThrow(() -> new RuntimeException("Chapter not found"));
            rule.setChapter(chapter);
        }
        
        if (request.getTopicId() != null) {
            Topic topic = topicRepository.findById(request.getTopicId())
                    .orElseThrow(() -> new RuntimeException("Topic not found"));
            rule.setTopic(topic);
        }
        
        rule.setHourlyRate(request.getHourlyRate());
        rule.setActive(request.getActive() != null ? request.getActive() : true);
        
        PricingRule savedRule = pricingRuleRepository.save(rule);
        return convertToDto(savedRule);
    }

    public PricingRuleDto updatePricingRule(Long id, UpdatePricingRuleRequest request) {
        PricingRule rule = pricingRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pricing rule not found"));

        // Update taxonomy references if provided
        if (request.getBoardId() != null) {
            Board board = boardRepository.findById(request.getBoardId())
                    .orElseThrow(() -> new RuntimeException("Board not found"));
            rule.setBoard(board);
        }
        
        if (request.getGradeId() != null) {
            Grade grade = gradeRepository.findById(request.getGradeId())
                    .orElseThrow(() -> new RuntimeException("Grade not found"));
            rule.setGrade(grade);
        }
        
        if (request.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(request.getSubjectId())
                    .orElseThrow(() -> new RuntimeException("Subject not found"));
            rule.setSubject(subject);
        }
        
        if (request.getChapterId() != null) {
            Chapter chapter = chapterRepository.findById(request.getChapterId())
                    .orElseThrow(() -> new RuntimeException("Chapter not found"));
            rule.setChapter(chapter);
        }
        
        if (request.getTopicId() != null) {
            Topic topic = topicRepository.findById(request.getTopicId())
                    .orElseThrow(() -> new RuntimeException("Topic not found"));
            rule.setTopic(topic);
        }
        
        if (request.getHourlyRate() != null) {
            rule.setHourlyRate(request.getHourlyRate());
        }
        
        if (request.getActive() != null) {
            rule.setActive(request.getActive());
        }
        
        PricingRule savedRule = pricingRuleRepository.save(rule);
        return convertToDto(savedRule);
    }

    public boolean togglePricingRuleStatus(Long id) {
        PricingRule rule = pricingRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pricing rule not found"));
        
        rule.setActive(!rule.getActive());
        pricingRuleRepository.save(rule);
        
        return rule.getActive();
    }

    public void deletePricingRule(Long id) {
        PricingRule rule = pricingRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pricing rule not found"));
        
        pricingRuleRepository.delete(rule);
    }

    public PricingRuleDto resolvePricingRule(Long boardId, Long gradeId, Long subjectId, 
                                            Long chapterId, Long topicId) {
        List<PricingRule> rules = pricingRuleRepository.findBestMatchPricingRule(
                boardId, gradeId, subjectId, chapterId, topicId);
        
        if (rules.isEmpty()) {
            return null;
        }
        
        // Return the most specific rule (first in the ordered list)
        return convertToDto(rules.get(0));
    }

    private PricingRuleDto convertToDto(PricingRule rule) {
        return new PricingRuleDto(
                rule.getId(),
                rule.getBoard() != null ? rule.getBoard().getId() : null,
                rule.getBoard() != null ? rule.getBoard().getName() : null,
                rule.getGrade() != null ? rule.getGrade().getId() : null,
                rule.getGrade() != null ? rule.getGrade().getName() : null,
                rule.getSubject() != null ? rule.getSubject().getId() : null,
                rule.getSubject() != null ? rule.getSubject().getName() : null,
                rule.getChapter() != null ? rule.getChapter().getId() : null,
                rule.getChapter() != null ? rule.getChapter().getName() : null,
                rule.getTopic() != null ? rule.getTopic().getId() : null,
                rule.getTopic() != null ? rule.getTopic().getTitle() : null,
                rule.getHourlyRate(),
                rule.getActive(),
                rule.getCreatedAt(),
                rule.getUpdatedAt()
        );
    }
}
