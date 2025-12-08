package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.student.AddToStudyListRequest;
import com.ankurshala.backend.dto.student.StudyListItemDto;
import com.ankurshala.backend.dto.student.UpdateStudyListItemRequest;
import com.ankurshala.backend.entity.StudentProfile;
import com.ankurshala.backend.entity.StudentStudyList;
import com.ankurshala.backend.entity.StudentStudyList.StudyStatus;
import com.ankurshala.backend.entity.Topic;
import com.ankurshala.backend.repository.StudentProfileRepository;
import com.ankurshala.backend.repository.StudentStudyListRepository;
import com.ankurshala.backend.repository.TopicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudyListService {

    @Autowired
    private StudentStudyListRepository studyListRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private TopicRepository topicRepository;

    /**
     * Get all items in the student's study list
     */
    public List<StudyListItemDto> getStudyList(Long userId) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Student profile not found"));

        List<StudentStudyList> items = studyListRepository
                .findByStudentIdOrderByAddedAtDesc(profile.getId());

        return items.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get study list items filtered by status
     */
    public List<StudyListItemDto> getStudyListByStatus(Long userId, String status) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Student profile not found"));

        StudyStatus studyStatus = StudyStatus.valueOf(status);
        List<StudentStudyList> items = studyListRepository
                .findByStudentIdAndStatusOrderByAddedAtDesc(profile.getId(), studyStatus);

        return items.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Add a topic to the student's study list
     */
    public StudyListItemDto addTopic(Long userId, AddToStudyListRequest request) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Student profile not found"));

        // Check if topic exists
        Topic topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        // Check if already in study list
        if (studyListRepository.existsByStudentIdAndTopicId(profile.getId(), request.getTopicId())) {
            throw new RuntimeException("Topic already in study list");
        }

        // Create new study list item
        StudentStudyList item = new StudentStudyList();
        item.setStudentId(profile.getId());
        item.setTopicId(request.getTopicId());
        item.setStatus(StudyStatus.ADDED);
        item.setAddedAt(ZonedDateTime.now());
        item.setNotes(request.getNotes());

        StudentStudyList savedItem = studyListRepository.save(item);
        return convertToDto(savedItem);
    }

    /**
     * Update a study list item (status or notes)
     */
    public StudyListItemDto updateItem(Long userId, Long itemId, UpdateStudyListItemRequest request) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Student profile not found"));

        StudentStudyList item = studyListRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Study list item not found"));

        // Verify the item belongs to this student
        if (!item.getStudentId().equals(profile.getId())) {
            throw new RuntimeException("Unauthorized access to study list item");
        }

        // Update status
        if (request.getStatus() != null) {
            StudyStatus newStatus = StudyStatus.valueOf(request.getStatus());
            item.setStatus(newStatus);
            
            // Set doneAt timestamp when marking as DONE
            if (StudyStatus.DONE.equals(newStatus)) {
                item.setDoneAt(ZonedDateTime.now());
            } else {
                item.setDoneAt(null);
            }
        }

        // Update notes
        if (request.getNotes() != null) {
            item.setNotes(request.getNotes());
        }

        StudentStudyList savedItem = studyListRepository.save(item);
        return convertToDto(savedItem);
    }

    /**
     * Mark a study list item as done
     */
    public StudyListItemDto markAsDone(Long userId, Long itemId) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Student profile not found"));

        StudentStudyList item = studyListRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Study list item not found"));

        // Verify the item belongs to this student
        if (!item.getStudentId().equals(profile.getId())) {
            throw new RuntimeException("Unauthorized access to study list item");
        }

        item.setStatus(StudyStatus.DONE);
        item.setDoneAt(ZonedDateTime.now());

        StudentStudyList savedItem = studyListRepository.save(item);
        return convertToDto(savedItem);
    }

    /**
     * Remove a topic from the study list
     */
    public void removeItem(Long userId, Long itemId) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Student profile not found"));

        StudentStudyList item = studyListRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Study list item not found"));

        // Verify the item belongs to this student
        if (!item.getStudentId().equals(profile.getId())) {
            throw new RuntimeException("Unauthorized access to study list item");
        }

        studyListRepository.delete(item);
    }

    /**
     * Get count of items by status
     */
    public long getCountByStatus(Long userId, String status) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Student profile not found"));

        StudyStatus studyStatus = StudyStatus.valueOf(status);
        return studyListRepository.countByStudentIdAndStatus(profile.getId(), studyStatus);
    }

    /**
     * Convert entity to DTO
     */
    private StudyListItemDto convertToDto(StudentStudyList item) {
        StudyListItemDto dto = new StudyListItemDto();
        dto.setId(item.getId());
        dto.setStudentId(item.getStudentId());
        dto.setTopicId(item.getTopicId());
        dto.setStatus(item.getStatus().name());
        dto.setAddedAt(item.getAddedAt());
        dto.setDoneAt(item.getDoneAt());
        dto.setNotes(item.getNotes());

        // Load topic details
        topicRepository.findById(item.getTopicId()).ifPresent(topic -> {
            dto.setTopicName(topic.getTitle());
            dto.setChapterId(topic.getChapter().getId());
            dto.setChapterName(topic.getChapter().getName());
            dto.setSubjectId(topic.getChapter().getSubject().getId());
            dto.setSubjectName(topic.getChapter().getSubject().getName());
        });

        return dto;
    }
}
