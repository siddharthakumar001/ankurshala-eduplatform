/**
 * Authentication and JWT Token Management Utilities
 * Provides comprehensive token validation, refresh, and security features
 */

interface TokenPayload {
  sub: string;
  email: string;
  roles: string[];
  iat: number;
  exp: number;
}

interface AuthState {
  isAuthenticated: boolean;
  token: string | null;
  refreshToken: string | null;
  user: any | null;
  lastActivity: number;
  isTokenExpired: boolean;
}

class AuthManager {
  private static instance: AuthManager;
  private authState: AuthState;
  private heartbeatInterval: NodeJS.Timeout | null = null;
  private idleTimeout: NodeJS.Timeout | null = null;
  private readonly IDLE_TIMEOUT = 45 * 60 * 1000; // 45 minutes in milliseconds
  private readonly HEARTBEAT_INTERVAL = 5 * 60 * 1000; // 5 minutes in milliseconds
  private readonly TOKEN_REFRESH_THRESHOLD = 5 * 60 * 1000; // 5 minutes before expiry

  private constructor() {
    this.authState = {
      isAuthenticated: false,
      token: null,
      refreshToken: null,
      user: null,
      lastActivity: Date.now(),
      isTokenExpired: false
    };
    this.loadFromStorage();
    this.setupEventListeners();
  }

  public static getInstance(): AuthManager {
    if (!AuthManager.instance) {
      AuthManager.instance = new AuthManager();
    }
    return AuthManager.instance;
  }

  /**
   * Parse JWT token and extract payload
   */
  private parseToken(token: string): TokenPayload | null {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      return JSON.parse(jsonPayload);
    } catch (error) {
      console.error('Error parsing JWT token:', error);
      return null;
    }
  }

  /**
   * Check if token is expired
   */
  private isTokenExpired(token: string): boolean {
    const payload = this.parseToken(token);
    if (!payload) return true;
    
    const currentTime = Math.floor(Date.now() / 1000);
    return payload.exp < currentTime;
  }

  /**
   * Check if token will expire soon
   */
  private isTokenExpiringSoon(token: string): boolean {
    const payload = this.parseToken(token);
    if (!payload) return true;
    
    const currentTime = Math.floor(Date.now() / 1000);
    const timeUntilExpiry = payload.exp - currentTime;
    return timeUntilExpiry < (this.TOKEN_REFRESH_THRESHOLD / 1000);
  }

  /**
   * Load authentication state from localStorage
   */
  private loadFromStorage(): void {
    try {
      const token = localStorage.getItem('accessToken');
      const refreshToken = localStorage.getItem('refreshToken');
      const user = localStorage.getItem('user');
      const lastActivity = localStorage.getItem('lastActivity');

      if (token && !this.isTokenExpired(token)) {
        this.authState.token = token;
        this.authState.refreshToken = refreshToken;
        this.authState.user = user ? JSON.parse(user) : null;
        this.authState.isAuthenticated = true;
        this.authState.lastActivity = lastActivity ? parseInt(lastActivity) : Date.now();
        this.authState.isTokenExpired = false;
        
        this.startHeartbeat();
        this.resetIdleTimeout();
      } else {
        this.clearAuthState();
      }
    } catch (error) {
      console.error('Error loading auth state from storage:', error);
      this.clearAuthState();
    }
  }

  /**
   * Save authentication state to localStorage
   */
  private saveToStorage(): void {
    try {
      if (this.authState.token) {
        localStorage.setItem('accessToken', this.authState.token);
        localStorage.setItem('lastActivity', this.authState.lastActivity.toString());
      }
      if (this.authState.refreshToken) {
        localStorage.setItem('refreshToken', this.authState.refreshToken);
      }
      if (this.authState.user) {
        localStorage.setItem('user', JSON.stringify(this.authState.user));
      }
    } catch (error) {
      console.error('Error saving auth state to storage:', error);
    }
  }

  /**
   * Clear authentication state
   */
  private clearAuthState(): void {
    this.authState = {
      isAuthenticated: false,
      token: null,
      refreshToken: null,
      user: null,
      lastActivity: Date.now(),
      isTokenExpired: false
    };
    
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    localStorage.removeItem('lastActivity');
    
    this.stopHeartbeat();
    this.clearIdleTimeout();
  }

  /**
   * Setup event listeners for user activity
   */
  private setupEventListeners(): void {
    const events = ['mousedown', 'mousemove', 'keypress', 'scroll', 'touchstart', 'click'];
    
    events.forEach(event => {
      document.addEventListener(event, this.updateActivity.bind(this), true);
    });

    // Handle page visibility changes
    document.addEventListener('visibilitychange', () => {
      if (document.visibilityState === 'visible' && this.authState.isAuthenticated) {
        this.checkTokenValidity();
      }
    });

    // Handle page unload
    window.addEventListener('beforeunload', () => {
      this.stopHeartbeat();
      this.clearIdleTimeout();
    });
  }

  /**
   * Update last activity timestamp
   */
  private updateActivity(): void {
    if (this.authState.isAuthenticated) {
      this.authState.lastActivity = Date.now();
      this.saveToStorage();
      this.resetIdleTimeout();
    }
  }

  /**
   * Start heartbeat mechanism
   */
  private startHeartbeat(): void {
    this.stopHeartbeat();
    
    this.heartbeatInterval = setInterval(() => {
      if (this.authState.isAuthenticated && this.authState.token) {
        this.checkTokenValidity();
      }
    }, this.HEARTBEAT_INTERVAL);
  }

  /**
   * Stop heartbeat mechanism
   */
  private stopHeartbeat(): void {
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval);
      this.heartbeatInterval = null;
    }
  }

  /**
   * Reset idle timeout
   */
  private resetIdleTimeout(): void {
    this.clearIdleTimeout();
    
    this.idleTimeout = setTimeout(() => {
      this.handleIdleTimeout();
    }, this.IDLE_TIMEOUT);
  }

  /**
   * Clear idle timeout
   */
  private clearIdleTimeout(): void {
    if (this.idleTimeout) {
      clearTimeout(this.idleTimeout);
      this.idleTimeout = null;
    }
  }

  /**
   * Handle idle timeout
   */
  private handleIdleTimeout(): void {
    console.log('User idle timeout reached');
    this.logout();
  }

  /**
   * Check token validity and refresh if needed
   */
  private async checkTokenValidity(): Promise<void> {
    if (!this.authState.token) return;

    if (this.isTokenExpired(this.authState.token)) {
      console.log('Token expired, attempting refresh');
      await this.refreshToken();
    } else if (this.isTokenExpiringSoon(this.authState.token)) {
      console.log('Token expiring soon, attempting refresh');
      await this.refreshToken();
    }
  }

  /**
   * Refresh JWT token
   */
  public async refreshToken(): Promise<boolean> {
    try {
      if (!this.authState.refreshToken) {
        throw new Error('No refresh token available');
      }

      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/auth/refresh`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          refreshToken: this.authState.refreshToken
        })
      });

      if (response.ok) {
        const data = await response.json();
        this.authState.token = data.accessToken;
        this.authState.refreshToken = data.refreshToken;
        this.authState.isTokenExpired = false;
        this.saveToStorage();
        return true;
      } else {
        throw new Error('Token refresh failed');
      }
    } catch (error) {
      console.error('Token refresh error:', error);
      this.logout();
      return false;
    }
  }

  /**
   * Public methods
   */

  /**
   * Check if user is authenticated
   */
  public isAuthenticated(): boolean {
    return this.authState.isAuthenticated && 
           this.authState.token !== null && 
           !this.isTokenExpired(this.authState.token);
  }

  /**
   * Get current token
   */
  public getToken(): string | null {
    if (this.isAuthenticated()) {
      return this.authState.token;
    }
    return null;
  }

  /**
   * Get current user
   */
  public getUser(): any | null {
    return this.authState.user;
  }

  /**
   * Set authentication data
   */
  public setAuth(token: string, refreshToken: string, user: any): void {
    this.authState.token = token;
    this.authState.refreshToken = refreshToken;
    this.authState.user = user;
    this.authState.isAuthenticated = true;
    this.authState.isTokenExpired = false;
    this.authState.lastActivity = Date.now();
    
    this.saveToStorage();
    this.startHeartbeat();
    this.resetIdleTimeout();
  }

  /**
   * Logout user
   */
  public logout(): void {
    this.clearAuthState();
    
    // Redirect to login page
    if (typeof window !== 'undefined') {
      window.location.href = '/login';
    }
  }

  /**
   * Force logout with message
   */
  public forceLogout(message: string = 'Session expired. Please log in again.'): void {
    console.log('Force logout:', message);
    this.logout();
  }

  /**
   * Get token expiration time
   */
  public getTokenExpiration(): Date | null {
    if (!this.authState.token) return null;
    
    const payload = this.parseToken(this.authState.token);
    if (!payload) return null;
    
    return new Date(payload.exp * 1000);
  }

  /**
   * Get time until token expires
   */
  public getTimeUntilExpiry(): number | null {
    if (!this.authState.token) return null;
    
    const payload = this.parseToken(this.authState.token);
    if (!payload) return null;
    
    const currentTime = Math.floor(Date.now() / 1000);
    return (payload.exp - currentTime) * 1000; // Return in milliseconds
  }
}

// Export singleton instance
export const authManager = AuthManager.getInstance();

// Export utility functions
export const isAuthenticated = () => authManager.isAuthenticated();
export const getToken = () => authManager.getToken();
export const getUser = () => authManager.getUser();
export const logout = () => authManager.logout();
export const forceLogout = (message?: string) => authManager.forceLogout(message);
export const getTokenExpiration = () => authManager.getTokenExpiration();
export const getTimeUntilExpiry = () => authManager.getTimeUntilExpiry();
