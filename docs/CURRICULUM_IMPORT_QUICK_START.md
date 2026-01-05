# Enhanced Curriculum Import - Quick Start Guide

## 🚀 Quick Upload

### Step 1: Prepare Your XLSX File

Your XLSX file should have these columns:

| Board | Grade | Subject | Chapter | Topics | Related Topics |
|-------|-------|---------|---------|--------|----------------|
| CBSE | 7 | Mathematics | Number Systems | Whole numbers, integers, rational numbers | Class 6 Maths - Arithmetic |
| CBSE | 7 | Science | Plant Nutrition | Photosynthesis, heterotrophs, food chains | Class 6 Science - Living organisms |

**Key Points:**
- ✅ First row = Headers
- ✅ Topics can be comma-separated
- ✅ Related Topics are optional
- ✅ Supports multiple languages

### Step 2: Upload via API

```bash
# Using curl
curl -X POST http://localhost:8080/admin/content/import/curriculum \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -F "file=@your-curriculum.xlsx"
  
# With dry-run (validation only)
curl -X POST http://localhost:8080/admin/content/import/curriculum \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -F "file=@your-curriculum.xlsx" \
  -F "dryRun=true"
```

### Step 3: Check Status

```bash
# Get job status (use jobId from upload response)
curl http://localhost:8080/admin/content/import/jobs/123 \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

## 📊 What Happens Behind the Scenes

```
XLSX File Upload
     ↓
Parse & Validate
     ↓
Create/Find Board (CBSE)
     ↓
Create/Find Grade (7)
     ↓
Create/Find Subject (Mathematics)
     ↓
Create/Find Chapter (Number Systems)
     ↓
Split Topics → Create Multiple Topic Entries
     ├─→ "Whole numbers" → Topic (Code: MATH_NUM_WHO_1)
     ├─→ "integers" → Topic (Code: MATH_NUM_INT_1)
     └─→ "rational numbers" → Topic (Code: MATH_NUM_RAT_1)
```

## 💡 Example: Before & After

### Before (Old System)
❌ **Verbose and Repetitive**
```
Row 1: GRADE_9, Chemistry, Matter, Physical nature, Description..., Summary...
Row 2: GRADE_9, Chemistry, Matter, States of matter, Description..., Summary...
Row 3: GRADE_9, Chemistry, Matter, Interconversion, Description..., Summary...
```
3 rows for 3 topics in same chapter!

### After (New System)
✅ **Concise and Structured**
```
Row 1: CBSE, 9, Chemistry, Matter, "Physical nature, States of matter, Interconversion", "Class 8 - Particle theory"
```
1 row creates 3 topics with relationships!

## 🎯 Real Example with Sample File

Using the provided `CBSE Bihar Board Curriculum.xlsx`:

**Input Row:**
```
Board: CBSE
Grade: 7
Subject: Mathematics
Chapter: Number Systems
Topics: "Whole numbers, integers, rational numbers, operations with rationals"
Related Topics: "Class 6 Maths - Arithmetic operations; Class 8 Maths - Powers and exponents"
```

**Creates:**
1. Board: `CBSE` (id: 1)
2. Grade: `7` (id: 7) under CBSE
3. Subject: `Mathematics` (id: 42) under Grade 7
4. Chapter: `Number Systems` (id: 156) under Mathematics
5. Topics (4 entries):
   - `Whole numbers` (Code: MATH_NUM_WHO_1)
   - `integers` (Code: MATH_NUM_INT_1)
   - `rational numbers` (Code: MATH_NUM_RAT_1)
   - `operations with rationals` (Code: MATH_NUM_OPE_1)

**Result:**
Students can now:
- Browse by Board → CBSE
- Filter by Grade → 7
- Select Subject → Mathematics
- View Chapter → Number Systems
- Learn Topics → 4 individual topics
- See Prerequisites → Links to Class 6 & 8 content

## 🔍 How to Verify Upload

### 1. Check Boards
```bash
curl http://localhost:8080/admin/content/boards \
  -H "Authorization: Bearer YOUR_TOKEN"
```

Expected: List of boards including CBSE, Bihar Board

### 2. Check Subjects for CBSE Grade 7
```bash
curl http://localhost:8080/admin/content/subjects?boardId=1&gradeId=7 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

Expected: Mathematics, Science, Social Science, English

### 3. Check Topics
```bash
curl http://localhost:8080/admin/content/topics?chapterId=156 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

Expected: All topics under "Number Systems" chapter

## 📱 Frontend Integration

### In Admin Panel
```typescript
// Upload curriculum file
const uploadCurriculum = async (file: File) => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('dryRun', 'false');
  
  const response = await fetch('/admin/content/import/curriculum', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`
    },
    body: formData
  });
  
  const result = await response.json();
  console.log('Job ID:', result.jobId);
  
  // Poll for status
  pollJobStatus(result.jobId);
};
```

### In Student Dashboard
```typescript
// Browse topics by hierarchy
const loadTopics = async () => {
  // 1. Select board
  const boards = await fetch('/admin/content/boards');
  
  // 2. Select grade
  const grades = await fetch(`/admin/content/grades?boardId=${boardId}`);
  
  // 3. Select subject
  const subjects = await fetch(`/admin/content/subjects?gradeId=${gradeId}`);
  
  // 4. Select chapter
  const chapters = await fetch(`/admin/content/chapters?subjectId=${subjectId}`);
  
  // 5. Load topics
  const topics = await fetch(`/admin/content/topics?chapterId=${chapterId}`);
};
```

## ⚠️ Common Issues & Solutions

### Issue 1: "Invalid File Type"
**Cause:** File is not .xlsx
**Solution:** Save Excel file as .xlsx (not .xls, .csv)

### Issue 2: "Missing Required Headers"
**Cause:** Column names don't match expected
**Solution:** Ensure exact column names: Board, Grade, Subject, Chapter, Topics

### Issue 3: "Topics is required"
**Cause:** Topics column is empty
**Solution:** Provide at least one topic (can be comma-separated)

### Issue 4: Job Status = "FAILED"
**Cause:** Data validation error
**Solution:** Check job.errors field for specific row errors

## 📚 Sample XLSX Templates

### Minimal Template
```
| Board | Grade | Subject | Chapter | Topics |
|-------|-------|---------|---------|--------|
| CBSE  | 7     | Math    | Ch1     | Topic1 |
```

### Full Template
```
| Board | Grade | Subject | Chapter | Topics | Related Topics |
|-------|-------|---------|---------|--------|----------------|
| CBSE  | 7     | Math    | Ch1     | T1, T2 | Class 6 Math - Ch0 |
```

### Download Sample
Use the provided file: `docs/CBSE Bihar Board Curriculum.xlsx`

## 🎓 Educational Use Cases

### Use Case 1: Curriculum Planning
Upload entire year's curriculum in one file:
- All grades (7-12)
- All subjects
- All chapters
- All topics

### Use Case 2: Cross-Grade Linkage
Map prerequisites:
```
Grade 7: Number Systems → Related to Class 6 Arithmetic
Grade 8: Powers → Related to Class 7 Number Systems
Grade 9: Exponents → Related to Class 8 Powers
```

### Use Case 3: Multi-Board Support
Single file with multiple boards:
- CBSE curriculum (English)
- Bihar Board curriculum (Hindi)
- State Board curriculum (Regional language)

## ✅ Checklist Before Upload

- [ ] File is .xlsx format
- [ ] First row has correct headers
- [ ] Board names are consistent
- [ ] Grade numbers are correct (7-12)
- [ ] Subject names are clear
- [ ] Chapter names are descriptive
- [ ] Topics are meaningful
- [ ] Topics separated by commas if multiple
- [ ] Related topics follow format: "Class X Subject - Topic"
- [ ] File size < 10MB

## 🚀 Next Steps

1. ✅ Upload your curriculum file
2. ✅ Verify data in admin panel
3. ✅ Test student browsing
4. ✅ Configure teacher expertise mapping
5. ✅ Enable topic-based bookings

## 📞 Need Help?

- 📖 Full docs: `docs/ENHANCED_CURRICULUM_IMPORT.md`
- 🧪 Test script: `scripts/test-curriculum-import.sh`
- 💬 API errors include helpful messages
- 📊 Check job status for detailed errors

---

**Happy Curriculum Building! 📚**
