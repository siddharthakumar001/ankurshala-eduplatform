package com.ankurshala.backend.util;

import com.ankurshala.backend.entity.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Random;

/**
 * Utility class for generating realistic Indian data for seeding.
 * Uses deterministic random generation for consistent results across runs.
 */
@Component
public class IndianDataGenerator {

    private static final Random RANDOM = new Random(12345); // Fixed seed for deterministic results

    // Indian first names
    private static final String[] MALE_FIRST_NAMES = {
        "Arjun", "Rahul", "Vikram", "Suresh", "Rajesh", "Amit", "Deepak", "Kumar", "Sanjay", "Vishal",
        "Rohit", "Pradeep", "Manoj", "Naveen", "Sandeep", "Anil", "Sunil", "Vinod", "Ashok", "Ravi"
    };

    private static final String[] FEMALE_FIRST_NAMES = {
        "Priya", "Sunita", "Kavita", "Meera", "Anita", "Rekha", "Sushma", "Pooja", "Neha", "Shilpa",
        "Deepa", "Ritu", "Suman", "Jyoti", "Manju", "Sarita", "Usha", "Geeta", "Lata", "Kamala"
    };

    // Indian last names
    private static final String[] LAST_NAMES = {
        "Sharma", "Verma", "Gupta", "Singh", "Kumar", "Yadav", "Patel", "Jain", "Agarwal", "Reddy",
        "Mishra", "Pandey", "Choudhary", "Malhotra", "Bansal", "Tiwari", "Joshi", "Nair", "Iyer", "Rao"
    };

    // Indian cities
    private static final String[] CITIES = {
        "Mumbai", "Delhi", "Bangalore", "Hyderabad", "Chennai", "Kolkata", "Pune", "Ahmedabad", "Jaipur", "Surat",
        "Lucknow", "Kanpur", "Nagpur", "Indore", "Thane", "Bhopal", "Visakhapatnam", "Pimpri-Chinchwad", "Patna", "Vadodara"
    };

    // Indian states
    private static final String[] STATES = {
        "Maharashtra", "Delhi", "Karnataka", "Telangana", "Tamil Nadu", "West Bengal", "Gujarat", "Rajasthan",
        "Uttar Pradesh", "Madhya Pradesh", "Andhra Pradesh", "Bihar", "Haryana", "Punjab", "Kerala", "Odisha"
    };

    // School names
    private static final String[] SCHOOL_NAMES = {
        "Delhi Public School", "Kendriya Vidyalaya", "Army Public School", "DAV Public School", "St. Xavier's School",
        "Modern School", "Springdales School", "The Heritage School", "Ryan International School", "GD Goenka School",
        "Amity International School", "Lotus Valley School", "Manav Rachna School", "Vasant Valley School", "Tagore International School"
    };

    // Universities/Institutions
    private static final String[] UNIVERSITIES = {
        "Delhi University", "JNU", "IIT Delhi", "IIT Mumbai", "IIT Bangalore", "IIT Chennai", "IIT Kanpur",
        "BITS Pilani", "Anna University", "Jadavpur University", "Calcutta University", "Mumbai University",
        "Pune University", "Bangalore University", "Hyderabad University", "Chandigarh University"
    };

    // Subjects
    private static final String[] SUBJECTS = {
        "Mathematics", "Physics", "Chemistry", "Biology", "English", "Hindi", "History", "Geography", "Economics",
        "Computer Science", "Accountancy", "Business Studies", "Political Science", "Psychology", "Sociology"
    };

    // Banks
    private static final String[] BANKS = {
        "State Bank of India", "HDFC Bank", "ICICI Bank", "Axis Bank", "Kotak Mahindra Bank", "Punjab National Bank",
        "Bank of Baroda", "Canara Bank", "Union Bank of India", "Indian Bank", "Bank of India", "Central Bank of India"
    };

    // IFSC codes (sample)
    private static final String[] IFSC_CODES = {
        "SBIN0001234", "HDFC0001234", "ICIC0001234", "AXIS0001234", "KOTK0001234", "PUNB0001234",
        "BARB0001234", "CNRB0001234", "UBIN0001234", "IDIB0001234", "BKID0001234", "CBIN0001234"
    };

    // Languages
    private static final String[] LANGUAGES = {
        "Hindi", "English", "Bengali", "Telugu", "Marathi", "Tamil", "Gujarati", "Kannada", "Malayalam", "Punjabi"
    };

    // Certification names
    private static final String[] CERTIFICATIONS = {
        "CTET", "NET", "GATE", "IELTS", "TOEFL", "Microsoft Certified Educator", "Google Certified Educator",
        "Adobe Certified Expert", "Cisco Certified Network Associate", "Oracle Certified Professional"
    };

    // Issuing authorities
    private static final String[] ISSUING_AUTHORITIES = {
        "CBSE", "NCERT", "Microsoft", "Google", "Adobe", "Cisco", "Oracle", "British Council", "ETS", "NTA"
    };

    public String generateFirstName(Gender gender) {
        String[] names = gender == Gender.FEMALE ? FEMALE_FIRST_NAMES : MALE_FIRST_NAMES;
        return names[RANDOM.nextInt(names.length)];
    }

    public String generateLastName() {
        return LAST_NAMES[RANDOM.nextInt(LAST_NAMES.length)];
    }

    public String generateCity() {
        return CITIES[RANDOM.nextInt(CITIES.length)];
    }

    public String generateState() {
        return STATES[RANDOM.nextInt(STATES.length)];
    }

    public String generateSchoolName() {
        return SCHOOL_NAMES[RANDOM.nextInt(SCHOOL_NAMES.length)];
    }

    public String generateUniversity() {
        return UNIVERSITIES[RANDOM.nextInt(UNIVERSITIES.length)];
    }

    public String generateSubject() {
        return SUBJECTS[RANDOM.nextInt(SUBJECTS.length)];
    }

    public String generateBankName() {
        return BANKS[RANDOM.nextInt(BANKS.length)];
    }

    public String generateIFSCCode() {
        return IFSC_CODES[RANDOM.nextInt(IFSC_CODES.length)];
    }

    public String generateLanguage() {
        return LANGUAGES[RANDOM.nextInt(LANGUAGES.length)];
    }

    public String generateCertification() {
        return CERTIFICATIONS[RANDOM.nextInt(CERTIFICATIONS.length)];
    }

    public String generateIssuingAuthority() {
        return ISSUING_AUTHORITIES[RANDOM.nextInt(ISSUING_AUTHORITIES.length)];
    }

    public Gender generateGender() {
        return Gender.values()[RANDOM.nextInt(Gender.values().length)];
    }

    public EducationalBoard generateEducationalBoard() {
        return EducationalBoard.values()[RANDOM.nextInt(EducationalBoard.values().length)];
    }

    public ClassLevel generateClassLevel() {
        ClassLevel[] levels = {ClassLevel.GRADE_7, ClassLevel.GRADE_8, ClassLevel.GRADE_9, 
                              ClassLevel.GRADE_10, ClassLevel.GRADE_11, ClassLevel.GRADE_12};
        return levels[RANDOM.nextInt(levels.length)];
    }

    public TeacherStatus generateTeacherStatus() {
        return TeacherStatus.ACTIVE; // All seeded teachers are active
    }

    public AccountType generateAccountType() {
        return AccountType.SAVINGS; // All seeded accounts are savings
    }

    public AddressType generateAddressType() {
        return AddressType.values()[RANDOM.nextInt(AddressType.values().length)];
    }

    public LocalDate generateDateOfBirth(int minAge, int maxAge) {
        int age = minAge + RANDOM.nextInt(maxAge - minAge + 1);
        return LocalDate.now().minusYears(age);
    }

    public LocalDate generateExperienceFromDate() {
        int yearsAgo = 1 + RANDOM.nextInt(10); // 1-10 years ago
        return LocalDate.now().minusYears(yearsAgo);
    }

    public LocalDate generateExperienceToDate(LocalDate fromDate) {
        int durationMonths = 6 + RANDOM.nextInt(30); // 6-36 months
        return fromDate.plusMonths(durationMonths);
    }

    public LocalTime generateAvailableFrom() {
        int hour = 6 + RANDOM.nextInt(6); // 6 AM to 12 PM
        return LocalTime.of(hour, 0);
    }

    public LocalTime generateAvailableTo() {
        int hour = 18 + RANDOM.nextInt(6); // 6 PM to 12 AM
        return LocalTime.of(hour, 0);
    }

    public BigDecimal generateHourlyRate() {
        int rate = 200 + RANDOM.nextInt(800); // ₹200-1000 per hour
        return BigDecimal.valueOf(rate);
    }

    public BigDecimal generateRating() {
        double rating = 3.5 + RANDOM.nextDouble() * 1.4; // 3.5-4.9
        return BigDecimal.valueOf(Math.round(rating * 10.0) / 10.0);
    }

    public int generateYearsOfExperience() {
        return 1 + RANDOM.nextInt(15); // 1-15 years
    }

    public int generateTotalReviews() {
        return 10 + RANDOM.nextInt(100); // 10-109 reviews
    }

    public String generatePhoneNumber() {
        return "9" + (10000000 + RANDOM.nextInt(90000000)); // 10-digit Indian mobile
    }

    public String generateAlternatePhoneNumber() {
        return "8" + (10000000 + RANDOM.nextInt(90000000)); // 10-digit Indian mobile
    }

    public String generatePANNumber() {
        String[] letters = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
        StringBuilder pan = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            pan.append(letters[RANDOM.nextInt(letters.length)]);
        }
        pan.append(RANDOM.nextInt(10));
        pan.append(letters[RANDOM.nextInt(letters.length)]);
        pan.append(RANDOM.nextInt(10));
        pan.append(RANDOM.nextInt(10));
        pan.append(RANDOM.nextInt(10));
        return pan.toString();
    }

    public String generateAccountNumber() {
        return String.valueOf(1000000000L + RANDOM.nextInt(900000000)); // 10-digit account number
    }

    public String generateDocumentUrl(String documentType) {
        return "https://example.com/documents/" + documentType.toLowerCase().replace(" ", "-") + "-" + RANDOM.nextInt(1000) + ".pdf";
    }

    public String generatePhotoUrl(String type) {
        return "https://example.com/photos/" + type.toLowerCase() + "-" + RANDOM.nextInt(1000) + ".jpg";
    }

    public String generateLanguagesSpoken() {
        int count = 2 + RANDOM.nextInt(3); // 2-4 languages
        StringBuilder languages = new StringBuilder("[");
        for (int i = 0; i < count; i++) {
            if (i > 0) languages.append(",");
            languages.append("\"").append(generateLanguage()).append("\"");
        }
        languages.append("]");
        return languages.toString();
    }

    public String generatePreferredStudentLevels() {
        ClassLevel[] levels = {ClassLevel.GRADE_7, ClassLevel.GRADE_8, ClassLevel.GRADE_9, 
                              ClassLevel.GRADE_10, ClassLevel.GRADE_11, ClassLevel.GRADE_12};
        int count = 2 + RANDOM.nextInt(3); // 2-4 levels
        StringBuilder levelsJson = new StringBuilder("[");
        for (int i = 0; i < count; i++) {
            if (i > 0) levelsJson.append(",");
            levelsJson.append("\"").append(levels[RANDOM.nextInt(levels.length)]).append("\"");
        }
        levelsJson.append("]");
        return levelsJson.toString();
    }

    public String generateSubjectsExpertise() {
        int count = 2 + RANDOM.nextInt(4); // 2-5 subjects
        StringBuilder subjects = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0) subjects.append(", ");
            subjects.append(generateSubject());
        }
        return subjects.toString();
    }

    public String generateBio() {
        String[] bios = {
            "Passionate educator with years of experience in helping students achieve their academic goals.",
            "Dedicated teacher committed to creating engaging learning experiences for all students.",
            "Experienced educator specializing in making complex concepts easy to understand.",
            "Enthusiastic teacher with a proven track record of student success and improvement.",
            "Professional educator focused on personalized learning and student development."
        };
        return bios[RANDOM.nextInt(bios.length)];
    }

    public String generateQualificationText() {
        String[] qualifications = {
            "Master's degree in Mathematics with specialization in Applied Mathematics",
            "Bachelor's degree in Physics with teaching certification",
            "Master's degree in Chemistry with research experience",
            "Bachelor's degree in Biology with education diploma",
            "Master's degree in English Literature with teaching experience",
            "Bachelor's degree in Computer Science with technical certifications"
        };
        return qualifications[RANDOM.nextInt(qualifications.length)];
    }

    public String generateAddressLine1() {
        String[] streets = {"MG Road", "Park Street", "Church Street", "Brigade Road", "Commercial Street",
                           "Residency Road", "Cunningham Road", "Richmond Road", "St. Mark's Road", "Lavelle Road"};
        return RANDOM.nextInt(100) + ", " + streets[RANDOM.nextInt(streets.length)];
    }

    public String generateAddressLine2() {
        String[] areas = {"Near Metro Station", "Opposite Mall", "Behind Hospital", "Next to School", "Close to Market"};
        return areas[RANDOM.nextInt(areas.length)];
    }

    public String generateZipCode() {
        return String.valueOf(100000 + RANDOM.nextInt(900000)); // 6-digit Indian postal code
    }
}
