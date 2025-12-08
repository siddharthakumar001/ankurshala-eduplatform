import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useAuthStore } from '@/store/auth';
import StudentSignupPage from '@/app/register-student/page';

// Mock the auth store
jest.mock('@/store/auth', () => ({
  useAuthStore: jest.fn(),
}));

// Mock next/navigation
jest.mock('next/navigation', () => ({
  useRouter: () => ({
    push: jest.fn(),
    replace: jest.fn(),
  }),
}));

// Mock axios
jest.mock('axios', () => ({
  post: jest.fn(),
}));

const createTestQueryClient = () =>
  new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
    },
  });

const TestWrapper = ({ children }: { children: React.ReactNode }) => {
  const queryClient = createTestQueryClient();
  return (
    <QueryClientProvider client={queryClient}>
      {children}
    </QueryClientProvider>
  );
};

describe('StudentSignupPage', () => {
  const mockUseAuthStore = useAuthStore as jest.MockedFunction<typeof useAuthStore>;

  beforeEach(() => {
    mockUseAuthStore.mockReturnValue({
      user: null,
      isAuthenticated: false,
      signupStudent: jest.fn(),
      logout: jest.fn(),
      isLoading: false,
    });
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('renders student signup form correctly', () => {
    render(
      <TestWrapper>
        <StudentSignupPage />
      </TestWrapper>
    );

    expect(screen.getByLabelText(/name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/board/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/grade/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/language/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/school/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/date of birth/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/pincode/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/guardian name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/guardian contact/i)).toBeInTheDocument();
  });

  it('shows validation errors for empty required fields', async () => {
    render(
      <TestWrapper>
        <StudentSignupPage />
      </TestWrapper>
    );

    const submitButton = screen.getByRole('button', { name: /create account/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(/name is required/i)).toBeInTheDocument();
      expect(screen.getByText(/email is required/i)).toBeInTheDocument();
      expect(screen.getByText(/password is required/i)).toBeInTheDocument();
      expect(screen.getByText(/board is required/i)).toBeInTheDocument();
      expect(screen.getByText(/grade is required/i)).toBeInTheDocument();
      expect(screen.getByText(/language is required/i)).toBeInTheDocument();
      expect(screen.getByText(/school is required/i)).toBeInTheDocument();
      expect(screen.getByText(/date of birth is required/i)).toBeInTheDocument();
      expect(screen.getByText(/pincode is required/i)).toBeInTheDocument();
      expect(screen.getByText(/guardian name is required/i)).toBeInTheDocument();
      expect(screen.getByText(/guardian contact is required/i)).toBeInTheDocument();
    });
  });

  it('shows validation error for invalid email format', async () => {
    render(
      <TestWrapper>
        <StudentSignupPage />
      </TestWrapper>
    );

    const emailInput = screen.getByLabelText(/email/i);
    fireEvent.change(emailInput, { target: { value: 'invalid-email' } });

    const submitButton = screen.getByRole('button', { name: /create account/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(/invalid email format/i)).toBeInTheDocument();
    });
  });

  it('shows validation error for weak password', async () => {
    render(
      <TestWrapper>
        <StudentSignupPage />
      </TestWrapper>
    );

    const passwordInput = screen.getByLabelText(/password/i);
    fireEvent.change(passwordInput, { target: { value: '123' } });

    const submitButton = screen.getByRole('button', { name: /create account/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(/password must be at least 8 characters/i)).toBeInTheDocument();
    });
  });

  it('shows validation error for invalid Indian mobile number', async () => {
    render(
      <TestWrapper>
        <StudentSignupPage />
      </TestWrapper>
    );

    const guardianContactInput = screen.getByLabelText(/guardian contact/i);
    fireEvent.change(guardianContactInput, { target: { value: '1234567890' } });

    const submitButton = screen.getByRole('button', { name: /create account/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(/invalid indian mobile number/i)).toBeInTheDocument();
    });
  });

  it('shows validation error for invalid Indian pincode', async () => {
    render(
      <TestWrapper>
        <StudentSignupPage />
      </TestWrapper>
    );

    const pincodeInput = screen.getByLabelText(/pincode/i);
    fireEvent.change(pincodeInput, { target: { value: '12345' } });

    const submitButton = screen.getByRole('button', { name: /create account/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(/invalid indian pincode/i)).toBeInTheDocument();
    });
  });

  it('calls signup function with correct data', async () => {
    const mockSignupStudent = jest.fn();
    mockUseAuthStore.mockReturnValue({
      user: null,
      isAuthenticated: false,
      signupStudent: mockSignupStudent,
      logout: jest.fn(),
      isLoading: false,
    });

    render(
      <TestWrapper>
        <StudentSignupPage />
      </TestWrapper>
    );

    // Fill in valid form data
    fireEvent.change(screen.getByLabelText(/name/i), { target: { value: 'John Doe' } });
    fireEvent.change(screen.getByLabelText(/email/i), { target: { value: 'john@example.com' } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'SecurePass123!' } });
    fireEvent.change(screen.getByLabelText(/board/i), { target: { value: 'CBSE' } });
    fireEvent.change(screen.getByLabelText(/grade/i), { target: { value: '10' } });
    fireEvent.change(screen.getByLabelText(/language/i), { target: { value: 'English' } });
    fireEvent.change(screen.getByLabelText(/school/i), { target: { value: 'Test School' } });
    fireEvent.change(screen.getByLabelText(/date of birth/i), { target: { value: '2005-05-15' } });
    fireEvent.change(screen.getByLabelText(/pincode/i), { target: { value: '110001' } });
    fireEvent.change(screen.getByLabelText(/guardian name/i), { target: { value: 'Jane Doe' } });
    fireEvent.change(screen.getByLabelText(/guardian contact/i), { target: { value: '9876543210' } });

    const submitButton = screen.getByRole('button', { name: /create account/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(mockSignupStudent).toHaveBeenCalledWith(
        expect.objectContaining({
          name: 'John Doe',
          email: 'john@example.com',
          password: 'SecurePass123!',
          board: 'CBSE',
          grade: '10',
          language: 'English',
          school: 'Test School',
          dob: '2005-05-15',
          pincode: '110001',
          guardianName: 'Jane Doe',
          guardianContact: '9876543210',
        })
      );
    });
  });

  it('shows loading state during signup', () => {
    mockUseAuthStore.mockReturnValue({
      user: null,
      isAuthenticated: false,
      signupStudent: jest.fn(),
      logout: jest.fn(),
      isLoading: true,
    });

    render(
      <TestWrapper>
        <StudentSignupPage />
      </TestWrapper>
    );

    expect(screen.getByText(/creating account/i)).toBeInTheDocument();
  });
});
