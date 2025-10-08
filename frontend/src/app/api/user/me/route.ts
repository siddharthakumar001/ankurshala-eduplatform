import { NextRequest, NextResponse } from 'next/server'
import { cookies } from 'next/headers'

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080/api'

export async function GET(request: NextRequest) {
  try {
    const cookieStore = cookies()
    const accessToken = cookieStore.get('accessToken')?.value

    if (!accessToken) {
      return NextResponse.json(
        { success: false, message: 'No access token found' },
        { status: 401 }
      )
    }

    // Forward request to backend with token
    const backendResponse = await fetch(`${BACKEND_URL}/user/me`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json',
      },
    })

    if (!backendResponse.ok) {
      if (backendResponse.status === 401) {
        // Clear invalid cookies
        cookieStore.delete('accessToken')
        cookieStore.delete('refreshToken')
      }
      
      const errorData = await backendResponse.json()
      return NextResponse.json(errorData, { status: backendResponse.status })
    }

    const userData = await backendResponse.json()
    
    return NextResponse.json({
      success: true,
      data: userData.data || userData,
      message: 'User data retrieved successfully'
    })

  } catch (error) {
    console.error('User me API error:', error)
    return NextResponse.json(
      { success: false, message: 'Internal server error' },
      { status: 500 }
    )
  }
}
