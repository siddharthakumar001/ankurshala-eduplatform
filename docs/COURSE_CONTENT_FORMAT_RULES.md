# Course Content Upload Format Rules

## Overview
This guide explains the required format and validation rules for uploading curriculum content via CSV/XLSX files to the Ankurshala platform.

---

## File Format Requirements

### Supported File Types
- **CSV** (.csv) - Comma-separated values with UTF-8 encoding
- **XLSX** (.xlsx) - Microsoft Excel format

### File Structure
- **First row must be headers** (column names)
- **Data starts from row 2**
- **Empty rows are skipped**
- **Maximum 100 errors per file** (processing stops after 100 errors to prevent overload)

---

## Required Columns (Must be present)

### 1. **Board** (Required)
- **Description**: Educational board name
- **Format**: Text string
- **Validation**: Cannot be empty or null
- **Examples**: 
  - ✅ `CBSE`
  - ✅ `ICSE`
  - ✅ `State Board`
  - ✅ `IB`
  - ❌ `""` (empty)
  - ❌ `null`

---

### 2. **Grade** (Required)
- **Description**: Class/Grade level
- **Format**: Text string (will be normalized automatically)
- **Validation**: Cannot be empty or null
- **Auto-normalization**: System automatically formats grades consistently
- **Examples**: 
  - ✅ `Class 8`
  - ✅ `8`
  - ✅ `Grade 8`
  - ✅ `8th`
  - ✅ `VIII`
  - All converted to: `Class 8`
  - ❌ `""` (empty)

---

### 3. **Subject** (Required)
- **Description**: Subject name
- **Format**: Text string
- **Validation**: Cannot be empty or null
- **Examples**: 
  - ✅ `Science`
  - ✅ `Mathematics`
  - ✅ `Social Science`
  - ✅ `English`
  - ❌ `""` (empty)

---

### 4. **Chapter** (Required)
**Column name can be either "Chapter" OR "Chapters"**
- **Description**: Chapter name/title
- **Format**: Text string
- **Validation**: Cannot be empty or null
- **Examples**: 
  - ✅ `CROP PRODUCTION AND MANAGEMENT`
  - ✅ `Introduction to Algebra`
  - ✅ `The Mughal Empire`
  - ❌ `""` (empty)
- **Note**: Multiple topics can belong to the same chapter

---

### 5. **Topics** (Required)
- **Description**: Topic name(s) within the chapter
- **Format**: 
  - Single topic: `TOPIC_NAME`
  - Multiple topics (comma-separated): `TOPIC1, TOPIC2, TOPIC3`
- **Validation**: 
  - Cannot be empty or null
  - At least one topic must be provided
  - Commas are used as separators for multiple topics
- **Examples**: 
  - ✅ `AGRICULTURE AND SOIL`
  - ✅ `AGRICULTURE AND SOIL, SOWING MANURE AND FERTILIZERS`
  - ✅ `"IRRIGATION, PROTECTING FROM WEEDS, HARVESTING"` (with quotes in CSV)
  - ❌ `""` (empty)
  - ❌ `,,,` (only commas)

---

## Optional Columns (Recommended for AI Features)

### 6. **Brief Description** (Optional but Recommended)
- **Description**: Detailed explanation of topic content
- **Format**: Free text, can be lengthy
- **Validation**: Optional (can be empty)
- **Usage**: 
  - Used for AI-powered content generation
  - First 200 characters used as summary
  - Full text stored as description
- **Examples**: 
  - ✅ `Introduction to the concept of agriculture, understanding different types of crops, importance of soil preparation, various methods of soil preparation including plowing and leveling`
  - ✅ `Covers the water cycle, sources of irrigation, modern irrigation techniques like drip and sprinkler systems, importance of water conservation in agriculture`
  - ✅ `""` (empty is allowed)

---

### 7. **Duration** (Optional but Recommended)
- **Description**: Expected time to complete the topic
- **Format**: Multiple formats supported (auto-parsed)
- **Validation**: Optional (can be empty)
- **Supported Formats**:
  - Hours: `2 hrs`, `1.5 hours`, `2 hr`, `1.5 hour`
  - Minutes: `90 mins`, `45 minutes`, `120 min`, `60 minute`
  - Plain numbers: `90` (assumed to be minutes)
- **Auto-conversion**: All formats converted to minutes internally
- **Examples**: 
  - ✅ `45 mins` → 45 minutes
  - ✅ `1.5 hrs` → 90 minutes
  - ✅ `2 hours` → 120 minutes
  - ✅ `90` → 90 minutes
  - ✅ `""` (empty is allowed)
  - ❌ `BOD` (invalid format)
  - ❌ `40 TO 45 MINT` (invalid format)
  - ❌ `TBD` (invalid format)

**Recommended Format**: Use `[number] mins` or `[number] hrs` for clarity
- Best: `45 mins`, `1.5 hrs`, `90 mins`

---

### 8. **Suggested Topics** (Optional, AI Feature)
- **Description**: Related topics that could be studied next or in parallel
- **Format**: Comma-separated list of topic names
- **Validation**: Optional (can be empty)
- **Usage**: 
  - Used by AI recommendation engine
  - Helps create learning paths
  - Full-text searchable with GIN index
- **Examples**: 
  - ✅ `IRRIGATION, CROP ROTATION, ORGANIC FARMING`
  - ✅ `Photosynthesis, Respiration, Cell Structure`
  - ✅ `""` (empty is allowed)

---

### 9. **Related Topics** (Optional, Legacy Field)
- **Description**: Topics related to the current topic (used for topic relationships)
- **Format**: Comma-separated list
- **Validation**: Optional (can be empty)
- **Status**: Legacy field, **Suggested Topics** is preferred
- **Examples**: 
  - ✅ `SOWING, HARVESTING, STORAGE`
  - ✅ `""` (empty is allowed)

---

## Complete Valid Row Examples

### Example 1: Full Row with All Fields
```csv
CBSE,Class 8,Science,CROP PRODUCTION AND MANAGEMENT,"AGRICULTURE AND SOIL, SOWING","Introduction to agriculture, types of crops, soil preparation methods, sowing techniques and seed selection",45 mins,"IRRIGATION, FERTILIZERS",
```

### Example 2: Minimum Required Fields Only
```csv
CBSE,Class 8,Mathematics,Algebra,Introduction to Variables,,,,
```

### Example 3: NCERT Practice Row (Common Pattern)
```csv
CBSE,Class 8,Science,FORCE AND PRESSURE,NCERT EXAMPLES AND SOLUTIONS,"Practice problems and solutions from NCERT textbook covering all topics in this chapter",60 mins,,
```

---

## Common Data Issues and How to Fix Them

### ❌ Problem: Invalid Duration Format
```csv
CBSE,Class 8,Science,FRICTION,Types of Friction,...,BOD,...
```
**Error**: `Could not parse duration: BOD`

✅ **Fix**:
```csv
CBSE,Class 8,Science,FRICTION,Types of Friction,...,60 mins,...
```

---

### ❌ Problem: Empty Required Fields
```csv
CBSE,Class 8,,,MAGNETISM,...
```
**Error**: `Row 15: Subject is required`

✅ **Fix**:
```csv
CBSE,Class 8,Science,ELECTRICITY,MAGNETISM,...
```

---

### ❌ Problem: Incorrect Duration Format
```csv
...,40 TO 45 MINT,...
```
**Error**: `Could not parse duration: 40 TO 45 MINT`

✅ **Fix**:
```csv
...,45 mins,...
```

---

### ❌ Problem: Empty Topics Field
```csv
CBSE,Class 8,Science,LIGHT,,,...
```
**Error**: `Row 23: Topics is required`

✅ **Fix**:
```csv
CBSE,Class 8,Science,LIGHT,REFLECTION AND REFRACTION,...
```

---

## Validation Rules Summary

| Field | Required | Format | Can Be Empty | Auto-Normalized |
|-------|----------|--------|--------------|-----------------|
| Board | ✅ Yes | Text | ❌ No | ❌ No |
| Grade | ✅ Yes | Text | ❌ No | ✅ Yes |
| Subject | ✅ Yes | Text | ❌ No | ❌ No |
| Chapter | ✅ Yes | Text | ❌ No | ❌ No |
| Topics | ✅ Yes | Text/CSV | ❌ No | ❌ No |
| Brief Description | ❌ No | Text | ✅ Yes | ❌ No |
| Duration | ❌ No | Time | ✅ Yes | ✅ Yes |
| Suggested Topics | ❌ No | Text/CSV | ✅ Yes | ❌ No |
| Related Topics | ❌ No | Text/CSV | ✅ Yes | ❌ No |

---

## Best Practices

### ✅ DO:
1. **Use consistent naming**: Keep Board, Grade, Subject names consistent across rows
2. **Provide descriptions**: Even if optional, descriptions improve AI features
3. **Use standard duration formats**: `45 mins` or `1.5 hrs`
4. **Group related topics**: Multiple topics in same chapter go together
5. **Add NCERT practice rows**: Include practice content for each chapter
6. **Use UTF-8 encoding**: Especially for CSV files
7. **Quote comma-containing fields**: In CSV, use quotes for fields with commas
8. **Test with small batch first**: Upload 5-10 rows first to verify format

### ❌ DON'T:
1. **Don't use abbreviations**: Avoid `BOD`, `TBD`, `N/A` for duration
2. **Don't leave required fields empty**: System will reject the row
3. **Don't mix formats**: Use consistent date/time formats
4. **Don't use special characters**: Avoid `|`, `\`, `\t` as separators
5. **Don't include HTML/markup**: Plain text only
6. **Don't exceed reasonable lengths**: Keep descriptions under 5000 characters
7. **Don't duplicate topics**: Same topic in same chapter will be ignored

---

## What Happens During Import

### 1. **File Upload**
- File is uploaded via multipart form
- System validates file type (CSV/XLSX only)
- Import job is created with status `PENDING`

### 2. **Header Validation**
- System checks for all required columns
- Case-insensitive column name matching
- Accepts both "Chapter" and "Chapters"

### 3. **Row Processing**
- Each row is processed sequentially
- Errors are collected with row numbers
- Maximum 100 errors before stopping

### 4. **Data Creation**
- Board, Grade, Subject, Chapter entities are created or retrieved
- Topics are created under their chapter
- AI fields (description, summary, suggestedTopics, duration) are populated
- Topic relationships are established

### 5. **Job Completion**
- Status changes to `SUCCEEDED` or `FAILED`
- Error details are available in job response
- Admin receives detailed error messages

---

## Error Response Format

When errors occur, you'll receive detailed information:

```json
{
  "jobId": 123,
  "status": "FAILED",
  "errorMessage": "Import failed: Row 15 Error: Duration is invalid | Data: Board='CBSE', Grade='Class 8', Subject='Science', Chapter='FRICTION', Topics='Types of Friction', Duration='BOD'",
  "totalRows": 50,
  "successRows": 14,
  "errorRows": 36,
  "errors": [
    "Row 15 Error: Duration is invalid | Data: ...",
    "Row 23 Error: Topics is required | Data: ...",
    "..."
  ]
}
```

Each error includes:
- **Row number** (for easy location in file)
- **Error message** (what went wrong)
- **Field values** (actual data causing the error)

---

## Sample Template

### CSV Template
```csv
Board,Grade,Subject,Chapter,Topics,Brief Description,Duration,Suggested Topics,Related Topics
CBSE,Class 8,Science,SAMPLE CHAPTER,SAMPLE TOPIC,"This is a brief description of the topic covering key concepts",45 mins,"RELATED TOPIC 1, RELATED TOPIC 2",
```

### Download Sample File
A sample file is available at: `docs/sample-class7-physics-cbse.csv`

---

## API Endpoint

**POST** `/api/admin/content/import/curriculum`

**Content-Type**: `multipart/form-data`

**Parameters**:
- `file`: The CSV or XLSX file (required)
- `dryRun`: Boolean, if true performs validation without saving (optional, default: false)

**Authentication**: Admin role required

---

## Support

If you encounter issues:
1. Check error messages carefully - they contain the exact row and field causing problems
2. Verify all required fields are present
3. Validate duration format matches supported patterns
4. Ensure file encoding is UTF-8 (for CSV)
5. Test with small batch first (5-10 rows)

For additional help, contact the development team with:
- The import job ID
- The error message
- The problematic row(s) from your file
