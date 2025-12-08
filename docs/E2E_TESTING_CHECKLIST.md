# E2E Testing Checklist - Student Booking Flow

**Status**: Ready for Testing  
**Date**: November 27, 2025  
**Prerequisites**: Backend and Frontend running, Demo data seeded

---

## Test Credentials

```
Student: student1@ankurshala.com
Password: Maza@123

Teacher IDs: Check admin panel or database
Demo teacher: teacher1@ankurshala.com (get ID from backend)
```

---

## Test 1: Discover to Booking Flow

### Steps
1. ✅ Navigate to `http://localhost:3000/student/discover`
2. ✅ Login with student credentials if not already logged in
3. ✅ Select Board (e.g., CBSE)
4. ✅ Select Grade (e.g., 9th)
5. ✅ Select Subject (e.g., Chemistry)
6. ✅ Select Chapter (e.g., Matter in Our Surroundings)
7. ✅ Select Topic from the list
8. ✅ Click "Book Class Now" button
9. ✅ Verify redirect to `/student/booking?topicId=X`
10. ✅ Verify topic name is pre-filled in booking page

### Expected Results
- [ ] All cascade dropdowns populate correctly
- [ ] Topic details display in right panel
- [ ] "Book Class Now" button is clickable
- [ ] URL contains correct topicId parameter
- [ ] Booking page shows topic name and breadcrumb path

### Console Errors
- [ ] No errors in browser console

---

## Test 2: Booking Creation Flow

### Steps (Starting from booking page with pre-filled topic)

#### Step 1: Topic Selection & Scheduling
1. ✅ Verify topic is pre-filled (from URL param)
2. ✅ Verify topic path shown (Board / Grade / Subject / Chapter)
3. ✅ Select Date: Choose tomorrow's date
4. ✅ Select Time: Choose 2:00 PM
5. ✅ Enter Teacher ID: Use a valid teacher ID from database
6. ✅ Add Notes: "Test booking for E2E testing"
7. ✅ Click "Get Quote" button

#### Step 2: Review & Quote
8. ✅ Verify quote displays with price range
9. ✅ Verify booking details shown correctly
10. ✅ Check for buffer warning (if applicable)
11. ✅ Click "Confirm Booking" button

#### Step 3: Success
12. ✅ Verify success message displayed
13. ✅ Wait for auto-redirect to calendar (2 seconds)
14. ✅ Or click "View Calendar" button manually

### Expected Results
- [ ] Date picker blocks past dates
- [ ] Time slots show 9 AM - 8 PM (30-min intervals)
- [ ] Quote returns with minPrice and maxPrice
- [ ] Buffer warning shows if booking too close to another
- [ ] Success message: "Booking Request Submitted!"
- [ ] Auto-redirect works after 2 seconds

### API Calls to Verify
- [ ] `POST /api/student/bookings/quote` - Returns quote
- [ ] `POST /api/student/bookings` - Creates booking
- [ ] Network tab shows 200 OK responses

### Console Errors
- [ ] No errors in browser console
- [ ] No failed API calls

---

## Test 3: Calendar Verification

### Steps
1. ✅ Navigate to `/student/calendar` (or auto-redirected)
2. ✅ Locate the newly created booking
3. ✅ Verify booking displays on correct date
4. ✅ Click on the booking card
5. ✅ Verify details modal opens

### Expected Results
- [ ] Booking appears in calendar view
- [ ] Status badge shows "PENDING" or "REQUESTED"
- [ ] Date and time are correct
- [ ] Topic name matches what was booked
- [ ] Teacher name is shown
- [ ] Notes are visible in details modal

### Modal Actions
- [ ] "Cancel Booking" button available
- [ ] "View Details" or "Close" button works
- [ ] Modal can be dismissed by clicking outside

---

## Test 4: History Page Pagination

### Steps
1. ✅ Wait until booking date/time passes (or manually update DB to past date)
2. ✅ Navigate to `/student/history`
3. ✅ Verify booking appears in history list
4. ✅ Check pagination controls

### Expected Results
- [ ] Past bookings display in list/card format
- [ ] Newest bookings first (sorted by startTs descending)
- [ ] Status badges show correct state
- [ ] Pagination controls visible (if >20 bookings)
- [ ] Default: 20 items per page

### Pagination Tests
- [ ] Click "Next Page" - loads page 2
- [ ] Click "Previous Page" - returns to page 1
- [ ] Change page size dropdown - updates display
- [ ] Total count matches actual number of bookings

### API Call
- [ ] `GET /api/student/bookings/history?page=0&size=20`
- [ ] Response has Page structure with metadata

---

## Test 5: Cancel Booking Flow

### Steps
1. ✅ From calendar, click on a booking
2. ✅ Click "Cancel Booking" button
3. ✅ Enter cancellation reason in dialog
4. ✅ Confirm cancellation
5. ✅ Verify status updates

### Expected Results
- [ ] Cancellation dialog appears
- [ ] Reason field is required
- [ ] Confirmation prompt shown
- [ ] Status changes to "CANCELLED"
- [ ] Booking remains in calendar but marked cancelled
- [ ] Cancelled booking appears in history

### API Call
- [ ] `POST /api/student/bookings/{id}/cancel`
- [ ] Response updates booking status

---

## Test 6: Error Handling

### Test 6a: Invalid Teacher ID
1. ✅ Enter non-existent teacher ID (e.g., 99999)
2. ✅ Try to get quote
3. ✅ Verify error toast displayed

**Expected**: Error message like "Teacher not found"

### Test 6b: Past Date (Should be blocked by date picker)
1. ✅ Try to select yesterday's date
2. ✅ Verify date picker blocks selection

**Expected**: Past dates are disabled/grayed out

### Test 6c: Buffer Violation
1. ✅ Create a booking for 2:00 PM
2. ✅ Try to create another booking for 2:10 PM (same day)
3. ✅ Verify warning toast shown

**Expected**: Warning toast but booking still allowed

### Test 6d: Network Error
1. ✅ Open DevTools → Network tab
2. ✅ Set "Offline" mode
3. ✅ Try to get quote
4. ✅ Verify error handling

**Expected**: Error toast: "Network error" or similar

---

## Test 7: Dark Mode

### Steps
1. ✅ Toggle dark mode (usually in header/settings)
2. ✅ Navigate through all pages: discover, booking, calendar, history
3. ✅ Verify all pages render correctly in dark mode

### Expected Results
- [ ] All text is readable (light text on dark backgrounds)
- [ ] Buttons and inputs have proper contrast
- [ ] Cards/modals have dark backgrounds
- [ ] No white flashes or broken styling
- [ ] All 3 steps of booking wizard look good

---

## Test 8: Study List Integration

### Steps
1. ✅ Navigate to `/student/discover`
2. ✅ Select a topic
3. ✅ Click "Add to Study List" button
4. ✅ Navigate to `/student/study-list`
5. ✅ Verify topic appears in list
6. ✅ Update status to IN_PROGRESS
7. ✅ Add notes
8. ✅ Click "Book Class" link from study list
9. ✅ Verify redirects to booking page with topicId

### Expected Results
- [ ] Topic added successfully
- [ ] Toast confirmation shown
- [ ] Topic visible in study list
- [ ] Status updates work
- [ ] Notes save correctly
- [ ] Book Class link works (pre-fills topic)

---

## Test 9: Notifications

### Steps
1. ✅ Create a booking (triggers notification)
2. ✅ Navigate to `/student/notifications`
3. ✅ Verify booking notification appears
4. ✅ Mark as read
5. ✅ Verify unread count decreases

### Expected Results
- [ ] New notification appears
- [ ] Unread badge shows correct count
- [ ] Mark as read works
- [ ] Mark all as read works
- [ ] Notifications sorted by newest first

---

## Test 10: Performance & Load Time

### Metrics to Check
- [ ] Page load: < 2 seconds
- [ ] API response: < 500ms
- [ ] No memory leaks (check DevTools Memory)
- [ ] Smooth animations and transitions
- [ ] No layout shifts (CLS)

### Tools
- Chrome DevTools → Lighthouse
- Network tab → Check timing
- Performance tab → Record page load

---

## Browser Compatibility

Test on:
- [ ] Chrome 90+ (Latest)
- [ ] Firefox 88+ (Latest)
- [ ] Safari 14+ (Latest)
- [ ] Edge 90+ (Latest)

---

## Mobile Responsiveness

Test on mobile viewport:
- [ ] Booking page responsive
- [ ] Calendar displays correctly
- [ ] History page readable
- [ ] Buttons are clickable (not too small)
- [ ] Forms are usable

### DevTools Mobile Emulation
1. Open DevTools
2. Toggle device toolbar (Cmd+Shift+M)
3. Select iPhone 12 Pro or similar
4. Test all flows

---

## Known Issues to Verify

### Issue 1: Teacher Selection
- **Current**: Manual ID input
- **Test**: Verify teacher ID input works
- **Future**: Implement teacher search/browse

### Issue 2: Buffer Warning
- **Current**: Shows warning but allows booking
- **Test**: Verify warning displays correctly
- **Consider**: Should it block booking?

### Issue 3: Notification Settings
- **Current**: UI exists but backend not connected
- **Test**: Verify settings dialog opens
- **Note**: Settings won't persist (expected)

---

## API Testing Script

Run the pagination test:
```bash
cd /Users/siddhartha/Documents/ankurshala-eduplatform
./scripts/test-pagination-api.sh
```

Expected output:
```
✅ Test 1 passed - Default pagination
✅ Test 2 passed - Custom page size
✅ Test 3 passed - Second page
✅ Test 4 passed - Response structure
✅ Test 5 passed - Sort order
```

---

## Database Verification

### Check Created Booking
```sql
SELECT id, student_id, teacher_id, topic_id, start_ts, status, created_at
FROM bookings
WHERE student_id = (SELECT id FROM users WHERE email = 'student1@ankurshala.com')
ORDER BY created_at DESC
LIMIT 5;
```

### Check Topic Details
```sql
SELECT t.id, t.name, c.name as chapter, s.name as subject, g.name as grade, b.name as board
FROM topics t
JOIN chapters c ON t.chapter_id = c.id
JOIN subjects s ON c.subject_id = s.id
JOIN grades g ON s.grade_id = g.id
JOIN boards b ON g.board_id = b.id
WHERE t.id = <topicId>;
```

---

## Smoke Test Summary

**Quick 5-minute smoke test before deployment:**

1. ✅ Login as student
2. ✅ Discover → select topic → book class
3. ✅ Booking page → get quote → confirm
4. ✅ Calendar → verify booking appears
5. ✅ History → check pagination works
6. ✅ Cancel booking → verify status updates
7. ✅ No console errors

---

## Test Results Template

### Test Session Info
- **Date**: ___________
- **Tester**: ___________
- **Environment**: Local / Staging / Production
- **Browser**: ___________
- **Test Duration**: ___________

### Summary
- **Total Tests**: 10
- **Passed**: ___
- **Failed**: ___
- **Blocked**: ___
- **Not Tested**: ___

### Issues Found
1. **Issue Description**: ___________
   - **Severity**: Critical / Major / Minor
   - **Steps to Reproduce**: ___________
   - **Expected**: ___________
   - **Actual**: ___________
   - **Screenshot**: ___________

### Sign-off
- [ ] All critical tests passed
- [ ] No blocking issues
- [ ] Ready for deployment

**Signature**: ___________  
**Date**: ___________

---

## Next Steps After E2E Testing

1. **Fix any bugs found** during testing
2. **Update documentation** with test results
3. **Create deployment plan** for staging
4. **User acceptance testing** (UAT) with real users
5. **Production deployment** checklist
6. **Monitor logs** for first 24 hours after deployment

---

**Document Version**: 1.0  
**Last Updated**: November 27, 2025  
**Next Review**: After E2E testing completion
