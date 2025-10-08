'use client'

import { useState } from 'react'

export default function TestCookiesPage() {
  const [result, setResult] = useState<string>('')

  const testCookies = async () => {
    try {
      // Test setting a cookie
      document.cookie = 'testCookie=testValue; path=/; secure; samesite=lax'
      
      // Test reading cookies
      const cookies = document.cookie
      
      // Test API call
      const response = await fetch('/api/user/me', {
        credentials: 'include'
      })
      
      const data = await response.text()
      
      setResult(`
Cookies: ${cookies}
API Response Status: ${response.status}
API Response: ${data}
      `)
    } catch (error) {
      setResult(`Error: ${error}`)
    }
  }

  return (
    <div className="p-8">
      <h1 className="text-2xl font-bold mb-4">Cookie Test Page</h1>
      <button 
        onClick={testCookies}
        className="bg-blue-500 text-white px-4 py-2 rounded"
      >
        Test Cookies
      </button>
      <pre className="mt-4 p-4 bg-gray-100 rounded">
        {result}
      </pre>
    </div>
  )
}
