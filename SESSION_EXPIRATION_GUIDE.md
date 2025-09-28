# Session Expiration Warning System

## Overview
The Session Expiration Warning System provides users with proactive notifications about their session expiration and gives them the option to extend their session or logout gracefully. This feature enhances user experience by preventing unexpected logouts and data loss.

## 🚨 Features Implemented

### 1. **Proactive Session Warning**
- **Warning Threshold**: Shows warning 5 minutes before session expires
- **Real-time Countdown**: Live countdown timer showing exact time remaining
- **Visual Progress Bar**: Color-coded progress bar indicating urgency level
- **Toast Notifications**: Non-intrusive toast notifications for initial warning

### 2. **User Choice Options**
- **Extend Session**: Allows users to refresh their token and continue working
- **Logout**: Graceful logout option for users who want to end their session
- **Automatic Logout**: Force logout if session expires without user action

### 3. **Smart Urgency Levels**
- **🟢 Low (Green)**: More than 50% time remaining
- **🟡 Medium (Yellow)**: 25-50% time remaining  
- **🔴 High (Red)**: Less than 25% time remaining

### 4. **Comprehensive Session Management**
- **Global Integration**: Works across all authenticated pages
- **Context Provider**: Session state management via React Context
- **Automatic Monitoring**: Continuous session monitoring every 30 seconds
- **Activity Detection**: Respects user activity for session extension

## 🔧 Implementation Details

### Core Components

#### 1. **SessionExpirationManager** (`/components/SessionExpirationManager.tsx`)
```tsx
<SessionExpirationManager
  warningThreshold={5 * 60 * 1000} // 5 minutes
  checkInterval={30 * 1000}        // 30 seconds
>
  {children}
</SessionExpirationManager>
```

**Features:**
- Monitors session expiration continuously
- Shows warning dialog when threshold is reached
- Handles token refresh and logout logic
- Provides visual countdown and progress indicators

#### 2. **SessionProvider** (`/contexts/SessionContext.tsx`)
```tsx
<SessionProvider>
  <App />
</SessionProvider>
```

**Features:**
- Global session state management
- Provides session context to all components
- Handles session updates and notifications
- Integrates with SessionExpirationManager

#### 3. **Enhanced AuthManager** (`/utils/auth.ts`)
```typescript
// Public method for manual token refresh
public async refreshToken(): Promise<boolean>

// Get time until token expires
public getTimeUntilExpiry(): number | null

// Get token expiration date
public getTokenExpiration(): Date | null
```

### Integration Points

#### 1. **Main Layout Integration**
```tsx
// In /app/layout.tsx
<SessionProvider>
  <ConditionalNavbar />
  {children}
</SessionProvider>
```

#### 2. **Admin Pages Integration**
```tsx
// In admin pages
<AuthGuard requiredRoles={['ADMIN']}>
  <SessionManager showSessionInfo={true}>
    <AdminContent />
  </SessionManager>
</AuthGuard>
```

## 🎯 User Experience Flow

### 1. **Normal Session Flow**
1. User logs in and receives JWT tokens
2. Session monitoring starts automatically
3. User works normally with active session
4. Tokens refresh automatically every 5 minutes when active

### 2. **Session Expiration Warning Flow**
1. **5 minutes before expiry**: Warning dialog appears
2. **User sees**: Countdown timer, progress bar, urgency indicator
3. **User options**:
   - **Extend Session**: Token refreshed, warning dismissed
   - **Logout**: Graceful logout, redirected to login
4. **If no action**: Automatic logout when session expires

### 3. **Visual Indicators**
- **Progress Bar**: Shows time remaining as percentage
- **Color Coding**: Green → Yellow → Red based on urgency
- **Icons**: Clock, Shield, AlertCircle for visual clarity
- **Toast Messages**: Initial warning and success/error notifications

## 🔒 Security Features

### 1. **Token Security**
- Automatic token refresh before expiration
- Secure token storage in localStorage
- Token validation on every request
- Graceful handling of refresh failures

### 2. **Session Security**
- Idle timeout detection (45 minutes)
- Activity-based session extension
- Secure logout with token cleanup
- Protection against session hijacking

### 3. **User Security**
- Clear session status communication
- Non-intrusive warning system
- Secure logout options
- Protection against data loss

## 📱 Responsive Design

### 1. **Mobile-Friendly**
- Responsive dialog design
- Touch-friendly buttons
- Optimized for small screens
- Accessible on all devices

### 2. **Accessibility**
- Keyboard navigation support
- Screen reader compatibility
- High contrast color schemes
- Clear visual indicators

## 🛠️ Configuration Options

### 1. **Timing Configuration**
```typescript
// Warning threshold (default: 5 minutes)
warningThreshold: 5 * 60 * 1000

// Check interval (default: 30 seconds)
checkInterval: 30 * 1000

// Idle timeout (default: 45 minutes)
IDLE_TIMEOUT: 45 * 60 * 1000

// Heartbeat interval (default: 5 minutes)
HEARTBEAT_INTERVAL: 5 * 60 * 1000
```

### 2. **Visual Configuration**
```typescript
// Urgency levels
const getUrgencyLevel = (): 'low' | 'medium' | 'high' => {
  const percentage = getProgressPercentage()
  if (percentage > 50) return 'low'
  if (percentage > 25) return 'medium'
  return 'high'
}
```

## 🚀 Usage Examples

### 1. **Basic Usage**
```tsx
import { SessionProvider } from '@/contexts/SessionContext'

function App() {
  return (
    <SessionProvider>
      <YourAppContent />
    </SessionProvider>
  )
}
```

### 2. **With Session Info Display**
```tsx
import SessionManager from '@/components/SessionManager'

function AdminPage() {
  return (
    <SessionManager showSessionInfo={true}>
      <AdminContent />
    </SessionManager>
  )
}
```

### 3. **Using Session Context**
```tsx
import { useSessionContext } from '@/contexts/SessionContext'

function MyComponent() {
  const { isAuthenticated, user, timeUntilExpiry, extendSession, logout } = useSessionContext()
  
  return (
    <div>
      {isAuthenticated && (
        <p>Session expires in: {timeUntilExpiry}ms</p>
      )}
    </div>
  )
}
```

## 🔍 Monitoring & Debugging

### 1. **Console Logging**
- Session state changes logged
- Token refresh attempts logged
- Warning dialog events logged
- Error conditions logged

### 2. **Toast Notifications**
- Initial warning notifications
- Success/failure messages
- Session extension confirmations
- Logout confirmations

### 3. **Visual Debugging**
- Session status widget (optional)
- Real-time countdown display
- Progress bar visualization
- Urgency level indicators

## 🎉 Benefits

### 1. **User Experience**
- **No Surprise Logouts**: Users are warned before session expires
- **Data Protection**: Users can save work before logout
- **Flexibility**: Users can choose to extend or logout
- **Transparency**: Clear communication about session status

### 2. **Security**
- **Proactive Management**: Prevents expired token usage
- **Graceful Degradation**: Handles token refresh failures
- **User Control**: Users can manage their own sessions
- **Audit Trail**: Session events are logged

### 3. **System Reliability**
- **Automatic Recovery**: Handles token refresh automatically
- **Error Handling**: Graceful handling of network issues
- **State Management**: Consistent session state across app
- **Performance**: Efficient monitoring with minimal overhead

## 🔄 Future Enhancements

### 1. **Advanced Features**
- **Remember Me**: Extended sessions for trusted devices
- **Session Sharing**: Multiple device session management
- **Activity Tracking**: Detailed user activity monitoring
- **Customizable Warnings**: User-configurable warning thresholds

### 2. **Analytics**
- **Session Metrics**: Track session duration and patterns
- **User Behavior**: Analyze session extension patterns
- **Performance Monitoring**: Track token refresh success rates
- **Security Analytics**: Monitor for suspicious session activity

This comprehensive session expiration warning system ensures that users are never caught off-guard by session expiration while maintaining security and providing a smooth user experience.
