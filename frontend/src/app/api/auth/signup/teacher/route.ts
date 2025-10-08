import { NextRequest, NextResponse } from 'next/server'
import { cookies } from 'next/headers'

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080/api'

export async function POST(request: NextRequest) {
  try {
    const body = await request.json()
    
    // Forward request to backend
    const backendResponse = await fetch(`${BACKEND_URL}/auth/signup/teacher`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    })

    if (!backendResponse.ok) {
      const errorData = await backendResponse.json()
      return NextResponse.json(errorData, { status: backendResponse.status })
    }

    const authData = await backendResponse.json()
    
    // Extract tokens from response
    const { accessToken, refreshToken, ...userData } = authData.data || authData

    // Set secure httpOnly cookies
    const cookieStore = cookies()
    
    // Access token - 15 minutes
    cookieStore.set('accessToken', accessToken, {
      httpOnly: true,
      secure: process.env.NODE_ENV === 'production',
      sameSite: 'strict',
      maxAge: 15 * 60, // 15 minutes
      path: '/',
    })

    // Refresh token - 7 days
    cookieStore.set('refreshToken', refreshToken, {
      httpOnly: true,
      secure: process.env.NODE_ENV === 'production',
      sameSite: 'strict',
      maxAge: 7 * 24 * 60 * 60, // 7 days
      path: '/',
    })

    // Return user data without tokens
    return NextResponse.json({
      success: true,
      data: userData,
      message: 'Teacher registration successful'
    })

  } catch (error) {
    console.error('Teacher signup API error:', error)
    return NextResponse.json(
      { success: false, message: 'Internal server error' },
      { status: 500 }
    )
  }
}
