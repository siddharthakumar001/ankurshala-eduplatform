package com.ankurshala.backend.test;

import com.ankurshala.backend.bootstrap.BulkDemoSeeder;
import com.ankurshala.backend.bootstrap.BulkDemoSeeder.SeedingResult;
import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.repository.*;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for bulk demo seeder functionality.
 * Tests the complete seeding process with Testcontainers.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "DEMO_BULK_SEED=true",
    "DEMO_ENV=test",
    "DEMO_FORCE=true"
})
public class BulkDemoSeederIntegrationTest extends BaseIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(BulkDemoSeederIntegrationTest.class);

    @Autowired
    private BulkDemoSeeder bulkDemoSeeder;

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
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    @org.junit.jupiter.api.BeforeEach
    public void setUp() {
        // Initialize TransactionTemplate
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    @Test
    @Transactional
    public void testBulkSeedingCreatesCorrectNumberOfUsers() {
        // Clear existing data to ensure fresh seeding
        clearAllData();
        
        // Run bulk seeding
        SeedingResult result = bulkDemoSeeder.seedAllUsers();

        // Verify counts
        assertEquals(15, result.getStudentsCreated());
        assertEquals(15, result.getTeachersCreated());
        assertEquals(3, result.getAdminsCreated());
        assertEquals(0, result.getStudentsUpdated());
        assertEquals(0, result.getTeachersUpdated());
        assertEquals(0, result.getAdminsUpdated());
        assertTrue(result.getErrors().isEmpty());
    }
    
    private void clearAllData() {
        // Clear all data in reverse dependency order using a new transaction
        transactionTemplate.execute(status -> {
            try {
                // Delete child entities first - use native queries to avoid Hibernate cascade issues
                studentDocumentRepository.deleteAll();
                teacherDocumentRepository.deleteAll();
                teacherBankDetailsRepository.deleteAll();
                teacherAddressRepository.deleteAll();
                teacherAvailabilityRepository.deleteAll();
                teacherCertificationRepository.deleteAll();
                teacherExperienceRepository.deleteAll();
                teacherQualificationRepository.deleteAll();
                teacherProfessionalInfoRepository.deleteAll();
                
                // Flush to ensure all deletes are executed
                studentDocumentRepository.flush();
                teacherDocumentRepository.flush();
                teacherBankDetailsRepository.flush();
                teacherAddressRepository.flush();
                teacherAvailabilityRepository.flush();
                teacherCertificationRepository.flush();
                teacherExperienceRepository.flush();
                teacherQualificationRepository.flush();
                teacherProfessionalInfoRepository.flush();
                
                // Then delete parent entities
                studentProfileRepository.deleteAll();
                teacherProfileRepository.deleteAll();
                teacherRepository.deleteAll();
                adminProfileRepository.deleteAll();
                userRepository.deleteAll();
                
                // Flush to ensure all deletes are executed
                studentProfileRepository.flush();
                teacherProfileRepository.flush();
                teacherRepository.flush();
                adminProfileRepository.flush();
                userRepository.flush();
                
                return null;
            } catch (Exception e) {
                logger.error("Error clearing data", e);
                status.setRollbackOnly();
                throw e;
            }
        });
    }

    @Test
    public void testStudentSeedingCreatesCompleteProfile() {
        // Clear existing data to ensure fresh seeding
        clearAllData();
        
        // Seed a student
        String email = "student1@ankurshala.com";
        BulkDemoSeeder.UserSeedResult result = bulkDemoSeeder.seedStudent(email, "Maza@123");

        assertTrue(result.isCreated());
        assertFalse(result.isUpdated());
        assertEquals(email, result.getEmail());

        // Verify user exists
        Optional<User> userOpt = userRepository.findByEmail(email);
        assertTrue(userOpt.isPresent());
        User user = userOpt.get();
        assertEquals(Role.STUDENT, user.getRole());
        assertTrue(user.getEnabled());

        // Verify student profile exists
        Optional<StudentProfile> profileOpt = studentProfileRepository.findByUser(user);
        assertTrue(profileOpt.isPresent());
        StudentProfile profile = profileOpt.get();

        assertNotNull(profile.getFirstName());
        assertNotNull(profile.getLastName());
        assertNotNull(profile.getEducationalBoard());
        assertNotNull(profile.getClassLevel());
        assertNotNull(profile.getSchoolName());
        assertNotNull(profile.getMobileNumber());
        assertNotNull(profile.getDateOfBirth());

        // Verify student documents exist (at least 2)
        List<StudentDocument> documents = studentDocumentRepository.findAll()
                .stream()
                .filter(doc -> doc.getStudentProfile().getId().equals(profile.getId()))
                .toList();
        assertTrue(documents.size() >= 2);

        // Verify document types
        boolean hasReportCard = documents.stream()
                .anyMatch(doc -> "Report Card".equals(doc.getDocumentName()));
        boolean hasIdCard = documents.stream()
                .anyMatch(doc -> "ID Card".equals(doc.getDocumentName()));
        assertTrue(hasReportCard);
        assertTrue(hasIdCard);
    }

    @Test
    public void testTeacherSeedingCreatesCompleteProfile() {
        // Clear existing data to ensure fresh seeding
        clearAllData();
        
        // Seed a teacher
        String email = "teacher1@ankurshala.com";
        BulkDemoSeeder.UserSeedResult result = bulkDemoSeeder.seedTeacher(email, "Maza@123");

        assertTrue(result.isCreated());
        assertFalse(result.isUpdated());
        assertEquals(email, result.getEmail());

        // Verify user exists
        Optional<User> userOpt = userRepository.findByEmail(email);
        assertTrue(userOpt.isPresent());
        User user = userOpt.get();
        assertEquals(Role.TEACHER, user.getRole());
        assertTrue(user.getEnabled());

        // Verify teacher entity exists
        Optional<Teacher> teacherOpt = teacherRepository.findByUser(user);
        assertTrue(teacherOpt.isPresent());
        Teacher teacher = teacherOpt.get();

        assertNotNull(teacher.getName());
        assertNotNull(teacher.getEmail());
        assertNotNull(teacher.getGender());
        assertEquals(TeacherStatus.ACTIVE, teacher.getStatus());

        // Verify teacher profile exists
        Optional<TeacherProfile> profileOpt = teacherProfileRepository.findByUser(user);
        assertTrue(profileOpt.isPresent());
        TeacherProfile profile = profileOpt.get();

        assertNotNull(profile.getBio());
        assertNotNull(profile.getSpecialization());
        assertNotNull(profile.getYearsOfExperience());
        assertNotNull(profile.getRating());
        assertNotNull(profile.getTotalReviews());

        // Verify professional info exists
        Optional<TeacherProfessionalInfo> profInfoOpt = teacherProfessionalInfoRepository.findAll()
                .stream()
                .filter(info -> info.getTeacher().getId().equals(teacher.getId()))
                .findFirst();
        assertTrue(profInfoOpt.isPresent());

        // Verify qualifications exist (at least 2)
        List<TeacherQualification> qualifications = teacherQualificationRepository.findAll()
                .stream()
                .filter(qual -> qual.getTeacher().getId().equals(teacher.getId()))
                .toList();
        assertTrue(qualifications.size() >= 2);

        // Verify experiences exist (at least 2)
        List<TeacherExperience> experiences = teacherExperienceRepository.findAll()
                .stream()
                .filter(exp -> exp.getTeacher().getId().equals(teacher.getId()))
                .toList();
        assertTrue(experiences.size() >= 2);

        // Verify at least one experience is currently working
        boolean hasCurrentJob = experiences.stream()
                .anyMatch(exp -> Boolean.TRUE.equals(exp.getCurrentlyWorking()));
        assertTrue(hasCurrentJob);

        // Verify certifications exist (at least 1)
        List<TeacherCertification> certifications = teacherCertificationRepository.findAll()
                .stream()
                .filter(cert -> cert.getTeacher().getId().equals(teacher.getId()))
                .toList();
        assertTrue(certifications.size() >= 1);

        // Verify availability exists
        Optional<TeacherAvailability> availabilityOpt = teacherAvailabilityRepository.findAll()
                .stream()
                .filter(avail -> avail.getTeacher().getId().equals(teacher.getId()))
                .findFirst();
        assertTrue(availabilityOpt.isPresent());

        // Verify documents exist (at least 1)
        List<TeacherDocument> documents = teacherDocumentRepository.findAll()
                .stream()
                .filter(doc -> doc.getTeacher().getId().equals(teacher.getId()))
                .toList();
        assertTrue(documents.size() >= 1);

        // Verify bank details exist
        Optional<TeacherBankDetails> bankDetailsOpt = teacherBankDetailsRepository.findAll()
                .stream()
                .filter(bank -> bank.getTeacher().getId().equals(teacher.getId()))
                .findFirst();
        assertTrue(bankDetailsOpt.isPresent());

        TeacherBankDetails bankDetails = bankDetailsOpt.get();
        assertNotNull(bankDetails.getPanNumber());
        assertNotNull(bankDetails.getBankName());
        assertNotNull(bankDetails.getAccountNumber());
        assertNotNull(bankDetails.getIfscCode());
        assertEquals(AccountType.SAVINGS, bankDetails.getAccountType());

        // Verify bank account number is properly handled (should be decrypted when retrieved)
        String accountNumber = bankDetails.getAccountNumber();
        logger.info("Account number from DB: {}", accountNumber);
        logger.info("Account number length: {}", accountNumber.length());
        logger.info("Is plain numeric string: {}", accountNumber.matches("\\d+"));
        assertTrue(accountNumber.matches("\\d+")); // Should be plain numeric string (decrypted by converter)
        assertEquals(10, accountNumber.length()); // Should be 10 digits

        // Verify addresses exist (PERMANENT and CURRENT)
        List<TeacherAddress> addresses = teacherAddressRepository.findAll()
                .stream()
                .filter(addr -> addr.getTeacher().getId().equals(teacher.getId()))
                .toList();
        assertEquals(2, addresses.size());

        boolean hasPermanent = addresses.stream()
                .anyMatch(addr -> AddressType.PERMANENT.equals(addr.getAddressType()));
        boolean hasCurrent = addresses.stream()
                .anyMatch(addr -> AddressType.CURRENT.equals(addr.getAddressType()));
        assertTrue(hasPermanent);
        assertTrue(hasCurrent);
    }

    @Test
    public void testAdminSeedingCreatesCompleteProfile() {
        // Clear existing data to ensure fresh seeding
        clearAllData();
        
        // Seed an admin
        String email = "sidd@ankurshala.com";
        BulkDemoSeeder.UserSeedResult result = bulkDemoSeeder.seedAdmin(email, "ankur@123", "Sidd");

        assertTrue(result.isCreated());
        assertFalse(result.isUpdated());
        assertEquals(email, result.getEmail());

        // Verify user exists
        Optional<User> userOpt = userRepository.findByEmail(email);
        assertTrue(userOpt.isPresent());
        User user = userOpt.get();
        assertEquals(Role.ADMIN, user.getRole());
        assertTrue(user.getEnabled());
        assertEquals("Sidd", user.getName());

        // Verify admin profile exists
        Optional<AdminProfile> profileOpt = adminProfileRepository.findByUser(user);
        assertTrue(profileOpt.isPresent());
        AdminProfile profile = profileOpt.get();

        assertNotNull(profile.getPhoneNumber());
        assertFalse(profile.getIsSuperAdmin()); // Seeded admins are not super admins
    }

    @Test
    public void testIdempotencyStudentSeeding() {
        // Clear existing data to ensure fresh seeding
        clearAllData();
        
        String email = "student2@ankurshala.com";

        // First seeding
        BulkDemoSeeder.UserSeedResult firstResult = bulkDemoSeeder.seedStudent(email, "Maza@123");
        assertTrue(firstResult.isCreated());
        assertFalse(firstResult.isUpdated());

        // Second seeding (should update, not create)
        BulkDemoSeeder.UserSeedResult secondResult = bulkDemoSeeder.seedStudent(email, "Maza@123");
        assertFalse(secondResult.isCreated());
        assertTrue(secondResult.isUpdated());

        // Verify only one user exists
        Optional<User> userOpt = userRepository.findByEmail(email);
        assertTrue(userOpt.isPresent());
        assertEquals(1, userRepository.findAll().stream()
                .filter(user -> email.equals(user.getEmail()))
                .count());
    }

    @Test
    public void testIdempotencyTeacherSeeding() {
        // Clear existing data to ensure fresh seeding
        clearAllData();
        
        String email = "teacher2@ankurshala.com";

        // First seeding
        BulkDemoSeeder.UserSeedResult firstResult = bulkDemoSeeder.seedTeacher(email, "Maza@123");
        assertTrue(firstResult.isCreated());
        assertFalse(firstResult.isUpdated());

        // Second seeding (should update, not create)
        BulkDemoSeeder.UserSeedResult secondResult = bulkDemoSeeder.seedTeacher(email, "Maza@123");
        assertFalse(secondResult.isCreated());
        assertTrue(secondResult.isUpdated());

        // Verify only one user exists
        Optional<User> userOpt = userRepository.findByEmail(email);
        assertTrue(userOpt.isPresent());
        assertEquals(1, userRepository.findAll().stream()
                .filter(user -> email.equals(user.getEmail()))
                .count());
    }

    @Test
    public void testIdempotencyAdminSeeding() {
        // Clear existing data to ensure fresh seeding
        clearAllData();
        
        String email = "mayank@ankurshala.com";

        // First seeding
        BulkDemoSeeder.UserSeedResult firstResult = bulkDemoSeeder.seedAdmin(email, "mayank@123", "Mayank");
        assertTrue(firstResult.isCreated());
        assertFalse(firstResult.isUpdated());

        // Second seeding (should update, not create)
        BulkDemoSeeder.UserSeedResult secondResult = bulkDemoSeeder.seedAdmin(email, "mayank@123", "Mayank");
        assertFalse(secondResult.isCreated());
        assertTrue(secondResult.isUpdated());

        // Verify only one user exists
        Optional<User> userOpt = userRepository.findByEmail(email);
        assertTrue(userOpt.isPresent());
        assertEquals(1, userRepository.findAll().stream()
                .filter(user -> email.equals(user.getEmail()))
                .count());
    }

    @Test
    public void testBulkSeedingIdempotency() {
        // Clear existing data to ensure fresh seeding
        clearAllData();
        
        // First bulk seeding
        SeedingResult firstResult = bulkDemoSeeder.seedAllUsers();
        assertEquals(15, firstResult.getStudentsCreated());
        assertEquals(15, firstResult.getTeachersCreated());
        assertEquals(3, firstResult.getAdminsCreated());

        // Second bulk seeding (should update all, create none)
        SeedingResult secondResult = bulkDemoSeeder.seedAllUsers();
        assertEquals(0, secondResult.getStudentsCreated());
        assertEquals(0, secondResult.getTeachersCreated());
        assertEquals(0, secondResult.getAdminsCreated());
        assertEquals(15, secondResult.getStudentsUpdated());
        assertEquals(15, secondResult.getTeachersUpdated());
        assertEquals(3, secondResult.getAdminsUpdated());

        // Verify total user count is correct (no duplicates)
        long totalUsers = userRepository.count();
        assertEquals(33, totalUsers); // 15 students + 15 teachers + 3 admins
    }
}

