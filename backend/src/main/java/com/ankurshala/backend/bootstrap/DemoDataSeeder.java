package com.ankurshala.backend.bootstrap;

import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Stage-1 FE complete: Simple demo data seeder for testing
 * Seeds basic demo users (1 admin, 5 students, 5 teachers) with minimal profiles
 */
@Component
public class DemoDataSeeder implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DemoDataSeeder.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private TeacherProfileRepository teacherProfileRepository;

    @Autowired
    private AdminProfileRepository adminProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Environment environment;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String demoSeedOnStart = environment.getProperty("DEMO_SEED_ON_START", "false");
        String demoEnv = environment.getProperty("DEMO_ENV", "local");
        String demoForce = environment.getProperty("DEMO_FORCE", "false");

        // Safety guard: Don't seed in production unless explicitly forced
        if ("prod".equals(demoEnv) && !"true".equals(demoForce)) {
            logger.warn("DEMO_ENV=prod detected. Skipping demo data seeding for safety. Set DEMO_FORCE=true to override.");
            return;
        }

        if (!"true".equals(demoSeedOnStart)) {
            logger.info("DEMO_SEED_ON_START=false. Skipping demo data seeding.");
            return;
        }

        logger.info("Starting demo data seeding for environment: {}", demoEnv);
        
        try {
            SeedResult result = seedDemoData();
            logger.info("Demo data seeding completed successfully: {}", result);
        } catch (Exception e) {
            logger.error("Demo data seeding failed", e);
            throw e;
        }
    }

    public SeedResult seedDemoData() {
        SeedResult result = new SeedResult();
        
        // Seed Admin User
        User adminUser = seedAdminUser();
        if (adminUser != null) result.adminUsersCreated++;

        // Seed Student Users
        for (int i = 1; i <= 5; i++) {
            User studentUser = seedStudentUser(i);
            if (studentUser != null) result.studentUsersCreated++;
        }

        // Seed Teacher Users
        for (int i = 1; i <= 5; i++) {
            User teacherUser = seedTeacherUser(i);
            if (teacherUser != null) result.teacherUsersCreated++;
        }

        return result;
    }

    private User seedAdminUser() {
        String email = "siddhartha@ankurshala.com";
        
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            user = new User();
            user.setName("Siddhartha Admin");
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode("Maza@123"));
            user.setRole(Role.ADMIN);
            user.setEnabled(true);
            user = userRepository.save(user);

            // Create admin profile
            AdminProfile adminProfile = new AdminProfile(user);
            adminProfile.setPhoneNumber("+91-9876543210");
            adminProfile.setIsSuperAdmin(true);
            adminProfileRepository.save(adminProfile);

            logger.info("Created admin user: {}", email);
        } else {
            logger.info("Admin user already exists: {}", email);
        }
        
        return user;
    }

    private User seedStudentUser(int studentNumber) {
        String email = "student" + studentNumber + "@ankurshala.com";
        String mobileNumber = "+91-987654321" + studentNumber;
        
        // Check if user already exists by email
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            logger.info("Student user already exists: {}", email);
            return user;
        }
        
        // Check if mobile number is already in use
        boolean mobileExists = studentProfileRepository.existsByMobileNumber(mobileNumber);
        if (mobileExists) {
            logger.warn("Mobile number {} already exists, skipping student {}", mobileNumber, studentNumber);
            return null;
        }
        
        try {
            user = new User();
            user.setName("Student " + studentNumber);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode("Maza@123"));
            user.setRole(Role.STUDENT);
            user.setEnabled(true);
            user = userRepository.save(user);

            // Create basic student profile
            StudentProfile studentProfile = new StudentProfile();
            studentProfile.setUser(user);
            studentProfile.setFirstName("Student");
            studentProfile.setLastName(studentNumber + "");
            studentProfile.setMobileNumber(mobileNumber);
            studentProfile.setEducationalBoard(EducationalBoard.CBSE);
            studentProfile.setClassLevel(ClassLevel.GRADE_8);
            studentProfile.setSchoolName("Delhi Public School " + studentNumber);
            studentProfileRepository.save(studentProfile);

            logger.info("Created student user: {} with mobile: {}", email, mobileNumber);
        } catch (Exception e) {
            logger.error("Failed to create student user {}: {}", email, e.getMessage());
            return null;
        }
        
        return user;
    }

    private User seedTeacherUser(int teacherNumber) {
        String email = "teacher" + teacherNumber + "@ankurshala.com";
        String mobileNumber = "+91-987654321" + teacherNumber;
        
        // Check if user already exists by email
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            logger.info("Teacher user already exists: {}", email);
            return user;
        }
        
        // Check if mobile number is already in use
        boolean mobileExists = teacherProfileRepository.existsByMobileNumber(mobileNumber);
        if (mobileExists) {
            logger.warn("Mobile number {} already exists, skipping teacher {}", mobileNumber, teacherNumber);
            return null;
        }
        
        try {
            user = new User();
            user.setName("Teacher " + teacherNumber);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode("Maza@123"));
            user.setRole(Role.TEACHER);
            user.setEnabled(true);
            user = userRepository.save(user);

            // Check if Teacher entity exists
            Teacher teacher = teacherRepository.findByUserId(user.getId()).orElse(null);
            if (teacher == null) {
                teacher = new Teacher();
                teacher.setUser(user);
                teacher.setName("Teacher " + teacherNumber);
                teacher.setEmail(email);
                teacher.setStatus(TeacherStatus.ACTIVE);
                teacher = teacherRepository.save(teacher);
            }

            // Check if TeacherProfile exists
            TeacherProfile teacherProfile = teacherProfileRepository.findByUserId(user.getId()).orElse(null);
            if (teacherProfile == null) {
                teacherProfile = new TeacherProfile();
                teacherProfile.setUser(user);
                teacherProfile.setTeacher(teacher);
                teacherProfile.setFirstName("Teacher");
                teacherProfile.setLastName(teacherNumber + "");
                teacherProfile.setMobileNumber(mobileNumber);
                teacherProfile.setBio("Experienced teacher with " + (5 + teacherNumber) + " years of teaching experience");
                teacherProfileRepository.save(teacherProfile);
            }

            logger.info("Created teacher user: {} with mobile: {}", email, mobileNumber);
        } catch (Exception e) {
            logger.error("Failed to create teacher user {}: {}", email, e.getMessage());
            return null;
        }
        
        return user;
    }

    public static class SeedResult {
        public int adminUsersCreated = 0;
        public int studentUsersCreated = 0;
        public int teacherUsersCreated = 0;

        @Override
        public String toString() {
            return String.format("Admin: %d, Students: %d, Teachers: %d", 
                adminUsersCreated, studentUsersCreated, teacherUsersCreated);
        }
    }
}