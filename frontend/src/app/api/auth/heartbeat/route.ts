import { NextRequest, NextResponse } from 'next/server'
import { cookies } from 'next/headers'

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080/api'

export async function POST(request: NextRequest) {
  try {
    const cookieStore = cookies()
    const accessToken = cookieStore.get('accessToken')?.value

    if (!accessToken) {
      return NextResponse.json(
        { success: false, message: 'No access token found' },
        { status: 401 }
      )
    }

    // Forward heartbeat request to backend
    const backendResponse = await fetch(`${BACKEND_URL}/auth/heartbeat`, {
      method: 'POST',
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

    const responseData = await backendResponse.json()
    
    return NextResponse.json({
      success: true,
      data: responseData.data || responseData,
      message: 'Heartbeat successful'
    })

  } catch (error) {
    console.error('Heartbeat API error:', error)
    return NextResponse.json(
      { success: false, message: 'Internal server error' },
      { status: 500 }
    )
  }
}
