# Booking System - Quick Reference Guide

## 🚀 Quick Start

### For Developers: Accepting a Booking

```java
@Autowired
private BookingConcurrencyService bookingService;

@PostMapping("/bookings/{id}/accept")
public ResponseEntity<?> acceptBooking(@PathVariable Long id, @AuthUser Teacher teacher) {
    try {
        // Use this method - it handles everything automatically
        Booking booking = bookingService.acceptBookingWithLock(id, teacher.getId());
        return ResponseEntity.ok(booking);
        
    } catch (BookingAlreadyAcceptedException e) {
        return ResponseEntity.status(409).body("Booking already taken");
        
    } catch (TeacherTimeConflictException e) {
        return ResponseEntity.status(409).body("Time conflict with your schedule");
    }
}
```

### For Frontend: Handling Booking Acceptance

```typescript
// WebSocket listener for real-time updates
stompClient.subscribe('/topic/teachers', (message) => {
    const data = JSON.parse(message.body);
    
    if (data.type === 'booking.requested') {
        // Show new booking to teacher
        displayBookingRequest(data);
        
    } else if (data.type === 'booking.taken') {
        // Remove booking from UI (another teacher accepted)
        removeBookingFromUI(data.bookingId);
    }
});

// Accept booking
async function acceptBooking(bookingId: number) {
    try {
        const response = await fetch(`/api/teacher/bookings/${bookingId}/accept`, {
            method: 'POST'
        });
        
        if (response.status === 200) {
            showSuccess("Booking accepted!");
        } else if (response.status === 409) {
            showError("Sorry, another teacher accepted this booking first");
        }
        
    } catch (error) {
        showError("Failed to accept booking");
    }
}
```

## 📋 Key Concepts

### Race Condition Prevention

**Problem**: Multiple teachers click "Accept" simultaneously
```
Teacher A: Click Accept → Server processes → SUCCESS ✅
Teacher B: Click Accept → Server processes → CONFLICT ❌
Teacher C: Click Accept → Server processes → CONFLICT ❌
```

**Solution**: First-accept-wins with atomic operations
- Database-level row locking (SELECT FOR UPDATE)
- Optimistic locking with version field
- Distributed application lock
- Atomic UPDATE query with state check

### Three Booking Acceptance Methods

#### Method 1: Pessimistic Locking (Recommended)
```java
bookingService.acceptBookingWithLock(bookingId, teacherId)
```
- **Pros**: Most reliable, prevents all race conditions
- **Cons**: Slightly slower (database lock)
- **Use When**: High concurrency expected (multiple teachers)

#### Method 2: Optimistic Locking with Retry
```java
bookingService.acceptBookingWithOptimisticLock(bookingId, teacherId)
```
- **Pros**: Fast for low contention
- **Cons**: May fail and retry
- **Use When**: Low concurrency scenarios

#### Method 3: Atomic Query (Highest Performance)
```java
boolean success = bookingService.acceptBookingAtomic(bookingId, teacherId)
```
- **Pros**: Fastest, single SQL query
- **Cons**: Less detailed error handling
- **Use When**: Performance critical, simple accept/reject logic

## 🔒 Concurrency Layers Explained

### Layer 1: Database Optimistic Lock
```java
@Version
private Long version;
```
- JPA auto-increments on each update
- Throws exception if version mismatch
- Good for detecting conflicts

### Layer 2: Database Pessimistic Lock
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<Booking> findByIdWithLock(Long id);
```
- Generates: `SELECT ... FOR UPDATE`
- Holds database row lock
- Prevents concurrent reads/writes

### Layer 3: Application Distributed Lock
```java
lockService.executeWithLock("booking:" + id, 10, () -> {
    // Only one application instance can execute this
});
```
- Prevents multiple app instances
- Current: In-memory (single instance)
- Production: Redis/Redisson (multi-instance)

### Layer 4: Atomic Database Query
```sql
UPDATE booking 
SET state = 'ACCEPTED', teacher_id = ?
WHERE id = ? AND state = 'REQUESTED' AND teacher_id IS NULL
```
- Single atomic operation
- Database handles concurrency
- Compare-and-swap pattern

## ⏰ Booking Expiration

### How It Works
1. Student creates booking → State: `REQUESTED`
2. WebSocket broadcasts to eligible teachers
3. Teachers have 10 minutes to accept
4. If no teacher accepts → Auto-expires
5. Student notified via WebSocket

### Configuration
```properties
# application.properties
booking.expiration.minutes=10
```

### Manual Expiration Check
```java
@Autowired
private BookingExpirationService expirationService;

// Check if booking expired
boolean expired = expirationService.isBookingExpired(booking);

// Get remaining time
long seconds = expirationService.getRemainingSecondsUntilExpiration(booking);
```

## 📊 Monitoring Queries

### Check for Double Bookings (Should be 0)
```sql
SELECT booking_id, COUNT(*) as teacher_count
FROM booking 
WHERE status = 'ACCEPTED'
GROUP BY booking_id
HAVING COUNT(*) > 1;
```

### Booking Metrics (Last 7 Days)
```sql
SELECT * FROM get_booking_acceptance_metrics(7);
```

### Current Pending Bookings
```sql
SELECT id, student_id, created_at, 
       NOW() - created_at as pending_duration
FROM booking 
WHERE state = 'REQUESTED' AND teacher_id IS NULL
ORDER BY created_at DESC;
```

### Teacher Time Conflicts (Should be 0)
```sql
SELECT * FROM booking_conflicts;
```

## 🐛 Troubleshooting

### Issue: Booking Accepted by Multiple Teachers

**Diagnosis**:
```sql
-- Find double bookings
SELECT * FROM booking WHERE id IN (
    SELECT booking_id FROM booking 
    WHERE status = 'ACCEPTED'
    GROUP BY booking_id HAVING COUNT(*) > 1
);
```

**Fix**:
1. Check `version` column exists: `SELECT version FROM booking LIMIT 1;`
2. Verify pessimistic locking enabled
3. Check transaction isolation level

### Issue: High Lock Contention

**Symptoms**: Slow booking acceptance, timeout errors

**Diagnosis**:
```sql
-- Check active locks
SELECT * FROM pg_locks WHERE relation::regclass::text = 'booking';
```

**Fix**:
1. Switch to atomic query method: `acceptBookingAtomic()`
2. Increase lock timeout: `booking.lock.timeout.seconds=30`
3. Add more database connections

### Issue: Booking Never Expires

**Diagnosis**:
```bash
# Check scheduled task running
curl http://localhost:8080/actuator/scheduledtasks | jq '.scheduledTasks[] | select(.runnable.target | contains("expirePendingBookings"))'
```

**Fix**:
1. Verify `@EnableScheduling` on main class
2. Check application logs for errors
3. Verify `booking.expiration.minutes` property

## 📈 Performance Tips

### Database Optimization
```sql
-- Ensure indexes exist
\d booking

-- Should see these indexes:
-- idx_booking_state_created
-- idx_booking_acceptance
-- idx_booking_teacher_time
```

### Application Tuning
```properties
# Increase connection pool
spring.datasource.hikari.maximum-pool-size=50
spring.datasource.hikari.minimum-idle=10

# Optimize JPA batch size
spring.jpa.properties.hibernate.jdbc.batch_size=20
```

### Load Balancer (Multi-Instance)
```nginx
upstream booking_backend {
    server backend1:8080;
    server backend2:8080;
    server backend3:8080;
    
    # Sticky sessions for WebSocket
    ip_hash;
}
```

## 🧪 Testing

### Unit Test Example
```java
@Test
void testConcurrentBookingAcceptance() throws Exception {
    int teacherCount = 10;
    ExecutorService executor = Executors.newFixedThreadPool(teacherCount);
    
    List<Future<Boolean>> futures = new ArrayList<>();
    for (int i = 0; i < teacherCount; i++) {
        final Long teacherId = (long) (i + 1);
        futures.add(executor.submit(() -> {
            try {
                bookingService.acceptBookingWithLock(bookingId, teacherId);
                return true;
            } catch (Exception e) {
                return false;
            }
        }));
    }
    
    int successCount = 0;
    for (Future<Boolean> future : futures) {
        if (future.get()) successCount++;
    }
    
    assertEquals(1, successCount); // Only one should succeed
}
```

### Load Test (JMeter)
```xml
<ThreadGroup>
    <numThreads>100</numThreads>
    <rampUp>5</rampUp>
    <HTTPSamplerProxy>
        <path>/api/teacher/bookings/${bookingId}/accept</path>
        <method>POST</method>
    </HTTPSamplerProxy>
</ThreadGroup>
```

## 📞 Support Checklist

When reporting booking issues, include:
- [ ] Booking ID
- [ ] Teacher IDs involved
- [ ] Timestamp of issue
- [ ] Database query results:
  ```sql
  SELECT * FROM booking WHERE id = ?;
  SELECT * FROM pg_locks WHERE relation::regclass::text = 'booking';
  ```
- [ ] Application logs (search for `[BOOKING_ACCEPT]`)
- [ ] WebSocket connection status

## 🎯 Key Metrics SLA

| Metric | Target | Critical |
|--------|--------|----------|
| Acceptance Latency | < 500ms | < 2s |
| Acceptance Rate | > 90% | > 80% |
| Expiration Rate | < 10% | < 20% |
| Double-Booking Rate | 0% | 0% |
| Conflict Rate | < 5% | < 10% |

## 🔗 Related Documentation

- **Full Guide**: `/docs/BOOKING_SYSTEM_CONCURRENCY.md`
- **Implementation Summary**: `/docs/BOOKING_SYSTEM_IMPLEMENTATION_SUMMARY.md`
- **Database Migration**: `/backend/src/main/resources/db/migration/V2__booking_concurrency_enhancement.sql`
- **Test Suite**: `/backend/src/test/java/com/ankurshala/backend/service/BookingConcurrencyTest.java`

---

**Last Updated**: January 2024  
**Quick Reference Version**: 1.0
