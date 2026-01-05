import { NextRequest, NextResponse } from 'next/server'
import { cookies } from 'next/headers'

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080/api'

export async function GET(
  request: NextRequest,
  { params }: { params: { path: string[] } }
) {
  try {
    const cookieStore = cookies()
    const accessToken = cookieStore.get('accessToken')?.value
    
    if (!accessToken) {
      return NextResponse.json(
        { success: false, message: 'Not authenticated' },
        { status: 401 }
      )
    }

    // Build the path and include original query string
    const path = params.path.join('/')
    const search = request.nextUrl.search
    
    // Forward request to backend with auth token
    const backendResponse = await fetch(`${BACKEND_URL}/admin/${path}${search || ''}`, {
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
    console.error('Admin API proxy error:', error)
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
    const cookieStore = cookies()
    const accessToken = cookieStore.get('accessToken')?.value
    
    if (!accessToken) {
      return NextResponse.json(
        { success: false, message: 'Not authenticated' },
        { status: 401 }
      )
    }

    const path = params.path.join('/')
    const search = request.nextUrl.search
    const contentType = request.headers.get('content-type') || ''

    // Forward request body appropriately based on content type
    let forwardHeaders: Record<string, string> = {
      'Authorization': `Bearer ${accessToken}`,
    }
    
    let forwardBody: BodyInit | undefined
    if (contentType.includes('application/json')) {
      forwardHeaders['Content-Type'] = contentType
      const json = await request.json()
      forwardBody = JSON.stringify(json)
    } else if (contentType.includes('multipart/form-data')) {
      // For multipart/form-data, preserve the original Content-Type header with boundary
      forwardHeaders['Content-Type'] = contentType
      forwardBody = await request.arrayBuffer()
    } else {
      // For text/csv or other types, forward raw bytes
      if (contentType) {
        forwardHeaders['Content-Type'] = contentType
      }
      const arrayBuffer = await request.arrayBuffer()
      forwardBody = Buffer.from(arrayBuffer)
    }

    const backendResponse = await fetch(`${BACKEND_URL}/admin/${path}${search || ''}`, {
      method: 'POST',
      headers: forwardHeaders,
      body: forwardBody,
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
    console.error('Admin API proxy error:', error)
    return NextResponse.json(
      { success: false, message: 'Internal server error' },
      { status: 500 }
    )
  }
}

export async function PUT(
  request: NextRequest,
  { params }: { params: { path: string[] } }
) {
  try {
    const cookieStore = cookies()
    const accessToken = cookieStore.get('accessToken')?.value
    
    if (!accessToken) {
      return NextResponse.json(
        { success: false, message: 'Not authenticated' },
        { status: 401 }
      )
    }

    const body = await request.json()
    const path = params.path.join('/')
    
    const backendResponse = await fetch(`${BACKEND_URL}/admin/${path}`, {
      method: 'PUT',
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
    console.error('Admin API proxy error:', error)
    return NextResponse.json(
      { success: false, message: 'Internal server error' },
      { status: 500 }
    )
  }
}

export async function DELETE(
  request: NextRequest,
  { params }: { params: { path: string[] } }
) {
  try {
    const cookieStore = cookies()
    const accessToken = cookieStore.get('accessToken')?.value
    
    if (!accessToken) {
      return NextResponse.json(
        { success: false, message: 'Not authenticated' },
        { status: 401 }
      )
    }

    const path = params.path.join('/')
    
    const backendResponse = await fetch(`${BACKEND_URL}/admin/${path}`, {
      method: 'DELETE',
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
    console.error('Admin API proxy error:', error)
    return NextResponse.json(
      { success: false, message: 'Internal server error' },
      { status: 500 }
    )
  }
}

export async function PATCH(
  request: NextRequest,
  { params }: { params: { path: string[] } }
) {
  try {
    const cookieStore = cookies()
    const accessToken = cookieStore.get('accessToken')?.value
    
    if (!accessToken) {
      return NextResponse.json(
        { success: false, message: 'Not authenticated' },
        { status: 401 }
      )
    }

    const path = params.path.join('/')
    
    // PATCH typically doesn't have a body, but check if there is one
    let body = null
    try {
      body = await request.json()
    } catch {
      // No body is fine for PATCH requests like toggle-status
    }
    
    const backendResponse = await fetch(`${BACKEND_URL}/admin/${path}`, {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${accessToken}`,
      },
      body: body ? JSON.stringify(body) : undefined,
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
    console.error('Admin API proxy error:', error)
    return NextResponse.json(
      { success: false, message: 'Internal server error' },
      { status: 500 }
    )
  }
}
