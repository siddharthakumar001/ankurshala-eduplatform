package com.ankurshala.backend.bootstrap;

import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.repository.*;
import com.ankurshala.backend.util.IndianDataGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Bulk demo seeder that creates 15 students, 15 teachers, and 3 admins with complete profile data.
 * Implements idempotent seeding - can be run multiple times without creating duplicates.
 * Protected by environment flags to prevent accidental execution in production.
 */
@Component
public class BulkDemoSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(BulkDemoSeeder.class);

    @Value("${DEMO_ENV:local}")
    private String demoEnv;

    @Value("${DEMO_BULK_SEED:false}")
    private boolean demoBulkSeed;

    @Value("${DEMO_FORCE:false}")
    private boolean demoForce;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private StudentDocumentRepository studentDocumentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private TeacherProfileRepository teacherProfileRepository;

    @Autowired
    private TeacherProfessionalInfoRepository teacherProfessionalInfoRepository;

    @Autowired
    private TeacherQualificationRepository teacherQualificationRepository;

    @Autowired
    private TeacherExperienceRepository teacherExperienceRepository;

    @Autowired
    private TeacherCertificationRepository teacherCertificationRepository;

    @Autowired
    private TeacherAvailabilityRepository teacherAvailabilityRepository;

    @Autowired
    private TeacherDocumentRepository teacherDocumentRepository;

    @Autowired
    private TeacherBankDetailsRepository teacherBankDetailsRepository;

    @Autowired
    private TeacherAddressRepository teacherAddressRepository;

    @Autowired
    private AdminProfileRepository adminProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private IndianDataGenerator dataGenerator;

    @Override
    public void run(String... args) throws Exception {
        if (!shouldRunSeeder()) {
            logger.info("Bulk demo seeder skipped. DEMO_ENV={}, DEMO_BULK_SEED={}, DEMO_FORCE={}", 
                       demoEnv, demoBulkSeed, demoForce);
            return;
        }

        logger.info("Starting bulk demo seeder...");
        
        try {
            SeedingResult result = seedAllUsers();
            logger.info("Bulk seeding completed successfully: {}", result);
        } catch (Exception e) {
            logger.error("Bulk seeding failed", e);
            throw e;
        }
    }

    private boolean shouldRunSeeder() {
        logger.info("BulkDemoSeeder: Environment check - DEMO_ENV: {}, DEMO_BULK_SEED: {}, DEMO_FORCE: {}", 
                    demoEnv, demoBulkSeed, demoForce);
        
        if (!demoBulkSeed) {
            logger.info("BulkDemoSeeder: DEMO_BULK_SEED is false, skipping bulk seeding");
            return false;
        }

        if ("prod".equalsIgnoreCase(demoEnv) && !demoForce) {
            logger.warn("Refusing to run bulk seeder in production environment without DEMO_FORCE=true");
            return false;
        }

        return true;
    }

    @Transactional
    public SeedingResult seedAllUsers() {
        SeedingResult result = new SeedingResult();

        // Seed students
        for (int i = 1; i <= 15; i++) {
            try {
                String email = "student" + i + "@ankurshala.com";
                UserSeedResult userResult = seedStudent(email, "Maza@123");
                result.addStudentResult(userResult);
            } catch (Exception e) {
                logger.error("Failed to seed student {}", i, e);
                result.addError("Student " + i + ": " + e.getMessage());
            }
        }

        // Seed teachers
        for (int i = 1; i <= 15; i++) {
            try {
                String email = "teacher" + i + "@ankurshala.com";
                UserSeedResult userResult = seedTeacher(email, "Maza@123");
                result.addTeacherResult(userResult);
            } catch (Exception e) {
                logger.error("Failed to seed teacher {}", i, e);
                result.addError("Teacher " + i + ": " + e.getMessage());
            }
        }

        // Seed admins
        String[] adminEmails = {"sidd@ankurshala.com", "mayank@ankurshala.com", "tejas@ankurshala.com"};
        String[] adminPasswords = {"ankur@123", "mayank@123", "tejas@123"};
        String[] adminNames = {"Sidd", "Mayank", "Tejas"};

        for (int i = 0; i < adminEmails.length; i++) {
            try {
                UserSeedResult userResult = seedAdmin(adminEmails[i], adminPasswords[i], adminNames[i]);
                result.addAdminResult(userResult);
            } catch (Exception e) {
                logger.error("Failed to seed admin {}", adminEmails[i], e);
                result.addError("Admin " + adminEmails[i] + ": " + e.getMessage());
            }
        }

        return result;
    }

    @Transactional
    public UserSeedResult seedStudent(String email, String password) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        
        if (existingUser.isPresent()) {
            logger.info("Student {} already exists, updating profile", email);
            return updateStudentProfile(existingUser.get());
        } else {
            logger.info("Creating new student: {}", email);
            return createStudentProfile(email, password);
        }
    }

    private UserSeedResult createStudentProfile(String email, String password) {
        // Create user
        User user = new User();
        user.setName("Student " + email.split("@")[0]);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.STUDENT);
        user.setEnabled(true);
        user = userRepository.save(user);

        // Create student profile
        StudentProfile profile = createStudentProfileData(user);
        profile = studentProfileRepository.save(profile);

        // Create student documents
        createStudentDocuments(profile);

        return new UserSeedResult(true, false, email);
    }

    private UserSeedResult updateStudentProfile(User user) {
        StudentProfile profile = user.getStudentProfile();
        if (profile == null) {
            // Create missing profile
            profile = createStudentProfileData(user);
            profile = studentProfileRepository.save(profile);
            createStudentDocuments(profile);
        }
        return new UserSeedResult(false, true, user.getEmail());
    }

    private StudentProfile createStudentProfileData(User user) {
        StudentProfile profile = new StudentProfile();
        profile.setUser(user);
        
        String firstName = dataGenerator.generateFirstName(Gender.values()[user.getId().intValue() % 3]);
        String lastName = dataGenerator.generateLastName();
        
        profile.setFirstName(firstName);
        profile.setLastName(lastName);
        profile.setMiddleName(dataGenerator.generateFirstName(Gender.MALE)); // Random middle name
        
        profile.setMotherName(dataGenerator.generateFirstName(Gender.FEMALE) + " " + lastName);
        profile.setFatherName(dataGenerator.generateFirstName(Gender.MALE) + " " + lastName);
        profile.setGuardianName(profile.getFatherName());
        
        profile.setMobileNumber(dataGenerator.generatePhoneNumber());
        profile.setAlternateMobileNumber(dataGenerator.generateAlternatePhoneNumber());
        profile.setDateOfBirth(dataGenerator.generateDateOfBirth(13, 18)); // 13-18 years old
        
        profile.setEducationalBoard(dataGenerator.generateEducationalBoard());
        profile.setClassLevel(dataGenerator.generateClassLevel());
        profile.setGradeLevel(profile.getClassLevel().name().replace("GRADE_", ""));
        profile.setSchoolName(dataGenerator.generateSchoolName());
        profile.setEmergencyContact(dataGenerator.generatePhoneNumber());
        
        profile.setStudentPhotoUrl(dataGenerator.generatePhotoUrl("student"));
        profile.setSchoolIdCardUrl(dataGenerator.generateDocumentUrl("school-id-card"));
        
        return profile;
    }

    private void createStudentDocuments(StudentProfile profile) {
        // Create at least 2 documents
        StudentDocument doc1 = new StudentDocument();
        doc1.setStudentProfile(profile);
        doc1.setDocumentName("Report Card");
        doc1.setDocumentUrl(dataGenerator.generateDocumentUrl("report-card"));
        doc1.setDocumentType("ACADEMIC");
        studentDocumentRepository.save(doc1);

        StudentDocument doc2 = new StudentDocument();
        doc2.setStudentProfile(profile);
        doc2.setDocumentName("ID Card");
        doc2.setDocumentUrl(dataGenerator.generateDocumentUrl("id-card"));
        doc2.setDocumentType("IDENTITY");
        studentDocumentRepository.save(doc2);
    }

    @Transactional
    public UserSeedResult seedTeacher(String email, String password) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        
        if (existingUser.isPresent()) {
            logger.info("Teacher {} already exists, updating profile", email);
            return updateTeacherProfile(existingUser.get());
        } else {
            logger.info("Creating new teacher: {}", email);
            return createTeacherProfile(email, password);
        }
    }

    private UserSeedResult createTeacherProfile(String email, String password) {
        // Create user
        User user = new User();
        user.setName("Teacher " + email.split("@")[0]);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.TEACHER);
        user.setEnabled(true);
        user = userRepository.save(user);

        // Create teacher entity
        Teacher teacher = new Teacher();
        teacher.setUser(user);
        teacher.setName(user.getName());
        teacher.setEmail(email);
        teacher.setPhoneNumber(dataGenerator.generatePhoneNumber());
        teacher.setAlternatePhoneNumber(dataGenerator.generateAlternatePhoneNumber());
        teacher.setDateOfBirth(dataGenerator.generateDateOfBirth(25, 50)); // 25-50 years old
        teacher.setGender(dataGenerator.generateGender());
        teacher.setProfilePictureUrl(dataGenerator.generatePhotoUrl("teacher"));
        teacher.setBio(dataGenerator.generateBio());
        teacher.setStatus(dataGenerator.generateTeacherStatus());
        teacher = teacherRepository.save(teacher);

        // Create teacher profile
        TeacherProfile profile = createTeacherProfileData(user, teacher);
        profile = teacherProfileRepository.save(profile);

        // Create all related entities
        createTeacherProfessionalInfo(teacher);
        createTeacherQualifications(teacher);
        createTeacherExperiences(teacher);
        createTeacherCertifications(teacher);
        createTeacherAvailability(teacher);
        createTeacherDocuments(teacher);
        createTeacherBankDetails(teacher);
        createTeacherAddresses(teacher);

        return new UserSeedResult(true, false, email);
    }

    private UserSeedResult updateTeacherProfile(User user) {
        Teacher teacher = user.getTeacher();
        if (teacher == null) {
            // Create missing teacher entity and all related data
            return createTeacherProfile(user.getEmail(), "Maza@123");
        }
        return new UserSeedResult(false, true, user.getEmail());
    }

    private TeacherProfile createTeacherProfileData(User user, Teacher teacher) {
        TeacherProfile profile = new TeacherProfile();
        profile.setUser(user);
        profile.setTeacher(teacher);
        
        String firstName = dataGenerator.generateFirstName(teacher.getGender());
        String lastName = dataGenerator.generateLastName();
        
        profile.setFirstName(firstName);
        profile.setLastName(lastName);
        profile.setMiddleName(dataGenerator.generateFirstName(Gender.MALE));
        
        profile.setBio(dataGenerator.generateBio());
        profile.setQualifications(dataGenerator.generateQualificationText());
        profile.setYearsOfExperience(dataGenerator.generateYearsOfExperience());
        profile.setSpecialization(dataGenerator.generateSubject());
        profile.setVerified(false);
        
        profile.setMobileNumber(teacher.getPhoneNumber());
        profile.setAlternateMobileNumber(teacher.getAlternatePhoneNumber());
        profile.setContactEmail(user.getEmail());
        profile.setHighestEducation("Master's Degree");
        
        String city = dataGenerator.generateCity();
        String state = dataGenerator.generateState();
        
        profile.setPostalAddress(dataGenerator.generateAddressLine1() + ", " + dataGenerator.generateAddressLine2());
        profile.setCity(city);
        profile.setState(state);
        profile.setCountry("India");
        profile.setSecondaryAddress(profile.getPostalAddress());
        
        profile.setProfilePhotoUrl(teacher.getProfilePictureUrl());
        profile.setGovtIdProofUrl(dataGenerator.generateDocumentUrl("govt-id"));
        
        profile.setRating(dataGenerator.generateRating());
        profile.setTotalReviews(dataGenerator.generateTotalReviews());
        
        return profile;
    }

    private void createTeacherProfessionalInfo(Teacher teacher) {
        TeacherProfessionalInfo info = new TeacherProfessionalInfo();
        info.setTeacher(teacher);
        info.setSpecialization(dataGenerator.generateSubject());
        info.setYearsOfExperience(dataGenerator.generateYearsOfExperience());
        info.setHourlyRate(dataGenerator.generateHourlyRate());
        info.setCurrentInstitution(dataGenerator.generateSchoolName());
        info.setDesignation("Senior Teacher");
        info.setSubjectsExpertise(dataGenerator.generateSubjectsExpertise());
        teacherProfessionalInfoRepository.save(info);
    }

    private void createTeacherQualifications(Teacher teacher) {
        // Create at least 2 qualifications
        TeacherQualification qual1 = new TeacherQualification();
        qual1.setTeacher(teacher);
        qual1.setDegree("Bachelor's Degree");
        qual1.setSpecialization(dataGenerator.generateSubject());
        qual1.setUniversity(dataGenerator.generateUniversity());
        qual1.setYear(2015 + (int)(teacher.getId() % 10)); // 2015-2024
        teacherQualificationRepository.save(qual1);

        TeacherQualification qual2 = new TeacherQualification();
        qual2.setTeacher(teacher);
        qual2.setDegree("Master's Degree");
        qual2.setSpecialization(dataGenerator.generateSubject());
        qual2.setUniversity(dataGenerator.generateUniversity());
        qual2.setYear(2018 + (int)(teacher.getId() % 6)); // 2018-2023
        teacherQualificationRepository.save(qual2);
    }

    private void createTeacherExperiences(Teacher teacher) {
        // Create at least 2 experiences
        TeacherExperience exp1 = new TeacherExperience();
        exp1.setTeacher(teacher);
        exp1.setInstitution(dataGenerator.generateSchoolName());
        exp1.setRole("Teacher");
        exp1.setSubjectsTaught(dataGenerator.generateSubjectsExpertise());
        exp1.setFromDate(dataGenerator.generateExperienceFromDate());
        exp1.setToDate(dataGenerator.generateExperienceToDate(exp1.getFromDate()));
        exp1.setCurrentlyWorking(false);
        teacherExperienceRepository.save(exp1);

        TeacherExperience exp2 = new TeacherExperience();
        exp2.setTeacher(teacher);
        exp2.setInstitution(dataGenerator.generateSchoolName());
        exp2.setRole("Senior Teacher");
        exp2.setSubjectsTaught(dataGenerator.generateSubjectsExpertise());
        exp2.setFromDate(LocalDate.now().minusMonths(6));
        exp2.setToDate(null); // Current job
        exp2.setCurrentlyWorking(true);
        teacherExperienceRepository.save(exp2);
    }

    private void createTeacherCertifications(Teacher teacher) {
        // Create at least 1 certification
        TeacherCertification cert = new TeacherCertification();
        cert.setTeacher(teacher);
        cert.setCertificationName(dataGenerator.generateCertification());
        cert.setIssuingAuthority(dataGenerator.generateIssuingAuthority());
        cert.setCertificationId("CERT" + teacher.getId() + "001");
        cert.setIssueYear(2020 + (int)(teacher.getId() % 4)); // 2020-2023
        cert.setExpiryDate(LocalDate.now().plusYears(2));
        teacherCertificationRepository.save(cert);
    }

    private void createTeacherAvailability(Teacher teacher) {
        TeacherAvailability availability = new TeacherAvailability();
        availability.setTeacher(teacher);
        availability.setAvailableFrom(dataGenerator.generateAvailableFrom());
        availability.setAvailableTo(dataGenerator.generateAvailableTo());
        availability.setPreferredStudentLevels(dataGenerator.generatePreferredStudentLevels());
        availability.setLanguagesSpoken(dataGenerator.generateLanguagesSpoken());
        teacherAvailabilityRepository.save(availability);
    }

    private void createTeacherDocuments(Teacher teacher) {
        // Create at least 1 document
        TeacherDocument doc = new TeacherDocument();
        doc.setTeacher(teacher);
        doc.setDocumentName("Teaching Certificate");
        doc.setDocumentUrl(dataGenerator.generateDocumentUrl("teaching-certificate"));
        doc.setDocumentType("CERTIFICATE");
        teacherDocumentRepository.save(doc);
    }

    private void createTeacherBankDetails(Teacher teacher) {
        TeacherBankDetails bankDetails = new TeacherBankDetails();
        bankDetails.setTeacher(teacher);
        bankDetails.setPanNumber(dataGenerator.generatePANNumber());
        bankDetails.setAccountHolderName(teacher.getName());
        bankDetails.setBankName(dataGenerator.generateBankName());
        bankDetails.setAccountNumber(dataGenerator.generateAccountNumber()); // Will be encrypted by converter
        bankDetails.setIfscCode(dataGenerator.generateIFSCCode());
        bankDetails.setAccountType(dataGenerator.generateAccountType());
        bankDetails.setBranchAddress(dataGenerator.generateAddressLine1() + ", " + dataGenerator.generateCity());
        bankDetails.setMicrCode("123456789");
        bankDetails.setMobileNumber(teacher.getPhoneNumber());
        bankDetails.setEmail(teacher.getEmail());
        bankDetails.setTermsAccepted(true);
        bankDetails.setVerified(false);
        teacherBankDetailsRepository.save(bankDetails);
    }

    private void createTeacherAddresses(Teacher teacher) {
        // Create PERMANENT address
        TeacherAddress permanentAddress = new TeacherAddress();
        permanentAddress.setTeacher(teacher);
        permanentAddress.setAddressLine1(dataGenerator.generateAddressLine1());
        permanentAddress.setAddressLine2(dataGenerator.generateAddressLine2());
        permanentAddress.setCity(dataGenerator.generateCity());
        permanentAddress.setState(dataGenerator.generateState());
        permanentAddress.setZipCode(dataGenerator.generateZipCode());
        permanentAddress.setCountry("India");
        permanentAddress.setAddressType(AddressType.PERMANENT);
        teacherAddressRepository.save(permanentAddress);

        // Create CURRENT address
        TeacherAddress currentAddress = new TeacherAddress();
        currentAddress.setTeacher(teacher);
        currentAddress.setAddressLine1(dataGenerator.generateAddressLine1());
        currentAddress.setAddressLine2(dataGenerator.generateAddressLine2());
        currentAddress.setCity(dataGenerator.generateCity());
        currentAddress.setState(dataGenerator.generateState());
        currentAddress.setZipCode(dataGenerator.generateZipCode());
        currentAddress.setCountry("India");
        currentAddress.setAddressType(AddressType.CURRENT);
        teacherAddressRepository.save(currentAddress);
    }

    @Transactional
    public UserSeedResult seedAdmin(String email, String password, String name) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        
        if (existingUser.isPresent()) {
            logger.info("Admin {} already exists, updating profile", email);
            return updateAdminProfile(existingUser.get());
        } else {
            logger.info("Creating new admin: {}", email);
            return createAdminProfile(email, password, name);
        }
    }

    private UserSeedResult createAdminProfile(String email, String password, String name) {
        // Create user
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.ADMIN);
        user.setEnabled(true);
        user = userRepository.save(user);

        // Create admin profile
        AdminProfile profile = new AdminProfile();
        profile.setUser(user);
        profile.setPhoneNumber(dataGenerator.generatePhoneNumber());
        profile.setIsSuperAdmin(false); // All seeded admins are regular admins
        profile = adminProfileRepository.save(profile);

        return new UserSeedResult(true, false, email);
    }

    private UserSeedResult updateAdminProfile(User user) {
        AdminProfile profile = user.getAdminProfile();
        if (profile == null) {
            // Create missing profile
            profile = new AdminProfile();
            profile.setUser(user);
            profile.setPhoneNumber(dataGenerator.generatePhoneNumber());
            profile.setIsSuperAdmin(false);
            adminProfileRepository.save(profile);
        }
        return new UserSeedResult(false, true, user.getEmail());
    }

    // Inner classes for result tracking
    public static class SeedingResult {
        private int studentsCreated = 0;
        private int studentsUpdated = 0;
        private int teachersCreated = 0;
        private int teachersUpdated = 0;
        private int adminsCreated = 0;
        private int adminsUpdated = 0;
        private List<String> errors = new ArrayList<>();

        public void addStudentResult(UserSeedResult result) {
            if (result.isCreated()) studentsCreated++;
            if (result.isUpdated()) studentsUpdated++;
        }

        public void addTeacherResult(UserSeedResult result) {
            if (result.isCreated()) teachersCreated++;
            if (result.isUpdated()) teachersUpdated++;
        }

        public void addAdminResult(UserSeedResult result) {
            if (result.isCreated()) adminsCreated++;
            if (result.isUpdated()) adminsUpdated++;
        }

        public void addError(String error) {
            errors.add(error);
        }

        @Override
        public String toString() {
            return String.format("SeedingResult{students: %d created, %d updated; teachers: %d created, %d updated; admins: %d created, %d updated; errors: %d}",
                    studentsCreated, studentsUpdated, teachersCreated, teachersUpdated, adminsCreated, adminsUpdated, errors.size());
        }

        // Getters for JSON serialization
        public int getStudentsCreated() { return studentsCreated; }
        public int getStudentsUpdated() { return studentsUpdated; }
        public int getTeachersCreated() { return teachersCreated; }
        public int getTeachersUpdated() { return teachersUpdated; }
        public int getAdminsCreated() { return adminsCreated; }
        public int getAdminsUpdated() { return adminsUpdated; }
        public List<String> getErrors() { return errors; }
    }

    public static class UserSeedResult {
        private boolean created;
        private boolean updated;
        private String email;

        public UserSeedResult(boolean created, boolean updated, String email) {
            this.created = created;
            this.updated = updated;
            this.email = email;
        }

        public boolean isCreated() { return created; }
        public boolean isUpdated() { return updated; }
        public String getEmail() { return email; }
    }
}
