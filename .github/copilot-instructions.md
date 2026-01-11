# GitHub Copilot Instructions for Ankurshala Project

## 🚨 CRITICAL DOCUMENTATION RULES

### ❌ DO NOT Create New Documentation Files
**NEVER** create new `.md` files for documentation purposes. This project already has comprehensive documentation.

### ✅ ALWAYS Update Existing Documentation
When documenting features, bug fixes, or changes:

1. **Find the appropriate existing file** in the `docs/` directory
2. **Update that file** with your changes
3. **Add a dated section** if needed (e.g., `## Update: January 9, 2026`)

## 📁 Existing Documentation Structure

### Core Documentation (docs/) - 8 Files Total

**Main Index:**
- `README.md` - Main documentation index and quick start guide

**System Documentation:**
- `ARCHITECTURE_AND_DESIGN.md` - System architecture, tech stack, database schema
- `AUTHENTICATION_AND_SECURITY.md` - Auth flow, JWT, RBAC, security best practices
- `TROUBLESHOOTING.md` - Common issues, debugging, optimization guides

**Module Documentation:**
- `STUDENT_MODULE.md` - Complete student module (dashboard, profile, booking, study list, notifications, etc.)
- `TEACHER_MODULE.md` - Complete teacher module (profile management, search, availability, qualifications)
- `BOOKING_SYSTEM.md` - Booking system with concurrency control, status flow, cancellation policy

**Specialized Documentation:**
- `ENHANCED_CURRICULUM_IMPORT.md` - Curriculum import system with automation scripts

### Project Root Documentation
- `README.md` - Project overview
- `DEPLOYMENT_STEPS.md` - Deployment steps
- `LOCAL_DEPLOYMENT_GUIDE.md` - Local setup
- `MANUAL_TESTING_GUIDE.md` - Manual testing guide

## 📝 How to Document Changes

### For New Features
1. **Identify the module**: Determine if it's student, teacher, booking, or system-wide
2. **Update the relevant file**:
   - Student features → Update `STUDENT_MODULE.md`
   - Teacher features → Update `TEACHER_MODULE.md`
   - Booking features → Update `BOOKING_SYSTEM.md`
   - Architecture changes → Update `ARCHITECTURE_AND_DESIGN.md`
3. **Add a dated section** if needed (e.g., `## Update: January 9, 2026`)

### For Bug Fixes
1. Update `TROUBLESHOOTING.md` with the issue and solution
2. Update relevant feature documentation if behavior changed

### For API Changes
1. Update the appropriate module documentation:
   - Student APIs → `STUDENT_MODULE.md` (API Endpoints section)
   - Teacher APIs → `TEACHER_MODULE.md` (API Endpoints section)
   - Booking APIs → `BOOKING_SYSTEM.md` (API Endpoints section)
2. Update `ARCHITECTURE_AND_DESIGN.md` if architectural changes were made

### For Configuration Changes
1. Update deployment sections in `ARCHITECTURE_AND_DESIGN.md`
2. Update `.env.example` files with new variables

### For Security Changes
1. Update `AUTHENTICATION_AND_SECURITY.md` with new security measures
2. Update relevant module docs if auth flow changed

## 🎯 Documentation Best Practices

### Keep It Consolidated
- One topic = One file (or section in existing file)
- No duplicate information across files
- Cross-reference other files when needed

### Use Consistent Format
```markdown
## Feature Name

### Overview
Brief description

### Implementation
How it works

### Usage
How to use it

### API Endpoints (if applicable)
Endpoint documentation

### Examples
Code examples

### Troubleshooting
Common issues and solutions
```

### Add Update Sections
When updating existing docs, add a dated section:
```markdown
## Update: January 9, 2026
- Added new feature X
- Fixed issue Y
- Updated configuration Z
```

## ⚠️ Exceptions

The ONLY cases where new files are acceptable:
1. **New major feature module** - Create ONE comprehensive doc (e.g., `NEW_FEATURE_IMPLEMENTATION.md`)
2. **Quick reference cards** - Only if approved and clearly named (e.g., `FEATURE_QUICK_REFERENCE.md`)
3. **Migration guides** - For major version upgrades

## 🔍 Before Creating Any File

Ask yourself:
1. ✅ Can this be added to an existing file?
2. ✅ Does this belong in `README.md` or `ARCHITECTURE_AND_DESIGN.md`?
3. ✅ Is there a specific feature doc I should update?
4. ✅ Can this be added as a section rather than a new file?

**If you answered YES to any of these, DO NOT create a new file!**

## 🛠️ Tools & Scripts

Scripts, automation, and utility files are fine to create:
- `.ps1` PowerShell scripts
- `.py` Python scripts
- `.sh` Shell scripts
- `.yml` Configuration files
- `.json` Data files

These are NOT documentation and are encouraged.

## 📚 Summary

**Golden Rule:** When in doubt, update `docs/README.md` or `ARCHITECTURE_AND_DESIGN.md` instead of creating a new file.

This keeps our documentation:
- ✅ Organized
- ✅ Maintainable
- ✅ Easy to navigate
- ✅ Free from redundancy
