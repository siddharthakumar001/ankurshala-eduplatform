import { NextRequest, NextResponse } from 'next/server'
import { cookies } from 'next/headers'

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080/api'

export async function POST(request: NextRequest) {
  try {
    const cookieStore = cookies()
    const refreshToken = cookieStore.get('refreshToken')?.value

    if (!refreshToken) {
      return NextResponse.json(
        { success: false, message: 'No refresh token found' },
        { status: 401 }
      )
    }

    // Forward refresh request to backend
    const backendResponse = await fetch(`${BACKEND_URL}/auth/refresh`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ refreshToken }),
    })

    if (!backendResponse.ok) {
      // Clear invalid cookies
      cookieStore.delete('accessToken')
      cookieStore.delete('refreshToken')
      
      const errorData = await backendResponse.json()
      return NextResponse.json(errorData, { status: backendResponse.status })
    }

    const authData = await backendResponse.json()
    
    // Extract new tokens from response
    const { accessToken, refreshToken: newRefreshToken, ...userData } = authData.data || authData

    // Set new secure httpOnly cookies
    cookieStore.set('accessToken', accessToken, {
      httpOnly: true,
      secure: process.env.NODE_ENV === 'production',
      sameSite: 'strict',
      maxAge: 15 * 60, // 15 minutes
      path: '/',
    })

    cookieStore.set('refreshToken', newRefreshToken, {
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
      message: 'Token refreshed successfully'
    })

  } catch (error) {
    console.error('Token refresh API error:', error)
    return NextResponse.json(
      { success: false, message: 'Internal server error' },
      { status: 500 }
    )
  }
}
