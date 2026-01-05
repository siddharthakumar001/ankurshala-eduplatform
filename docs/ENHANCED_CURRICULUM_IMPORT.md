# Enhanced Curriculum Content Import System

## Overview

The enhanced curriculum import system provides a more meaningful and educationally-sound way to upload course content into the Ankurshala platform. It supports hierarchical taxonomy, topic relationships, and proper mapping of educational content.

## 📋 File Format

The system accepts **XLSX files** with the following structure:

### Required Columns

| Column Name | Description | Example |
|-------------|-------------|---------|
| **Board** | Educational board | CBSE, Bihar Board, ICSE |
| **Grade** | Class/Grade level | 7, 8, 9, 10, 11, 12 |
| **Subject** | Subject name | Mathematics, Science, Social Science |
| **Chapter** | Chapter name | Number Systems, Plant Nutrition |
| **Topics** | Comma-separated list of topics | "Whole numbers, integers, rational numbers" |

### Optional Columns

| Column Name | Description | Example |
|-------------|-------------|---------|
| **Related Topics** | Cross-references to other topics | "Class 6 Maths - Arithmetic; Class 8 Maths - Powers" |

## 🎯 Key Features

### 1. Hierarchical Taxonomy
Content is organized in a proper educational hierarchy:
```
Board (CBSE, Bihar Board)
  └── Grade (7, 8, 9, 10, 11, 12)
      └── Subject (Mathematics, Science, etc.)
          └── Chapter (Number Systems, etc.)
              └── Topics (Individual learning units)
```

### 2. Multiple Topics Per Chapter
The system intelligently handles comma-separated topics, creating individual topic entries while maintaining their relationship to the parent chapter:

**Input:**
```
Chapter: Number Systems
Topics: "Whole numbers, integers, rational numbers, operations with rationals"
```

**Output:** Creates 4 separate topic entries:
- Whole numbers
- integers  
- rational numbers
- operations with rationals

### 3. Related Topics & Prerequisites
The "Related Topics" column supports educational linkages:

**Format:** `Class/Grade [Number] [Subject] - [Topic description]`

**Examples:**
- `Class 6 Maths - Arithmetic operations`
- `Grade 8 Science - Cell structure`
- `Class 11 Physics - Newton's laws; Class 10 Maths - Trigonometry`

This enables:
- Creating learning pathways
- Identifying prerequisites
- Suggesting related content
- Building knowledge graphs

### 4. Automatic Code Generation
Each topic receives a unique code based on:
- Subject prefix (MATH, PHY, CHEM, BIO, etc.)
- Chapter prefix
- Topic prefix
- Auto-incrementing counter for duplicates

**Example:** `MATH_NUM_WHO_1` for "Whole numbers" in "Number Systems" chapter

### 5. Bilingual Support
The system handles multiple languages in the same file:
- CBSE (English)
- Bihar Board (Hindi/local language)
- Any other language in Unicode

## 🚀 API Endpoint

### Upload Curriculum File

**Endpoint:** `POST /admin/content/import/curriculum`

**Content-Type:** `multipart/form-data`

**Parameters:**
- `file`: XLSX file (required)
- `dryRun`: Boolean (optional, default: false) - If true, validates without saving

**Example Request:**
```bash
curl -X POST http://localhost:8080/admin/content/import/curriculum \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@CBSE_Bihar_Board_Curriculum.xlsx" \
  -F "dryRun=false"
```

**Example Response:**
```json
{
  "message": "Curriculum file uploaded and processing started",
  "jobId": 123,
  "status": "RUNNING",
  "fileName": "CBSE_Bihar_Board_Curriculum.xlsx",
  "fileSize": 45678,
  "dryRun": false
}
```

### Check Import Status

**Endpoint:** `GET /admin/content/import/jobs/{jobId}`

**Example Response:**
```json
{
  "id": 123,
  "fileName": "CBSE_Bihar_Board_Curriculum.xlsx",
  "status": "SUCCEEDED",
  "totalRows": 15,
  "successRows": 15,
  "errorRows": 0,
  "stats": {
    "topicsCreated": 48,
    "linksCreated": 0,
    "boards": 2
  },
  "startedAt": "2025-12-28T10:30:00",
  "completedAt": "2025-12-28T10:30:05"
}
```

## 📊 Database Structure

The content is stored in the following tables:

### 1. boards
- id
- name (CBSE, Bihar Board, etc.)
- active

### 2. grades
- id
- name (7, 8, 9, etc.)
- display_name (Grade 7, Grade 8, etc.)
- board_id
- active

### 3. subjects
- id
- name (Mathematics, Science, etc.)
- board_id
- grade_id
- active

### 4. chapters
- id
- name (Number Systems, etc.)
- subject_id
- board_id
- grade_id
- active

### 5. topics
- id
- title (Whole numbers, etc.)
- code (MATH_NUM_WHO_1)
- description
- summary
- expected_time_mins
- chapter_id
- subject_id
- board_id
- grade_id
- active

### 6. topic_links
- id
- topic_id
- type (PREREQUISITE, RELATED)
- linked_topic_id

## 💡 Benefits for Students

1. **Clear Learning Paths**: Students can navigate from basic to advanced topics
2. **Related Content Discovery**: Easily find prerequisites and related topics
3. **Structured Learning**: Content organized by board, grade, and subject
4. **Cross-References**: See how topics connect across grades and subjects
5. **Time Estimates**: Expected learning time for each topic

## 💡 Benefits for Teachers

1. **Easy Content Creation**: Simple XLSX format for bulk uploads
2. **Flexible Structure**: Support for multiple boards and languages
3. **Relationship Mapping**: Define prerequisites and related topics
4. **Batch Operations**: Upload entire curriculum at once
5. **Validation**: Dry-run mode to test before committing

## 📝 Sample XLSX File

You can use the provided sample file: [CBSE Bihar Board Curriculum.xlsx](../../docs/CBSE%20Bihar%20Board%20Curriculum.xlsx)

## 🔄 Migration from Old Format

If you have existing course content in the old format (single row per topic with all fields), you can:

1. Export existing content
2. Restructure into the new format
3. Use the new import endpoint

The old format is still supported via `/admin/content/import/csv` endpoint.

## 🛠️ Implementation Details

### Service Class
`EnhancedCurriculumImportService.java` handles:
- XLSX parsing
- Validation
- Hierarchical entity creation
- Topic code generation
- Relationship mapping

### Key Methods
- `parseXlsxFile()`: Reads and validates XLSX structure
- `processAndSaveCurriculum()`: Creates database entities
- `parseTopicsList()`: Splits comma-separated topics
- `parseRelatedTopics()`: Extracts prerequisite/related topic links
- `generateTopicCode()`: Creates unique topic codes

## 📖 Example Data Flow

**Input Row:**
```
Board: CBSE
Grade: 7
Subject: Mathematics  
Chapter: Number Systems
Topics: "Whole numbers, integers, rational numbers"
Related Topics: "Class 6 Maths - Arithmetic; Class 8 Maths - Powers"
```

**Processing:**
1. ✅ Get or create Board "CBSE"
2. ✅ Get or create Grade "7" under CBSE
3. ✅ Get or create Subject "Mathematics" under Grade 7
4. ✅ Get or create Chapter "Number Systems" under Mathematics
5. ✅ Create 3 Topics:
   - "Whole numbers" (Code: MATH_NUM_WHO_1)
   - "integers" (Code: MATH_NUM_INT_1)
   - "rational numbers" (Code: MATH_NUM_RAT_1)
6. ✅ Store related topic references for later linking

**Result:**
- 1 Board, 1 Grade, 1 Subject, 1 Chapter, 3 Topics created
- All properly linked in hierarchy
- Topic codes generated for easy reference
- Related topic hints stored for cross-referencing

## ⚠️ Important Notes

1. **Duplicate Handling**: Existing entities are reused (get-or-create pattern)
2. **Case Sensitivity**: Names are case-sensitive, normalize beforehand if needed
3. **Grade Format**: Accepts "7", "Grade 7", "GRADE_7" - all normalized to "7"
4. **Async Processing**: Large files are processed in background
5. **Error Handling**: Failed rows are logged, successful rows are still imported

## 🔍 Validation Rules

- Board name: Required, non-empty
- Grade: Required, normalized to number format
- Subject: Required, non-empty
- Chapter: Required, non-empty
- Topics: At least one topic required
- Related Topics: Optional, validated format

## 📈 Performance

- Batch inserts for efficiency
- Automatic transaction management
- Async processing for large files
- Progress tracking via import jobs

## 🎓 Educational Best Practices

The system supports:
- **Bloom's Taxonomy**: Topics can be tagged by cognitive level
- **Prerequisite Chains**: Clear learning sequences
- **Spiral Curriculum**: Topics revisited across grades
- **Cross-Subject Integration**: Math in Science, etc.
- **Localization**: Multi-language support

## 🤝 Integration Points

- **Student Dashboard**: Browse topics by board/grade/subject
- **Teacher Search**: Find teachers by topic expertise
- **Booking System**: Book sessions for specific topics
- **Progress Tracking**: Mark topics as completed
- **Study Plans**: Generate learning pathways

---

## 📞 Support

For issues or questions:
- Check import job status via API
- Review error messages in job details
- Validate XLSX format using dry-run mode
- Contact admin for bulk imports

---

*Last Updated: December 28, 2025*
