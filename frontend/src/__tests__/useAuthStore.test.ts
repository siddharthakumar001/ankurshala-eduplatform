import { renderHook, act } from '@testing-library/react';
import { useAuthStore } from '@/store/auth';

// Mock axios
jest.mock('axios', () => ({
  post: jest.fn(),
}));

// Mock next/navigation
jest.mock('next/navigation', () => ({
  useRouter: () => ({
    push: jest.fn(),
    replace: jest.fn(),
  }),
}));

describe('useAuthStore', () => {
  beforeEach(() => {
    // Reset the store state before each test
    useAuthStore.getState().logout();
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('should initialize with correct default state', () => {
    const { result } = renderHook(() => useAuthStore());

    expect(result.current.user).toBeNull();
    expect(result.current.isAuthenticated).toBe(false);
    expect(result.current.isLoading).toBe(false);
  });

  it('should handle login successfully', async () => {
    const { result } = renderHook(() => useAuthStore());

    const mockUser = {
      id: 1,
      name: 'Test User',
      email: 'test@example.com',
      role: 'STUDENT',
    };

    const mockResponse = {
      data: {
        data: {
          accessToken: 'mock-access-token',
          refreshToken: 'mock-refresh-token',
          userId: 1,
          name: 'Test User',
          email: 'test@example.com',
          role: 'STUDENT',
        },
      },
    };

    // Mock axios.post to return successful response
    const axios = require('axios');
    axios.post.mockResolvedValue(mockResponse);

    await act(async () => {
      await result.current.login('test@example.com', 'password123');
    });

    expect(result.current.user).toEqual(mockUser);
    expect(result.current.isAuthenticated).toBe(true);
    expect(result.current.isLoading).toBe(false);
  });

  it('should handle login failure', async () => {
    const { result } = renderHook(() => useAuthStore());

    // Mock axios.post to return error
    const axios = require('axios');
    axios.post.mockRejectedValue(new Error('Invalid credentials'));

    await act(async () => {
      try {
        await result.current.login('test@example.com', 'wrongpassword');
      } catch (error) {
        // Expected to throw
      }
    });

    expect(result.current.user).toBeNull();
    expect(result.current.isAuthenticated).toBe(false);
    expect(result.current.isLoading).toBe(false);
  });

  it('should handle student signup successfully', async () => {
    const { result } = renderHook(() => useAuthStore());

    const mockUser = {
      id: 1,
      name: 'New Student',
      email: 'newstudent@example.com',
      role: 'STUDENT',
    };

    const mockResponse = {
      data: {
        data: {
          accessToken: 'mock-access-token',
          refreshToken: 'mock-refresh-token',
          userId: 1,
          name: 'New Student',
          email: 'newstudent@example.com',
          role: 'STUDENT',
        },
      },
    };

    // Mock axios.post to return successful response
    const axios = require('axios');
    axios.post.mockResolvedValue(mockResponse);

    const signupData = {
      name: 'New Student',
      email: 'newstudent@example.com',
      password: 'SecurePass123!',
      board: 'CBSE',
      grade: '10',
      language: 'English',
      school: 'Test School',
      dob: '2005-05-15',
      pincode: '110001',
      guardianName: 'Guardian Name',
      guardianContact: '9876543210',
    };

    await act(async () => {
      await result.current.signupStudent(signupData);
    });

    expect(result.current.user).toEqual(mockUser);
    expect(result.current.isAuthenticated).toBe(true);
    expect(result.current.isLoading).toBe(false);
  });

  it('should handle teacher signup successfully', async () => {
    const { result } = renderHook(() => useAuthStore());

    const mockUser = {
      id: 1,
      name: 'New Teacher',
      email: 'newteacher@example.com',
      role: 'TEACHER',
    };

    const mockResponse = {
      data: {
        data: {
          accessToken: 'mock-access-token',
          refreshToken: 'mock-refresh-token',
          userId: 1,
          name: 'New Teacher',
          email: 'newteacher@example.com',
          role: 'TEACHER',
        },
      },
    };

    // Mock axios.post to return successful response
    const axios = require('axios');
    axios.post.mockResolvedValue(mockResponse);

    const signupData = {
      name: 'New Teacher',
      email: 'newteacher@example.com',
      password: 'SecurePass123!',
      bio: 'Experienced teacher',
      yearsExperience: 5,
      languages: ['English', 'Hindi'],
      categories: ['STANDARD'],
      hourlyRate: 500,
      subjectExpertise: [],
      availability: [],
    };

    await act(async () => {
      await result.current.signupTeacher(signupData);
    });

    expect(result.current.user).toEqual(mockUser);
    expect(result.current.isAuthenticated).toBe(true);
    expect(result.current.isLoading).toBe(false);
  });

  it('should handle logout', () => {
    const { result } = renderHook(() => useAuthStore());

    // First set a user
    act(() => {
      result.current.setUser({
        id: 1,
        name: 'Test User',
        email: 'test@example.com',
        role: 'STUDENT',
      });
    });

    expect(result.current.isAuthenticated).toBe(true);

    // Then logout
    act(() => {
      result.current.logout();
    });

    expect(result.current.user).toBeNull();
    expect(result.current.isAuthenticated).toBe(false);
  });

  it('should handle token refresh', async () => {
    const { result } = renderHook(() => useAuthStore());

    const mockResponse = {
      data: {
        data: {
          accessToken: 'new-access-token',
          refreshToken: 'new-refresh-token',
          userId: 1,
          name: 'Test User',
          email: 'test@example.com',
          role: 'STUDENT',
        },
      },
    };

    // Mock axios.post to return successful response
    const axios = require('axios');
    axios.post.mockResolvedValue(mockResponse);

    await act(async () => {
      await result.current.refreshToken('old-refresh-token');
    });

    expect(result.current.user).toEqual({
      id: 1,
      name: 'Test User',
      email: 'test@example.com',
      role: 'STUDENT',
    });
    expect(result.current.isAuthenticated).toBe(true);
  });

  it('should set loading state during async operations', async () => {
    const { result } = renderHook(() => useAuthStore());

    // Mock axios.post to return a delayed response
    const axios = require('axios');
    axios.post.mockImplementation(() => 
      new Promise(resolve => setTimeout(() => resolve({ data: { data: {} } }), 100))
    );

    act(() => {
      result.current.login('test@example.com', 'password123');
    });

    // Should be loading initially
    expect(result.current.isLoading).toBe(true);

    // Wait for the operation to complete
    await act(async () => {
      await new Promise(resolve => setTimeout(resolve, 150));
    });

    // Should not be loading after completion
    expect(result.current.isLoading).toBe(false);
  });
});
