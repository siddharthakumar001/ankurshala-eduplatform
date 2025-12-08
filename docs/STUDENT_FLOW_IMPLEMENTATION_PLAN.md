# Student Flow Implementation Plan

**Version:** 1.0  
**Date:** November 23, 2025  
**Status:** In Progress

---

## Executive Summary

This document outlines the complete implementation plan for the Student flow in the AnkurShala platform. The implementation follows a modular, microservice-ready architecture with iterative development: implement → test → wire UI → e2e.

**Timeline Estimate:** 2-3 weeks (full-time)  
**Complexity:** High  
**Priority:** Critical Path

---

## Architecture Principles

1. **Package-by-Feature** - Domain modules (student, content, booking, pricing, wallet, notification)
2. **Service Layer Isolation** - No repository leakage across modules
3. **Read Models/DTOs** - Cross-module communication via interfaces
4. **Event-Driven** - Domain events for booking lifecycle
5. **Cache-First** - Redis for read-heavy taxonomy data
6. **Policy-Driven** - Configurable cancellation/reschedule rules

---

## Phase 1: Domain Model & Database (Days 1-2)

### 1.1 Entity Updates

#### StudentProfile Enhancement
**File:** `backend/src/main/java/com/ankurshala/backend/entity/StudentProfile.java`

**Add Fields:**
```java
@Column(name = "board_id")
private Long boardId;

@Column(name = "grade_id") 
private Long gradeId;

@Column(name = "language", length = 50)
private String language;

@Column(name = "goals", length = 1000)
private String goals;

@Column(name = "avatar_url", length = 500)
private String avatarUrl;

@Column(name = "is_complete", nullable = false)
private Boolean isComplete = false;
```

#### NEW: StudentStudyList Entity
**File:** `backend/src/main/java/com/ankurshala/backend/entity/StudentStudyList.java`

```java
@Entity
@Table(name = "student_study_list", 
    indexes = {
        @Index(name = "idx_study_list_student", columnList = "student_id"),
        @Index(name = "idx_study_list_topic", columnList = "topic_id"),
        @Index(name = "idx_study_list_status", columnList = "status")
    }
)
public class StudentStudyList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @Column(name = "topic_id", nullable = false)
    private Long topicId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudyStatus status; // ADDED, IN_PROGRESS, DONE
    
    @Column(name = "added_at", nullable = false)
    private ZonedDateTime addedAt;
    
    @Column(name = "done_at")
    private ZonedDateTime doneAt;
}

enum StudyStatus {
    ADDED, IN_PROGRESS, DONE
}
```

#### Booking Entity Enhancement
**File:** `backend/src/main/java/com/ankurshala/backend/entity/Booking.java`

**Add Fields:**
```java
@Column(name = "topic_id")
private Long topicId;

@Column(name = "price_min")
private BigDecimal priceMin;

@Column(name = "price_max")
private BigDecimal priceMax;

@Column(name = "cancellation_fee")
private BigDecimal cancellationFee;

@Column(name = "notes", length = 2000)
private String notes;
```

#### NEW: StudentNotification Entity
**File:** `backend/src/main/java/com/ankurshala/backend/entity/StudentNotification.java`

```java
@Entity
@Table(name = "student_notifications",
    indexes = {
        @Index(name = "idx_notif_student", columnList = "student_id"),
        @Index(name = "idx_notif_read", columnList = "is_read"),
        @Index(name = "idx_notif_created", columnList = "created_at")
    }
)
public class StudentNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @Column(nullable = false, length = 50)
    private String kind; // BOOKING_ACCEPTED, CLASS_REMINDER, etc.
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(nullable = false, length = 1000)
    private String body;
    
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;
    
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;
}
```

#### WalletTransaction Entity Enhancement
**File:** `backend/src/main/java/com/ankurshala/backend/entity/WalletTransaction.java`

**Add Fields:**
```java
@Column(name = "booking_id")
private Long bookingId;

@Column(name = "reason", length = 500)
private String reason;
```

### 1.2 Flyway Migrations

#### V30__enhance_student_profile.sql
```sql
ALTER TABLE student_profiles 
ADD COLUMN board_id BIGINT,
ADD COLUMN grade_id BIGINT,
ADD COLUMN language VARCHAR(50),
ADD COLUMN goals VARCHAR(1000),
ADD COLUMN avatar_url VARCHAR(500),
ADD COLUMN is_complete BOOLEAN NOT NULL DEFAULT false;

ALTER TABLE student_profiles
ADD CONSTRAINT fk_student_board FOREIGN KEY (board_id) REFERENCES boards(id),
ADD CONSTRAINT fk_student_grade FOREIGN KEY (grade_id) REFERENCES grades(id);

CREATE INDEX idx_student_board ON student_profiles(board_id);
CREATE INDEX idx_student_grade ON student_profiles(grade_id);
```

#### V31__create_student_study_list.sql
```sql
CREATE TABLE student_study_list (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES student_profiles(id) ON DELETE CASCADE,
    topic_id BIGINT NOT NULL REFERENCES topics(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL CHECK (status IN ('ADDED', 'IN_PROGRESS', 'DONE')),
    added_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    done_at TIMESTAMP WITH TIME ZONE,
    UNIQUE(student_id, topic_id)
);

CREATE INDEX idx_study_list_student ON student_study_list(student_id);
CREATE INDEX idx_study_list_topic ON student_study_list(topic_id);
CREATE INDEX idx_study_list_status ON student_study_list(status);
```

#### V32__enhance_bookings.sql
```sql
ALTER TABLE bookings
ADD COLUMN topic_id BIGINT REFERENCES topics(id),
ADD COLUMN price_min DECIMAL(10,2),
ADD COLUMN price_max DECIMAL(10,2),
ADD COLUMN cancellation_fee DECIMAL(10,2) DEFAULT 0,
ADD COLUMN notes TEXT;

CREATE INDEX idx_booking_topic ON bookings(topic_id);
CREATE INDEX idx_booking_student_time ON bookings(student_id, start_ts);
```

#### V33__create_student_notifications.sql
```sql
CREATE TABLE student_notifications (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    kind VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    body VARCHAR(1000) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notif_student ON student_notifications(student_id);
CREATE INDEX idx_notif_read ON student_notifications(is_read);
CREATE INDEX idx_notif_created ON student_notifications(created_at);
```

#### V34__enhance_wallet_transactions.sql
```sql
ALTER TABLE wallet_transactions
ADD COLUMN booking_id BIGINT REFERENCES bookings(id),
ADD COLUMN reason VARCHAR(500);

CREATE INDEX idx_wallet_txn_booking ON wallet_transactions(booking_id);
```

### 1.3 Repository Layer

**Package:** `backend/src/main/java/com/ankurshala/backend/repository/student/`

- `StudentProfileRepository.java`
- `StudentStudyListRepository.java`
- `StudentNotificationRepository.java`
- `StudentWalletRepository.java`

**Key Methods:**
- `findByUserId(Long userId)`
- `findByStudentIdAndStatus(Long studentId, StudyStatus status)`
- `findByStudentIdAndIsReadOrderByCreatedAtDesc(Long studentId, Boolean isRead, Pageable pageable)`

---

## Phase 2: Backend Services & APIs (Days 3-7)

### 2.1 Module Structure

```
backend/src/main/java/com/ankurshala/backend/
├── module/
│   ├── student/
│   │   ├── controller/
│   │   │   ├── StudentProfileController.java
│   │   │   ├── StudentBookingController.java
│   │   │   ├── StudentStudyListController.java
│   │   │   ├── StudentNotificationController.java
│   │   │   └── StudentDashboardController.java (exists)
│   │   ├── service/
│   │   │   ├── StudentProfileService.java
│   │   │   ├── StudentBookingService.java (exists)
│   │   │   ├── StudentStudyListService.java
│   │   │   └── StudentDashboardService.java (exists)
│   │   ├── dto/
│   │   │   ├── request/
│   │   │   │   ├── UpdateStudentProfileRequest.java
│   │   │   │   ├── CreateBookingRequest.java
│   │   │   │   ├── RescheduleBookingRequest.java
│   │   │   │   └── AddToStudyListRequest.java
│   │   │   └── response/
│   │   │       ├── StudentProfileDto.java
│   │   │       ├── BookingDto.java
│   │   │       └── StudyListItemDto.java
│   │   └── event/
│   │       ├── BookingRequestedEvent.java
│   │       ├── BookingAcceptedEvent.java
│   │       └── BookingCancelledEvent.java
│   ├── content/
│   │   ├── controller/
│   │   │   └── ContentController.java
│   │   ├── service/
│   │   │   └── ContentCacheService.java
│   │   └── dto/
│   ├── booking/
│   │   ├── service/
│   │   │   ├── BookingPolicyService.java
│   │   │   └── BookingMatchingService.java
│   ├── pricing/
│   │   └── service/
│   │       └── PricingResolutionService.java
│   └── wallet/
│       └── service/
│           └── WalletTransactionService.java
```

### 2.2 API Endpoints

#### Student Profile & Onboarding
```
GET    /api/student/profile
PUT    /api/student/profile
POST   /api/student/profile/documents
DELETE /api/student/profile/documents/{id}
```

**Implementation Priority:** HIGH  
**Dependencies:** StudentProfile entity, StudentProfileRepository  
**Cache:** No  
**Auth:** @PreAuthorize("hasRole('STUDENT')")

#### Content Discovery (Cascading)
```
GET /api/content/boards
GET /api/content/grades?boardId=
GET /api/content/subjects?boardId=&gradeId=
GET /api/content/chapters?subjectId=
GET /api/content/topics?chapterId=
GET /api/content/topic/{id}
```

**Implementation Priority:** HIGH  
**Dependencies:** Existing taxonomy entities  
**Cache:** Redis (60-300s TTL)  
**Auth:** Authenticated users

#### Student Study List
```
GET    /api/student/study-list
POST   /api/student/study-list (add topic)
PATCH  /api/student/study-list/{id}/done
DELETE /api/student/study-list/{id}
```

**Implementation Priority:** MEDIUM  
**Dependencies:** StudentStudyList entity  
**Cache:** No  
**Auth:** @PreAuthorize("hasRole('STUDENT')")

#### Student Bookings
```
POST   /api/student/bookings
GET    /api/student/bookings/upcoming
GET    /api/student/bookings/history
GET    /api/student/bookings/{id}
POST   /api/student/bookings/{id}/cancel
POST   /api/student/bookings/{id}/reschedule
POST   /api/student/bookings/{id}/notes
```

**Implementation Priority:** CRITICAL  
**Dependencies:** Booking entity, PricingService, PolicyService, WalletService  
**Cache:** No  
**Auth:** @PreAuthorize("hasRole('STUDENT')")

**Business Rules:**
- endTime = startTime + topic.expected_minutes
- Enforce 15-min gap from prior class end
- Resolve price band via `/api/admin/pricing/resolve`
- Publish `booking.requested` event
- Broadcast to matching teachers (async)

#### Calendar & Notifications
```
GET  /api/student/calendar?from=&to=
GET  /api/student/notifications
POST /api/student/notifications/{id}/read
```

**Implementation Priority:** MEDIUM  
**Dependencies:** Booking entity, StudentNotification entity  
**Cache:** No  
**Auth:** @PreAuthorize("hasRole('STUDENT')")

#### Dashboard & Reports
```
GET /api/student/dashboard
GET /api/student/reports/export?format=csv|pdf
```

**Implementation Priority:** LOW  
**Dependencies:** Booking, StudyList analytics  
**Cache:** Short (30s)  
**Auth:** @PreAuthorize("hasRole('STUDENT')")

#### AI Stubs (Feature-Flagged)
```
POST /api/student/ai/recommendations
POST /api/student/ai/summary
```

**Implementation Priority:** LOW (Stub only)  
**Dependencies:** Environment flag  
**Auth:** @PreAuthorize("hasRole('STUDENT')")

### 2.3 Redis Caching Strategy

**Configuration:**
```yaml
spring:
  data:
    redis:
      host: redis
      port: 6379
      timeout: 2000ms
  cache:
    type: redis
    redis:
      time-to-live: 300000 # 5 minutes
```

**Cache Keys:**
```
content:boards                    → TTL: 300s
content:grades:{boardId}          → TTL: 300s
content:subjects:{boardId}:{gradeId} → TTL: 180s
content:chapters:{subjectId}      → TTL: 180s
content:topics:{chapterId}        → TTL: 180s
content:topic:{topicId}           → TTL: 300s
```

**Invalidation:**
- Manual: Admin content updates clear related keys
- Automatic: TTL expiry

### 2.4 Service Layer Details

#### StudentProfileService
```java
@Service
@Transactional
public class StudentProfileService {
    
    public StudentProfileDto getProfile(Long userId) {
        // Fetch StudentProfile by userId
        // Include board name, grade name (via joins/queries)
        // Check isComplete flag
    }
    
    public StudentProfileDto updateProfile(Long userId, UpdateProfileRequest req) {
        // Validate board_id, grade_id exist
        // Update all fields
        // Set isComplete=true if all required fields present
        // Clear any onboarding flags
    }
    
    public void addDocument(Long userId, DocumentRequest req) {
        // Create StudentDocument with URL
    }
    
    public void deleteDocument(Long userId, Long docId) {
        // Soft delete or hard delete
    }
    
    public boolean isProfileComplete(Long userId) {
        // Check: boardId, gradeId, language, goals all non-null
    }
}
```

#### ContentCacheService
```java
@Service
public class ContentCacheService {
    
    @Cacheable(value = "content:boards", unless = "#result.isEmpty()")
    public List<BoardDto> getBoards() { }
    
    @Cacheable(value = "content:grades", key = "#boardId")
    public List<GradeDto> getGrades(Long boardId) { }
    
    @Cacheable(value = "content:subjects", key = "#boardId + ':' + #gradeId")
    public List<SubjectDto> getSubjects(Long boardId, Long gradeId) { }
    
    @CacheEvict(value = {"content:boards", "content:grades", "content:subjects"}, allEntries = true)
    public void invalidateContentCache() { }
}
```

#### StudentBookingService
```java
@Service
@Transactional
public class StudentBookingService {
    
    private final BookingRepository bookingRepository;
    private final PricingResolutionService pricingService;
    private final BookingPolicyService policyService;
    private final WalletTransactionService walletService;
    private final ApplicationEventPublisher eventPublisher;
    
    public BookingDto createBooking(Long studentId, CreateBookingRequest req) {
        // 1. Validate topic exists
        // 2. Calculate endTime = startTime + topic.expected_minutes
        // 3. Check 15-min gap rule
        // 4. Resolve pricing (price_min, price_max)
        // 5. Create Booking with status=REQUESTED
        // 6. Publish BookingRequestedEvent
        // 7. Async: Broadcast to matching teachers
        return bookingDto;
    }
    
    public BookingDto rescheduleBooking(Long studentId, Long bookingId, RescheduleRequest req) {
        // 1. Fetch booking, verify ownership
        // 2. Check current status (must be REQUESTED or ACCEPTED)
        // 3. Calculate reschedule fee via policyService
        // 4. Update booking with new times, status=RESCHEDULED
        // 5. Debit fee from wallet if applicable
        // 6. Publish BookingRescheduledEvent
    }
    
    public void cancelBooking(Long studentId, Long bookingId) {
        // 1. Fetch booking, verify ownership
        // 2. Calculate cancellation fee via policyService
        // 3. Update status=CANCELLED
        // 4. Debit fee from wallet
        // 5. Check admin fee waiver rules (override fee to 0 if applicable)
        // 6. Publish BookingCancelledEvent
    }
    
    private void enforceGapRule(Long studentId, ZonedDateTime newStartTime) {
        // Find last booking for student ending close to newStartTime
        // Throw exception if gap < 15 minutes
    }
}
```

#### BookingPolicyService
```java
@Service
public class BookingPolicyService {
    
    public BigDecimal calculateCancellationFee(Booking booking) {
        long minutesUntilStart = ChronoUnit.MINUTES.between(ZonedDateTime.now(), booking.getStartTs());
        
        if (minutesUntilStart > 1440) return BigDecimal.ZERO; // >24h
        if (minutesUntilStart > 720) return booking.getPriceMin().multiply(new BigDecimal("0.25")); // 12-24h
        if (minutesUntilStart > 180) return booking.getPriceMin().multiply(new BigDecimal("0.50")); // 3-12h
        return booking.getPriceMin(); // <3h full charge
    }
    
    public BigDecimal calculateRescheduleFee(Booking booking) {
        long minutesUntilStart = ChronoUnit.MINUTES.between(ZonedDateTime.now(), booking.getStartTs());
        
        if (minutesUntilStart > 720) return BigDecimal.ZERO; // >12h free
        return booking.getPriceMin().multiply(new BigDecimal("0.10")); // <12h 10% fee
    }
    
    public boolean isWaiverApplicable(Long studentId) {
        // Check admin fee_waivers table for active waiver
        // Return true if waiver exists and is valid
    }
}
```

#### PricingResolutionService
```java
@Service
public class PricingResolutionService {
    
    public PriceBandDto resolvePricing(Long boardId, Long gradeId, Long subjectId, Long chapterId, Long topicId) {
        // Call existing /api/admin/pricing/resolve endpoint
        // Or implement similar logic:
        // 1. Find most specific pricing rule matching the taxonomy
        // 2. Return min/max hourly rate
        // Default: { min: 200, max: 500 }
    }
}
```

#### WalletTransactionService
```java
@Service
@Transactional
public class WalletTransactionService {
    
    public void debit(Long studentId, BigDecimal amount, String reason, Long bookingId) {
        StudentWallet wallet = walletRepository.findByStudentId(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
        
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient wallet balance");
        }
        
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);
        
        WalletTransaction txn = new WalletTransaction();
        txn.setWalletId(wallet.getId());
        txn.setType(WalletTxnType.DEBIT);
        txn.setAmount(amount);
        txn.setReason(reason);
        txn.setBookingId(bookingId);
        txn.setCreatedAt(ZonedDateTime.now());
        walletTxnRepository.save(txn);
    }
    
    public void credit(Long studentId, BigDecimal amount, String reason, Long bookingId) {
        // Similar logic for credit
    }
}
```

---

## Phase 3: Frontend Implementation (Days 8-12)

### 3.1 Directory Structure

```
frontend/src/app/student/
├── layout.tsx                    # Student theme wrapper
├── onboarding/
│   └── page.tsx                  # Onboarding wizard
├── dashboard/
│   └── page.tsx                  # Dashboard (exists, enhance)
├── discover/
│   └── page.tsx                  # Content discovery
├── booking/
│   └── page.tsx                  # Book a class
├── calendar/
│   └── page.tsx                  # Calendar view
├── history/
│   └── page.tsx                  # Booking history
├── study-list/
│   └── page.tsx                  # My study list
├── notifications/
│   └── page.tsx                  # Notifications
└── profile/
    └── page.tsx                  # Profile management (exists)
```

### 3.2 Route Guards

**File:** `frontend/src/components/guards/StudentRoute.tsx`

```tsx
'use client';

export function StudentRoute({ children }: { children: React.ReactNode }) {
  const { user, isLoading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!isLoading) {
      if (!user) {
        router.push('/login');
      } else if (user.role !== 'STUDENT') {
        router.push('/forbidden');
      } else if (!user.profileComplete) {
        router.push('/student/onboarding');
      }
    }
  }, [user, isLoading]);

  if (isLoading) return <LoadingSpinner />;
  if (!user || user.role !== 'STUDENT') return null;
  if (!user.profileComplete && !window.location.pathname.includes('/onboarding')) {
    return null;
  }

  return <>{children}</>;
}
```

### 3.3 Key Pages

#### Onboarding Page
**Features:**
- Multi-step wizard (3 steps)
- Step 1: Board & Grade selection (cascading)
- Step 2: Language & Goals (textarea)
- Step 3: Avatar upload (URL for now)
- Form validation with Zod
- Auto-redirect to dashboard on completion

#### Dashboard Page
**Features:**
- KPI cards: Upcoming classes, Completed, Total hours
- Weak areas chart (subjects with low completion)
- Recommended topics (AI stub)
- "Continue Learning" section (study list preview)
- Quick actions: Book Class, Browse Content

#### Discover Page
**Features:**
- Cascading selectors: Board → Grade → Subject → Chapter → Topic
- Selectors auto-populate based on student's profile (board/grade)
- Topic details panel (right side):
  - Name, summary, prerequisites
  - Expected time
  - Actions: "Add to Study List", "Book Class Now"
- Loading states for each selector level
- Empty states when no data

#### Booking Page
**Features:**
- Form with topic selection (pre-filled from discover page if coming from there)
- Date picker (disable past dates)
- Time slot picker (30-min intervals)
- Price band display (live via pricing API)
- Notes field (optional)
- Submit → "Request Sent" toast → redirect to calendar

#### Calendar Page
**Features:**
- FullCalendar component
- Day/Week/Month views
- Multiple events per day with status color:
  - Blue: REQUESTED
  - Green: ACCEPTED
  - Yellow: RESCHEDULED
  - Red: CANCELLED
  - Gray: COMPLETED
- Click event → modal with:
  - Topic details
  - Teacher info (if assigned)
  - Actions: Reschedule, Cancel (if applicable)
  - Zoom link (if available)

#### Study List Page
**Features:**
- List of added topics
- Filterable by status (ADDED, IN_PROGRESS, DONE)
- Mark as done action
- Remove from list action
- Link to book class for each topic

### 3.4 State Management

**Zustand Store:** `frontend/src/store/studentStore.ts`

```typescript
interface StudentStore {
  profile: StudentProfile | null;
  studyList: StudyListItem[];
  notifications: Notification[];
  unreadCount: number;
  
  setProfile: (profile: StudentProfile) => void;
  updateProfile: (updates: Partial<StudentProfile>) => void;
  addToStudyList: (topicId: number) => Promise<void>;
  markStudyItemDone: (id: number) => Promise<void>;
  markNotificationRead: (id: number) => Promise<void>;
}
```

### 3.5 API Client

**File:** `frontend/src/lib/api/studentApi.ts`

```typescript
export const studentApi = {
  // Profile
  getProfile: () => apiClient.get<StudentProfile>('/api/student/profile'),
  updateProfile: (data: UpdateProfileRequest) => 
    apiClient.put<StudentProfile>('/api/student/profile', data),
  
  // Study List
  getStudyList: () => apiClient.get<StudyListItem[]>('/api/student/study-list'),
  addToStudyList: (topicId: number) => 
    apiClient.post('/api/student/study-list', { topicId }),
  markDone: (id: number) => 
    apiClient.patch(`/api/student/study-list/${id}/done`),
  
  // Bookings
  createBooking: (data: CreateBookingRequest) => 
    apiClient.post<Booking>('/api/student/bookings', data),
  getUpcoming: () => apiClient.get<Booking[]>('/api/student/bookings/upcoming'),
  getHistory: (page: number) => 
    apiClient.get<PagedResponse<Booking>>(`/api/student/bookings/history?page=${page}`),
  cancelBooking: (id: number) => 
    apiClient.post(`/api/student/bookings/${id}/cancel`),
  rescheduleBooking: (id: number, data: RescheduleRequest) => 
    apiClient.post(`/api/student/bookings/${id}/reschedule`, data),
  
  // Calendar
  getCalendar: (from: string, to: string) => 
    apiClient.get<CalendarEvent[]>(`/api/student/calendar?from=${from}&to=${to}`),
  
  // Notifications
  getNotifications: (page: number) => 
    apiClient.get<PagedResponse<Notification>>(`/api/student/notifications?page=${page}`),
  markRead: (id: number) => 
    apiClient.post(`/api/student/notifications/${id}/read`),
  
  // Dashboard
  getDashboard: () => apiClient.get<DashboardData>('/api/student/dashboard'),
};
```

---

## Phase 4: Testing (Days 13-15)

### 4.1 Backend Unit Tests

**Location:** `backend/src/test/java/com/ankurshala/backend/module/student/service/`

- `StudentProfileServiceTest.java`
- `StudentBookingServiceTest.java`
- `BookingPolicyServiceTest.java`
- `WalletTransactionServiceTest.java`

**Coverage Target:** 80%+

### 4.2 Backend Integration Tests

**Location:** `backend/src/test/java/com/ankurshala/backend/module/student/integration/`

**Use:** Testcontainers (Postgres + Redis)

- `StudentProfileIntegrationTest.java`
- `ContentDiscoveryIntegrationTest.java`
- `BookingFlowIntegrationTest.java`

### 4.3 Frontend E2E Tests

**Location:** `frontend/e2e/student/`

**Playwright Tests:**
- `student-onboarding.spec.ts`
- `student-discover.spec.ts`
- `student-booking.spec.ts`
- `student-calendar.spec.ts`
- `student-study-list.spec.ts`

**Scenarios:**
1. New student login → forced onboarding → complete → dashboard visible
2. Discover flow: cascade selectors work correctly
3. Booking flow: topic → date/time → price preview → submit → calendar shows
4. Cancel/reschedule: fees calculated correctly
5. Study list: add → mark done → removed from list

---

## Phase 5: Dev Seeder & Docker (Days 16-17)

### 5.1 Seeder Updates

**File:** `backend/src/main/java/com/ankurshala/backend/service/DevSeedService.java`

**Add:**
- 2 demo students (1 complete profile, 1 incomplete)
- Study list items (5 topics added, 2 marked done)
- Pricing rules for demo topics
- 1 fee waiver rule (for testing waiver logic)
- 3 demo bookings (REQUESTED, ACCEPTED, COMPLETED states)
- Notifications for each student

### 5.2 Docker Verification

**Commands:**
```bash
docker compose down -v
docker compose up --build
# Verify all services healthy
# Verify frontend HMR works
# Verify backend API responds
```

---

## Phase 6: Acceptance Verification (Days 18-19)

### Acceptance Checklist

- [ ] **Profile & Onboarding**
  - [ ] Incomplete profile → forced to onboarding
  - [ ] Onboarding wizard validates all required fields
  - [ ] Completion redirects to dashboard
  - [ ] Profile page shows all data correctly

- [ ] **Content Discovery**
  - [ ] Cascading selectors load fast (<500ms)
  - [ ] Board/Grade auto-filtered by student profile
  - [ ] Topic details display correctly
  - [ ] Add to study list works

- [ ] **Booking Flow**
  - [ ] Create booking calculates endTime correctly
  - [ ] 15-min gap rule enforced
  - [ ] Price band resolved and displayed
  - [ ] Request sent successfully

- [ ] **Calendar**
  - [ ] Multiple classes per day display cleanly
  - [ ] Status colors correct
  - [ ] Click event shows details
  - [ ] Reschedule/cancel actions work

- [ ] **Cancellation & Fees**
  - [ ] Cancel within 3h → full fee
  - [ ] Cancel 12-24h → 25% fee
  - [ ] Admin waiver overrides fees
  - [ ] Wallet transactions recorded

- [ ] **Study List**
  - [ ] Add topic works
  - [ ] Mark done updates status
  - [ ] Remove from list works

- [ ] **Notifications**
  - [ ] List displays correctly
  - [ ] Unread count badge updates
  - [ ] Mark read works

- [ ] **UI/UX**
  - [ ] Loading states everywhere
  - [ ] Empty states with helpful messages
  - [ ] Error toasts for failures
  - [ ] Form validation with clear errors

- [ ] **Tests**
  - [ ] All backend unit tests green
  - [ ] All integration tests green
  - [ ] All E2E tests pass
  - [ ] No console errors in browser

- [ ] **Docker**
  - [ ] `docker compose up` works first time
  - [ ] All services healthy
  - [ ] No CORS errors
  - [ ] Seeder creates demo data

---

## Implementation Order (Sprint Breakdown)

### Sprint 1: Foundation (Days 1-3)
1. Database migrations
2. Entity updates
3. Repository layer
4. Basic service layer skeleton

### Sprint 2: Core APIs (Days 4-7)
1. Profile & Onboarding APIs
2. Content Discovery APIs with Redis
3. Study List APIs
4. Basic Booking API (no policies yet)

### Sprint 3: Booking Logic (Days 8-10)
1. Pricing resolution service
2. Policy service (cancellation/reschedule)
3. Wallet transaction service
4. Complete booking flow with fees

### Sprint 4: Frontend Core (Days 11-13)
1. Route guards
2. Onboarding page
3. Dashboard enhancements
4. Discover page

### Sprint 5: Frontend Booking (Days 14-15)
1. Booking page
2. Calendar page
3. History page
4. Study list page

### Sprint 6: Polish & Testing (Days 16-19)
1. Backend tests
2. Frontend E2E tests
3. Bug fixes
4. Seeder & Docker verification
5. Acceptance testing

---

## Risk Mitigation

### High-Risk Areas

1. **Redis Caching Complexity**
   - Mitigation: Start without cache, add incrementally
   - Fallback: Query DB directly on cache miss

2. **15-Min Gap Rule**
   - Risk: Complex query logic, potential race conditions
   - Mitigation: Use database-level locking or SELECT FOR UPDATE

3. **Policy Fee Calculations**
   - Risk: Business logic bugs in fee calculation
   - Mitigation: Comprehensive unit tests, admin override capability

4. **Event Publishing**
   - Risk: Async failures, message loss
   - Mitigation: Start with in-memory events, upgrade to Kafka later

5. **Frontend State Management**
   - Risk: Stale data, cache invalidation
   - Mitigation: React Query with conservative stale times

### Performance Considerations

- **Content APIs:** Redis cache = sub-100ms response times
- **Booking Creation:** <500ms target (including pricing resolution)
- **Calendar Load:** Paginated, max 100 events per query
- **Dashboard:** Aggressive caching (30s TTL)

---

## Success Metrics

- **Code Coverage:** >80% backend, >70% frontend
- **API Response Time:** p95 <500ms for reads, <1s for writes
- **E2E Test Pass Rate:** 100%
- **Zero Critical Bugs:** in acceptance testing
- **Docker Startup:** <3 minutes for full stack

---

## Post-Implementation

### Microservice Extraction Readiness

This implementation is designed for future "strangler pattern" extraction:

1. **student** module → Student Service
2. **booking** module → Booking Service
3. **pricing** module → Pricing Service
4. **wallet** module → Wallet Service
5. **notification** module → Notification Service

**Key Patterns:**
- Service-to-service via REST/gRPC interfaces
- Event-driven communication (Kafka)
- Shared read models (DTOs)
- Database per service (when extracted)

---

## Appendix

### Tech Stack Reference
- **Backend:** Spring Boot 3.2.5, Java 17, PostgreSQL 15, Redis
- **Frontend:** Next.js 14, TypeScript, Tailwind, shadcn/ui, React Query, Zustand, FullCalendar
- **Testing:** JUnit 5, Testcontainers, Playwright
- **Infrastructure:** Docker, Docker Compose

### Useful Commands

```bash
# Backend
mvn clean test
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Frontend
npm run dev
npm run test:e2e

# Docker
docker compose up --build
docker compose logs -f backend
docker exec -it ankurshala_db_local psql -U ankur -d ankurshala

# Database
psql -U ankur -d ankurshala -c "SELECT * FROM student_profiles;"
```

---

**Document Status:** DRAFT → APPROVED  
**Next Review:** After Sprint 1 completion  
**Owner:** Development Team
