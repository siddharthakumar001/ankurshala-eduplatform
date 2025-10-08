import { NextRequest, NextResponse } from 'next/server'
import { cookies } from 'next/headers'

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080/api'

export async function POST(request: NextRequest) {
  try {
    const cookieStore = cookies()
    const refreshToken = cookieStore.get('refreshToken')?.value

    if (refreshToken) {
      // Forward logout request to backend
      try {
        await fetch(`${BACKEND_URL}/auth/logout`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ refreshToken }),
        })
      } catch (error) {
        // Continue with logout even if backend call fails
        console.error('Backend logout error:', error)
      }
    }

    // Clear all auth cookies
    cookieStore.delete('accessToken')
    cookieStore.delete('refreshToken')

    return NextResponse.json({
      success: true,
      message: 'Logout successful'
    })

  } catch (error) {
    console.error('Logout API error:', error)
    return NextResponse.json(
      { success: false, message: 'Internal server error' },
      { status: 500 }
    )
  }
}
