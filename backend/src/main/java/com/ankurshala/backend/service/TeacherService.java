package com.ankurshala.backend.service;

import com.ankurshala.backend.entity.Teacher;
import com.ankurshala.backend.entity.TeacherProfile;
import com.ankurshala.backend.entity.User;
import com.ankurshala.backend.repository.TeacherRepository;
import com.ankurshala.backend.repository.TeacherProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TeacherService {

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private TeacherProfileRepository teacherProfileRepository;

    public Teacher createTeacher(User user) {
        Teacher teacher = new Teacher();
        teacher.setUser(user);
        teacher.setName(user.getName());
        teacher.setEmail(user.getEmail());
        teacher.setStatus(com.ankurshala.backend.entity.TeacherStatus.PENDING);

        Teacher savedTeacher = teacherRepository.save(teacher);

        // Create teacher profile
        TeacherProfile profile = new TeacherProfile();
        profile.setUser(user);
        profile.setTeacher(savedTeacher);
        teacherProfileRepository.save(profile);

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
