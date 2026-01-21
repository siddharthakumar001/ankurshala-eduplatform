import { NextRequest, NextResponse } from 'next/server'
import { cookies } from 'next/headers'

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8080/api'

const buildProxyResponse = async (backendResponse: Response) => {
  if (backendResponse.status === 204) {
    return new NextResponse(null, { status: 204 })
  }

  const contentType = backendResponse.headers.get('content-type') || ''
  if (contentType.includes('text/event-stream') && backendResponse.body) {
    const headers = new Headers()
    headers.set('Content-Type', contentType)
    const cacheControl = backendResponse.headers.get('cache-control')
    if (cacheControl) {
      headers.set('Cache-Control', cacheControl)
    }
    return new NextResponse(backendResponse.body, {
      status: backendResponse.status,
      headers
    })
  }

  if (contentType.includes('application/json')) {
    const data = await backendResponse.json()
    return NextResponse.json(data, { status: backendResponse.status })
  }

  const buffer = await backendResponse.arrayBuffer()
  const headers = new Headers()
  const contentDisposition = backendResponse.headers.get('content-disposition')

  if (contentType) {
    headers.set('Content-Type', contentType)
  }
  if (contentDisposition) {
    headers.set('Content-Disposition', contentDisposition)
  }

  return new NextResponse(buffer, {
    status: backendResponse.status,
    headers
  })
}

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

    const backendResponse = await fetch(`${BACKEND_URL}/teacher/${path}${search}`, {
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

    return buildProxyResponse(backendResponse)
  } catch (error) {
    console.error('Teacher API proxy error:', error)
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

    const backendResponse = await fetch(`${BACKEND_URL}/teacher/${path}${search}`, {
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

    return buildProxyResponse(backendResponse)
  } catch (error) {
    console.error('Teacher API proxy error:', error)
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

    const backendResponse = await fetch(`${BACKEND_URL}/teacher/${path}${search}`, {
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

    return buildProxyResponse(backendResponse)
  } catch (error) {
    console.error('Teacher API proxy error:', error)
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

    const backendResponse = await fetch(`${BACKEND_URL}/teacher/${path}${search}`, {
      method: 'PATCH',
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

    return buildProxyResponse(backendResponse)
  } catch (error) {
    console.error('Teacher API proxy error:', error)
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
    const accessToken = requireAccessToken()
    if (!accessToken) {
      return NextResponse.json(
        { success: false, message: 'Not authenticated' },
        { status: 401 }
      )
    }

    const path = params.path.join('/')
    const search = request.nextUrl.search

    const backendResponse = await fetch(`${BACKEND_URL}/teacher/${path}${search}`, {
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

    return buildProxyResponse(backendResponse)
  } catch (error) {
    console.error('Teacher API proxy error:', error)
    return NextResponse.json(
      { success: false, message: 'Internal server error' },
      { status: 500 }
    )
  }
}
