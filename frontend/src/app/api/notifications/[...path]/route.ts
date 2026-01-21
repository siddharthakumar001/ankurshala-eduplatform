import { NextRequest, NextResponse } from 'next/server'
import { cookies } from 'next/headers'

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080/api'

const requireAccessToken = () => {
  const cookieStore = cookies()
  return cookieStore.get('accessToken')?.value
}

export async function GET(
  request: NextRequest,
  { params }: { params: { path: string[] } }
) {
  try {
    const accessToken = requireAccessToken()
    if (!accessToken) {
      return NextResponse.json(
        { success: false, message: 'Not authenticated' },
        { status: 401 }
      )
    }

    const path = params.path.join('/')
    const search = request.nextUrl.search

    const backendResponse = await fetch(`${BACKEND_URL}/notifications/${path}${search}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${accessToken}`,
      },
    })

    if (!backendResponse.ok) {
      const errorData = await backendResponse.json().catch(() => ({
        success: false,
        message: 'Request failed'
      }))
      return NextResponse.json(errorData, { status: backendResponse.status })
    }

    const data = await backendResponse.json()
    return NextResponse.json(data)
  } catch (error) {
    console.error('Notifications API proxy error:', error)
    return NextResponse.json(
      { success: false, message: 'Internal server error' },
      { status: 500 }
    )
  }
}

export async function POST(
  request: NextRequest,
  { params }: { params: { path: string[] } }
) {
  try {
    const accessToken = requireAccessToken()
    if (!accessToken) {
      return NextResponse.json(
        { success: false, message: 'Not authenticated' },
        { status: 401 }
      )
    }

    const body = await request.json()
    const path = params.path.join('/')
    const search = request.nextUrl.search

    const backendResponse = await fetch(`${BACKEND_URL}/notifications/${path}${search}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${accessToken}`,
      },
      body: JSON.stringify(body),
    })

    if (!backendResponse.ok) {
      const errorData = await backendResponse.json().catch(() => ({
        success: false,
        message: 'Request failed'
      }))
      return NextResponse.json(errorData, { status: backendResponse.status })
    }

    const data = await backendResponse.json()
    return NextResponse.json(data)
  } catch (error) {
    console.error('Notifications API proxy error:', error)
    return NextResponse.json(
      { success: false, message: 'Internal server error' },
      { status: 500 }
    )
  }
}
