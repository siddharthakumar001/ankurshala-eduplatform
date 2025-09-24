# Stage-1 Last Mile Audit

## 🎯 **FAILURE ANALYSIS**

### **Failure #1: Student Profile Data Persistence**
- **File**: `tests/e2e.stage1.full.spec.ts:104`
- **Error**: `input[placeholder="Enter your first name"]` not found after page reload
- **Current Selector**: `input[placeholder="Enter your first name"]`
- **Expected DOM Structure**: `<input placeholder="Enter your first name" />`
- **Hypothesis**: 
  - **Auth redirect race**: After reload, user gets redirected to login, then re-login
  - **Hydration delay**: Page loads but form fields not rendered yet
  - **Loading state**: Form is in loading state and fields not visible
  - **Wrong page state**: After re-login, page doesn't navigate back to profile

### **Failure #2: Document Management Timeout**
- **File**: `tests/e2e.stage1.spec.ts:173`
- **Error**: `text=Documents` tab not found (30s timeout)
- **Current Selector**: `text=Documents`
- **Expected DOM Structure**: `<button>Documents</button>` or `<div>Documents</div>`
- **Hypothesis**:
  - **Hidden tab**: Documents tab exists but is hidden/not visible
  - **Wrong label**: Tab text is different (e.g., "Document" vs "Documents")
  - **Auth redirect**: User not properly authenticated to see tabs
  - **Tab system not loaded**: Tab component not rendered yet

## 🔧 **ROOT CAUSE ANALYSIS**

### **Common Issues**:
1. **No loading guards**: Forms render before data is loaded
2. **Brittle selectors**: Using placeholder text instead of stable test IDs
3. **Auth race conditions**: Page reloads cause auth state loss
4. **No deterministic waits**: Tests don't wait for proper page state

### **Solution Strategy**:
1. **Add stable test IDs** to all critical elements
2. **Implement loading guards** with skeleton states
3. **Use deterministic waits** for network idle and element visibility
4. **Fix auth redirect races** with proper state management

## 📋 **IMPLEMENTATION PLAN**

### **Phase 1: Stabilize Student Profile Page**
- Add `data-testid` attributes to form fields
- Implement loading skeleton with `data-testid="student-profile-loading"`
- Add network idle waits after reload
- Ensure form fields only render after data is loaded

### **Phase 2: Stabilize Tab System**
- Add `data-testid="tab-documents"` to Documents tab button
- Add `data-testid="panel-documents"` to Documents panel
- Ensure tab is always discoverable and clickable
- Add proper tab state management

### **Phase 3: Update Playwright Tests**
- Replace brittle selectors with stable test IDs
- Add proper waits for network idle and element visibility
- Use deterministic selectors for all critical elements

### **Phase 4: Verify 100% Pass Rate**
- Run full E2E suite
- Generate artifacts and traces
- Create final pass report
