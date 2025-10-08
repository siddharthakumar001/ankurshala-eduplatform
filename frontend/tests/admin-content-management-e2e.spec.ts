import { test, expect, Page } from '@playwright/test';

// Test configuration
const BASE_URL = 'http://localhost:3000';
const API_BASE_URL = 'http://localhost:8080';

// Test users
const TEST_USERS = {
  admin: {
    email: 'siddhartha@ankurshala.com',
    password: 'Maza@123',
    role: 'ADMIN'
  }
};

// Test data for content creation
const TEST_DATA = {
  board: {
    name: `Test Board ${Date.now()}`,
    active: true
  },
  grade: {
    name: `grade-${Date.now()}`,
    displayName: `Grade Test ${Date.now()}`,
    active: true
  },
  subject: {
    name: `Test Subject ${Date.now()}`,
    active: true
  },
  chapter: {
    name: `Test Chapter ${Date.now()}`,
    active: true
  },
  topic: {
    title: `Test Topic ${Date.now()}`,
    description: 'Test topic description for automated testing',
    summary: 'Test topic summary',
    expectedTimeMins: 45,
    active: true
  },
  topicNote: {
    title: `Test Note ${Date.now()}`,
    content: 'This is a test topic note content for automated testing.',
    active: true
  }
};

// Helper functions
async function loginAsAdmin(page: Page) {
  await page.goto(`${BASE_URL}/login`);
  
  // Wait for login form to be visible
  await page.waitForSelector('input[name="email"]', { timeout: 10000 });
  
  await page.fill('input[name="email"]', TEST_USERS.admin.email);
  await page.fill('input[name="password"]', TEST_USERS.admin.password);
  
  // Click submit and wait for navigation
  await page.click('button[type="submit"]');
  
  // Wait for successful login
  await Promise.race([
    page.waitForURL('**/admin/dashboard**', { timeout: 15000 }),
    page.waitForURL('**/admin/**', { timeout: 15000 }),
    page.waitForTimeout(10000)
  ]);
  
  // Additional wait for auth state to settle
  await page.waitForTimeout(2000);
}

async function navigateToContentManagement(page: Page) {
  await page.goto(`${BASE_URL}/admin/content/manage`);
  
  // Wait for page to load completely
  await page.waitForSelector('text=Content Management', { timeout: 15000 });
  
  // Wait for tabs to be visible
  await page.waitForSelector('[role="tablist"]', { timeout: 10000 });
}

async function waitForContentLoad(page: Page, contentType: string) {
  // Wait for loading states to complete
  const loadingSelectors = [
    `text=Loading ${contentType.toLowerCase()}...`,
    `text=Loading...`,
    '[data-testid="loading"]'
  ];
  
  for (const selector of loadingSelectors) {
    try {
      await page.waitForSelector(selector, { state: 'hidden', timeout: 5000 });
    } catch (e) {
      // Ignore if selector doesn't exist
    }
  }
  
  // Wait for content to be visible
  await page.waitForTimeout(1000);
}

async function openCreateDialog(page: Page, contentType: string) {
  const buttonText = `Add ${contentType}`;
  await page.getByRole('button', { name: buttonText }).click();
  
  // Wait for dialog to open
  await page.waitForSelector('[role="dialog"]', { timeout: 5000 });
}

async function fillCreateForm(page: Page, contentType: string, data: any, parentId?: number) {
  // Fill common fields - check if it's an edit form
  const isEditForm = await page.locator('input[id="edit-name"]').isVisible();
  
  if (data.name) {
    const fieldId = isEditForm ? 'edit-name' : 'name';
    await page.fill(`input[id="${fieldId}"]`, data.name);
  }
  if (data.title) {
    const fieldId = isEditForm ? 'edit-title' : 'title';
    await page.fill(`input[id="${fieldId}"]`, data.title);
  }
  if (data.displayName) {
    const fieldId = isEditForm ? 'edit-displayName' : 'displayName';
    await page.fill(`input[id="${fieldId}"]`, data.displayName);
  }
  if (data.description) {
    const fieldId = isEditForm ? 'edit-description' : 'description';
    await page.fill(`textarea[id="${fieldId}"]`, data.description);
  }
  if (data.summary) {
    const fieldId = isEditForm ? 'edit-summary' : 'summary';
    await page.fill(`textarea[id="${fieldId}"]`, data.summary);
  }
  if (data.content) {
    const fieldId = isEditForm ? 'edit-content' : 'content';
    await page.fill(`textarea[id="${fieldId}"]`, data.content);
  }
  if (data.expectedTimeMins) {
    const fieldId = isEditForm ? 'edit-expectedTimeMins' : 'expectedTimeMins';
    await page.fill(`input[id="${fieldId}"]`, data.expectedTimeMins.toString());
  }
  
  // Fill parent relationship fields
  if (parentId && contentType !== 'Board') {
    const parentFieldMap = {
      'Grade': 'boardId',
      'Subject': 'gradeId',
      'Chapter': 'subjectId',
      'Topic': 'chapterId',
      'Topic Note': 'topicId'
    };
    
    const fieldName = parentFieldMap[contentType];
    if (fieldName) {
      await page.selectOption(`select[name="${fieldName}"]`, parentId.toString());
    }
  }
  
  // Handle active status
  if (data.active !== undefined) {
    // Try clicking the label first, then the switch
    const labelId = isEditForm ? 'edit-active' : 'active';
    const label = page.locator(`label[for="${labelId}"]`);
    if (await label.isVisible()) {
      await label.click();
    } else {
      // Fallback to clicking the switch directly
      const switchElement = page.locator(`[role="switch"][id="${labelId}"]`);
      await switchElement.click();
    }
  }
}

async function submitCreateForm(page: Page, contentType: string) {
  const buttonText = `Create ${contentType}`;
  await page.getByRole('button', { name: buttonText }).click();
  
  // Wait for form submission and API response
  await page.waitForTimeout(3000);
  
  // Wait for dialog to close
  await page.waitForSelector('[role="dialog"]', { state: 'hidden', timeout: 10000 });
  
  // Additional wait for data to refresh
  await page.waitForTimeout(2000);
}

async function createContentItem(page: Page, contentType: string, data: any, parentId?: number) {
  await openCreateDialog(page, contentType);
  await fillCreateForm(page, contentType, data, parentId);
  await submitCreateForm(page, contentType);
}

async function searchContent(page: Page, searchTerm: string) {
  const searchInput = page.locator('input[placeholder*="Search"]').first();
  await searchInput.fill(searchTerm);
  await searchInput.press('Enter');
  
  // Wait for search results
  await page.waitForTimeout(1000);
}

async function filterByStatus(page: Page, status: 'all' | 'active' | 'inactive') {
  // Click on the status filter select trigger
  const statusSelectTrigger = page.locator('[role="combobox"]').first();
  await statusSelectTrigger.click();
  
  // Wait for dropdown to open and select the option
  await page.waitForTimeout(500);
  await page.locator(`text=${status === 'all' ? 'All Status' : status === 'active' ? 'Active' : 'Inactive'}`).click();
  
  // Wait for filter to apply
  await page.waitForTimeout(1000);
}

async function deleteContentItem(page: Page, contentType: string, itemName: string) {
  // Set up dialog handler for native confirm dialogs
  page.on('dialog', async dialog => {
    console.log('Delete dialog message:', dialog.message());
    await dialog.accept();
  });
  
  // Find the delete button using aria-label
  const deleteButton = page.getByRole('button', { name: `Delete ${itemName}` });
  await deleteButton.click();
  
  // Wait for deletion to complete
  await page.waitForTimeout(2000);
}

async function editContentItem(page: Page, contentType: string, itemName: string, newData: any) {
  // Find the edit button using aria-label
  const editButton = page.getByRole('button', { name: `Edit ${itemName}` });
  await editButton.click();
  
  // Wait for edit dialog to open
  await page.waitForSelector('[role="dialog"]', { timeout: 5000 });
  
  // Fill updated data
  await fillCreateForm(page, contentType, newData);
  
  // Submit update
  const updateButtonText = `Update ${contentType}`;
  await page.getByRole('button', { name: updateButtonText }).click();
  
  // Wait for update to complete
  await page.waitForTimeout(2000);
  
  // Wait for dialog to close
  await page.waitForSelector('[role="dialog"]', { state: 'hidden', timeout: 10000 });
}

// Test suite for Admin Content Management
test.describe('Admin Content Management E2E Tests', () => {
  let page: Page;
  let createdBoardId: number;
  let createdGradeId: number;
  let createdSubjectId: number;
  let createdChapterId: number;
  let createdTopicId: number;
  let createdTopicNoteId: number;

  test.beforeEach(async ({ browser }) => {
    page = await browser.newPage();
    await loginAsAdmin(page);
    await navigateToContentManagement(page);
  });

  test.afterEach(async () => {
    await page.close();
  });

  // =========================== BOARD TESTS ===========================

  test('should create a new board successfully', async () => {
    // Ensure we're logged in and navigate properly
    await page.goto('http://localhost:3000/login');
    await page.fill('input[name="email"]', 'siddhartha@ankurshala.com');
    await page.fill('input[name="password"]', 'Maza@123');
    await page.click('button[type="submit"]');
    
    // Wait for login
    await page.waitForURL('**/admin/**');
    await page.waitForTimeout(2000);
    
    // Navigate to content management
    await page.goto('http://localhost:3000/admin/content/manage');
    await page.waitForSelector('text=Content Management');
    
    await page.click('text=Boards');
    await page.waitForTimeout(2000);

    // Create board with a unique name
    const uniqueBoardName = `Test Board ${Date.now()}`;
    
    // Click Add Board button
    await page.getByRole('button', { name: 'Add Board' }).click();
    await page.waitForSelector('[role="dialog"]');
    
    // Fill form
    await page.fill('input[id="name"]', uniqueBoardName);
    
    // Submit form
    await page.getByRole('button', { name: 'Create Board' }).click();
    
    // Wait for dialog to close
    await page.waitForSelector('[role="dialog"]', { state: 'hidden', timeout: 10000 });
    await page.waitForTimeout(3000);

    // Verify board was created by checking if it appears in the list
    await expect(page.locator('h3').filter({ hasText: uniqueBoardName })).toBeVisible({ timeout: 10000 });
  });

  test('should edit board successfully', async () => {
    await page.click('text=Boards');
    await waitForContentLoad(page, 'boards');

    // Create a board first
    const uniqueBoardName = `Test Board ${Date.now()}`;
    const boardData = { ...TEST_DATA.board, name: uniqueBoardName };
    
    await createContentItem(page, 'Board', boardData);

    // Search for the created board
    const searchInput = page.locator('input[placeholder*="Search"]').first();
    await searchInput.fill(uniqueBoardName);
    await searchInput.press('Enter');
    await page.waitForTimeout(2000);

    const updatedData = { ...boardData, name: uniqueBoardName + ' Updated' };
    await editContentItem(page, 'Board', uniqueBoardName, updatedData);

    // Verify board was updated by searching again
    await searchInput.fill(updatedData.name);
    await searchInput.press('Enter');
    await page.waitForTimeout(2000);
    
    await expect(page.locator(`text=${updatedData.name}`)).toBeVisible({ timeout: 10000 });
  });

  test('should search boards successfully', async () => {
    await page.click('text=Boards');
    await waitForContentLoad(page, 'boards');

    await searchContent(page, TEST_DATA.board.name);

    // Verify search results
    await expect(page.locator('text=' + TEST_DATA.board.name)).toBeVisible();
  });

  test('should filter boards by status', async () => {
    await page.click('text=Boards');
    await waitForContentLoad(page, 'boards');

    await filterByStatus(page, 'active');

    // Verify only active boards are shown
    const inactiveBoards = page.locator('text=Inactive');
    await expect(inactiveBoards).not.toBeVisible();
  });

  test('should delete board successfully', async () => {
    await page.click('text=Boards');
    await waitForContentLoad(page, 'boards');

    // Create a board first
    const uniqueBoardName = `Test Board ${Date.now()}`;
    const boardData = { ...TEST_DATA.board, name: uniqueBoardName };
    
    await createContentItem(page, 'Board', boardData);

    // Search for the created board
    const searchInput = page.locator('input[placeholder*="Search"]').first();
    await searchInput.fill(uniqueBoardName);
    await searchInput.press('Enter');
    await page.waitForTimeout(2000);

    await deleteContentItem(page, 'Board', uniqueBoardName);

    // Verify board was deleted
    await expect(page.locator(`text=${uniqueBoardName}`)).not.toBeVisible();
  });

  // =========================== GRADE TESTS ===========================

  test('should create a new grade successfully', async () => {
    // First create a board for the grade
    await page.click('text=Boards');
    await waitForContentLoad(page, 'boards');
    await createContentItem(page, 'Board', TEST_DATA.board);

    // Get the board ID (we'll need to extract this from the UI)
    const boardRow = page.locator('tr').filter({ hasText: TEST_DATA.board.name });
    const boardId = await boardRow.getAttribute('data-id') || '1'; // Fallback to 1

    // Now create a grade
    await page.click('text=Grades');
    await waitForContentLoad(page, 'grades');

    await createContentItem(page, 'Grade', TEST_DATA.grade, parseInt(boardId));

    // Verify grade was created
    await expect(page.locator('text=' + TEST_DATA.grade.displayName)).toBeVisible();
  });

  test('should edit grade successfully', async () => {
    await page.click('text=Grades');
    await waitForContentLoad(page, 'grades');

    const updatedData = { ...TEST_DATA.grade, displayName: TEST_DATA.grade.displayName + ' Updated' };
    await editContentItem(page, 'Grade', TEST_DATA.grade.displayName, updatedData);

    // Verify grade was updated
    await expect(page.locator('text=' + updatedData.displayName)).toBeVisible();
  });

  test('should search grades successfully', async () => {
    await page.click('text=Grades');
    await waitForContentLoad(page, 'grades');

    await searchContent(page, TEST_DATA.grade.displayName);

    // Verify search results
    await expect(page.locator('text=' + TEST_DATA.grade.displayName)).toBeVisible();
  });

  test('should filter grades by status', async () => {
    await page.click('text=Grades');
    await waitForContentLoad(page, 'grades');

    await filterByStatus(page, 'active');

    // Verify only active grades are shown
    const inactiveGrades = page.locator('text=Inactive');
    await expect(inactiveGrades).not.toBeVisible();
  });

  test('should delete grade successfully', async () => {
    await page.click('text=Grades');
    await waitForContentLoad(page, 'grades');

    await deleteContentItem(page, 'Grade', TEST_DATA.grade.displayName);

    // Verify grade was deleted
    await expect(page.locator('text=' + TEST_DATA.grade.displayName)).not.toBeVisible();
  });

  // =========================== SUBJECT TESTS ===========================

  test('should create a new subject successfully', async () => {
    // First create a board and grade
    await page.click('text=Boards');
    await waitForContentLoad(page, 'boards');
    await createContentItem(page, 'Board', TEST_DATA.board);

    await page.click('text=Grades');
    await waitForContentLoad(page, 'grades');
    await createContentItem(page, 'Grade', TEST_DATA.grade, 1);

    // Now create a subject
    await page.click('text=Subjects');
    await waitForContentLoad(page, 'subjects');

    await createContentItem(page, 'Subject', TEST_DATA.subject, 1);

    // Verify subject was created
    await expect(page.locator('text=' + TEST_DATA.subject.name)).toBeVisible();
  });

  test('should edit subject successfully', async () => {
    await page.click('text=Subjects');
    await waitForContentLoad(page, 'subjects');

    const updatedData = { ...TEST_DATA.subject, name: TEST_DATA.subject.name + ' Updated' };
    await editContentItem(page, 'Subject', TEST_DATA.subject.name, updatedData);

    // Verify subject was updated
    await expect(page.locator('text=' + updatedData.name)).toBeVisible();
  });

  test('should search subjects successfully', async () => {
    await page.click('text=Subjects');
    await waitForContentLoad(page, 'subjects');

    await searchContent(page, TEST_DATA.subject.name);

    // Verify search results
    await expect(page.locator('text=' + TEST_DATA.subject.name)).toBeVisible();
  });

  test('should filter subjects by status', async () => {
    await page.click('text=Subjects');
    await waitForContentLoad(page, 'subjects');

    await filterByStatus(page, 'active');

    // Verify only active subjects are shown
    const inactiveSubjects = page.locator('text=Inactive');
    await expect(inactiveSubjects).not.toBeVisible();
  });

  test('should delete subject successfully', async () => {
    await page.click('text=Subjects');
    await waitForContentLoad(page, 'subjects');

    await deleteContentItem(page, 'Subject', TEST_DATA.subject.name);

    // Verify subject was deleted
    await expect(page.locator('text=' + TEST_DATA.subject.name)).not.toBeVisible();
  });

  // =========================== CHAPTER TESTS ===========================

  test('should create a new chapter successfully', async () => {
    // Create prerequisite content
    await page.click('text=Boards');
    await waitForContentLoad(page, 'boards');
    await createContentItem(page, 'Board', TEST_DATA.board);

    await page.click('text=Grades');
    await waitForContentLoad(page, 'grades');
    await createContentItem(page, 'Grade', TEST_DATA.grade, 1);

    await page.click('text=Subjects');
    await waitForContentLoad(page, 'subjects');
    await createContentItem(page, 'Subject', TEST_DATA.subject, 1);

    // Now create a chapter
    await page.click('text=Chapters');
    await waitForContentLoad(page, 'chapters');

    await createContentItem(page, 'Chapter', TEST_DATA.chapter, 1);

    // Verify chapter was created
    await expect(page.locator('text=' + TEST_DATA.chapter.name)).toBeVisible();
  });

  test('should edit chapter successfully', async () => {
    await page.click('text=Chapters');
    await waitForContentLoad(page, 'chapters');

    const updatedData = { ...TEST_DATA.chapter, name: TEST_DATA.chapter.name + ' Updated' };
    await editContentItem(page, 'Chapter', TEST_DATA.chapter.name, updatedData);

    // Verify chapter was updated
    await expect(page.locator('text=' + updatedData.name)).toBeVisible();
  });

  test('should search chapters successfully', async () => {
    await page.click('text=Chapters');
    await waitForContentLoad(page, 'chapters');

    await searchContent(page, TEST_DATA.chapter.name);

    // Verify search results
    await expect(page.locator('text=' + TEST_DATA.chapter.name)).toBeVisible();
  });

  test('should filter chapters by status', async () => {
    await page.click('text=Chapters');
    await waitForContentLoad(page, 'chapters');

    await filterByStatus(page, 'active');

    // Verify only active chapters are shown
    const inactiveChapters = page.locator('text=Inactive');
    await expect(inactiveChapters).not.toBeVisible();
  });

  test('should delete chapter successfully', async () => {
    await page.click('text=Chapters');
    await waitForContentLoad(page, 'chapters');

    await deleteContentItem(page, 'Chapter', TEST_DATA.chapter.name);

    // Verify chapter was deleted
    await expect(page.locator('text=' + TEST_DATA.chapter.name)).not.toBeVisible();
  });

  // =========================== TOPIC TESTS ===========================

  test('should create a new topic successfully', async () => {
    // Create prerequisite content
    await page.click('text=Boards');
    await waitForContentLoad(page, 'boards');
    await createContentItem(page, 'Board', TEST_DATA.board);

    await page.click('text=Grades');
    await waitForContentLoad(page, 'grades');
    await createContentItem(page, 'Grade', TEST_DATA.grade, 1);

    await page.click('text=Subjects');
    await waitForContentLoad(page, 'subjects');
    await createContentItem(page, 'Subject', TEST_DATA.subject, 1);

    await page.click('text=Chapters');
    await waitForContentLoad(page, 'chapters');
    await createContentItem(page, 'Chapter', TEST_DATA.chapter, 1);

    // Now create a topic
    await page.click('text=Topics');
    await waitForContentLoad(page, 'topics');

    await createContentItem(page, 'Topic', TEST_DATA.topic, 1);

    // Verify topic was created
    await expect(page.locator('text=' + TEST_DATA.topic.title)).toBeVisible();
  });

  test('should edit topic successfully', async () => {
    await page.click('text=Topics');
    await waitForContentLoad(page, 'topics');

    const updatedData = { ...TEST_DATA.topic, title: TEST_DATA.topic.title + ' Updated' };
    await editContentItem(page, 'Topic', TEST_DATA.topic.title, updatedData);

    // Verify topic was updated
    await expect(page.locator('text=' + updatedData.title)).toBeVisible();
  });

  test('should search topics successfully', async () => {
    await page.click('text=Topics');
    await waitForContentLoad(page, 'topics');

    await searchContent(page, TEST_DATA.topic.title);

    // Verify search results
    await expect(page.locator('text=' + TEST_DATA.topic.title)).toBeVisible();
  });

  test('should filter topics by status', async () => {
    await page.click('text=Topics');
    await waitForContentLoad(page, 'topics');

    await filterByStatus(page, 'active');

    // Verify only active topics are shown
    const inactiveTopics = page.locator('text=Inactive');
    await expect(inactiveTopics).not.toBeVisible();
  });

  test('should delete topic successfully', async () => {
    await page.click('text=Topics');
    await waitForContentLoad(page, 'topics');

    await deleteContentItem(page, 'Topic', TEST_DATA.topic.title);

    // Verify topic was deleted
    await expect(page.locator('text=' + TEST_DATA.topic.title)).not.toBeVisible();
  });

  // =========================== TOPIC NOTES TESTS ===========================

  test('should create a new topic note successfully', async () => {
    // Create prerequisite content
    await page.click('text=Boards');
    await waitForContentLoad(page, 'boards');
    await createContentItem(page, 'Board', TEST_DATA.board);

    await page.click('text=Grades');
    await waitForContentLoad(page, 'grades');
    await createContentItem(page, 'Grade', TEST_DATA.grade, 1);

    await page.click('text=Subjects');
    await waitForContentLoad(page, 'subjects');
    await createContentItem(page, 'Subject', TEST_DATA.subject, 1);

    await page.click('text=Chapters');
    await waitForContentLoad(page, 'chapters');
    await createContentItem(page, 'Chapter', TEST_DATA.chapter, 1);

    await page.click('text=Topics');
    await waitForContentLoad(page, 'topics');
    await createContentItem(page, 'Topic', TEST_DATA.topic, 1);

    // Now create a topic note
    await page.click('text=Topic Notes');
    await waitForContentLoad(page, 'topicnotes');

    await createContentItem(page, 'Topic Note', TEST_DATA.topicNote, 1);

    // Verify topic note was created
    await expect(page.locator('text=' + TEST_DATA.topicNote.title)).toBeVisible();
  });

  test('should edit topic note successfully', async () => {
    await page.click('text=Topic Notes');
    await waitForContentLoad(page, 'topicnotes');

    const updatedData = { ...TEST_DATA.topicNote, title: TEST_DATA.topicNote.title + ' Updated' };
    await editContentItem(page, 'Topic Note', TEST_DATA.topicNote.title, updatedData);

    // Verify topic note was updated
    await expect(page.locator('text=' + updatedData.title)).toBeVisible();
  });

  test('should search topic notes successfully', async () => {
    await page.click('text=Topic Notes');
    await waitForContentLoad(page, 'topicnotes');

    await searchContent(page, TEST_DATA.topicNote.title);

    // Verify search results
    await expect(page.locator('text=' + TEST_DATA.topicNote.title)).toBeVisible();
  });

  test('should filter topic notes by status', async () => {
    await page.click('text=Topic Notes');
    await waitForContentLoad(page, 'topicnotes');

    await filterByStatus(page, 'active');

    // Verify only active topic notes are shown
    const inactiveNotes = page.locator('text=Inactive');
    await expect(inactiveNotes).not.toBeVisible();
  });

  test('should delete topic note successfully', async () => {
    await page.click('text=Topic Notes');
    await waitForContentLoad(page, 'topicnotes');

    await deleteContentItem(page, 'Topic Note', TEST_DATA.topicNote.title);

    // Verify topic note was deleted
    await expect(page.locator('text=' + TEST_DATA.topicNote.title)).not.toBeVisible();
  });

  // =========================== CASCADING DELETE TESTS ===========================

  test('should handle cascading soft delete for boards', async () => {
    // Create a complete hierarchy
    await page.click('text=Boards');
    await waitForContentLoad(page, 'boards');
    await createContentItem(page, 'Board', TEST_DATA.board);

    await page.click('text=Grades');
    await waitForContentLoad(page, 'grades');
    await createContentItem(page, 'Grade', TEST_DATA.grade, 1);

    await page.click('text=Subjects');
    await waitForContentLoad(page, 'subjects');
    await createContentItem(page, 'Subject', TEST_DATA.subject, 1);

    await page.click('text=Chapters');
    await waitForContentLoad(page, 'chapters');
    await createContentItem(page, 'Chapter', TEST_DATA.chapter, 1);

    await page.click('text=Topics');
    await waitForContentLoad(page, 'topics');
    await createContentItem(page, 'Topic', TEST_DATA.topic, 1);

    await page.click('text=Topic Notes');
    await waitForContentLoad(page, 'topicnotes');
    await createContentItem(page, 'Topic Note', TEST_DATA.topicNote, 1);

    // Delete the board (should cascade to all children)
    await page.click('text=Boards');
    await waitForContentLoad(page, 'boards');
    await deleteContentItem(page, 'Board', TEST_DATA.board.name);

    // Verify cascading delete - all related content should be soft deleted
    await page.click('text=Grades');
    await waitForContentLoad(page, 'grades');
    await expect(page.locator('text=' + TEST_DATA.grade.displayName)).not.toBeVisible();

    await page.click('text=Subjects');
    await waitForContentLoad(page, 'subjects');
    await expect(page.locator('text=' + TEST_DATA.subject.name)).not.toBeVisible();

    await page.click('text=Chapters');
    await waitForContentLoad(page, 'chapters');
    await expect(page.locator('text=' + TEST_DATA.chapter.name)).not.toBeVisible();

    await page.click('text=Topics');
    await waitForContentLoad(page, 'topics');
    await expect(page.locator('text=' + TEST_DATA.topic.title)).not.toBeVisible();

    await page.click('text=Topic Notes');
    await waitForContentLoad(page, 'topicnotes');
    await expect(page.locator('text=' + TEST_DATA.topicNote.title)).not.toBeVisible();
  });

  // =========================== ERROR HANDLING TESTS ===========================

  test('should display error message for invalid board creation', async () => {
    await page.click('text=Boards');
    await waitForContentLoad(page, 'boards');

    await openCreateDialog(page, 'Board');
    
    // Try to submit without filling required fields - button should be disabled
    const createButton = page.getByRole('button', { name: 'Create Board' });
    await expect(createButton).toBeDisabled();
    
    // Fill the name field to enable the button
    await page.fill('input[id="name"]', 'Test Board');
    
    // Now the button should be enabled
    await expect(createButton).toBeEnabled();
  });

  test('should display error message for invalid grade creation', async () => {
    await page.click('text=Grades');
    await waitForContentLoad(page, 'grades');

    await openCreateDialog(page, 'Grade');
    
    // Try to submit without filling required fields
    await page.getByRole('button', { name: 'Create Grade' }).click();

    // Verify error message is displayed
    await expect(page.locator('text=Name is required')).toBeVisible();
  });

  test('should display error message for invalid subject creation', async () => {
    await page.click('text=Subjects');
    await waitForContentLoad(page, 'subjects');

    await openCreateDialog(page, 'Subject');
    
    // Try to submit without filling required fields
    await page.getByRole('button', { name: 'Create Subject' }).click();

    // Verify error message is displayed
    await expect(page.locator('text=Name is required')).toBeVisible();
  });

  test('should display error message for invalid chapter creation', async () => {
    await page.click('text=Chapters');
    await waitForContentLoad(page, 'chapters');

    await openCreateDialog(page, 'Chapter');
    
    // Try to submit without filling required fields
    await page.getByRole('button', { name: 'Create Chapter' }).click();

    // Verify error message is displayed
    await expect(page.locator('text=Name is required')).toBeVisible();
  });

  test('should display error message for invalid topic creation', async () => {
    await page.click('text=Topics');
    await waitForContentLoad(page, 'topics');

    await openCreateDialog(page, 'Topic');
    
    // Try to submit without filling required fields
    await page.getByRole('button', { name: 'Create Topic' }).click();

    // Verify error message is displayed
    await expect(page.locator('text=Title is required')).toBeVisible();
  });

  test('should display error message for invalid topic note creation', async () => {
    await page.click('text=Topic Notes');
    await waitForContentLoad(page, 'topicnotes');

    await openCreateDialog(page, 'Topic Note');
    
    // Try to submit without filling required fields
    await page.getByRole('button', { name: 'Create Topic Note' }).click();

    // Verify error message is displayed
    await expect(page.locator('text=Title is required')).toBeVisible();
  });

  // =========================== INTEGRATION TESTS ===========================

  test('should handle pagination correctly', async () => {
    await page.click('text=Boards');
    await waitForContentLoad(page, 'boards');

    // Create multiple boards to test pagination
    for (let i = 0; i < 15; i++) {
      await createContentItem(page, 'Board', { ...TEST_DATA.board, name: `Test Board ${i} ${Date.now()}` });
    }

    // Check if pagination controls are visible
    await expect(page.locator('button:has-text("Next")')).toBeVisible();
    
    // Test pagination
    await page.click('button:has-text("Next")');
    await page.waitForTimeout(1000);
    
    // Verify we're on the next page
    await expect(page.locator('button:has-text("Previous")')).toBeVisible();
  });

  test('should handle sorting correctly', async () => {
    await page.click('text=Boards');
    await waitForContentLoad(page, 'boards');

    // Test sorting by name
    await page.click('th:has-text("Name")');
    await page.waitForTimeout(1000);

    // Verify sorting indicator is visible
    await expect(page.locator('th:has-text("Name") [data-sort]')).toBeVisible();
  });

  test('should handle bulk operations correctly', async () => {
    await page.click('text=Boards');
    await waitForContentLoad(page, 'boards');

    // Create multiple boards
    const boardNames = [];
    for (let i = 0; i < 5; i++) {
      const boardName = `Bulk Test Board ${i} ${Date.now()}`;
      boardNames.push(boardName);
      await createContentItem(page, 'Board', { ...TEST_DATA.board, name: boardName });
    }

    // Test bulk selection (if implemented)
    const selectAllCheckbox = page.locator('input[type="checkbox"][aria-label="Select all"]');
    if (await selectAllCheckbox.isVisible()) {
      await selectAllCheckbox.click();
      
      // Test bulk delete (if implemented)
      const bulkDeleteButton = page.locator('button:has-text("Delete Selected")');
      if (await bulkDeleteButton.isVisible()) {
        await bulkDeleteButton.click();
        
        // Confirm bulk delete
        await page.getByRole('button', { name: 'Delete All' }).click();
        
        // Verify all selected items are deleted
        for (const boardName of boardNames) {
          await expect(page.locator('text=' + boardName)).not.toBeVisible();
        }
      }
    }
  });

  // =========================== PERFORMANCE TESTS ===========================

  test('should load content management page within acceptable time', async () => {
    const startTime = Date.now();
    
    await navigateToContentManagement(page);
    
    const loadTime = Date.now() - startTime;
    
    // Verify page loads within 5 seconds
    expect(loadTime).toBeLessThan(5000);
    
    // Verify all tabs are visible
    await expect(page.locator('[role="tablist"]')).toBeVisible();
  });

  test('should handle large datasets efficiently', async () => {
    await page.click('text=Boards');
    await waitForContentLoad(page, 'boards');

    // Create a large number of items
    const startTime = Date.now();
    
    for (let i = 0; i < 20; i++) {
      await createContentItem(page, 'Board', { ...TEST_DATA.board, name: `Performance Test Board ${i} ${Date.now()}` });
    }
    
    const creationTime = Date.now() - startTime;
    
    // Verify creation time is reasonable (less than 30 seconds for 20 items)
    expect(creationTime).toBeLessThan(30000);
    
    // Verify search works efficiently with large dataset
    await searchContent(page, 'Performance Test');
    await expect(page.locator('text=Performance Test Board')).toBeVisible();
  });
});
