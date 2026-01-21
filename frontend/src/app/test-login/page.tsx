'use client'

import { useState } from 'react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { authManager } from '@/utils/auth'
import { api } from '@/utils/api'

export default function TestLoginPage() {
  const [results, setResults] = useState<string[]>([])

  const addResult = (message: string) => {
    setResults(prev => [...prev, `${new Date().toLocaleTimeString()}: ${message}`])
  }

  const testCredentials = [
    { email: 'siddhartha@ankurshala.com', password: 'Maza@123', expectedRole: 'ADMIN' },
    { email: 'student1@ankurshala.com', password: 'Maza@123', expectedRole: 'STUDENT' },
    { email: 'teacher1@ankurshala.com', password: 'Maza@123', expectedRole: 'TEACHER' }
  ]

  const testLogin = async (email: string, password: string, expectedRole: string) => {
    try {
      addResult(`Testing login for ${email}...`)
      
      const response = await api.post('/auth/signin', {
        email,
        password
      }, { requireAuth: false })

      if (response.data) {
        const responseData = response.data as any
        const userData = {
          id: responseData.userId.toString(),
          email: responseData.email,
          name: responseData.name,
          role: responseData.role
        }

        authManager.setAuth(
          responseData.accessToken,
          responseData.refreshToken,
          userData
        )

        addResult(`OK Login successful! Role: ${responseData.role}, Expected: ${expectedRole}`)
        
        if (responseData.role === expectedRole) {
          addResult(`OK Role matches expected: ${expectedRole}`)
        } else {
          addResult(`ERROR Role mismatch! Got: ${responseData.role}, Expected: ${expectedRole}`)
        }

        // Test token validation
        const userResponse = await api.get('/user/me')
        addResult(`OK Token validation successful: ${JSON.stringify(userResponse.data)}`)

        // Logout for next test
        authManager.logout()
        addResult(`OK Logout successful`)
        
      }
    } catch (error: any) {
      addResult(`ERROR Login failed: ${error.message || error}`)
      console.error('Login test error:', error)
    }
  }

  const testAllCredentials = async () => {
    setResults([])
    addResult('Starting comprehensive login flow tests...')

    for (const cred of testCredentials) {
      await testLogin(cred.email, cred.password, cred.expectedRole)
      await new Promise(resolve => setTimeout(resolve, 1000)) // Wait 1 second between tests
    }

    addResult('All tests completed!')
  }

  const clearResults = () => {
    setResults([])
  }

  return (
    <div className="min-h-screen bg-transparent p-8">
      <div className="max-w-4xl mx-auto">
        <Card className="glass-panel border border-white/40">
          <CardHeader>
            <CardTitle>Login Flow Test Dashboard</CardTitle>
            <CardDescription>
              Test the enhanced login flow with proper role-based redirects and security
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="flex gap-4">
              <Button onClick={testAllCredentials} className="btn-primary">
                Test All Login Credentials
              </Button>
              <Button variant="outline" onClick={clearResults}>
                Clear Results
              </Button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {testCredentials.map((cred, index) => (
                <Card key={index} className="glass border border-white/40">
                  <CardHeader>
                    <CardTitle className="text-sm">{cred.expectedRole}</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <p className="text-xs text-gray-600 mb-2">Email: {cred.email}</p>
                    <p className="text-xs text-gray-600 mb-4">Password: {cred.password}</p>
                    <Button 
                      size="sm" 
                      onClick={() => testLogin(cred.email, cred.password, cred.expectedRole)}
                    >
                      Test Login
                    </Button>
                  </CardContent>
                </Card>
              ))}
            </div>

            <div className="bg-slate-950/90 text-emerald-200 p-4 rounded-2xl font-mono text-sm max-h-96 overflow-y-auto border border-white/10 backdrop-blur">
              <div className="mb-2 text-white">Test Results:</div>
              {results.length === 0 ? (
                <div className="text-slate-400">No tests run yet...</div>
              ) : (
                results.map((result, index) => (
                  <div key={index} className="mb-1">
                    {result}
                  </div>
                ))
              )}
            </div>

            <div className="glass rounded-2xl border border-white/40 p-4">
              <h3 className="font-semibold mb-2 text-ankur-secondary dark:text-white">Security Improvements Made:</h3>
              <ul className="space-y-1 text-sm text-gray-600 dark:text-gray-300">
                <li>OK Removed credential exposure from URL parameters</li>
                <li>OK Added role-based dashboard redirects</li>
                <li>OK Enhanced form validation and error handling</li>
                <li>OK Improved responsive design with company logo</li>
                <li>OK Added comprehensive security headers</li>
                <li>OK Implemented proper route guards</li>
                <li>OK Added automatic redirect for authenticated users on homepage</li>
              </ul>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
