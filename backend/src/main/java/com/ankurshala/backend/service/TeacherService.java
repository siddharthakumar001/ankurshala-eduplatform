package com.ankurshala.backend.service;

import com.ankurshala.backend.entity.Teacher;
import com.ankurshala.backend.entity.TeacherProfile;
import com.ankurshala.backend.entity.User;
import com.ankurshala.backend.repository.TeacherRepository;
import com.ankurshala.backend.repository.TeacherProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class TeacherService {

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private TeacherProfileRepository teacherProfileRepository;

    public Teacher createTeacher(User user) {
        log.info("Creating teacher for user ID: {}, email: {}", user.getId(), user.getEmail());
        
        Teacher teacher = new Teacher();
        teacher.setUser(user);
        teacher.setName(user.getName());
        teacher.setEmail(user.getEmail());
        teacher.setStatus(com.ankurshala.backend.entity.TeacherStatus.PENDING);

        Teacher savedTeacher = teacherRepository.save(teacher);
        log.info("Teacher created with ID: {}", savedTeacher.getId());

        // Create teacher profile
        TeacherProfile profile = new TeacherProfile();
        profile.setUser(user);
        profile.setTeacher(savedTeacher);
        TeacherProfile savedProfile = teacherProfileRepository.save(profile);
        log.info("Teacher profile created with ID: {} for user ID: {}", savedProfile.getId(), user.getId());

        return savedTeacher;
    }

    public Teacher getTeacherByUserId(Long userId) {
        return teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
    }

    public TeacherProfile getTeacherProfileByUserId(Long userId) {
        return teacherProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Teacher profile not found"));
    }
}
