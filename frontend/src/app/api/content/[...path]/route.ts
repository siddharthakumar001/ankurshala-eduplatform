import { NextRequest, NextResponse } from 'next/server'
import { cookies } from 'next/headers'

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080/api'

export async function GET(
  request: NextRequest,
  { params }: { params: { path: string[] } }
) {
  try {
    const path = params.path.join('/')
    const search = request.nextUrl.search
    const accessToken = cookies().get('accessToken')?.value

    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    }
    if (accessToken) {
      headers['Authorization'] = `Bearer ${accessToken}`
    }

    const backendResponse = await fetch(`${BACKEND_URL}/content/${path}${search}`, {
      method: 'GET',
      headers,
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
    console.error('Content API proxy error:', error)
    return NextResponse.json(
      { success: false, message: 'Internal server error' },
      { status: 500 }
    )
  }
}
