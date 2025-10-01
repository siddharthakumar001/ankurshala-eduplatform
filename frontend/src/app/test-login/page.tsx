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

        addResult(`✅ Login successful! Role: ${responseData.role}, Expected: ${expectedRole}`)
        
        if (responseData.role === expectedRole) {
          addResult(`✅ Role matches expected: ${expectedRole}`)
        } else {
          addResult(`❌ Role mismatch! Got: ${responseData.role}, Expected: ${expectedRole}`)
        }

        // Test token validation
        const userResponse = await api.get('/user/me')
        addResult(`✅ Token validation successful: ${JSON.stringify(userResponse.data)}`)

        // Logout for next test
        authManager.logout()
        addResult(`✅ Logout successful`)
        
      }
    } catch (error: any) {
      addResult(`❌ Login failed: ${error.message || error}`)
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
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 p-8">
      <div className="max-w-4xl mx-auto">
        <Card>
          <CardHeader>
            <CardTitle>Login Flow Test Dashboard</CardTitle>
            <CardDescription>
              Test the enhanced login flow with proper role-based redirects and security
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="flex gap-4">
              <Button onClick={testAllCredentials}>
                Test All Login Credentials
              </Button>
              <Button variant="outline" onClick={clearResults}>
                Clear Results
              </Button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {testCredentials.map((cred, index) => (
                <Card key={index}>
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

            <div className="bg-black text-green-400 p-4 rounded-lg font-mono text-sm max-h-96 overflow-y-auto">
              <div className="mb-2 text-white">Test Results:</div>
              {results.length === 0 ? (
                <div className="text-gray-500">No tests run yet...</div>
              ) : (
                results.map((result, index) => (
                  <div key={index} className="mb-1">
                    {result}
                  </div>
                ))
              )}
            </div>

            <div className="bg-blue-50 dark:bg-blue-900/20 p-4 rounded-lg">
              <h3 className="font-semibold mb-2">Security Improvements Made:</h3>
              <ul className="space-y-1 text-sm">
                <li>✅ Removed credential exposure from URL parameters</li>
                <li>✅ Added role-based dashboard redirects</li>
                <li>✅ Enhanced form validation and error handling</li>
                <li>✅ Improved responsive design with company logo</li>
                <li>✅ Added comprehensive security headers</li>
                <li>✅ Implemented proper route guards</li>
                <li>✅ Added automatic redirect for authenticated users on homepage</li>
              </ul>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
