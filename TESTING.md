# Ankurshala Education Platform - Testing Suite

This document provides comprehensive information about the testing setup for the Ankurshala Education Platform, including Playwright E2E tests, security testing, and performance testing.

## 🚀 Quick Start

### Prerequisites
- Node.js 18+ 
- npm or yarn
- Java 17+
- Docker (for backend)
- Git

### Running Tests Locally

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd ankurshala-eduplatform
   ```

2. **Start the backend**
   ```bash
   cd backend
   docker-compose up -d
   ```

3. **Start the frontend**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

4. **Run all tests**
   ```bash
   cd frontend
   ./run-tests.sh
   ```

## 📋 Test Coverage

### Authentication Flow Tests
- ✅ Login page security and validation
- ✅ Credential sanitization and XSS prevention
- ✅ Role-based authentication and redirects
- ✅ Session management and token refresh
- ✅ Logout functionality
- ✅ Password strength validation
- ✅ Email format validation

### Admin Content Management Tests
- ✅ All content tabs (Boards, Grades, Subjects, Chapters, Topics, Topic Notes)
- ✅ CRUD operations for all entities
- ✅ Search and filter functionality
- ✅ Pagination and sorting
- ✅ Soft delete and force delete operations
- ✅ Deletion impact analysis
- ✅ Status toggle functionality

### API Integration Tests
- ✅ API endpoint testing
- ✅ Error handling and retry mechanisms
- ✅ Token refresh and authentication
- ✅ Network timeout handling
- ✅ Large dataset handling

### Performance Tests
- ✅ Page load times (< 5 seconds)
- ✅ Large dataset handling (< 10 seconds)
- ✅ Pagination efficiency
- ✅ Memory usage optimization

### Accessibility Tests
- ✅ ARIA labels and semantic HTML
- ✅ Keyboard navigation support
- ✅ Heading hierarchy
- ✅ Color contrast compliance
- ✅ Screen reader compatibility

### Cross-browser Compatibility
- ✅ Chrome/Chromium
- ✅ Firefox
- ✅ Safari/WebKit
- ✅ Mobile browsers (Chrome Mobile, Safari Mobile)

### Mobile Responsiveness
- ✅ Mobile viewport testing (375x667)
- ✅ Touch-friendly interfaces (44px minimum touch targets)
- ✅ Responsive design and layout
- ✅ Mobile-specific functionality

## 🔒 Security Features Tested

### Input Validation
- ✅ Email format validation
- ✅ Password strength requirements
- ✅ XSS prevention and sanitization
- ✅ SQL injection prevention
- ✅ CSRF protection

### Authentication Security
- ✅ Secure credential handling
- ✅ Token-based authentication
- ✅ Session management
- ✅ Automatic logout on inactivity
- ✅ Secure headers implementation

### Data Protection
- ✅ Sensitive data encryption
- ✅ Secure API communication
- ✅ Input sanitization
- ✅ Output encoding

## 🛠️ Test Configuration

### Playwright Configuration
The tests are configured in `playwright.config.ts` with the following features:
- Multiple browser support (Chrome, Firefox, Safari)
- Mobile device testing
- Screenshot and video capture on failure
- Trace collection for debugging
- Parallel test execution
- Automatic retry on failure

### Test Data
Test data is defined in each test file with realistic user accounts:
- **Admin**: `siddhartha@ankurshala.com` / `Maza@123`
- **Teacher**: `teacher@ankurshala.com` / `Teacher@123`
- **Student**: `student@ankurshala.com` / `Student@123`

### Environment Setup
Tests automatically:
- Start the backend server
- Start the frontend development server
- Wait for services to be ready
- Clean up after test completion

## 📊 Test Reports

### HTML Report
After running tests, view the detailed HTML report:
```bash
npx playwright show-report
```
The report includes:
- Test results and status
- Screenshots of failures
- Video recordings
- Trace files for debugging
- Performance metrics

### Test Results Location
- **HTML Report**: `playwright-report/index.html`
- **Screenshots**: `test-results/screenshots/`
- **Videos**: `test-results/videos/`
- **JSON Results**: `test-results/results.json`
- **JUnit Results**: `test-results/results.xml`

## 🚀 CI/CD Integration

### GitHub Actions
The project includes a comprehensive GitHub Actions workflow (`.github/workflows/e2e-tests.yml`) that:
- Runs tests on every push and pull request
- Tests across multiple browsers
- Generates and uploads test reports
- Includes security scanning
- Performance testing
- Automated deployment on success

### Test Execution in CI
```yaml
- name: Run Playwright tests
  working-directory: ./frontend
  run: npx playwright test
```

## 🔧 Troubleshooting

### Common Issues

1. **Backend not starting**
   ```bash
   # Check Docker status
   docker ps
   
   # Restart backend
   cd backend
   docker-compose down
   docker-compose up -d
   ```

2. **Frontend not loading**
   ```bash
   # Check if port 3001 is available
   lsof -i :3001
   
   # Kill existing processes
   pkill -f "npm run dev"
   
   # Restart frontend
   npm run dev
   ```

3. **Tests failing due to timeouts**
   - Increase timeout in `playwright.config.ts`
   - Check network connectivity
   - Verify backend API responses

4. **Authentication issues**
   - Verify test user credentials
   - Check JWT token expiration
   - Ensure backend authentication is working

### Debug Mode
Run tests in debug mode:
```bash
npx playwright test --debug
```

### Headed Mode
Run tests with visible browser:
```bash
npx playwright test --headed
```

### Specific Test Execution
Run specific test suites:
```bash
# Authentication tests only
npx playwright test tests/auth.spec.ts

# Admin content tests only
npx playwright test tests/admin-content.spec.ts

# Integration tests only
npx playwright test tests/integration.spec.ts
```

## 📈 Performance Benchmarks

### Expected Performance
- **Page Load Time**: < 5 seconds
- **API Response Time**: < 2 seconds
- **Large Dataset Handling**: < 10 seconds
- **Mobile Performance**: < 8 seconds

### Performance Monitoring
Tests automatically measure and report:
- Page load times
- API response times
- Memory usage
- Network requests
- Rendering performance

## 🔍 Test Maintenance

### Adding New Tests
1. Create test file in `tests/` directory
2. Follow naming convention: `*.spec.ts`
3. Use existing helper functions
4. Add appropriate test data
5. Update this documentation

### Updating Test Data
1. Modify test data in test files
2. Ensure data consistency across tests
3. Update documentation
4. Verify tests still pass

### Test Environment Updates
1. Update `playwright.config.ts` for configuration changes
2. Update CI/CD workflows for environment changes
3. Update this documentation
4. Test locally before committing

## 📚 Additional Resources

- [Playwright Documentation](https://playwright.dev/docs/intro)
- [Testing Best Practices](https://playwright.dev/docs/best-practices)
- [CI/CD Integration](https://playwright.dev/docs/ci)
- [Debugging Tests](https://playwright.dev/docs/debug)

## 🤝 Contributing

When contributing to the test suite:
1. Follow existing test patterns
2. Add appropriate test coverage
3. Update documentation
4. Ensure all tests pass
5. Add new test scenarios for new features

## 📞 Support

For issues with the testing setup:
1. Check this documentation
2. Review test logs and reports
3. Check GitHub Issues
4. Contact the development team

---

**Note**: This testing suite ensures the Ankurshala Education Platform meets production-grade standards for security, performance, and user experience.
