import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useAuthStore } from '@/store/auth';
import LoginPage from '@/app/login/page';

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
  useSearchParams: () => ({
    get: jest.fn(),
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

describe('LoginPage', () => {
  const mockUseAuthStore = useAuthStore as jest.MockedFunction<typeof useAuthStore>;

  beforeEach(() => {
    mockUseAuthStore.mockReturnValue({
      user: null,
      isAuthenticated: false,
      login: jest.fn(),
      logout: jest.fn(),
      isLoading: false,
    });
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('renders login form correctly', () => {
    render(
      <TestWrapper>
        <LoginPage />
      </TestWrapper>
    );

    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument();
  });

  it('shows validation errors for empty fields', async () => {
    render(
      <TestWrapper>
        <LoginPage />
      </TestWrapper>
    );

    const submitButton = screen.getByRole('button', { name: /sign in/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(/email is required/i)).toBeInTheDocument();
      expect(screen.getByText(/password is required/i)).toBeInTheDocument();
    });
  });

  it('shows validation error for invalid email', async () => {
    render(
      <TestWrapper>
        <LoginPage />
      </TestWrapper>
    );

    const emailInput = screen.getByLabelText(/email/i);
    fireEvent.change(emailInput, { target: { value: 'invalid-email' } });

    const submitButton = screen.getByRole('button', { name: /sign in/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(/invalid email format/i)).toBeInTheDocument();
    });
  });

  it('calls login function with correct credentials', async () => {
    const mockLogin = jest.fn();
    mockUseAuthStore.mockReturnValue({
      user: null,
      isAuthenticated: false,
      login: mockLogin,
      logout: jest.fn(),
      isLoading: false,
    });

    render(
      <TestWrapper>
        <LoginPage />
      </TestWrapper>
    );

    const emailInput = screen.getByLabelText(/email/i);
    const passwordInput = screen.getByLabelText(/password/i);
    const submitButton = screen.getByRole('button', { name: /sign in/i });

    fireEvent.change(emailInput, { target: { value: 'test@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'password123' } });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith('test@example.com', 'password123');
    });
  });

  it('shows loading state during login', () => {
    mockUseAuthStore.mockReturnValue({
      user: null,
      isAuthenticated: false,
      login: jest.fn(),
      logout: jest.fn(),
      isLoading: true,
    });

    render(
      <TestWrapper>
        <LoginPage />
      </TestWrapper>
    );

    expect(screen.getByText(/signing in/i)).toBeInTheDocument();
  });

  it('redirects authenticated users', () => {
    mockUseAuthStore.mockReturnValue({
      user: { id: 1, name: 'Test User', email: 'test@example.com', role: 'STUDENT' },
      isAuthenticated: true,
      login: jest.fn(),
      logout: jest.fn(),
      isLoading: false,
    });

    render(
      <TestWrapper>
        <LoginPage />
      </TestWrapper>
    );

    // Should redirect, so login form should not be visible
    expect(screen.queryByLabelText(/email/i)).not.toBeInTheDocument();
  });
});
