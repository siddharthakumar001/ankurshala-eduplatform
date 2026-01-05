# Curriculum Data Import Guide

This guide explains how to import curriculum data from the Excel file into the database.

## Prerequisites

1. **Backend must be running** on `http://localhost:8080` (or update the URL in scripts)
2. **Excel file** must be in the root directory: `CBSE Bihar Board Curriculum Class 7-12.xlsx`
3. **Admin credentials** for authentication

## Quick Start

### Step 1: Get Authentication Token

Run the authentication script to get a JWT token:

```powershell
.\get-token.ps1
```

This will:
- Authenticate with the backend using default admin credentials
- Save the token to `token.txt` for convenience
- Display the token so you can use it manually if needed

**Default credentials:**
- Email: `siddhartha@ankurshala.com`
- Password: `Maza@123`

You can customize these:
```powershell
.\get-token.ps1 -Email "your-email@example.com" -Password "your-password"
```

### Step 2: Import the Curriculum File

Run the import script:

```powershell
.\import-curriculum.ps1
```

This will:
- Check if the backend is running
- Read the token from `token.txt` (or use `-Token` parameter)
- Upload the Excel file to the backend
- Start the import process asynchronously
- Display the job ID for status checking

**Options:**
- `-File`: Specify a different file (default: `CBSE Bihar Board Curriculum Class 7-12.xlsx`)
- `-Token`: Provide token manually (default: reads from `token.txt`)
- `-BackendUrl`: Specify backend URL (default: `http://localhost:8080`)
- `-DryRun`: Validate file without importing (default: `$false`)

**Example with dry run:**
```powershell
.\import-curriculum.ps1 -DryRun
```

### Step 3: Check Import Status

After uploading, you'll receive a job ID. Check the status:

```powershell
.\check-job-status.ps1 -JobId 123
```

Replace `123` with your actual job ID from the upload response.

## Excel File Format

The Excel file should have the following columns:

| Column Name | Required | Description | Example |
|------------|----------|-------------|---------|
| **Board** | Yes | Educational board name | CBSE, Bihar Board |
| **Grade** | Yes | Class/Grade level | 7, 8, 9, 10, 11, 12 |
| **Subject** | Yes | Subject name | Mathematics, Science |
| **Chapter** | Yes | Chapter name | Number Systems, Plant Nutrition |
| **Topics** | Yes | Comma-separated list of topics | "Whole numbers, integers, rational numbers" |
| **Related Topics** | No | Related topic references | "Class 6 Maths - Arithmetic" |

**Important Notes:**
- First row must contain headers
- Topics can be comma-separated (multiple topics per chapter)
- Related Topics are optional
- Supports multiple languages (Unicode)

## What Happens During Import

The import process:

1. **Parses** the Excel file and validates the structure
2. **Creates/Finds** Board entities (CBSE, Bihar Board, etc.)
3. **Creates/Finds** Grade entities (7, 8, 9, etc.) for each board
4. **Creates/Finds** Subject entities (Mathematics, Science, etc.) for each grade
5. **Creates/Finds** Chapter entities for each subject
6. **Creates** Topic entities (one for each topic in the Topics column)
7. **Generates** unique topic codes (e.g., `MATH_NUM_WHO_1`)

The process is **idempotent** - running it multiple times with the same data won't create duplicates. Existing entities are reused.

## Manual API Usage

If you prefer to use curl directly:

### 1. Get Token
```bash
curl -X POST http://localhost:8080/api/auth/signin \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"siddhartha@ankurshala.com\",\"password\":\"Maza@123\"}"
```

Copy the `token` from the response.

### 2. Upload File
```bash
curl -X POST "http://localhost:8080/admin/content/import/curriculum?dryRun=false" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -F "file=@CBSE Bihar Board Curriculum Class 7-12.xlsx" \
  -F "dryRun=false"
```

### 3. Check Status
```bash
curl "http://localhost:8080/admin/content/import/jobs/JOB_ID" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## Troubleshooting

### Backend Not Running
**Error:** `Backend is not running at http://localhost:8080`

**Solution:** Start the backend first:
```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

### Authentication Failed
**Error:** `401 Unauthorized`

**Solution:** 
- Check your credentials
- Get a new token: `.\get-token.ps1`
- Make sure you're using an admin account

### File Not Found
**Error:** `File not found: CBSE Bihar Board Curriculum Class 7-12.xlsx`

**Solution:**
- Make sure the file is in the root directory
- Or specify the full path: `.\import-curriculum.ps1 -File "C:\path\to\file.xlsx"`

### Invalid File Format
**Error:** `Missing required columns: Board, Grade, Subject, Chapter, Topics`

**Solution:**
- Check that the Excel file has the correct headers in the first row
- Ensure all required columns are present
- Column names are case-sensitive

### Import Failed
**Error:** Job status shows `FAILED`

**Solution:**
- Check the error message in the job status response
- Review the Excel file for data issues
- Try a dry run first: `.\import-curriculum.ps1 -DryRun`

## Database Structure

After import, the data is stored in these tables:

- `boards` - Educational boards (CBSE, Bihar Board)
- `grades` - Grade levels (7-12)
- `subjects` - Subjects (Mathematics, Science, etc.)
- `chapters` - Chapters within subjects
- `topics` - Individual learning topics
- `topic_links` - Relationships between topics (if Related Topics column is used)

## Additional Information

- The import process runs **asynchronously** - large files may take time
- Use the job ID to check status periodically
- The process supports **dry runs** for validation before actual import
- All entities are created with `active=true` by default
- Topics get unique codes automatically generated

