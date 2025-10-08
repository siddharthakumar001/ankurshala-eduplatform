import { NextRequest, NextResponse } from 'next/server'

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080/api'

export async function GET(request: NextRequest) {
  try {
    // Forward request to backend to get CSRF token
    const backendResponse = await fetch(`${BACKEND_URL}/csrf`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        // Forward cookies for CSRF token generation
        'Cookie': request.headers.get('cookie') || '',
      },
    })

    if (!backendResponse.ok) {
      return NextResponse.json(
        { success: false, message: 'Failed to get CSRF token' },
        { status: 500 }
      )
    }

    // Forward the CSRF cookie from backend
    const setCookieHeaders = backendResponse.headers.getSetCookie()
    const response = NextResponse.json({ success: true, message: 'CSRF token set' })
    
    // Set the CSRF cookie in the frontend response
    setCookieHeaders.forEach(cookie => {
      response.headers.append('Set-Cookie', cookie)
    })

    return response

  } catch (error) {
    console.error('CSRF token API error:', error)
    return NextResponse.json(
      { success: false, message: 'Internal server error' },
      { status: 500 }
    )
  }
}
